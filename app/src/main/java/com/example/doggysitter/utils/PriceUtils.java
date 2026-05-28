package com.example.doggysitter.utils;

public final class PriceUtils {
    private PriceUtils() {
    }

    public static String formatWholeShekelAmount(double amount) {
        return String.valueOf(Math.round(amount));
    }
}
