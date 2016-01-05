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

boolean remoteUnit1ForcedOn = remoteUnit1.isActive(THREE_HOURS) && remoteUnit1.is(METHOD, ON);
boolean remoteUnit1ForcedOff = remoteUnit1.isActive(THREE_HOURS) && remoteUnit1.is(METHOD, OFF);

boolean remoteUnit2ForcedOn = remoteUnit2.isActive(THREE_HOURS) && remoteUnit2.is(METHOD, ON);
boolean remoteUnit2ForcedOff = remoteUnit2.isActive(THREE_HOURS) && remoteUnit2.is(METHOD, OFF);

// Remote forced on
when({ remoteUnit1ForcedOn }).then(
        {
            println ">> 1 forced ON"
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
).otherwise()
        .when({ remoteUnit1ForcedOff })
        .then(
        {
            println ">> 1 forced OFF"
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

                officeOn = true;
                livingRoomOn = true;
                kitchenOn = true;
                entranceOn = true;
                guestroomOn = true;
                gardenOn = true;
            }
            ).otherwise({
                println ">> NOT Right time or no motion"

                // Keep some lights on a specific times
                when({
                    Time.between("17:00", "22:00") || Time.between("07:00", "08:00")
                }).then({
                    livingRoomOn = true;
                    entranceOn = true;
                    gardenOn = true;
                }).run();
            }
            ).run();

            // Playroom
            when({
                // Motion sensor (limited by time)
                Time.between("05:30", "23:59") && anyRawDevicesIsActive(THIRTY_MINUTES,
                        entranceMotionSensor, kitchenMotionSensor)
            }).then({
                playroomOn = true;
            }
            ).run();

            // Stair
            when({
                // Motion sensor (limited by time)
                Time.between("05:30", "23:59") && anyRawDevicesIsActive(THIRTY_MINUTES,
                        entranceMotionSensor, upstairsHallwayMotionSensor, upstairsMotionSensor)
            }).then({
                stairOn = true;
            }
            ).run();
        }
).run();

// Remote forced on
when({ remoteUnit2ForcedOn }).then(
        {
            hallwayOn = true;
            bedroomOn = true;
            // kidsRoomOn = true;
        }

// Remote forced off
).otherwise()
        .when({ remoteUnit2ForcedOff })
        .then(
        {

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
                hallwayOn = true;
            }
            ).run();

            // Kids room
            when({
                // Motion sensor (limited by time)
                Time.between("13:00", "18:30") && anyRawDevicesIsActive(THIRTY_MINUTES,
                        upstairsHallwayMotionSensor, upstairsMotionSensor)
            }).then({
                kidsRoomOn = true;
            }
            ).run();

            // Bedroom
            when({
                // Motion sensor (limited by time)
                Time.between("08:00", "22:00") && anyRawDevicesIsActive(THIRTY_MINUTES,
                        upstairsHallwayMotionSensor, upstairsMotionSensor)
            }).then({
                bedroomOn = true;
            }
            ).run();
        }
).run();