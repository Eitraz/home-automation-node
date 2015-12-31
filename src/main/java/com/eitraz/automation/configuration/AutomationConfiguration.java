package com.eitraz.automation.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class AutomationConfiguration extends Configuration {
    @Valid
    private TellstickConfiguration tellstick;

    @Valid
    private ScriptsConfiguration scripts;

    @NotNull
    private Integer priority;

    @JsonProperty
    public TellstickConfiguration getTellstick() {
        return tellstick;
    }

    @JsonProperty
    public void setTellstick(TellstickConfiguration tellstick) {
        this.tellstick = tellstick;
    }

    @JsonProperty
    public ScriptsConfiguration getScripts() {
        return scripts;
    }

    @JsonProperty
    public void setScripts(ScriptsConfiguration scripts) {
        this.scripts = scripts;
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
