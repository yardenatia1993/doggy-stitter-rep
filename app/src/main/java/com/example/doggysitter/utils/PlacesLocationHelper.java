package com.example.doggysitter.utils;

import com.example.doggysitter.BuildConfig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PlacesLocationHelper {
    private PlacesLocationHelper() {
    }

    public static boolean isManualSearchAvailable(Context context) {
        return ensurePlacesInitialized(context);
    }

    public static Intent buildAutocompleteIntent(Activity activity) {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
        );

        return new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountries(Collections.singletonList("IL"))
                .build(activity);
    }

    public static String getReadableLabel(Place place) {
        String address = place.getAddress();
        if (!TextUtils.isEmpty(address)) {
            return address;
        }
        String name = place.getName();
        return name == null ? "" : name;
    }

    private static boolean ensurePlacesInitialized(Context context) {
        String apiKey = BuildConfig.GOOGLE_PLACES_API_KEY;
        if (TextUtils.isEmpty(apiKey)) {
            return false;
        }
        if (!Places.isInitialized()) {
            Places.initialize(context.getApplicationContext(), apiKey);
        }
        return true;
    }
}
