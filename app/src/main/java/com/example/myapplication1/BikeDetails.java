package com.example.myapplication1;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class BikeDetails extends AppCompatActivity {

    private TextView bikeNameText, bikeIdText, modelText, colourText, mileageText, addressText, contactText, rentText, durationText, totalRentText;
    private Button rentNowButton;
    private ImageView backArrow,bike_pic;

    // Declare totalRent as a class-level variable
    private int totalRent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bike_details);

        bikeNameText = findViewById(R.id.bikeNameText);
        bike_pic = findViewById(R.id.r991odudhxya);
        bikeIdText = findViewById(R.id.bikeNumberText);
        modelText = findViewById(R.id.modelText);
        colourText = findViewById(R.id.colourText);
        mileageText = findViewById(R.id.mileageText);
        addressText = findViewById(R.id.addressText);
        contactText = findViewById(R.id.contactText);
        rentText = findViewById(R.id.RentText);
        durationText = findViewById(R.id.durationText);
        totalRentText = findViewById(R.id.totalRentText);
        rentNowButton = findViewById(R.id.ryg6wnehzhhg);
        backArrow = findViewById(R.id.riz1elq9hmsq);

        String bikeId = getIntent().getStringExtra("bike_id");
        String owner_id = getIntent().getStringExtra("user_id");
        String bikeName = getIntent().getStringExtra("bike_name");
        String bikeModel = getIntent().getStringExtra("bike_model");
        String bikeColour = getIntent().getStringExtra("bike_colour");
        String mileage = getIntent().getStringExtra("mileage");
        String rentPriceStr = getIntent().getStringExtra("rent_price");
        String address = getIntent().getStringExtra("shop_address");
        String contact = getIntent().getStringExtra("mobile_number");
        String customer_user_id = getIntent().getStringExtra("customer_user_id");
        String fromDate = getIntent().getStringExtra("from_date");
        String fromTime = getIntent().getStringExtra("from_time");
        String toDate = getIntent().getStringExtra("to_date");
        String toTime = getIntent().getStringExtra("to_time");
        String imageUrl = config.BASE_URL + getIntent().getStringExtra("bike_image");
        Glide.with(BikeDetails.this)
                .load(imageUrl)
                .error(R.drawable.scooty)
                .into(bike_pic);

        bikeNameText.setText(bikeName != null ? bikeName : "N/A");
        bikeIdText.setText(bikeId != null ? bikeId : "N/A");
        modelText.setText(bikeModel != null ? bikeModel : "N/A");
        colourText.setText(bikeColour != null ? bikeColour : "N/A");
        mileageText.setText((mileage != null ? mileage : "0") + " km/l");
        addressText.setText(address != null ? address : "N/A");
        contactText.setText(contact != null ? contact : "N/A");
        rentText.setText("₹" + (rentPriceStr != null ? rentPriceStr : "0") + "/hr");

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

        try {
            Date startDate = inputFormat.parse(fromDate + " " + fromTime);
            Date endDate = inputFormat.parse(toDate + " " + toTime);

            long durationInMillis = endDate.getTime() - startDate.getTime();

            if (durationInMillis > 0) {
                String displayStart = displayFormat.format(startDate);
                String displayEnd = displayFormat.format(endDate);
                durationText.setText(displayStart + " - " + displayEnd);

                long totalHours = TimeUnit.MILLISECONDS.toHours(durationInMillis);
                long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % 60;
                if (remainingMinutes > 0) {
                    totalHours++;
                }

                int rentPerHour = Integer.parseInt(rentPriceStr != null ? rentPriceStr : "0");
                totalRent = (int) (totalHours * rentPerHour);
                totalRentText.setText("₹" + totalRent);
            } else {
                durationText.setText("Invalid duration");
                totalRentText.setText("₹0");
            }

        } catch (ParseException e) {
            durationText.setText("Invalid date/time format");
            totalRentText.setText("₹0");
            e.printStackTrace();
        }

        backArrow.setOnClickListener(v -> finish());

        rentNowButton.setOnClickListener(v -> {
            Intent intent = new Intent(BikeDetails.this, BookingData.class);
            intent.putExtra("customer_user_id", customer_user_id);
            intent.putExtra("owner_id", owner_id);
            intent.putExtra("bike_id", bikeId);
            intent.putExtra("from_date", fromDate);
            intent.putExtra("from_time", fromTime);
            intent.putExtra("to_date", toDate);
            intent.putExtra("to_time", toTime);
            intent.putExtra("total_rent", totalRent);
            startActivity(intent);
        });
    }
}
