package com.example.myapplication1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Customer_cart_previous extends AppCompatActivity {

    TextView bikeNameText, rentText, mileageText, colorText, ModelText, RegText, Lender_name, Lender_number, Store_name, Store_address;
    LinearLayout orderID, Lender_data, Lender_contente, vehiclefeedback, lender_feed;
    Button cancelButton;
    ImageView bike_pic;
    boolean isLenderVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_cart_previous);

        bikeNameText = findViewById(R.id.bike_name);
        rentText = findViewById(R.id.rent);
        mileageText = findViewById(R.id.mileage);
        colorText = findViewById(R.id.colour);
        ModelText = findViewById(R.id.model);
        bike_pic = findViewById(R.id.bike_image);
        RegText = findViewById(R.id.number);
        cancelButton = findViewById(R.id.cancel);
        Lender_name = findViewById(R.id.Lender_name_text);
        Lender_number = findViewById(R.id.mobile);
        Store_name = findViewById(R.id.store_name);
        Store_address = findViewById(R.id.address);
        vehiclefeedback = findViewById(R.id.vehicle_feedback);
        orderID = findViewById(R.id.order_details);
        Lender_data = findViewById(R.id.Lender);
        Lender_contente = findViewById(R.id.Lender_content);
        lender_feed = findViewById(R.id.lender_feedback);
        Lender_contente.setVisibility(View.GONE);
        String imageUrl = config.BASE_URL + getIntent().getStringExtra("bike_image");
        Glide.with(Customer_cart_previous.this)
                .load(imageUrl)
                .error(R.drawable.scooty)
                .into(bike_pic);

        String bikeName = getIntent().getStringExtra("bike_name");
        String bikeId = getIntent().getStringExtra("bike_id");
        String bookingType = getIntent().getStringExtra("booking_type");
        String rent = getIntent().getStringExtra("total_rent");
        String mileage = getIntent().getStringExtra("mileage");
        String color = getIntent().getStringExtra("color");
        String model = getIntent().getStringExtra("bike_model");
        String regnum = getIntent().getStringExtra("bike_id");
        String rent_id = getIntent().getStringExtra("rent_id");
        String lname = getIntent().getStringExtra("owner_name");
        String lnumber = getIntent().getStringExtra("owner_mobile");
        String sname = getIntent().getStringExtra("store_name");
        String saddress = getIntent().getStringExtra("store_address");

        bikeNameText.setText(bikeName);
        rentText.setText("Total Rent: " + rent + " ₹");
        mileageText.setText("Mileage: " + mileage + " km/l");
        colorText.setText("Color: " + color);
        ModelText.setText("Model: " + model);
        RegText.setText("Bike Register Number: " + regnum);
        Lender_name.setText(lname);
        Lender_number.setText(lnumber);
        Store_name.setText(sname);
        Store_address.setText(saddress);

        if ("current".equalsIgnoreCase(bookingType)) {
            cancelButton.setVisibility(View.VISIBLE);
        } else {
            cancelButton.setVisibility(View.GONE);
        }

        Lender_data.setOnClickListener(v -> {
            if (isLenderVisible) {
                Lender_contente.setVisibility(View.GONE);
                isLenderVisible = false;
            } else {
                Lender_contente.setVisibility(View.VISIBLE);
                isLenderVisible = true;
            }
        });

        orderID.setOnClickListener(v -> {
            Intent intent = new Intent(Customer_cart_previous.this, receipt.class);
            intent.putExtra("rent_id", rent_id);
            String customerUserId = getIntent().getStringExtra("customer_user_id");
            intent.putExtra("user_id", customerUserId);
            startActivity(intent);
        });

        vehiclefeedback.setOnClickListener(v -> {
            Intent intent = new Intent(Customer_cart_previous.this, customer_feedback.class);
            intent.putExtra("rent_id", rent_id);
            intent.putExtra("bike_image", getIntent().getStringExtra("bike_image"));
            String customerUserId = getIntent().getStringExtra("customer_user_id");
            intent.putExtra("user_id", customerUserId);
            startActivity(intent);
        });

        lender_feed.setOnClickListener(v -> {
            Intent intent = new Intent(Customer_cart_previous.this, CLenderFeedback.class);
            intent.putExtra("rent_id", rent_id);
            String customerUserId = getIntent().getStringExtra("customer_user_id");
            intent.putExtra("user_id", customerUserId);
            intent.putExtra("owner_name",lname);
            startActivity(intent);
        });


        cancelButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(Customer_cart_previous.this)
                    .setTitle("Cancel Booking")
                    .setMessage("Are you sure you want to cancel this booking?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        new Thread(() -> {
                            try {
                                URL url = new URL(config.BASE_URL+"cancel_bookings.php");
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("POST");
                                conn.setDoOutput(true);
                                conn.setDoInput(true);
                                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                                // Send data
                                String postData = "rent_id=" + URLEncoder.encode(rent_id, "UTF-8");
                                OutputStream os = conn.getOutputStream();
                                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                                writer.write(postData);
                                writer.flush();
                                writer.close();
                                os.close();

                                int responseCode = conn.getResponseCode();
                                if (responseCode == HttpURLConnection.HTTP_OK) {
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                    StringBuilder response = new StringBuilder();
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        response.append(line);
                                    }
                                    reader.close();

                                    runOnUiThread(() -> {
                                        Toast.makeText(Customer_cart_previous.this, "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                } else {
                                    runOnUiThread(() -> Toast.makeText(Customer_cart_previous.this, "Failed to cancel booking", Toast.LENGTH_SHORT).show());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(() -> Toast.makeText(Customer_cart_previous.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                            }
                        }).start();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

    }
}
