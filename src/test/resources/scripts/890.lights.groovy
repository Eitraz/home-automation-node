package scripts

import com.eitraz.automation.helpers.RawDeviceHelper
import com.eitraz.automation.helpers.Time

import static com.eitraz.automation.rule.When.when
import static com.eitraz.tellstick.core.rawdevice.events.RawDeviceEvent.*

def anyRawDevicesIsActive = { long timeout, RawDeviceHelper... devices ->
    for (RawDeviceHelper device : devices) {
        if (device.isActive(timeout))
            return true;
    }
    return false;
}

String OFFICE = "Office"
String LIVINGROOM = "LivingRoom"
String KITCHEN = "Kitchen"
String ENTRANCE = "Entrance"
String STAIR = "Stair"
String HALLWAY = "Hallway"
String BEDROOM = "Bedroom"
String PLAYROOM = "Playroom"
String KIDSROOM = "Kidsroom"
String GUESTROOM = "Guestroom"
String GARDEN = "Garden"

boolean remoteUnit1ForcedOn = remoteUnit1.isActive(THREE_HOURS) && remoteUnit1.is(METHOD, ON);
boolean remoteUnit1ForcedOff = remoteUnit1.isActive(THREE_HOURS) && remoteUnit1.is(METHOD, OFF);

boolean remoteUnit2ForcedOn = remoteUnit2.isActive(THREE_HOURS) && remoteUnit2.is(METHOD, ON);
boolean remoteUnit2ForcedOff = remoteUnit2.isActive(THREE_HOURS) && remoteUnit2.is(METHOD, OFF);

// Remote forced on
when({ remoteUnit1ForcedOn }).then(
        {
            println ">> 1 forced ON"
            tellstick.turnOn(OFFICE)
            tellstick.turnOn(LIVINGROOM)
            tellstick.turnOn(KITCHEN)
            tellstick.turnOn(ENTRANCE)
            tellstick.turnOn(STAIR)
            tellstick.turnOn(PLAYROOM)
            tellstick.turnOn(GUESTROOM)
            tellstick.turnOn(GARDEN)
        }

// Remote forced off
).otherwise()
        .when({ remoteUnit1ForcedOff })
        .then(
        {
            println ">> 1 forced OFF"
            tellstick.turnOff(OFFICE)
            tellstick.turnOff(LIVINGROOM)
            tellstick.turnOff(KITCHEN)
            tellstick.turnOff(ENTRANCE)
            tellstick.turnOff(STAIR)
            tellstick.turnOff(PLAYROOM)
            tellstick.turnOff(GUESTROOM)
            tellstick.turnOff(GARDEN)
        }

// Sun is set
).otherwise()
        .when({ sunIsSet })
        .then(
        {
            println "<< Sun is set"

            // Living area
            when({
                // Motion sensor (limited by time)
                Time.between("05:30", "23:59") && anyRawDevicesIsActive(THIRTY_MINUTES,
                        livingRoomMotionSensor, entranceMotionSensor, kitchenMotionSensor)
            }).then({
                println ">> Right time and motion"

                tellstick.turnOn(OFFICE)
                tellstick.turnOn(LIVINGROOM)
                tellstick.turnOn(KITCHEN)
                tellstick.turnOn(ENTRANCE)
                tellstick.turnOn(GUESTROOM)
                tellstick.turnOn(GARDEN)
            }
            ).otherwise({
                println ">> NOT Right time or no motion"

                // Keep some lights on a specific times
                when({
                    Time.between("17:00", "22:00") || Time.between("07:00", "08:00")
                }).then({
                    tellstick.turnOn(LIVINGROOM)
                    tellstick.turnOn(ENTRANCE)
                    tellstick.turnOn(GARDEN)
                }).otherwise({
                    tellstick.turnOff(LIVINGROOM)
                    tellstick.turnOff(ENTRANCE)
                    tellstick.turnOff(GARDEN)
                }).run();

                tellstick.turnOff(OFFICE)
                tellstick.turnOff(KITCHEN)
                tellstick.turnOff(ENTRANCE)
                tellstick.turnOff(GUESTROOM)
            }
            ).run();

            // Playroom
            when({
                // Motion sensor (limited by time)
                Time.between("05:30", "23:59") && anyRawDevicesIsActive(THIRTY_MINUTES,
                        entranceMotionSensor, kitchenMotionSensor)
            }).then({
                tellstick.turnOn(PLAYROOM)
            }
            ).otherwise({
                tellstick.turnOff(PLAYROOM)
            }
            ).run();

            // Stair
            when({
                // Motion sensor (limited by time)
                Time.between("05:30", "23:59") && anyRawDevicesIsActive(THIRTY_MINUTES,
                        entranceMotionSensor, upstairsHallwayMotionSensor, upstairsMotionSensor)
            }).then({
                tellstick.turnOn(STAIR)
            }
            ).otherwise({
                tellstick.turnOff(STAIR)
            }
            ).run();
        }
).otherwise({
    println ">> Sun is not set"

    tellstick.turnOff(OFFICE)
    tellstick.turnOff(LIVINGROOM)
    tellstick.turnOff(KITCHEN)
    tellstick.turnOff(ENTRANCE)
    tellstick.turnOff(STAIR)
    tellstick.turnOff(PLAYROOM)
    tellstick.turnOff(GUESTROOM)
    tellstick.turnOff(GARDEN)
}).run();

// Remote forced on
when({ remoteUnit2ForcedOn }).then(
        {
            tellstick.turnOn(HALLWAY)
            tellstick.turnOn(BEDROOM)
            // tellstick.turnOff(KIDSROOM) - don't use force on, don't want to wake the kids
        }

// Remote forced off
).otherwise()
        .when({ remoteUnit2ForcedOff })
        .then(
        {
            tellstick.turnOff(HALLWAY)
            tellstick.turnOff(BEDROOM)
            tellstick.turnOff(KIDSROOM)
        }

// Sun is set
).otherwise()
        .when({ sunIsSet })
        .then(
        {
            // Hallway (upstairs)
            when({
                // Motion sensor (limited by time)
                Time.between("06:30", "23:59") && anyRawDevicesIsActive(THIRTY_MINUTES,
                        upstairsHallwayMotionSensor, upstairsMotionSensor)
            }).then({
                tellstick.turnOn(HALLWAY)
            }
            ).otherwise({
                tellstick.turnOff(HALLWAY)
            }
            ).run();

            // Kids room
            when({
                // Motion sensor (limited by time)
                Time.between("13:00", "18:30") && anyRawDevicesIsActive(THIRTY_MINUTES,
                        upstairsHallwayMotionSensor, upstairsMotionSensor)
            }).then({
                tellstick.turnOn(KIDSROOM)
            }
            ).otherwise({
                tellstick.turnOff(KIDSROOM)
            }
            ).run();

            // Bedroom
            when({
                // Motion sensor (limited by time)
                Time.between("08:00", "22:00") && anyRawDevicesIsActive(THIRTY_MINUTES,
                        upstairsHallwayMotionSensor, upstairsMotionSensor)
            }).then({
                tellstick.turnOn(BEDROOM)
            }
            ).otherwise({
                tellstick.turnOff(BEDROOM)
            }
            ).run();
        }
).otherwise({
    tellstick.turnOff(HALLWAY)
    tellstick.turnOff(BEDROOM)
    tellstick.turnOff(KIDSROOM)
}).run();