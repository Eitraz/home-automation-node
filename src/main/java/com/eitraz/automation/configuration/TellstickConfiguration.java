package com.eitraz.automation.configuration;

import com.eitraz.automation.tellstick.JsonTellstickDevice;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TellstickConfiguration {
    private List<JsonTellstickDevice> devices;

    public TellstickConfiguration() {
    }

    @JsonProperty("devices")
    public List<JsonTellstickDevice> getDevices() {
        return devices;
    }

    @JsonProperty("devices")
    public void setDevices(List<JsonTellstickDevice> devices) {
        this.devices = devices;
    }
}