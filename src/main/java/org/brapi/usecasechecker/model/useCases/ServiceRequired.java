package org.brapi.usecasechecker.model.useCases;

import org.brapi.usecasechecker.exceptions.UseCaseBuilderException;

public class ServiceRequired {

    private String serviceName;
    private String methodRequired;
    private String versionRequired;

    public ServiceRequired() {
    }

    private ServiceRequired(Builder builder) {
        this.serviceName = builder.serviceName;
        this.methodRequired = builder.methodRequired;
        this.versionRequired = builder.versionRequired;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodRequired() {
        return methodRequired;
    }

    public void setMethodRequired(String methodRequired) {
        this.methodRequired = methodRequired;
    }

    public String getVersionRequired() {
        return versionRequired;
    }

    public void setVersionRequired(String versionRequired) {
        this.versionRequired = versionRequired;
    }

    public static class Builder {
        private String serviceName;
        private String methodRequired;
        private String versionRequired;

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder methodRequired(String methodRequired) {
            this.methodRequired = methodRequired;
            return this;
        }

        public Builder versionRequired(String versionRequired) {
            this.versionRequired = versionRequired;
            return this;
        }

        public ServiceRequired build() {

            if (this.versionRequired == null || this.versionRequired.isEmpty()) {
                throw new UseCaseBuilderException("No versionRequired provided to builder.");
            }

            if (this.serviceName == null || this.serviceName.isEmpty()) {
                throw new UseCaseBuilderException("No serviceName provided to builder.");
            }

            if (this.methodRequired == null || this.methodRequired.isEmpty()) {
                throw new UseCaseBuilderException("No methodsRequired provided to builder.");
            }

            return new ServiceRequired(this);
        }
    }
}
