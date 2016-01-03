package scripts

import com.eitraz.automation.helpers.RawDeviceHelper

// Record temperature
def recordTemperature = { String name, double temp -> println name + " temperature: " + temp; }

// Record humidity
def recordHumidity = { String name, double humidity -> println name + " humidity: " + humidity; }

// Record temperature and humidity for a sensor
def recordTempAndHumidity = { String name, RawDeviceHelper sensor ->
    double temp = Double.parseDouble(sensor.get("temp"));
    double humidity = Double.parseDouble(sensor.get("humidity"));

    recordTemperature(name, temp);
    recordHumidity(name, humidity);
}

// Check if sensor is updated, record temperature and humidity if so
def handleTemperatureAndHumiditySensor = { String name, RawDeviceHelper sensor ->
    if (rawDeviceIsUpdated(name, sensor)) {
        recordTempAndHumidity(name, sensor);
        markRawDeviceAsRead(name, sensor);
    }
}

handleTemperatureAndHumiditySensor("upstairs", upstairsTemperatureAndHumiditySensor);
handleTemperatureAndHumiditySensor("livingRoom", livingRoomTemperatureAndHumidityRoomSensor);
handleTemperatureAndHumiditySensor("outside", outsideTemperatureAndHumiditySensor);
handleTemperatureAndHumiditySensor("basement", basementTemperatureAndHumiditySensor);