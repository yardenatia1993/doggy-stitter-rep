package com.example.doggysitter.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doggysitter.R;
import com.example.doggysitter.models.IsraeliLocation;
import com.example.doggysitter.repositories.IsraeliLocationsProvider;

import java.util.ArrayList;
import java.util.List;

public final class IsraeliLocationPickerDialog {
    public interface Listener {
        void onLocationSelected(IsraeliLocation location);
    }

    private IsraeliLocationPickerDialog() {
    }

    public static void show(Context context, Listener listener) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * context.getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding / 2, padding, 0);

        TextView searchLabel = new TextView(context);
        searchLabel.setText(R.string.location_search_label);
        searchLabel.setTextDirection(View.TEXT_DIRECTION_RTL);
        container.addView(searchLabel);

        EditText searchEditText = new EditText(context);
        searchEditText.setHint(R.string.location_search_hint);
        searchEditText.setSingleLine(true);
        searchEditText.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
        container.addView(searchEditText);

        ProgressBar progressBar = new ProgressBar(context);
        container.addView(progressBar);

        TextView emptyTextView = new TextView(context);
        emptyTextView.setText(R.string.no_location_results);
        emptyTextView.setTextDirection(View.TEXT_DIRECTION_RTL);
        emptyTextView.setVisibility(View.GONE);
        container.addView(emptyTextView);

        ListView listView = new ListView(context);
        int listHeight = (int) (320 * context.getResources().getDisplayMetrics().density);
        listView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                listHeight
        ));
        container.addView(listView);

        ArrayAdapter<IsraeliLocation> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.choose_location_title)
                .setView(container)
                .setNegativeButton(R.string.cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            IsraeliLocation selectedLocation = adapter.getItem(position);
            if (selectedLocation != null) {
                listener.onLocationSelected(selectedLocation);
                dialog.dismiss();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence value, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence value, int start, int before, int count) {
                updateResults(context, adapter, emptyTextView, value.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        dialog.setOnShowListener(dialogInterface -> {
            progressBar.setVisibility(View.GONE);
            updateResults(context, adapter, emptyTextView, "");
        });
        dialog.show();
    }

    private static void updateResults(Context context, ArrayAdapter<IsraeliLocation> adapter,
                                      TextView emptyTextView, String query) {
        try {
            List<IsraeliLocation> results = IsraeliLocationsProvider.search(context, query);
            adapter.clear();
            adapter.addAll(results);
            adapter.notifyDataSetChanged();
            emptyTextView.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (Exception error) {
            adapter.clear();
            adapter.notifyDataSetChanged();
            emptyTextView.setVisibility(View.VISIBLE);
            Toast.makeText(context, R.string.error_load_locations, Toast.LENGTH_SHORT).show();
        }
    }
}
