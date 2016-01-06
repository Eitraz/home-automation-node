package scripts

import com.eitraz.automation.helpers.RawDeviceHelper
import com.eitraz.automation.helpers.Time

import static com.eitraz.tellstick.core.rawdevice.events.RawDeviceEvent.*

def anyRawDevicesIsActive = { long timeout, RawDeviceHelper... devices ->
    for (RawDeviceHelper device : devices) {
        if (device.isActive(timeout))
            return true;
    }
    return false;
}

boolean officeOn = false
boolean livingRoomOn = false
boolean kitchenOn = false
boolean entranceOn = false
boolean stairOn = false
boolean hallwayOn = false
boolean bedroomOn = false
boolean playroomOn = false
boolean kidsRoomOn = false
boolean guestroomOn = false
boolean gardenOn = false

boolean remoteUnit1ForcedOn = remoteUnit1.isActive(FOUR_HOURS) && remoteUnit1.is(METHOD, ON);
boolean remoteUnit1ForcedOff = remoteUnit1.isActive(FOUR_HOURS) && remoteUnit1.is(METHOD, OFF);

boolean remoteUnit2ForcedOn = remoteUnit2.isActive(FOUR_HOURS) && remoteUnit2.is(METHOD, ON);
boolean remoteUnit2ForcedOff = remoteUnit2.isActive(FOUR_HOURS) && remoteUnit2.is(METHOD, OFF);

// Remote forced on
if (remoteUnit1ForcedOn) {
    officeOn = true
    livingRoomOn = true;
    kitchenOn = true;
    entranceOn = true;
    stairOn = true;
    playroomOn = true;
    guestroomOn = true;
    gardenOn = true;
}
// Remote forced off
else if (remoteUnit1ForcedOff) {
    // off
}
// Sun is set
else if (sunIsSet) {
    // Living area
    if (Time.between("05:30", "23:59") && anyRawDevicesIsActive(THIRTY_MINUTES,
            livingRoomMotionSensor, entranceMotionSensor, kitchenMotionSensor)) {
        officeOn = true;
        livingRoomOn = true;
        kitchenOn = true;
        entranceOn = true;
        guestroomOn = true;
        gardenOn = true;
    }
    // Keep it on a specific times
    else if (Time.between("17:00", "22:00") || Time.between("07:00", "08:00")) {
        livingRoomOn = true;
        entranceOn = true;
        gardenOn = true;
    }

    // Playroom
    if (Time.between("05:30", "23:59") && anyRawDevicesIsActive(THIRTY_MINUTES,
            entranceMotionSensor, kitchenMotionSensor)) {
        playroomOn = true;
    }

    // Stair
    if (Time.between("05:30", "23:59") && anyRawDevicesIsActive(THIRTY_MINUTES,
            entranceMotionSensor, upstairsHallwayMotionSensor, upstairsMotionSensor)) {
        stairOn = true;
    }

    // Remote forced on
    if (remoteUnit2ForcedOn) {
        hallwayOn = true;
        bedroomOn = true;
        // kidsRoomOn = true;
    }
    // Remote forced off
    else if (remoteUnit2ForcedOff) {
        // off
    }
    // Sun is set
    else if (sunIsSet) {
        // Hallway (upstairs)
        if (Time.between("06:30", "23:59") && anyRawDevicesIsActive(THIRTY_MINUTES,
                upstairsHallwayMotionSensor, upstairsMotionSensor)) {
            hallwayOn = true;
        }

        // Kids room
        else if (Time.between("13:00", "18:30") && anyRawDevicesIsActive(THIRTY_MINUTES,
                upstairsHallwayMotionSensor, upstairsMotionSensor)) {
            kidsRoomOn = true;
        }

        // Bedroom
        else if (Time.between("08:00", "22:00") && anyRawDevicesIsActive(THIRTY_MINUTES,
                upstairsHallwayMotionSensor, upstairsMotionSensor)) {
            bedroomOn = true;
        }
    }
}

// TODO remove 'false'
if (false) {
    tellstick.turnOn("Office", officeOn);
    tellstick.turnOn("LivingRoom", livingRoomOn);
    tellstick.turnOn("Kitchen", kitchenOn);
    tellstick.turnOn("Entrance", entranceOn);
    tellstick.turnOn("Stair", stairOn);
    tellstick.turnOn("Hallway", hallwayOn);
    tellstick.turnOn("Bedroom", bedroomOn);
    tellstick.turnOn("Playroom", playroomOn);
    tellstick.turnOn("KidsRoom", kidsRoomOn);
    tellstick.turnOn("Guestroom", guestroomOn);
    tellstick.turnOn("Garden", gardenOn);
}