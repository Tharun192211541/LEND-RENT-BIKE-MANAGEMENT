package com.example.myapplication1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class delete_customers extends AppCompatActivity {
    private LinearLayout currentBookingsGrid;
    private ProgressDialog progressDialog;
    private static final String API_URL = config.BASE_URL+"CustomerOfOwner.php";
    private static final String CANCEL_API_URL = config.BASE_URL+"admin_cancel_bookings.php";
    private String OWNER_ID ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_customer);
        OWNER_ID = getIntent().getStringExtra("user_id");

        currentBookingsGrid = findViewById(R.id.reviewsContainer);

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
            progressDialog = new ProgressDialog(delete_customers.this);
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

                String postData = "owner_id=" + URLEncoder.encode(ownerId, "UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

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
                Toast.makeText(delete_customers.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray currentRents = jsonObject.getJSONArray("current_rents");

                runOnUiThread(() -> populateBookings(currentRents, currentBookingsGrid));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void populateBookings(JSONArray bookings, LinearLayout bookingsGrid) {
        bookingsGrid.removeAllViews();
        try {
            for (int i = 0; i < bookings.length(); i++) {
                JSONObject booking = bookings.getJSONObject(i);
                View bookingView = getLayoutInflater().inflate(R.layout.delect_customer_content, null);
                TextView userName = bookingView.findViewById(R.id.User_name);
                TextView mobile = bookingView.findViewById(R.id.Mobile);
                Button cancel = bookingView.findViewById(R.id.cancel_button);

                String name = booking.getString("customer_name");
                userName.setText(name);
                mobile.setText(booking.getString("mobile"));

                String rentId = booking.optString("rent_id", "N/A");
                String customerId = booking.optString("customer_id", "N/A");

                cancel.setOnClickListener(v -> showCancelConfirmationDialog(rentId, customerId));
                bookingView.setOnClickListener(v -> {
                    Intent intent = new Intent(delete_customers.this, admin_user_details.class);
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

    private void showCancelConfirmationDialog(String rentId, String customerId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> showCancellationReasonPopup(rentId, customerId))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showCancellationReasonPopup(String rentId, String customerId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Cancellation Reason");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.cancel_reason, null);
        final EditText input = viewInflated.findViewById(R.id.cancel_reason_input);

        builder.setView(viewInflated);
        builder.setPositiveButton("Submit", (dialog, which) -> {
            String reason = input.getText().toString().trim();
            if (!reason.isEmpty()) {
                new SendCancellationDetails().execute(rentId, customerId, reason);
            } else {
                Toast.makeText(delete_customers.this, "Reason cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private class SendCancellationDetails extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(delete_customers.this);
            progressDialog.setMessage("Cancelling booking...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String rentId = params[0];
            String customerId = params[1];
            String reason = params[2];

            try {
                URL url = new URL(CANCEL_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "owner_id=" + URLEncoder.encode(OWNER_ID, "UTF-8") +
                        "&customer_id=" + URLEncoder.encode(customerId, "UTF-8") +
                        "&rent_id=" + URLEncoder.encode(rentId, "UTF-8") +
                        "&reason=" + URLEncoder.encode(reason, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

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
            Toast.makeText(delete_customers.this, result != null ? "Booking cancelled successfully" : "Failed to cancel booking", Toast.LENGTH_SHORT).show();
        }
    }
}
