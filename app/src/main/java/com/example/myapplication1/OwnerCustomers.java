package com.example.myapplication1;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OwnerCustomers extends AppCompatActivity {
    private LinearLayout currentBookingsGrid, previousBookingsGrid;
    private ProgressDialog progressDialog;
    private static final String API_URL = config.BASE_URL+"CustomerOfOwner.php";
    private String OWNER_ID;

    public static int total =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cart);

        currentBookingsGrid = findViewById(R.id.current_bookings_grid);
        previousBookingsGrid = findViewById(R.id.previous_bookings_grid);


        // Fetch owner_id safely
        OWNER_ID = getIntent().getStringExtra("user_id");
        if (OWNER_ID == null || OWNER_ID.isEmpty()) {
            Toast.makeText(this, "Owner ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new FetchCustomerRents().execute(OWNER_ID);
    }

    private class FetchCustomerRents extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(OwnerCustomers.this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String ownerId = params[0];
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Sending owner_id as a POST parameter
                String postData = "owner_id=" + URLEncoder.encode(ownerId, "UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                // Read response from server
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            if (result == null) {
                runOnUiThread(() -> Toast.makeText(OwnerCustomers.this, "Failed to fetch data", Toast.LENGTH_SHORT).show());
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray currentRents = jsonObject.getJSONArray("current_rents");
                JSONArray previousBookings = jsonObject.getJSONArray("previous_bookings");

                // Get today's date in the format YYYY-MM-DD
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String todayDate = sdf.format(new Date());

                int todaysCount = 0;
                for (int i = 0; i < currentRents.length(); i++) {
                    JSONObject booking = currentRents.getJSONObject(i);
                    String fromDate = booking.optString("from_date", ""); // Extract the date

                    if (fromDate.equals(todayDate)) {
                        todaysCount++; // Count only today's requests
                    }
                }

                // Save count in SharedPreferences
                SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("todays_requests", todaysCount);
                editor.apply();  // Triggers UI update in HomeFragmentAdmin
                int finalTodaysCount = todaysCount;
                runOnUiThread(() -> {

                    populateBookings(currentRents, currentBookingsGrid);
                    populateBookings(previousBookings, previousBookingsGrid);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void populateBookings(JSONArray bookings, LinearLayout bookingsGrid) {
        bookingsGrid.removeAllViews();
        if (bookingsGrid == currentBookingsGrid) {
            total = bookings.length();

            SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("total_requests", total);
            editor.apply();


        }
        try {
            for (int i = 0; i < bookings.length(); i++) {

                JSONObject booking = bookings.getJSONObject(i);
                View bookingView = getLayoutInflater().inflate(R.layout.admin_current_requests, null);

                TextView userName = bookingView.findViewById(R.id.user_name);
                TextView mobile = bookingView.findViewById(R.id.mobile);
                String name = booking.getString("customer_name");
                userName.setText(name);
                mobile.setText(booking.getString("mobile"));




                bookingView.setOnClickListener(v -> {
                    Intent intent = new Intent(OwnerCustomers.this, admin_user_details.class);
                    intent.putExtra("rent_id", booking.optString("rent_id", "N/A"));
                    intent.putExtra("customer_id", booking.optString("customer_id", "N/A"));
                    intent.putExtra("customer_name", booking.optString("customer_name", "N/A"));
                    intent.putExtra("owner_id", booking.optString("owner_id", "N/A"));
                    intent.putExtra("bike_id", booking.optString("bike_id", "N/A"));
                    intent.putExtra("from_date", booking.optString("from_date", "N/A"));
                    intent.putExtra("from_time", booking.optString("from_time", "N/A"));
                    intent.putExtra("to_date", booking.optString("to_date", "N/A"));
                    intent.putExtra("to_time", booking.optString("to_time", "N/A"));
                    intent.putExtra("total_rent", booking.optString("total_rent", "N/A"));
                    intent.putExtra("mobile", booking.optString("mobile", "N/A"));
                    intent.putExtra("address", booking.optString("address", "N/A"));
                    intent.putExtra("profession", booking.optString("profession", "N/A"));
                    intent.putExtra("workplace", booking.optString("workplace", "N/A"));
                    intent.putExtra("selectedIDproof", booking.optString("selectedIDproof", "N/A"));
                    intent.putExtra("file_path", booking.optString("file_path", "N/A"));
                    startActivity(intent);
                });



                bookingsGrid.addView(bookingView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
