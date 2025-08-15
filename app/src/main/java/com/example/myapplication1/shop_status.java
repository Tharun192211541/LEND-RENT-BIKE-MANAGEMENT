package com.example.myapplication1;

import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class shop_status extends AppCompatActivity {

    private CheckBox selectAllDays;
    private final Map<String, DayTimePicker> dayPickers = new HashMap<>();
    private String globalOpenTime = "", globalCloseTime = "";
    private Button saveButton;
    private final String API_URL = config.BASE_URL + "shop_timings.php"; // Replace with your actual API URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_time);

        selectAllDays = findViewById(R.id.selectAllDays);
        saveButton = findViewById(R.id.save);

        setupDayPicker("Monday", R.id.mondayPicker);
        setupDayPicker("Tuesday", R.id.tuesdayPicker);
        setupDayPicker("Wednesday", R.id.wednesdayPicker);
        setupDayPicker("Thursday", R.id.thursdayPicker);
        setupDayPicker("Friday", R.id.fridayPicker);
        setupDayPicker("Saturday", R.id.saturdayPicker);
        setupDayPicker("Sunday", R.id.sundayPicker);

        selectAllDays.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                for (DayTimePicker picker : dayPickers.values()) {
                    picker.setTimes(globalOpenTime, globalCloseTime);
                    picker.closedCheckBox.setChecked(false); // Uncheck closed if selectAllDays is checked
                }
            }
        });

        saveButton.setOnClickListener(v -> sendDataToServer());
    }

    private void setupDayPicker(String day, int layoutId) {
        View dayLayout = findViewById(layoutId);
        TextView dayLabel = dayLayout.findViewById(R.id.dayLabel);
        dayLabel.setText(day);

        EditText openTime = dayLayout.findViewById(R.id.openTime);
        EditText closeTime = dayLayout.findViewById(R.id.closeTime);
        CheckBox closedCheckBox = dayLayout.findViewById(R.id.closed);

        openTime.setOnClickListener(v -> {
            if (!closedCheckBox.isChecked()) {
                showTimePicker(openTime, true);
            }
        });

        closeTime.setOnClickListener(v -> {
            if (!closedCheckBox.isChecked()) {
                showTimePicker(closeTime, false);
            }
        });

        closedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                openTime.setEnabled(false);
                closeTime.setEnabled(false);
                openTime.setText("");
                closeTime.setText("");
            } else {
                openTime.setEnabled(true);
                closeTime.setEnabled(true);
            }
        });

        dayPickers.put(day, new DayTimePicker(openTime, closeTime, closedCheckBox));
    }

    private void showTimePicker(EditText editText, boolean isOpeningTime) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    String time = hourOfDay + ":" + (minute1 < 10 ? "0" + minute1 : minute1);
                    editText.setText(time);

                    if (selectAllDays.isChecked()) {
                        if (isOpeningTime) {
                            globalOpenTime = time;
                        } else {
                            globalCloseTime = time;
                        }

                        for (DayTimePicker picker : dayPickers.values()) {
                            if (isOpeningTime) {
                                picker.openTime.setText(globalOpenTime);
                            } else {
                                picker.closeTime.setText(globalCloseTime);
                            }
                        }
                    }
                },
                hour, minute, true);
        timePickerDialog.show();
    }

    private void sendDataToServer() {
        JSONObject jsonObject = new JSONObject();
        try {
            String userId = getIntent().getStringExtra("user_id");  // Replace with actual dynamic user ID
            jsonObject.put("user_id", userId);

            for (Map.Entry<String, DayTimePicker> entry : dayPickers.entrySet()) {
                String day = entry.getKey();
                DayTimePicker picker = entry.getValue();

                String open = picker.openTime.getText().toString().trim();
                String close = picker.closeTime.getText().toString().trim();
                boolean isClosedChecked = picker.closedCheckBox.isChecked();

                if (isClosedChecked || (open.isEmpty() && close.isEmpty())) {
                    jsonObject.put(day.toLowerCase() + "_open", "");
                    jsonObject.put(day.toLowerCase() + "_close", "");
                    jsonObject.put(day.toLowerCase() + "_closed", 1);
                } else {
                    jsonObject.put(day.toLowerCase() + "_open", open);
                    jsonObject.put(day.toLowerCase() + "_close", close);
                    jsonObject.put(day.toLowerCase() + "_closed", 0);
                }
            }

            Log.d("RequestPayload", jsonObject.toString());
            new SendDataToServerTask().execute(jsonObject.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class DayTimePicker {
        private final EditText openTime, closeTime;
        private final CheckBox closedCheckBox;

        public DayTimePicker(EditText openTime, EditText closeTime, CheckBox closedCheckBox) {
            this.openTime = openTime;
            this.closeTime = closeTime;
            this.closedCheckBox = closedCheckBox;
        }

        public void setTimes(String open, String close) {
            if (!open.isEmpty()) openTime.setText(open);
            if (!close.isEmpty()) closeTime.setText(close);
        }
    }

    private class SendDataToServerTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(params[0]);
                writer.flush();
                writer.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else {
                    return "Error: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("ServerResponse", result);
            Toast.makeText(shop_status.this, "Response: " + result, Toast.LENGTH_SHORT).show();
        }
    }
}
