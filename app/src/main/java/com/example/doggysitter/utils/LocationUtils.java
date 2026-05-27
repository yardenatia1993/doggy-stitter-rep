package com.example.doggysitter.utils;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import java.util.Locale;

public final class LocationUtils {
    private static final double EARTH_RADIUS_KM = 6371.0;

    private LocationUtils() {
    }

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double startLat = Math.toRadians(lat1);
        double endLat = Math.toRadians(lat2);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(startLat) * Math.cos(endLat)
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static String formatDistanceHebrew(double distanceKm) {
        if (distanceKm < 1.0) {
            long meters = Math.round(distanceKm * 1000);
            return String.format(Locale.getDefault(), "%d מטר", meters);
        }
        return String.format(Locale.getDefault(), "%.1f ק״מ", distanceKm);
    }
}
