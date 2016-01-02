package scripts

import com.eitraz.automation.helpers.RawDeviceHelper
import com.eitraz.tellstick.core.rawdevice.events.RawDeviceEvent

// ===== Help functions =====

// Determine if a raw device is updated, using local cache
def rawDeviceIsUpdated = { String name, RawDeviceHelper device ->
    device.hasEvent() && device.lastEventTime() != cache.get(name, {
        return 0;
    })
}

// Mark device as read
def markRawDeviceAsRead = { String name, RawDeviceHelper device ->
    cache.set(name, device.lastEventTime());
}

// ===== Motion detectors =====

RawDeviceHelper livingroomMotion = new RawDeviceHelper(tellstick)
        .with(RawDeviceEvent.PROTOCOL, "arctech")
        .with(RawDeviceEvent.MODEL, "codeswitch")
        .with(RawDeviceEvent.HOUSE, "E");

RawDeviceHelper entranceMotion = new RawDeviceHelper(tellstick)
        .with(RawDeviceEvent.PROTOCOL, "arctech")
        .with(RawDeviceEvent.MODEL, "codeswitch")
        .with(RawDeviceEvent.HOUSE, "M");

RawDeviceHelper kitchenMotion = new RawDeviceHelper(tellstick)
        .with(RawDeviceEvent.PROTOCOL, "arctech")
        .with(RawDeviceEvent.MODEL, "selflearning")
        .with(RawDeviceEvent.HOUSE, "8040222")
        .with(RawDeviceEvent.UNIT, "10")
        .with(RawDeviceEvent.METHOD, "turnon");

RawDeviceHelper upstairsHallwayMotion = new RawDeviceHelper(tellstick)
        .with(RawDeviceEvent.PROTOCOL, "arctech")
        .with(RawDeviceEvent.MODEL, "selflearning")
        .with(RawDeviceEvent.HOUSE, "8090342")
        .with(RawDeviceEvent.UNIT, "10")
        .with(RawDeviceEvent.METHOD, "turnon");

RawDeviceHelper upstairsMotion = new RawDeviceHelper(tellstick)
        .with(RawDeviceEvent.PROTOCOL, "arctech")
        .with(RawDeviceEvent.MODEL, "selflearning")
        .with(RawDeviceEvent.HOUSE, "8042158")
        .with(RawDeviceEvent.UNIT, "10")
        .with(RawDeviceEvent.METHOD, "turnon");

// ===== Temperature/humidity =====

RawDeviceHelper upstairsTemperatureAndHumiditySensor = new RawDeviceHelper(tellstick)
        .with(RawDeviceEvent.PROTOCOL, "mandolyn")
        .with(RawDeviceEvent.MODEL, "temperaturehumidity")
        .with(RawDeviceEvent.ID, "111");

RawDeviceHelper livingTemperatureAndHumidityRoomSensor = new RawDeviceHelper(tellstick)
        .with(RawDeviceEvent.PROTOCOL, "mandolyn")
        .with(RawDeviceEvent.MODEL, "temperaturehumidity")
        .with(RawDeviceEvent.ID, "112");

RawDeviceHelper outsideTemperatureAndHumiditySensor = new RawDeviceHelper(tellstick)
        .with(RawDeviceEvent.PROTOCOL, "mandolyn")
        .with(RawDeviceEvent.MODEL, "temperaturehumidity")
        .with(RawDeviceEvent.ID, "113");

RawDeviceHelper basementTemperatureAndHumiditySensor = new RawDeviceHelper(tellstick)
        .with(RawDeviceEvent.PROTOCOL, "mandolyn")
        .with(RawDeviceEvent.MODEL, "temperaturehumidity")
        .with(RawDeviceEvent.ID, "114");

RawDeviceHelper extraTemperatureAndHumiditySensor = new RawDeviceHelper(tellstick)
        .with(RawDeviceEvent.PROTOCOL, "fineoffset")
        .with(RawDeviceEvent.MODEL, "temperaturehumidity")
        .with(RawDeviceEvent.ID, "215");