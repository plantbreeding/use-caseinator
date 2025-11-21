package org.brapi.usecasechecker.model.useCases;

import org.brapi.usecasechecker.exceptions.UseCaseBuilderException;

import java.util.List;

public class EntityRequired {
    private String entityName;
    private List<ServiceRequired> servicesRequired;

    public EntityRequired() {

    }

    private EntityRequired(Builder builder) {
        this.entityName = builder.entityName;
        this.servicesRequired = builder.servicesRequired;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public List<ServiceRequired> getServicesRequired() {
        return servicesRequired;
    }

    public void setServicesRequired(List<ServiceRequired> servicesRequired) {
        this.servicesRequired = servicesRequired;
    }

    public static class Builder {
        private String entityName;
        private List<ServiceRequired> servicesRequired;

        public Builder entityName(String entityName) {
            this.entityName = entityName;
            return this;
        }

        public Builder servicesRequired(List<ServiceRequired> servicesRequired) {
            this.servicesRequired = servicesRequired;
            return this;
        }

        public EntityRequired build() {
            if (this.entityName == null || this.entityName.isEmpty()) {
                throw new UseCaseBuilderException("No entityName provided to builder");
            }

            if (this.servicesRequired == null || this.servicesRequired.isEmpty()) {
                throw new UseCaseBuilderException("No servicesRequired provided to builder");
            }

            return new EntityRequired(this);
        }
    }

}
