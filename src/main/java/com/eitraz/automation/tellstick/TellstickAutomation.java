package com.eitraz.automation.tellstick;

import com.eitraz.automation.LifeCycleInstance;
import com.eitraz.library.Duration;
import com.eitraz.library.TimeoutHandler;
import com.eitraz.library.lifecycle.Startable;
import com.eitraz.library.lifecycle.Stopable;
import com.eitraz.tellstick.core.TellstickException;
import com.eitraz.tellstick.core.cluster.TellstickCluster;
import com.eitraz.tellstick.core.device.DeviceException;
import com.eitraz.tellstick.core.device.DeviceHandler;
import com.eitraz.tellstick.core.device.DeviceNotSupportedException;
import com.eitraz.tellstick.core.device.OnOffDevice;
import com.eitraz.tellstick.core.rawdevice.RawDeviceEventListener;
import com.eitraz.tellstick.core.rawdevice.events.RawDeviceEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.ITopic;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class TellstickAutomation implements Startable, Stopable, RawDeviceEventListener {
    protected static final Logger logger = Logger.getLogger(TellstickAutomation.class);

    public static final String HAZELCAST_TELLSTICK_DEVICE_STATUS = "tellstick.device.status";
    public static final String HAZELCAST_TELLSTICK_RAW_EVENTS_CACHE_PUBLISHER = "tellstick.raw.events.cachePublisher";
    public static final String HAZELCAST_TELLSTICK_DEVICES_CONFIGURATION_TOPIC = "tellstick.devices.configuration.topic";
    public static final String HAZELCAST_TELLSTICK_DEVICES_CONFIGURATION = "tellstick.devices.configuration";

    private TellstickCluster tellstick;

    private final Map<String, Serializable> deviceStatus;

    private String rawDeviceEventsCacheListenerId;
    private final ITopic<RawDeviceEvent> rawDeviceEventsCacheTopic;
    private final Set<RawDeviceEvent> rawDeviceEventsCache = new HashSet<>();

    private String devicesUpdatedTopicListenerId;
    private final ITopic<String> devicesUpdatedTopic;
    private final IList<TellstickDeviceConfiguration> devicesConfiguration;

    private final TimeoutHandler<String> rawEventTimeoutHandler = new TimeoutHandler<>(Duration.ONE_SECOND);
    private final Set<RawDeviceEventListener> rawDeviceEventListeners = new CopyOnWriteArraySet<>();

    public TellstickAutomation(HazelcastInstance hazelcast) {
        deviceStatus = hazelcast.getMap(HAZELCAST_TELLSTICK_DEVICE_STATUS);
        rawDeviceEventsCacheTopic = hazelcast.getReliableTopic(HAZELCAST_TELLSTICK_RAW_EVENTS_CACHE_PUBLISHER);

        devicesUpdatedTopic = hazelcast.getReliableTopic(HAZELCAST_TELLSTICK_DEVICES_CONFIGURATION_TOPIC);
        devicesConfiguration = hazelcast.getList(HAZELCAST_TELLSTICK_DEVICES_CONFIGURATION);

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

        // Raw event cache listener
        rawDeviceEventsCacheListenerId = rawDeviceEventsCacheTopic.addMessageListener(message -> {
            RawDeviceEvent event = message.getMessageObject();

            synchronized (rawDeviceEventsCache) {
                rawDeviceEventsCache.remove(event);
                rawDeviceEventsCache.add(event);
            }

            // Don't fire event to often
            if (rawEventTimeoutHandler.isReady(event.toString())) {
                handleRawDeviceEvent(event);
            }
        });

        // Listen for updated tellstick device configuration
        devicesUpdatedTopicListenerId = devicesUpdatedTopic.addMessageListener(message -> reloadDevices());
    }

    private void reloadDevices() {
        // Remove all local devices
        DeviceHandler localDeviceHandler = tellstick.getTellstick().getDeviceHandler();

        logger.info("Removing ALL local devices");
        localDeviceHandler.getDevices().forEach(localDeviceHandler::removeDevice);

        for (TellstickDeviceConfiguration device : devicesConfiguration) {
            try {
                logger.info("Creating device: " + device);
                localDeviceHandler.createDevice(device.getName(), device.getModel(), device.getProtocol(), device.getParameters());
            } catch (TellstickException | DeviceNotSupportedException e) {
                logger.error(String.format("Failed to create local device: %s", device), e);
            }
        }
    }

    @Override
    public void doStop() {
        if (devicesUpdatedTopicListenerId != null) {
            devicesUpdatedTopic.removeMessageListener(devicesUpdatedTopicListenerId);
            devicesUpdatedTopicListenerId = null;
        }

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

    public Serializable getDeviceStatus(String deviceName) {
        return deviceStatus.get(deviceName);
    }

    public Set<RawDeviceEvent> getRawDeviceEventsCache() {
        synchronized (rawDeviceEventsCache) {
            return new HashSet<>(rawDeviceEventsCache);
        }
    }
}
