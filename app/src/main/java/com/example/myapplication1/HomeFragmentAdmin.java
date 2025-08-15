package com.example.myapplication1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragmentAdmin extends Fragment {
    TextView todays, total;
    SharedPreferences prefs;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_admin, container, false);

        todays = view.findViewById(R.id.todays_req);
        total = view.findViewById(R.id.total_req);

        prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        updateTextViews(); // Set initial values

        // Listener to update UI whenever SharedPreferences change
        listener = (sharedPreferences, key) -> {
            if (key.equals("todays_requests") || key.equals("total_requests")) {
                updateTextViews();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);

        return view;
    }

    private void updateTextViews() {
        int todaysRequests = prefs.getInt("todays_requests", 0);
        int totalRequests = prefs.getInt("total_requests", 0);

        todays.setText(String.valueOf(todaysRequests));
        total.setText(String.valueOf(totalRequests));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (prefs != null) {
            prefs.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }
}
