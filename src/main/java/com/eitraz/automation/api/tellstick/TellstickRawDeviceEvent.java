package com.eitraz.automation.api.tellstick;

import com.eitraz.tellstick.core.rawdevice.events.RawDeviceEvent;

import java.util.Date;
import java.util.Map;

public class TellstickRawDeviceEvent {
    private Date time;
    private Map<String, String> parameters;

    public TellstickRawDeviceEvent(RawDeviceEvent event) {
        this.time = new Date(event.getTime());
        this.parameters = event.getParameters();
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
