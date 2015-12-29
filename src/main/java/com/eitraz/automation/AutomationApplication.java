package com.eitraz.automation;

import com.eitraz.automation.configuration.AutomationConfiguration;
import com.eitraz.automation.tellstick.TellstickAutomation;
import com.eitraz.automation.tellstick.TellstickConfigurer;
import com.eitraz.tellstick.core.rawdevice.RawDeviceEventListener;
import com.eitraz.tellstick.core.rawdevice.events.RawDeviceEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import io.dropwizard.Application;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.Serializable;

public class AutomationApplication extends Application<AutomationConfiguration> implements RawDeviceEventListener {
    private HazelcastInstance hazelcast;
    private TellstickAutomation tellstick;
    private static Evaluator evaluator;

    @Override
    public void run(final AutomationConfiguration automationConfiguration, Environment environment) throws Exception {
        if (tellstick != null || evaluator != null) {
            throw new RuntimeException("Already running!");
        }

        // Hazelcast
        this.hazelcast = Hazelcast.newHazelcastInstance();
        hazelcast.getCluster().getLocalMember().setIntAttribute("priority", automationConfiguration.getPriority());

        // Tellstick
        this.tellstick = LifeCycleInstance.register(new TellstickAutomation(hazelcast));

        // Evaluator
        evaluator = LifeCycleInstance.register(new Evaluator(hazelcast, tellstick));

        // Life cycle manager
        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {
                LifeCycleInstance.get().start();

                // Setup tellstick - TODO: Replace this with Hazelcast configured devices
                TellstickConfigurer.setup(tellstick.getTellstick(), automationConfiguration.getTellstick());

                getTellstick().addRawDeviceEventListener(AutomationApplication.this);
            }

            @Override
            public void stop() throws Exception {
                getTellstick().removeRawDeviceEventListener(AutomationApplication.this);

                LifeCycleInstance.get().stop();
                hazelcast.shutdown();

                evaluator = null;
            }
        });
    }

    @Override
    public void initialize(Bootstrap<AutomationConfiguration> bootstrap) {
    }

    public TellstickAutomation getTellstick() {
        return tellstick;
    }

    @Override
    public void rawDeviceEvent(RawDeviceEvent event) {
        evaluator.requestEvaluation();
    }
}
