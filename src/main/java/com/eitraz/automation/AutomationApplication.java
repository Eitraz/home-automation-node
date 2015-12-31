package com.eitraz.automation;

import com.eitraz.automation.configuration.AutomationConfiguration;
import com.eitraz.automation.evaulation.Evaluator;
import com.eitraz.automation.scripts.FileScriptLoader;
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
    public static final String HAZELCAST_MEMBER_PRIORITY = "priority";

    private HazelcastInstance hazelcast;
    private TellstickAutomation tellstick;
    private Evaluator evaluator;

    @Override
    public void run(final AutomationConfiguration automationConfiguration, Environment environment) throws Exception {
        // Hazelcast
        this.hazelcast = Hazelcast.newHazelcastInstance();
        hazelcast.getCluster().getLocalMember().setIntAttribute(HAZELCAST_MEMBER_PRIORITY, automationConfiguration.getPriority());

        // Tellstick
        this.tellstick = LifeCycleInstance.register(new TellstickAutomation(hazelcast));

        // Evaluator
        evaluator = LifeCycleInstance.register(new Evaluator(hazelcast, tellstick));

        // Load scripts
        new FileScriptLoader(automationConfiguration.getScripts().getPath()).populateHazelcast(hazelcast);

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
