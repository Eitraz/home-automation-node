package com.eitraz.automation.tellstick;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;
import java.util.Map;

public class TellstickDeviceConfiguration implements Serializable {
    @NotEmpty
    private String name;

    @NotEmpty
    private String model;

    @NotEmpty
    private String protocol;

    @NotEmpty
    private Map<String, String> parameters;

    public TellstickDeviceConfiguration() {
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public String getModel() {
        return model;
    }

    @JsonProperty
    public void setModel(String model) {
        this.model = model;
    }

    @JsonProperty
    public String getProtocol() {
        return protocol;
    }

    @JsonProperty
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @JsonProperty
    public Map<String, String> getParameters() {
        return parameters;
    }

    @JsonProperty
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "TellstickDeviceConfiguration{" +
                "name='" + name + '\'' +
                ", model='" + model + '\'' +
                ", protocol='" + protocol + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
