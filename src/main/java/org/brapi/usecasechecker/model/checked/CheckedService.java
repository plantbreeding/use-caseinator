package org.brapi.usecasechecker.model.checked;

import org.brapi.usecasechecker.model.useCases.ServiceRequired;

public class CheckedService {

    private boolean valid;
    private String message;
    private ServiceRequired service;

    public CheckedService(boolean valid, String message, ServiceRequired service) {
        this.valid = valid;
        this.message = message;
        this.service = service;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ServiceRequired getService() {
        return service;
    }

    public void setService(ServiceRequired service) {
        this.service = service;
    }
}
