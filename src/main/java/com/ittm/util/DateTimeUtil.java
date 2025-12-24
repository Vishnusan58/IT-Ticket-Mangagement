package com.ittm.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static LocalDate parseDate(String input) {
        return LocalDate.parse(input, DATE_FORMAT);
    }

    public static String formatDate(LocalDate date) {
        return DATE_FORMAT.format(date);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return DATE_TIME_FORMAT.format(dateTime);
    }
}
