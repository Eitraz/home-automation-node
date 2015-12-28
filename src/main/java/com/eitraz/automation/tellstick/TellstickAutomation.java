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
import com.eitraz.tellstick.core.rawdevice.events.RawDeviceEvent;
import com.hazelcast.core.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public class TellstickAutomation implements Startable, Stopable, RawDeviceEventListener, MessageListener<RawDeviceEvent> {
    private TellstickCluster tellstick;

    private final Map<String, Serializable> deviceStatus;

    private final ITopic<RawDeviceEvent> rawDeviceEventsCacheTopic;
    private final Set<RawDeviceEvent> rawDeviceEventsCache;
    private String rawDeviceEventsCacheListenerId;

    private final BlockingQueue<RawDeviceEvent> rawEvents;
    private final TimeoutHandler<String> rawEventTimeoutHandler = new TimeoutHandler<>(Duration.ONE_SECOND);
    private Thread rawEventsThread;
    private final Set<RawDeviceEventListener> rawDeviceEventListeners = new CopyOnWriteArraySet<>();

    public TellstickAutomation(HazelcastInstance hazelcast) {
        deviceStatus = hazelcast.getMap("tellstick.device.status");
        rawEvents = hazelcast.getQueue("tellstick.raw.events");
        rawDeviceEventsCacheTopic = hazelcast.getTopic("tellstick.raw.events.cachePublisher");

        rawDeviceEventsCache = new HashSet<>();

        tellstick = LifeCycleInstance.register(new TellstickCluster(hazelcast));
    }

    public void addRawDeviceEventListener(RawDeviceEventListener listener) {
        rawDeviceEventListeners.add(listener);
    }

    public void removeRawDeviceEventListener(RawDeviceEventListener listener) {
        rawDeviceEventListeners.remove(listener);
    }

    @Override
    public void doStart() {
        tellstick.getTellstick().getRawDeviceHandler().addRawDeviceEventListener(this);

        rawEventsThread = new Thread("rawEventsThread") {
            @Override
            public void run() {
                while (rawEventsThread == this) {
                    try {
                        RawDeviceEvent event = rawEvents.poll(1000, TimeUnit.SECONDS);
                        if (event != null) {
                            // Don't fire event to often
                            if (rawEventTimeoutHandler.isReady(event.toString()))
                                handleRawDeviceEvent(event);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (HazelcastInstanceNotActiveException ignored) {
                        break;
                    }
                }
            }
        };
        rawEventsThread.start();

        rawDeviceEventsCacheListenerId = rawDeviceEventsCacheTopic.addMessageListener(this);
    }

    @Override
    public void doStop() {
        tellstick.getTellstick().getRawDeviceHandler().removeRawDeviceEventListener(this);

        if (rawDeviceEventsCacheListenerId != null) {
            rawDeviceEventsCacheTopic.removeMessageListener(rawDeviceEventsCacheListenerId);
            rawDeviceEventsCacheListenerId = null;
        }

        if (rawEventsThread != null) {
            Thread previousRawEventsThread = rawEventsThread;
            rawEventsThread = null;

            try {
                previousRawEventsThread.join(5000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void rawDeviceEvent(RawDeviceEvent event) {
        rawDeviceEventsCacheTopic.publish(event);
        rawEvents.offer(event);
    }

    /**
     * @param event raw event
     */
    private void handleRawDeviceEvent(RawDeviceEvent event) {
        rawDeviceEventListeners.forEach(l -> l.rawDeviceEvent(event));
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

    public static boolean hasAllParameters(RawDeviceEvent event, Map<String, String> parameters) {
        for (Map.Entry<String, String> param : parameters.entrySet()) {
            if (!param.getValue().equalsIgnoreCase(event.get(param.getKey())))
                return false;
        }
        return true;
    }

    public RawDeviceEvent getRawDeviceEvent(Map<String, String> parameters) {
        Optional<RawDeviceEvent> first = rawDeviceEventsCache.stream()
                .filter(e -> hasAllParameters(e, parameters))
                .findFirst();
        return first.orElse(null);
    }

    @Override
    public void onMessage(Message<RawDeviceEvent> message) {
        rawDeviceEventsCache.add(message.getMessageObject());
    }
}
