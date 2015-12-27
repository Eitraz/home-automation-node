package com.eitraz.automation;

import com.eitraz.automation.tellstick.TellstickAutomation;
import com.eitraz.library.lifecycle.Startable;
import com.eitraz.library.lifecycle.Stopable;
import com.eitraz.tellstick.core.rawdevice.RawDeviceEventListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class Automation implements Startable, Stopable {
    private final HazelcastInstance hazelcast;
    private TellstickAutomation tellstick;

    public Automation() {
        this.hazelcast = Hazelcast.newHazelcastInstance();
        this.tellstick = LifeCycleInstance.register(new TellstickAutomation(hazelcast));
    }

    @Override
    public void doStart() {
        LifeCycleInstance.get().start();
    }

    @Override
    public void doStop() {
        LifeCycleInstance.get().stop();
    }

}
