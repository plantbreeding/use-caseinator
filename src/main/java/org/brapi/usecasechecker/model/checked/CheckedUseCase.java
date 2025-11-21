package org.brapi.usecasechecker.model.checked;

import java.util.List;

public class CheckedUseCase {
    private boolean isValid = false;
    private List<CheckedEntity> checkedEntities;
    private String useCaseName;

    public CheckedUseCase(boolean isValid, List<CheckedEntity> checkedEntities, String useCaseName) {
        this.isValid = isValid;
        this.checkedEntities = checkedEntities;
        this.useCaseName = useCaseName;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public List<CheckedEntity> getCheckedEntities() {
        return checkedEntities;
    }

    public void setCheckedEntities(List<CheckedEntity> checkedEntities) {
        this.checkedEntities = checkedEntities;
    }

    public String getUseCaseName() {
        return useCaseName;
    }

    public void setUseCaseName(String useCaseName) {
        this.useCaseName = useCaseName;
    }
}
