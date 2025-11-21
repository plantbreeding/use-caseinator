package org.brapi.usecasechecker.model.checked;

import java.util.List;

public class CheckedEntity {
    private String entityName;
    private List<CheckedService> checkedServices;
    private boolean isValid;

    public CheckedEntity(String entityName,
                         List<CheckedService> checkedServices,
                         boolean isValid) {
        this.entityName = entityName;
        this.checkedServices = checkedServices;
        this.isValid = isValid;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public List<CheckedService> getCheckedServices() {
        return checkedServices;
    }

    public void setCheckedServices(List<CheckedService> checkedServices) {
        this.checkedServices = checkedServices;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }
}
