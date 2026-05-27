package com.example.doggysitter.utils;

import com.example.doggysitter.models.Dog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class DateTimeUtils {
    private static final SimpleDateFormat ISO_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private DateTimeUtils() {
    }

    public static String toIsoDate(Calendar calendar) {
        return ISO_DATE_FORMAT.format(calendar.getTime());
    }

    public static String formatDisplayDate(String isoDate) {
        try {
            return DISPLAY_DATE_FORMAT.format(ISO_DATE_FORMAT.parse(isoDate));
        } catch (ParseException | NullPointerException error) {
            return isoDate;
        }
    }

    public static String formatTime(int hourOfDay, int minute) {
        return String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
    }

    public static String formatDogAge(Dog dog) {
        Integer ageMonths = dog.getAgeMonths();
        if (ageMonths != null && ageMonths > 0) {
            return formatAgeMonths(ageMonths);
        }

        Integer legacyAgeYears = dog.getAge();
        if (legacyAgeYears != null && legacyAgeYears > 0) {
            return formatAgeMonths(legacyAgeYears * 12);
        }

        return "לא צוין גיל";
    }

    public static String formatAgeMonths(int totalMonths) {
        if (totalMonths < 12) {
            return totalMonths + " חודשים";
        }

        int years = totalMonths / 12;
        int months = totalMonths % 12;
        if (months == 0) {
            return years + " שנים";
        }
        return years + " שנים ו-" + months + " חודשים";
    }
}
