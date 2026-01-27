package com.nexbank.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidades para manejo de fechas.
 */
public class DateUtils {
    
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * Convierte LocalDateTime a String con formato por defecto
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT));
    }
    
    /**
     * Convierte LocalDate a String
     */
    public static String format(LocalDate date) {
        if (date == null) return null;
        return date.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }
    
    /**
     * Obtiene la fecha/hora actual en zona horaria de México
     */
    public static ZonedDateTime nowInMexico() {
        return ZonedDateTime.now(ZoneId.of("America/Mexico_City"));
    }
    
    /**
     * Parsea String a LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) return null;
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT));
    }
}