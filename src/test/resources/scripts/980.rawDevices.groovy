package scripts

import com.eitraz.automation.helpers.RawDeviceHelper

import static com.eitraz.tellstick.core.rawdevice.events.RawDeviceEvent.*

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

RawDeviceHelper livingRoomMotionSensor = new RawDeviceHelper(tellstick)
        .with(PROTOCOL, "arctech")
        .with(MODEL, "codeswitch")
        .with(HOUSE, "E");

RawDeviceHelper entranceMotionSensor = new RawDeviceHelper(tellstick)
        .with(PROTOCOL, "arctech")
        .with(MODEL, "codeswitch")
        .with(HOUSE, "M");

RawDeviceHelper kitchenMotionSensor = new RawDeviceHelper(tellstick)
        .with(PROTOCOL, "arctech")
        .with(MODEL, "selflearning")
        .with(HOUSE, "8040222")
        .with(UNIT, "10")
        .with(METHOD, "turnon");

RawDeviceHelper upstairsHallwayMotionSensor = new RawDeviceHelper(tellstick)
        .with(PROTOCOL, "arctech")
        .with(MODEL, "selflearning")
        .with(HOUSE, "8090342")
        .with(UNIT, "10")
        .with(METHOD, "turnon");

RawDeviceHelper upstairsMotionSensor = new RawDeviceHelper(tellstick)
        .with(PROTOCOL, "arctech")
        .with(MODEL, "selflearning")
        .with(HOUSE, "8042158")
        .with(UNIT, "10")
        .with(METHOD, "turnon");

// ===== Temperature/humidity =====

RawDeviceHelper upstairsTemperatureAndHumiditySensor = new RawDeviceHelper(tellstick)
        .with(PROTOCOL, "mandolyn")
        .with(MODEL, "temperaturehumidity")
        .with(ID, "111");

//RawDeviceHelper livingRoomTemperatureAndHumidityRoomSensor = new RawDeviceHelper(tellstick)
//        .with(PROTOCOL, "mandolyn")
//        .with(MODEL, "temperaturehumidity")
//        .with(ID, "112");

RawDeviceHelper outsideTemperatureAndHumiditySensor = new RawDeviceHelper(tellstick)
        .with(PROTOCOL, "mandolyn")
        .with(MODEL, "temperaturehumidity")
        .with(ID, "113");

RawDeviceHelper basementTemperatureAndHumiditySensor = new RawDeviceHelper(tellstick)
        .with(PROTOCOL, "mandolyn")
        .with(MODEL, "temperaturehumidity")
        .with(ID, "114");

RawDeviceHelper livingRoomTemperatureAndHumidityRoomSensor = new RawDeviceHelper(tellstick)
        .with(PROTOCOL, "fineoffset")
        .with(MODEL, "temperaturehumidity")
        .with(ID, "215");

// ===== Brightness sensor =====

RawDeviceHelper brightnessSensor = new RawDeviceHelper(tellstick)
        .with(PROTOCOL, "arctech")
        .with(MODEL, "selflearning")
        .with(HOUSE, "8040222")
        .with(UNIT, "10");

// ===== Remotes =====

RawDeviceHelper remoteUnit1 = new RawDeviceHelper(tellstick)
        .with(PROTOCOL, "arctech")
        .with(MODEL, "selflearning")
        .with(HOUSE, "3232178")
        .with(UNIT, "1");

RawDeviceHelper remoteUnit2 = new RawDeviceHelper(tellstick)
        .with(PROTOCOL, "arctech")
        .with(MODEL, "selflearning")
        .with(HOUSE, "3232178")
        .with(UNIT, "2");