package com.eitraz.automation.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class AutomationConfiguration extends Configuration {
    @Valid
    private TellstickConfiguration tellstick;

    @NotNull
    private Integer priority;

    @JsonProperty("tellstick")
    public TellstickConfiguration getTellstick() {
        return tellstick;
    }

    @JsonProperty("tellstick")
    public void setTellstick(TellstickConfiguration tellstick) {
        this.tellstick = tellstick;
    }

    @JsonProperty
    public Integer getPriority() {
        return priority;
    }

    @JsonProperty
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
