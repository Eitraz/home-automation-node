package com.eitraz.automation.tellstick;

import com.eitraz.automation.configuration.TellstickConfiguration;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.ITopic;

public final class TellstickConfigurer {
    private TellstickConfigurer() {
    }

    public static void setup(HazelcastInstance hazelcast, TellstickConfiguration configuration) {
        ITopic<String> devicesUpdatedTopic = hazelcast.getReliableTopic(TellstickAutomation.HAZELCAST_TELLSTICK_DEVICES_CONFIGURATION_TOPIC);
        IList<TellstickDeviceConfiguration> devicesConfiguration = hazelcast.getList(TellstickAutomation.HAZELCAST_TELLSTICK_DEVICES_CONFIGURATION);

        if (configuration != null && configuration.getDevices() != null) {
            devicesConfiguration.clear();
            configuration.getDevices().forEach(devicesConfiguration::add);
            devicesUpdatedTopic.publish("reload devices");
        }
    }
}
