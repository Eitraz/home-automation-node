package com.eitraz.automation.helpers;

import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Time {
    protected static final Logger logger = Logger.getLogger(Time.class);

    private static final String[] TIME_FORMATS = {
            "HH:mm:ss",
            "HH:mm",
            "HH"
    };

    private static final String[] DATE_TIME_FORMATS = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd HH",
            "yyyy-MM-dd"
    };

    private Time() {
    }

    /**
     * @param start start time/date
     * @param end   end time/date
     * @return true if current time is between start and end time
     */
    public static boolean between(String start, String end) throws ParseException {
        long startTime;
        long currentTime = System.currentTimeMillis();

        long add = currentTime / (1000 * 60 * 60 * 24);
        add *= (1000 * 60 * 60 * 24);

        try {
            // Parse as time
            startTime = parseDateTime(start);

            logger.debug(String.format("Start time: %d (%s)", startTime, new Date(startTime)));
        } catch (ParseException e) {
            // Parse as time
            startTime = parseTime(start);

            startTime += add;
        }

        long endTime;
        try {
            // Parse as time
            endTime = parseDateTime(end);

            logger.debug(String.format("End time: %d (%s)", endTime, new Date(endTime)));

        } catch (ParseException e) {
            // Parse as time
            endTime = parseTime(end);

            endTime += add;

            // Move time forward one day if less than  start time
            if (endTime < startTime)
                endTime += 1000 * 60 * 60 * 24;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Current: %d (%s), start: %d (%s), end: %d (%s)",
                    currentTime, new Date(currentTime),
                    startTime, new Date(startTime),
                    endTime, new Date(endTime)));
        }

        return currentTime >= startTime && currentTime <= endTime;
    }

    /**
     * @param time time
     * @return time
     * @throws ParseException
     */
    public static long parseTime(String time) throws ParseException {
        for (String format : TIME_FORMATS) {
            try {
                return new SimpleDateFormat(format).parse(time).getTime();
            } catch (ParseException ignored) {
            }
        }

        String message = String.format("Unable to parse %s as time", time);

        if (logger.isTraceEnabled())
            logger.trace(message);

        throw new ParseException(message, 0);
    }

    /**
     * @param time date time
     * @return time
     * @throws ParseException
     */
    public static long parseDateTime(String time) throws ParseException {
        for (String format : DATE_TIME_FORMATS) {
            try {
                return new SimpleDateFormat(format).parse(time).getTime();
            } catch (ParseException ignored) {
            }
        }

        String message = String.format("Unable to parse %s as date time", time);

        if (logger.isTraceEnabled())
            logger.trace(message);

        throw new ParseException("Unable to parse " + time + " as date time", 0);
    }
}