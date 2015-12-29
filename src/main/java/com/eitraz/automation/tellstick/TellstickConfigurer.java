package com.eitraz.automation.tellstick;

import com.eitraz.automation.configuration.TellstickConfiguration;
import com.eitraz.tellstick.core.TellstickException;
import com.eitraz.tellstick.core.cluster.TellstickCluster;
import com.eitraz.tellstick.core.device.DeviceHandler;
import com.eitraz.tellstick.core.device.DeviceNotSupportedException;

public final class TellstickConfigurer {
    private TellstickConfigurer() {
    }

    public static void setup(TellstickCluster tellstick, TellstickConfiguration configuration) {
        // Remove all local devices
        DeviceHandler localDeviceHandler = tellstick.getTellstick().getDeviceHandler();
        localDeviceHandler.getDevices().forEach(localDeviceHandler::removeDevice);

        if (configuration.getDevices() != null) {
            try {
                for (JsonTellstickDevice device : configuration.getDevices()) {
                    localDeviceHandler.createDevice(device.getName(), device.getModel(), device.getProtocol(), device.getParameters());
                }
            } catch (TellstickException | DeviceNotSupportedException e) {
                throw new RuntimeException("Failed to create local Tellstick device", e);
            }
        }
    }
}
