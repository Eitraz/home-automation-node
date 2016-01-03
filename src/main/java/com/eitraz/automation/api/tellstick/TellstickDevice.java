package com.eitraz.automation.api.tellstick;

import com.eitraz.tellstick.core.device.Device;

import java.io.Serializable;

public class TellstickDevice {
    private String name;
    private String model;
    private String protocol;
    private Serializable status;

    public TellstickDevice() {
    }

    public TellstickDevice(Device device, Serializable status) {
        setName(device.getName());
        setModel(device.getModel());
        setProtocol(device.getProtocol());
        setStatus(status);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Serializable getStatus() {
        return status;
    }

    public void setStatus(Serializable status) {
        this.status = status;
    }
}
