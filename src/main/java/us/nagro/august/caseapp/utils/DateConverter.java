package us.nagro.august.caseapp.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Some static methods for converting between LocalDate and Date
 */
public class DateConverter {

    public static LocalDate convert(Date date) {
        return LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static Date convert(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
