package com.example.myapplication1;

import static android.app.PendingIntent.getActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

public class AddBike extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText bikeName, regNumber, milage, model, color, rentPerHour;
    private EditText fromDate, fromTime, toDate, toTime;
    private Spinner categorySpinner;
    private Button submitButton;
    private ImageView back,bike_img;
    String imageUrl;
    private String selectedCategory;
    private String userId;
    private Uri selectedImageUri = null;

    private static final String REGISTER_URL = config.BASE_URL+"add_bike.php";
    private static final String FETCH_BIKE_URL = config.BASE_URL+"fetch_bike.php";
    String owner_id,bikeId;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_vehicle);

        // Get user_id from previous activity
        userId = getIntent().getStringExtra("user_id");
        owner_id = getIntent().getStringExtra("owner_id");
        bikeId = getIntent().getStringExtra("bike_id");


        // Initialize UI elements
        bikeName = findViewById(R.id.rtjqal6c8ut);
        regNumber = findViewById(R.id.r2qvaj6nteki);
        milage = findViewById(R.id.rlcq1i21qfsg);
        model = findViewById(R.id.rtdwzw2ct8oe);
        color = findViewById(R.id.rlcq1i21qfs);
        rentPerHour = findViewById(R.id.rtdwzw2ct8o);
        fromDate = findViewById(R.id.from_date);
        fromTime = findViewById(R.id.from_time);
        toDate = findViewById(R.id.to_date);
        toTime = findViewById(R.id.to_time);
        submitButton = findViewById(R.id.ryg6wnehzhhg);
        categorySpinner = findViewById(R.id.spinner_category);
        back = findViewById(R.id.riz1elq9hmsq);
        bike_img = findViewById(R.id.r991odudhxya);

        back.setOnClickListener(v -> finish());
        bike_img.setOnClickListener(v -> {
                openGallery();
        });

        // Setup category spinner
        String[] categories = {"Scooty", "Gear Bike", "Bicycle"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        if (bikeId != null && !bikeId.isEmpty()) {
            fetchBikeData();
        }


        // Set date & time pickers
        fromDate.setOnClickListener(v -> showDatePicker(fromDate));
        fromTime.setOnClickListener(v -> showTimePicker(fromTime));
        toDate.setOnClickListener(v -> showDatePicker(toDate));
        toTime.setOnClickListener(v -> showTimePicker(toTime));

        // Submit button
        submitButton.setOnClickListener(v -> registerUser());
    }

    private void fetchBikeData() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(FETCH_BIKE_URL + "?bike_id=" + bikeId);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getString("status").equals("success")) {
                    JSONObject bikeData = jsonResponse.getJSONObject("data");
                    runOnUiThread(() -> populateFields(bikeData));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (conn != null) conn.disconnect();
                } catch (Exception ignored) {}
            }
        }).start();
    }

    private void populateFields(JSONObject bikeData) {
        try {
            bikeName.setText(bikeData.getString("Bike_Name"));
            regNumber.setText(bikeData.getString("Bike_Reg_number"));
            milage.setText(bikeData.getString("Milage"));
            model.setText(bikeData.getString("Model"));
            color.setText(bikeData.getString("colour"));
            rentPerHour.setText(bikeData.getString("rent_price"));
            fromDate.setText(bikeData.getString("From_date"));
            fromTime.setText(bikeData.getString("From_time"));
            toDate.setText(bikeData.getString("To_date"));
            toTime.setText(bikeData.getString("To_Time"));
            imageUrl = config.BASE_URL + bikeData.optString("bike_image");
            Glide.with(AddBike.this)
                    .load(imageUrl)
                    .error(R.drawable.scooty)
                    .into(bike_img);

            String category = bikeData.getString("Category");
            int spinnerPosition = ((ArrayAdapter<String>) categorySpinner.getAdapter()).getPosition(category);
            categorySpinner.setSelection(spinnerPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            bike_img.setImageURI(selectedImageUri);
        }
    }
private void showDatePicker(EditText dateField) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddBike.this,
                (view, year1, month1, dayOfMonth) -> dateField.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", year1, month1 + 1, dayOfMonth)),
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
                AddBike.this,
                (view, hourOfDay, minute1) -> timeField.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1)),
                hour, minute, true
        );
        timePickerDialog.show();
    }
    private void registerUser() {
        if (bikeName.getText().toString().trim().isEmpty() || regNumber.getText().toString().trim().isEmpty() ||
                milage.getText().toString().trim().isEmpty() || model.getText().toString().trim().isEmpty() ||
                color.getText().toString().trim().isEmpty() || rentPerHour.getText().toString().trim().isEmpty() ||
                fromDate.getText().toString().trim().isEmpty() || fromTime.getText().toString().trim().isEmpty() ||
                toDate.getText().toString().trim().isEmpty() || toTime.getText().toString().trim().isEmpty()) {
            showToast("All fields are required");
            return;
        }
        new RegisterBikeTask().execute();
    }


    private class RegisterBikeTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String boundary = "*****" + System.currentTimeMillis() + "*****";
                URL url = new URL(REGISTER_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                writeFormField(outputStream, boundary, "user_id", userId);
                writeFormField(outputStream, boundary, "Bike_Name", bikeName.getText().toString().trim());
                writeFormField(outputStream, boundary, "Bike_Reg_number", regNumber.getText().toString().trim());
                writeFormField(outputStream, boundary, "Milage", milage.getText().toString().trim());
                writeFormField(outputStream, boundary, "Model", model.getText().toString().trim());
                writeFormField(outputStream, boundary, "colour", color.getText().toString().trim());
                writeFormField(outputStream, boundary, "rent_price", rentPerHour.getText().toString().trim());
                writeFormField(outputStream, boundary, "From_date", fromDate.getText().toString().trim());
                writeFormField(outputStream, boundary, "From_time", fromTime.getText().toString().trim());
                writeFormField(outputStream, boundary, "To_date", toDate.getText().toString().trim());
                writeFormField(outputStream, boundary, "To_Time", toTime.getText().toString().trim());
                writeFormField(outputStream, boundary, "Category", selectedCategory);


                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        byte[] buffer = new byte[1024];
                        int bytesRead;

                        outputStream.writeBytes("--" + boundary + "\r\n");
                        outputStream.writeBytes("Content-Disposition: form-data; name=\"profile_photo\"; filename=\"profile.jpg\"\r\n");
                        outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n");

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        outputStream.writeBytes("\r\n"); // Ensure proper format
                        inputStream.close(); // Close the input stream after use
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

            }
                outputStream.writeBytes("--" + boundary + "--\r\n");
                outputStream.flush();
                outputStream.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                return response.toString();
            } catch (Exception e) {
                return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");
                if (status.equals("success")) {
                    showToast("Bike added successfully!");
                    new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> clearFields(), 1500);
                } else {
                    showToast("Failed: " + jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                showToast("Error parsing server response");
            }
        }

        private void writeFormField(DataOutputStream outputStream, String boundary, String fieldName, String fieldValue) throws Exception {
            outputStream.writeBytes("--" + boundary + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n");
            outputStream.writeBytes("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
            outputStream.writeBytes(fieldValue + "\r\n");
        }
    }


    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(AddBike.this, message, Toast.LENGTH_LONG).show());
    }
    private void clearFields() {
        runOnUiThread(() -> {
            bikeName.setText("");
            regNumber.setText("");
            milage.setText("");
            model.setText("");
            color.setText("");
            rentPerHour.setText("");
            fromDate.setText("");
            fromTime.setText("");
            toDate.setText("");
            toTime.setText("");
            categorySpinner.setSelection(0);
            bike_img.setImageDrawable(getResources().getDrawable(R.drawable.add_image));// Reset category spinner to first option
        });
    }

}