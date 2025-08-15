package com.example.myapplication1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Home_Fragment extends Fragment {
    private EditText fromDate, toDate, fromTime, toTime, zipCode;
    private Button registerButton;
    private String user_id;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Get user_id from Intent
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("user_id")) {
            user_id = intent.getStringExtra("user_id");
        }

        // Initialize UI elements
        fromDate = view.findViewById(R.id.from_date);
        toDate = view.findViewById(R.id.to_date);
        fromTime = view.findViewById(R.id.from_time);
        toTime = view.findViewById(R.id.to_time);
        zipCode = view.findViewById(R.id.ruew2utpeok9);
        registerButton = view.findViewById(R.id.button3);

        // Set Click Listeners
        fromDate.setOnClickListener(v -> showDatePicker(fromDate));
        toDate.setOnClickListener(v -> showDatePicker(toDate));
        fromTime.setOnClickListener(v -> showTimePicker(fromTime));
        toTime.setOnClickListener(v -> showTimePicker(toTime));
        fetchAndShowCanceledBookings();

        registerButton.setOnClickListener(v -> {
            if (validateAndSubmit()) {
                String zip = zipCode.getText().toString().trim();
                String fromDateStr = fromDate.getText().toString().trim();
                String toDateStr = toDate.getText().toString().trim();
                String fromTimeStr = fromTime.getText().toString().trim();
                String toTimeStr = toTime.getText().toString().trim();

                Intent exploreIntent = new Intent(getActivity(), ExploreResults.class);
                exploreIntent.putExtra("zip_code", zip);
                exploreIntent.putExtra("user_id",user_id);
                exploreIntent.putExtra("from_date", fromDateStr);
                exploreIntent.putExtra("to_date", toDateStr);
                exploreIntent.putExtra("from_time", fromTimeStr);
                exploreIntent.putExtra("to_time", toTimeStr);

                startActivity(exploreIntent);
            }
        });

        return view;
    }

    private void showDatePicker(EditText dateField) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireActivity(),
                (view, year1, month1, dayOfMonth) -> dateField.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month1 + 1, year1)),
                year, month, day
        );
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePicker(EditText timeField) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireActivity(),
                (view, hourOfDay, minute1) -> timeField.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1)),
                hour, minute, true
        );
        timePickerDialog.show();
    }
    private void fetchAndShowCanceledBookings() {
        if (user_id == null || user_id.isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(config.BASE_URL+"fetch_cancel_reason.php?customer_id=" + user_id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                if (response.toString().trim().isEmpty()) {
                    return;
                }

                JSONObject jsonResponse;
                try {
                    jsonResponse = new JSONObject(response.toString());
                } catch (Exception e) {
                    return;
                }

                if (!jsonResponse.getBoolean("success")) return;

                JSONArray jsonArray = jsonResponse.getJSONArray("data");

                // Filter entries where notified == 0
                JSONArray pendingAlerts = new JSONArray();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject booking = jsonArray.getJSONObject(i);
                    if (booking.getInt("notified") == 0) {
                        pendingAlerts.put(booking);
                    }
                }

                if (pendingAlerts.length() > 0 && getActivity() != null) {
                    getActivity().runOnUiThread(() -> showCancelAlerts(pendingAlerts, 0));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


        private void showCancelAlerts(JSONArray jsonArray, int index) {
        if (index >= jsonArray.length() || getActivity() == null) return;

        try {
            JSONObject booking = jsonArray.getJSONObject(index);
            String reason = booking.getString("reason");
            String rentId = booking.getString("rent_id");

            new AlertDialog.Builder(getActivity())
                    .setTitle("Booking Canceled")
                    .setMessage("Reason: " + reason)
                    .setPositiveButton("OK", (dialog, which) -> {
                        updateNotifiedStatus(rentId);
                        showCancelAlerts(jsonArray, index + 1); // Show next alert
                    })
                    .setCancelable(false)
                    .show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void updateNotifiedStatus(String rentId) {
        new Thread(() -> {
            try {
                URL url = new URL(config.BASE_URL+"admin_cancel_bookings.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write("rent_id=" + URLEncoder.encode(rentId, "UTF-8") + "&notified=1");
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    System.out.println("Updated notified status successfully for " + rentId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }



    private boolean validateAndSubmit() {
        String fromDateStr = fromDate.getText().toString().trim();
        String toDateStr = toDate.getText().toString().trim();
        String fromTimeStr = fromTime.getText().toString().trim();
        String toTimeStr = toTime.getText().toString().trim();
        String zip = zipCode.getText().toString().trim();

        if (fromDateStr.isEmpty() || toDateStr.isEmpty() || fromTimeStr.isEmpty() || toTimeStr.isEmpty()) {
            Toast.makeText(getActivity(), "Please select all date and time fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Date fromDateTime = dateTimeFormat.parse(fromDateStr + " " + fromTimeStr);
            Date toDateTime = dateTimeFormat.parse(toDateStr + " " + toTimeStr);

            if (fromDateTime != null && toDateTime != null) {
                long differenceInMillis = toDateTime.getTime() - fromDateTime.getTime();
                long differenceInHours = differenceInMillis / (60 * 60 * 1000);

                if (differenceInHours < 1) {
                    Toast.makeText(getActivity(), "To date and time must be at least 1 hour after from date and time", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } catch (ParseException e) {
            Toast.makeText(getActivity(), "Invalid date/time format", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!zip.matches("\\d{5,6}")) {
            Toast.makeText(getActivity(), "Enter a valid ZIP Code (5-6 digits)", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
