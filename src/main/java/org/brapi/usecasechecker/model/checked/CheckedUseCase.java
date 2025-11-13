package org.brapi.usecasechecker.model.checked;

import java.util.List;

public class CheckedUseCase {
    private boolean isValid = false;
    private List<CheckedService> checkedServices;
    private String useCaseName;

    public CheckedUseCase(boolean isValid, List<CheckedService> checkedServices, String useCaseName) {
        this.isValid = isValid;
        this.checkedServices = checkedServices;
        this.useCaseName = useCaseName;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public List<CheckedService> getCheckedServices() {
        return checkedServices;
    }

    public void setCheckedServices(List<CheckedService> checkedServices) {
        this.checkedServices = checkedServices;
    }

    public String getUseCaseName() {
        return useCaseName;
    }

    public void setUseCaseName(String useCaseName) {
        this.useCaseName = useCaseName;
    }
}
