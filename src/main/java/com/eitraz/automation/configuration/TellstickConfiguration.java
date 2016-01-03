package com.eitraz.automation.configuration;

import com.eitraz.automation.tellstick.TellstickDeviceConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TellstickConfiguration {
    private List<TellstickDeviceConfiguration> devices;

    public TellstickConfiguration() {
    }

    @JsonProperty("devices")
    public List<TellstickDeviceConfiguration> getDevices() {
        return devices;
    }

    @JsonProperty("devices")
    public void setDevices(List<TellstickDeviceConfiguration> devices) {
        this.devices = devices;
    }
}