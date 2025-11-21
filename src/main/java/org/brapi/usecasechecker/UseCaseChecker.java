package org.brapi.usecasechecker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.brapi.usecasechecker.exceptions.UseCaseCheckerException;
import org.brapi.usecasechecker.model.Call;
import org.brapi.usecasechecker.model.checked.CheckedEntity;
import org.brapi.usecasechecker.model.checked.CheckedService;
import org.brapi.usecasechecker.model.checked.CheckedUseCase;
import org.brapi.usecasechecker.model.useCases.*;

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

            List<EntityRequired> entitiesRequired = appUseCase.getEntitiesRequired();

            List<CheckedEntity> checkedEntitiesForUseCase = isUseCaseValid(entitiesRequired, availableServiceCalls);

            boolean useCaseValid = checkedEntitiesForUseCase.stream().allMatch(CheckedEntity::isValid);

            results.add(new CheckedUseCase(useCaseValid, checkedEntitiesForUseCase, appUseCase.getUseCaseName()));
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

        List<EntityRequired> servicesRequired = useCase.get().getEntitiesRequired();

        List<CheckedEntity> checkedEntities = isUseCaseValid(servicesRequired, availableServiceCalls);

        boolean validUseCase = checkedEntities.stream().allMatch(CheckedEntity::isValid);

        return new CheckedUseCase(validUseCase, checkedEntities, useCaseName);
    }

    private List<CheckedEntity> isUseCaseValid(List<EntityRequired> entitiesRequired, List<Call> availableServiceCalls) {
        Map<String, List<Call>> callsByService = availableServiceCalls.stream()
                .collect(Collectors.groupingBy(Call::getService));

        List<CheckedEntity> result = new ArrayList<>();

        for (EntityRequired entityRequired : entitiesRequired) {

            List<CheckedService> checkedServices = new ArrayList<>();

            for (ServiceRequired serviceRequired : entityRequired.getServicesRequired()) {
                List<Call> candidates = callsByService.get(serviceRequired.getServiceName());

                if (candidates == null || candidates.isEmpty()) {
                    String message = String.format("Service %s not found in BrAPI compatible server with serverInfo endpoint: [%s]",
                            serviceRequired.getServiceName(),
                            serverInfoUrl);

                    checkedServices.add(new CheckedService(false, message, serviceRequired));
                    continue;
                }

                Call call = candidates.get(0);

                if (!call.getVersions().contains(serviceRequired.getVersionRequired())) {
                    String message = String.format("Service %s did not have compatible version %s in BrAPI compatible server with serverInfo endpoint: %s",
                            serviceRequired.getServiceName(),
                            serviceRequired.getVersionRequired(),
                            serverInfoUrl);

                    checkedServices.add(new CheckedService(false, message, serviceRequired));
                    continue;
                }

                if (!call.getMethods().contains(serviceRequired.getMethodRequired())) {
                    String message = String.format("Service %s did not have a compatible HTTP Verb %s in BrAPI compatible server with serverInfo endpoint: %s",
                            serviceRequired.getServiceName(),
                            serviceRequired.getMethodRequired(),
                            serverInfoUrl);
                    checkedServices.add(new CheckedService(false, message, serviceRequired));
                    continue;
                }

                String message = String.format(("Service %s implemented and verified via server info endpoint %s with HTTP verb %s and version %s"),
                        serviceRequired.getServiceName(),
                        serverInfoUrl,
                        serviceRequired.getMethodRequired(),
                        serviceRequired.getVersionRequired());
                checkedServices.add(new CheckedService(true, message, serviceRequired));
            }

            boolean allServicesForEntityValid = checkedServices.stream().allMatch(CheckedService::isValid);

            result.add(new CheckedEntity(entityRequired.getEntityName(), checkedServices, allServicesForEntityValid));
        }
        return result;
    }
}