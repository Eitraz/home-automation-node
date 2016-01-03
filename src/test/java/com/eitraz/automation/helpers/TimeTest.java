package com.eitraz.automation.helpers;

import org.junit.Test;

import java.text.SimpleDateFormat;

import static com.eitraz.automation.helpers.Time.between;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimeTest {
    private long ONE_SECOND = 1000;
    private long TWO_SECONDS = 2 * ONE_SECOND;
    private long FIVE_SECONDS = 5 * ONE_SECOND;
    private long TEN_SECONDS = 10 * ONE_SECOND;
    private long THIRTY_SECONDS = 30 * ONE_SECOND;

    private long ONE_MINUTE = 60 * ONE_SECOND;
    private long TWO_MINUTE = 2 * ONE_SECOND;
    private long FIVE_MINUTES = 5 * ONE_MINUTE;
    private long TEN_MINUTES = 10 * ONE_MINUTE;
    private long THIRTY_MINUTES = 30 * ONE_MINUTE;

    private long ONE_HOUR = 60 * ONE_MINUTE;
    private long TWO_HOURS = 2 * ONE_HOUR;
    private long THREE_HOURS = 3 * ONE_HOUR;
    private long FOUR_HOURS = 4 * ONE_HOUR;
    private long FIVE_HOURS = 5 * ONE_HOUR;

    private String offset(long offset) {
        return new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis() + offset);
    }

    @Test
    public void testBetween() throws Exception {
        assertTrue(between(offset(-ONE_MINUTE), offset(ONE_MINUTE)));
        assertTrue(between(offset(0), offset(TWO_SECONDS)));
        assertFalse(between(offset(ONE_MINUTE), offset(ONE_HOUR)));
        assertFalse(between(offset(-ONE_HOUR), offset(-ONE_MINUTE)));
        assertTrue(between(offset(0), offset(23 * ONE_HOUR)));
    }
}