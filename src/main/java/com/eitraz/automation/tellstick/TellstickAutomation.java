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
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class TellstickAutomation implements Startable, Stopable, RawDeviceEventListener, MessageListener<RawDeviceEvent> {
    private TellstickCluster tellstick;

    private final Map<String, Serializable> deviceStatus;

    private final ITopic<RawDeviceEvent> rawDeviceEventsCacheTopic;
    private final Set<RawDeviceEvent> rawDeviceEventsCache = new HashSet<>();
    private String rawDeviceEventsCacheListenerId;

    private final TimeoutHandler<String> rawEventTimeoutHandler = new TimeoutHandler<>(Duration.ONE_SECOND);
    private final Set<RawDeviceEventListener> rawDeviceEventListeners = new CopyOnWriteArraySet<>();

    public TellstickAutomation(HazelcastInstance hazelcast) {
        deviceStatus = hazelcast.getMap("tellstick.device.status");
        rawDeviceEventsCacheTopic = hazelcast.getReliableTopic("tellstick.raw.events.cachePublisher");

        tellstick = LifeCycleInstance.register(new TellstickCluster(hazelcast));
    }

    public TellstickCluster getTellstick() {
        return tellstick;
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
        rawDeviceEventsCacheListenerId = rawDeviceEventsCacheTopic.addMessageListener(this);
    }

    @Override
    public void doStop() {
        if (rawDeviceEventsCacheListenerId != null) {
            rawDeviceEventsCacheTopic.removeMessageListener(rawDeviceEventsCacheListenerId);
            rawDeviceEventsCacheListenerId = null;
        }
        tellstick.getTellstick().getRawDeviceHandler().removeRawDeviceEventListener(this);
    }

    @Override
    public void rawDeviceEvent(RawDeviceEvent event) {
        rawDeviceEventsCacheTopic.publish(event);
    }

    /**
     * @param event raw event
     */
    private void handleRawDeviceEvent(RawDeviceEvent event) {
        rawDeviceEventListeners.forEach(l -> l.rawDeviceEvent(event));
    }

    public void turnOn(String device, boolean turnOn) {
        if (turnOn)
            turnOn(device);
        else
            turnOff(device);
    }

    public void turnOn(String device) {
        // Already on
        if (Boolean.TRUE.equals(deviceStatus.get(device)))
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
        if (Boolean.FALSE.equals(deviceStatus.get(device)))
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
        synchronized (rawDeviceEventsCache) {
            Optional<RawDeviceEvent> first = rawDeviceEventsCache.stream()
                    .filter(e -> hasAllParameters(e, parameters))
                    .findFirst();
            return first.orElse(null);
        }
    }

    @Override
    public void onMessage(Message<RawDeviceEvent> message) {
        RawDeviceEvent event = message.getMessageObject();

        synchronized (rawDeviceEventsCache) {
            rawDeviceEventsCache.remove(event);
            rawDeviceEventsCache.add(event);
        }

        // Don't fire event to often
        if (rawEventTimeoutHandler.isReady(event.toString())) {
            handleRawDeviceEvent(event);
        }
    }

    public Serializable getDeviceStatus(String deviceName) {
        return deviceStatus.get(deviceName);
    }

    public Set<RawDeviceEvent> getRawDeviceEventsCache() {
        synchronized (rawDeviceEventsCache) {
            return new HashSet<>(rawDeviceEventsCache);
        }
    }
}
