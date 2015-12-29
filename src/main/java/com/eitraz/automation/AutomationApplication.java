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
    private IExecutorService evaluateExecutor;
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
        evaluateExecutor = hazelcast.getExecutorService("evaluate-executor");
        hazelcast.getConfig().getExecutorConfig("evaluate-executor").setPoolSize(1);
        evaluator = new Evaluator(hazelcast, tellstick);

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

                tellstick = null;
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

    private Member getMemberWithHighestPriority() {
        int priority = Integer.MIN_VALUE;
        Member member = null;
        for (Member m : hazelcast.getCluster().getMembers()) {
            Integer p = m.getIntAttribute("priority");
            if (p > priority) {
                priority = p;
                member = m;
            }
        }
        return member;
    }

    @Override
    public void rawDeviceEvent(RawDeviceEvent event) {
        evaluateExecutor.executeOnMember(new RequestEvaluation(), getMemberWithHighestPriority());
    }

    static class RequestEvaluation implements Runnable, Serializable {
        @Override
        public void run() {
            evaluator.evaluate();
        }
    }
}
