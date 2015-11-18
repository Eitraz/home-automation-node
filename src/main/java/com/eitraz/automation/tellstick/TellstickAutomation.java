package com.eitraz.automation.tellstick;

import com.eitraz.automation.LifeCycleInstance;
import com.eitraz.library.Duration;
import com.eitraz.library.TimeoutHandler;
import com.eitraz.library.lifecycle.Startable;
import com.eitraz.library.lifecycle.Stopable;
import com.eitraz.tellstick.core.cluster.TellstickCluster;
import com.eitraz.tellstick.core.device.DeviceException;
import com.eitraz.tellstick.core.device.OnOffDevice;
import com.eitraz.tellstick.core.rawdevice.RawDeviceEventListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class TellstickAutomation implements Startable, Stopable, RawDeviceEventListener {
    private TellstickCluster tellstick;
    private final HazelcastInstance hazelcast;

    private final Map<String, Serializable> deviceStatus;

    private final BlockingQueue<Map<String, String>> rawEvents;
    private final TimeoutHandler<String> rawEventTimeoutHandler = new TimeoutHandler<>(Duration.ONE_SECOND);
    private Thread rawEventsThread;

    public TellstickAutomation() {
        hazelcast = Hazelcast.newHazelcastInstance();

        deviceStatus = hazelcast.getMap("tellstick.device.status");
        rawEvents = hazelcast.getQueue("tellstick.raw.events");

        tellstick = LifeCycleInstance.register(new TellstickCluster());
    }

    @Override
    public void doStart() {
        tellstick.getTellstick().getRawDeviceHandler().addRawDeviceEventListener(this);

        rawEventsThread = new Thread("rawEventsThread") {
            @Override
            public void run() {
                while (rawEventsThread != null && rawEventsThread == this) {
                    try {
                        Map<String, String> parameters = rawEvents.poll(1000, TimeUnit.SECONDS);
                        if (parameters != null) {
                            TreeMap<String, String> sortedMap = new TreeMap<>(parameters);

                            // Don't fire event to often
                            if (rawEventTimeoutHandler.isReady(sortedMap.toString()))
                                handleRawDeviceEvent(sortedMap);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        rawEventsThread.start();
    }

    @Override
    public void doStop() {
        tellstick.getTellstick().getRawDeviceHandler().removeRawDeviceEventListener(this);

        if (rawEventsThread != null) {
            rawEventsThread = null;
        }
    }

    @Override
    public void rawDeviceEvent(Map<String, String> parameters) {
        rawEvents.offer(parameters);
    }

    /**
     * @param parameters raw event parameters
     */
    private void handleRawDeviceEvent(Map<String, String> parameters) {
        // TODO Run logic
    }

    public void turnOn(String device) {
        // Already on
        if (Boolean.TRUE.equals(deviceStatus.get("device")))
            return;

        deviceStatus.put(device, Boolean.TRUE);
        try {
            tellstick.getProxiedDeviceByName(device, OnOffDevice.class).on();
        } catch (DeviceException e) {
            throw new RuntimeException(e);
        }
    }

    public void turnOff(String device) {
        // Already off
        if (Boolean.FALSE.equals(deviceStatus.get("device")))
            return;

        deviceStatus.put(device, Boolean.FALSE);
        try {
            tellstick.getProxiedDeviceByName(device, OnOffDevice.class).off();
        } catch (DeviceException e) {
            throw new RuntimeException(e);
        }
    }

}
