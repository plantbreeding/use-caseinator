package org.brapi.usecasechecker.model.useCases;

import org.brapi.usecasechecker.exceptions.UseCaseBuilderException;

import java.util.List;

public class UseCase {

    public UseCase() {

    }

    private UseCase(Builder builder) {
        this.useCaseName = builder.useCaseName;
        this.entitiesRequired = builder.entitiesRequired;
    }

    private String useCaseName;
    private List<EntityRequired> entitiesRequired;
    // Not required
    private String description;

    public String getUseCaseName() {
        return useCaseName;
    }

    public void setUseCaseName(String useCaseName) {
        this.useCaseName = useCaseName;
    }

    public List<EntityRequired> getEntitiesRequired() {
        return entitiesRequired;
    }

    public void setEntitiesRequired(List<EntityRequired> entitiesRequired) {
        this.entitiesRequired = entitiesRequired;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static class Builder {
        private String useCaseName;
        private List<EntityRequired> entitiesRequired;
        private String description;

        public Builder useCaseName(String useCaseName) {
            this.useCaseName = useCaseName;
            return this;
        }

        public Builder entitiesRequired(List<EntityRequired> entitiesRequired) {
            this.entitiesRequired = entitiesRequired;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public UseCase builder() {

            if (this.useCaseName == null || this.useCaseName.isEmpty()) {
                throw new UseCaseBuilderException("No useCaseName provided to builder.");
            }

            if (this.entitiesRequired == null || this.entitiesRequired.isEmpty()) {
                throw new UseCaseBuilderException("No servicesRequired provided to builder.");
            }

            return new UseCase(this);
        }
    }
}
