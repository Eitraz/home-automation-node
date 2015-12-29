package com.eitraz.automation;

import com.eitraz.automation.configuration.AutomationConfiguration;
import com.eitraz.automation.tellstick.TellstickAutomation;
import com.eitraz.automation.tellstick.TellstickConfigurer;
import com.eitraz.tellstick.core.rawdevice.RawDeviceEventListener;
import com.eitraz.tellstick.core.rawdevice.events.RawDeviceEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.dropwizard.Application;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class AutomationApplication extends Application<AutomationConfiguration> implements RawDeviceEventListener {
    private HazelcastInstance hazelcast;
    private static TellstickAutomation tellstick;

    @Override
    public void run(final AutomationConfiguration automationConfiguration, Environment environment) throws Exception {
        // Hazelcast
        this.hazelcast = Hazelcast.newHazelcastInstance();

        // Tellstick
        AutomationApplication.tellstick = LifeCycleInstance.register(new TellstickAutomation(hazelcast));
        getTellstick().addRawDeviceEventListener(this);

        // Life cycle manager
        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {
                LifeCycleInstance.get().start();

                // Setup tellstick - TODO: Replace this with Hazelcast configured devices
                TellstickConfigurer.setup(tellstick.getTellstick(), automationConfiguration.getTellstick());
            }

            @Override
            public void stop() throws Exception {
                LifeCycleInstance.get().stop();
                hazelcast.shutdown();
            }
        });
    }

    @Override
    public void initialize(Bootstrap<AutomationConfiguration> bootstrap) {
    }

    public static TellstickAutomation getTellstick() {
        return tellstick;
    }

    @Override
    public void rawDeviceEvent(RawDeviceEvent event) {

    }
}
