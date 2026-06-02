package com.example.doggysitter.repositories;

import android.content.Context;
import android.text.TextUtils;

import com.example.doggysitter.models.IsraeliLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class IsraeliLocationsProvider {
    private static final String ASSET_FILE_NAME = "israeli_locations.json";
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME_HE = "שם_ישוב";
    private static final String FIELD_LAT = "lat";
    private static final String FIELD_LNG = "lng";

    private static List<IsraeliLocation> cachedLocations;

    private IsraeliLocationsProvider() {
    }

    public static synchronized List<IsraeliLocation> getLocations(Context context)
            throws IOException, JSONException {
        if (cachedLocations == null) {
            cachedLocations = Collections.unmodifiableList(loadLocations(context));
        }
        return cachedLocations;
    }

    public static List<IsraeliLocation> search(Context context, String query)
            throws IOException, JSONException {
        List<IsraeliLocation> locations = getLocations(context);
        String normalizedQuery = normalize(query);
        if (TextUtils.isEmpty(normalizedQuery)) {
            return locations;
        }

        List<IsraeliLocation> results = new ArrayList<>();
        for (IsraeliLocation location : locations) {
            if (normalize(location.getNameHe()).contains(normalizedQuery)) {
                results.add(location);
            }
        }
        return results;
    }

    private static List<IsraeliLocation> loadLocations(Context context) throws IOException, JSONException {
        String json = readAsset(context);
        JSONArray jsonArray = new JSONArray(json);
        List<IsraeliLocation> locations = new ArrayList<>();

        for (int index = 0; index < jsonArray.length(); index++) {
            JSONObject jsonObject = jsonArray.optJSONObject(index);
            if (jsonObject == null) {
                continue;
            }

            String id = jsonObject.optString(FIELD_ID, "");
            String nameHe = jsonObject.optString(FIELD_NAME_HE, "");
            double lat = jsonObject.optDouble(FIELD_LAT, Double.NaN);
            double lng = jsonObject.optDouble(FIELD_LNG, Double.NaN);

            if (TextUtils.isEmpty(nameHe) || !isValidCoordinate(lat, lng)) {
                continue;
            }

            locations.add(new IsraeliLocation(id, nameHe, lat, lng));
        }

        return locations;
    }

    private static String readAsset(Context context) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (InputStream inputStream = context.getAssets().open(ASSET_FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     inputStream,
                     StandardCharsets.UTF_8
             ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private static boolean isValidCoordinate(double lat, double lng) {
        return !Double.isNaN(lat)
                && !Double.isNaN(lng)
                && lat >= -90
                && lat <= 90
                && lng >= -180
                && lng <= 180;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
