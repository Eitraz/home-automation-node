package scripts

import com.eitraz.automation.helpers.ForecastHelper
import com.eitraz.forecastio.data.Forecast

// Get forecast
Forecast forecast = cache.get("forecast", {
    return new ForecastHelper("API_KEY", 59.332722, 18.068565)
}).get();

// Adjust sunrise/sunset by offset and cloud cover
long sunSetOffset = 30 * 60 * 1000;
long cloudCoverOffset = forecast.getCurrently().cloudCover * 60 * 60 * 1000;

long sunrise = forecast.getDaily().getData().get(0).getSunriseTime() * 1000 + sunSetOffset + cloudCoverOffset;
long sunset = forecast.getDaily().getData().get(0).getSunsetTime() * 1000 - sunSetOffset - cloudCoverOffset;

boolean sunIsSet = time < sunrise || time > sunset;

println "Sun is set: " + sunIsSet;