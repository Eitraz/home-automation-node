package com.eitraz.automation.helpers;

import com.eitraz.automation.tellstick.TellstickAutomation;
import com.eitraz.tellstick.core.rawdevice.events.RawDeviceEvent;

import java.util.HashMap;
import java.util.Map;

public class RawDeviceHelper {
    private final TellstickAutomation tellstick;
    private Map<String, String> parameters = new HashMap<>();

    public RawDeviceHelper(TellstickAutomation tellstick) {
        this.tellstick = tellstick;
    }

    private RawDeviceEvent event;

    public RawDeviceHelper with(String key, String value) {
        parameters.put(key, value);
        return this;
    }

    private RawDeviceEvent getEvent() {
        if (event == null) {
            event = tellstick.getRawDeviceEvent(parameters);
        }
        return event;
    }

    public boolean hasEvent() {
        return getEvent() != null;
    }

    public String get(String key) {
        RawDeviceEvent event = getEvent();
        if (event != null) {
            return event.get(key);
        }
        return null;
    }

    public boolean is(String key, String value) {
        return value.equals(get(key));
    }

    public long lastEventTime() {
        RawDeviceEvent event = getEvent();
        if (event != null) {
            return event.getTime();
        }
        return -1;
    }

    public boolean isActive(long timeout) {
        RawDeviceEvent event = getEvent();
        return event != null && System.currentTimeMillis() < event.getTime() + timeout;
    }
}
