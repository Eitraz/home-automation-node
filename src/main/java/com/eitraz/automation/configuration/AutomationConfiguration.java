package com.eitraz.automation.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;

public class AutomationConfiguration extends Configuration {
    @Valid
    private TellstickConfiguration tellstick;

    @JsonProperty("tellstick")
    public TellstickConfiguration getTellstick() {
        return tellstick;
    }

    @JsonProperty("tellstick")
    public void setTellstick(TellstickConfiguration tellstick) {
        this.tellstick = tellstick;
    }
}
