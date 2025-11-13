package org.brapi.usecasechecker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.brapi.usecasechecker.exceptions.UseCaseCheckerException;
import org.brapi.usecasechecker.model.Call;
import org.brapi.usecasechecker.model.checked.CheckedService;
import org.brapi.usecasechecker.model.checked.CheckedUseCase;
import org.brapi.usecasechecker.model.useCases.App;
import org.brapi.usecasechecker.model.useCases.ServiceRequired;
import org.brapi.usecasechecker.model.useCases.UseCase;
import org.brapi.usecasechecker.model.useCases.UseCases;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class UseCaseChecker {

    private final OkHttpClient httpClient;
    private final String serverInfoUrl;
    private final UseCases loadedUseCases;
    private final ObjectMapper objectMapper;
    private final static String SERVER_INFO_PATH = "/serverinfo";

    UseCaseChecker(String serverBaseUrl,
                          UseCases loadedUseCases,
                          ObjectMapper objectMapper) {
        httpClient = new OkHttpClient();
        this.objectMapper = objectMapper;
        serverInfoUrl = serverBaseUrl + SERVER_INFO_PATH;
        this.loadedUseCases = loadedUseCases;
    }

    private List<Call> getServerInfoCalls() throws UseCaseCheckerException {
        Response response;

        JsonNode serverInfo = null;
        try {
            Request request = new Request.Builder()
                    .url(serverInfoUrl)
                    .get()
                    .build();

            response = httpClient.newCall(request).execute();

            if (response.body() != null && response.isSuccessful()) {
                serverInfo = objectMapper.readTree(response.body().string());
            }
        } catch (IOException e) {
            // Add logging
            throw new UseCaseCheckerException(e);
        }

        if (serverInfo == null || serverInfo.isEmpty()) {
            throw new UseCaseCheckerException(String.format("No serverinfo found with provided url [%s]", serverInfoUrl));
        }

        return objectMapper.convertValue(serverInfo.path("result").path("calls"),
                new TypeReference<List<Call>>() {
                });
    }

    public List<CheckedUseCase> allUseCasesCompliant(String brAppName) throws UseCaseCheckerException {
        List<Call> availableServiceCalls = getServerInfoCalls();

        Optional<App> app = loadedUseCases.getApps().stream()
                .filter(a -> a.getAppName().equals(brAppName))
                .findFirst();

        if (!app.isPresent()) {
            throw new UseCaseCheckerException(String.format("BrApp with name [%s] does not exist%n", brAppName));
        }

        List<CheckedUseCase> results = new ArrayList<>();

        for (UseCase appUseCase : app.get().getUseCases()) {
            List<ServiceRequired> servicesRequired = appUseCase.getServicesRequired();

            List<CheckedService> checkedServicesForUseCase = isUseCaseValid(servicesRequired, availableServiceCalls);

            boolean useCaseValid = checkedServicesForUseCase.stream().allMatch(CheckedService::isValid);

            results.add(new CheckedUseCase(useCaseValid, checkedServicesForUseCase, appUseCase.getUseCaseName()));
        }

        return results;
    }

    public CheckedUseCase isUseCaseCompliant(String brAppName, String useCaseName) throws UseCaseCheckerException {

        List<Call> availableServiceCalls = getServerInfoCalls();

        Optional<App> app = loadedUseCases.getApps().stream()
                .filter(a -> a.getAppName().equals(brAppName))
                .findFirst();

        if (!app.isPresent()) {
            throw new UseCaseCheckerException(String.format("BrApp with name [%s] does not exist%n", brAppName));
        }

        Optional<UseCase> useCase = app.get().getUseCases().stream()
                .filter(uc -> uc.getUseCaseName().equals(useCaseName))
                .findFirst();

        if (!useCase.isPresent()) {
            throw new UseCaseCheckerException(String.format("Use case with name [%s] does not exist for BrApp [%s]%n", useCaseName, brAppName));
        }

        List<ServiceRequired> servicesRequired = useCase.get().getServicesRequired();

        List<CheckedService> checkedServices = isUseCaseValid(servicesRequired, availableServiceCalls);

        boolean validUseCase = checkedServices.stream().allMatch(CheckedService::isValid);

        return new CheckedUseCase(validUseCase, checkedServices, useCaseName);
    }

    private List<CheckedService> isUseCaseValid(List<ServiceRequired> servicesRequired, List<Call> availableServiceCalls) {
        Map<String, List<Call>> callsByService = availableServiceCalls.stream()
                .collect(Collectors.groupingBy(Call::getService));

        List<CheckedService> result = new ArrayList<>();

        for (ServiceRequired serviceRequired : servicesRequired) {
            List<Call> candidates = callsByService.get(serviceRequired.getServiceName());

            if (candidates == null  || candidates.isEmpty()) {
                String message = String.format("Service [%s] not found in BrAPI compatible server with serverInfo endpoint: [%s]",
                        serviceRequired.getServiceName(),
                        serverInfoUrl);

                result.add(new CheckedService(false, message, serviceRequired));
                continue;
            }

            Call call = candidates.get(0);

            if (!call.getVersions().contains(serviceRequired.getVersionRequired())) {
                String message = String.format("Service [%s] did not have compatible version [%s] in BrAPI compatible server with serverInfo endpoint: [%s]",
                        serviceRequired.getServiceName(),
                        serviceRequired.getVersionRequired(),
                        serverInfoUrl);

                result.add(new CheckedService(false, message, serviceRequired));
                continue;
            }

            List<String> methodsNotFound
                    = serviceRequired.getMethodsRequired()
                    .stream()
                    .filter(m -> !call.getMethods().contains(m))
                    .collect(Collectors.toList());

            if (!methodsNotFound.isEmpty()) {
                String message = String.format("Service [%s] did not have a compatible HTTP Verb/s [%s] in BrAPI compatible server with serverInfo endpoint: [%s]",
                        serviceRequired.getServiceName(),
                        methodsNotFound,
                        serverInfoUrl);
                result.add(new CheckedService(false, message, serviceRequired));
                continue;
            }

            String message = String.format(("Service [%s] implemented and verified via server info with HTTP verb/s [%s] and version [%s]"),
                    serviceRequired.getServiceName(),
                    serviceRequired.getMethodsRequired(),
                    serviceRequired.getVersionRequired());
            result.add(new CheckedService(true, message, serviceRequired));
        }

        return result;
    }
}