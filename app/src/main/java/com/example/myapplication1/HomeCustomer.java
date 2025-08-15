package com.example.myapplication1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeCustomer extends AppCompatActivity {
    private EditText fromDate, toDate, fromTime, toTime, zipCode;
    private Button registerButton;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private String user_id; // Variable to store user_id

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_customer);

        // Receive user_id from login
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("user_id")) {
            user_id = intent.getStringExtra("user_id");
            } else {
            Toast.makeText(this, "Not Received userID: " + user_id, Toast.LENGTH_LONG).show();
            // Handle cases where user_id is not received
        }

        fromDate = findViewById(R.id.from_date);
        toDate = findViewById(R.id.to_date);
        fromTime = findViewById(R.id.from_time);
        toTime = findViewById(R.id.to_time);
        zipCode = findViewById(R.id.ruew2utpeok9);
        registerButton = findViewById(R.id.button3);

        // Pass user_id to CustomerEdit
        findViewById(R.id.you).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(HomeCustomer.this, CustomerEdit.class);
                editIntent.putExtra("user_id", user_id);
                startActivity(editIntent);
            }
        });

        fromDate.setOnClickListener(view -> showDatePicker(fromDate));
        toDate.setOnClickListener(view -> showDatePicker(toDate));
        fromTime.setOnClickListener(view -> showTimePicker(fromTime));
        toTime.setOnClickListener(view -> showTimePicker(toTime));

        registerButton.setOnClickListener(view -> {
            if (validateAndSubmit()) {
                // Get ZIP code from input field
                String zip = zipCode.getText().toString().trim();

                // Create intent and pass ZIP code along with user_id
                Intent exploreIntent = new Intent(HomeCustomer.this, ExploreResults.class);
                exploreIntent.putExtra("zip_code", zip);
                startActivity(exploreIntent);
            }
        });
    }

    private void showDatePicker(EditText dateField) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    dateField.setText(selectedDate);
                },
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
                this,
                (view, hourOfDay, minute1) -> {
                    String selectedTime = String.format("%02d:%02d", hourOfDay, minute1);
                    timeField.setText(selectedTime);
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    private boolean validateAndSubmit() {
        String fromDateStr = fromDate.getText().toString().trim();
        String toDateStr = toDate.getText().toString().trim();
        String fromTimeStr = fromTime.getText().toString().trim();
        String toTimeStr = toTime.getText().toString().trim();
        String zip = zipCode.getText().toString().trim();

        if (fromDateStr.isEmpty() || toDateStr.isEmpty() || fromTimeStr.isEmpty() || toTimeStr.isEmpty()) {
            Toast.makeText(this, "Please select all date and time fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Date fromDateTime = dateTimeFormat.parse(fromDateStr + " " + fromTimeStr);
            Date toDateTime = dateTimeFormat.parse(toDateStr + " " + toTimeStr);

            if (fromDateTime != null && toDateTime != null) {
                long differenceInMillis = toDateTime.getTime() - fromDateTime.getTime();
                long differenceInHours = differenceInMillis / (60 * 60 * 1000);

                if (differenceInHours < 1) {
                    Toast.makeText(this, "To date and time must be at least 1 hour after from date and time", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date/time format", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!zip.matches("\\d{5,6}")) {
            Toast.makeText(this, "Enter a valid ZIP Code (5-6 digits)", Toast.LENGTH_SHORT).show();
            return false;
        }
        Toast.makeText(this, "Data Submitted Successfully!", Toast.LENGTH_LONG).show();
        return true;
    }
}
