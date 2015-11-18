package com.eitraz.automation.helpers;

import com.eitraz.forecastio.data.Forecast;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@Ignore
public class ForecastHelperTest {
    public static final String API_KEY = System.getProperty("forecastApiKey");
    public static final Double LATITUDE = 59.332722;
    public static final Double LONGITUDE = 18.068565;

    private ForecastHelper forecast;

    @Before
    public void setUp() throws Exception {
        assertFalse("No forecast.io API key provided, use -DforecastApiKey=[API-KEY] when building", StringUtils.isBlank(API_KEY));
        forecast = new ForecastHelper(API_KEY, LATITUDE, LONGITUDE);
    }

    @Test
    public void testGetForecast() throws Exception {
        Forecast forecast = this.forecast.get();
        assertNotNull(forecast);
        assertNotNull(forecast.getCurrently().getTemperature());
    }
}