package scripts

import com.eitraz.automation.helpers.ForecastHelper
import com.eitraz.automation.helpers.Time
import com.eitraz.forecastio.data.Forecast

boolean sunIsSet;

// Get forecast
try {
    Forecast forecast = cache.get("forecast", {
        return new ForecastHelper("API_KEY", 59.332722, 18.068565)
    }).get();

    // Adjust sunrise/sunset by offset and cloud cover
    long sunSetOffset = THIRTY_MINUTES;
    long cloudCoverOffset = forecast.getCurrently().cloudCover * ONE_HOUR;

    long sunrise = forecast.getDaily().getData().get(0).getSunriseTime() * 1000 + sunSetOffset + cloudCoverOffset;
    long sunset = forecast.getDaily().getData().get(0).getSunsetTime() * 1000 - sunSetOffset - cloudCoverOffset;

    sunIsSet = time < sunrise || time > sunset;
} catch (Throwable e) {
    println "Unable to get forecast, using default time (17:00-07:30): " + e.getMessage();
    sunIsSet = Time.between("17:00", "07:30");
}

println "Sun is set: " + sunIsSet;
