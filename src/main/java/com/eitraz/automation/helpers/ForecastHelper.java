package com.eitraz.automation.helpers;

import com.eitraz.forecastio.ForecastIO;
import com.eitraz.forecastio.data.Forecast;
import com.eitraz.library.Duration;
import com.eitraz.library.TimeoutHandler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ForecastHelper {
    private TimeoutHandler<String> timeoutHandler = new TimeoutHandler<>(new Duration(15, TimeUnit.MINUTES));
    private double latitude;
    private double longitude;

    private ForecastIO io;
    private Forecast forecast;

    public ForecastHelper(String apiKey, double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;

        io = new ForecastIO(apiKey);
    }

    public Forecast get() throws IOException {
        if (timeoutHandler.isReady("forecast")) {
            forecast = io.getForecast(latitude, longitude);
        }
        return forecast;
    }
}
