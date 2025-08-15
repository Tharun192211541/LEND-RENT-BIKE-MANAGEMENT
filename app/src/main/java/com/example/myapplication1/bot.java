package com.example.myapplication1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class bot extends AppCompatActivity {

    private String fullApiResponse;
    private String selectedFeature = ""; // Store the selected feature

    // Additional parameters
    private String fromDate, fromTime, toDate, toTime, customerUserId;
    private String shopUserId, shopAddress, mobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bot); // Replace with your actual XML filename

        // Receive the intent data from the previous activity
        Intent intent = getIntent();
        if (intent.hasExtra("full_response")) {
            fullApiResponse = intent.getStringExtra("full_response");
        }

        fromDate = intent.getStringExtra("from_date");
        fromTime = intent.getStringExtra("from_time");
        toDate = intent.getStringExtra("to_date");
        toTime = intent.getStringExtra("to_time");
        customerUserId = intent.getStringExtra("customer_user_id");

        // ✅ Extract shop details and owner ID from full API response
        extractShopAndOwnerDetails(fullApiResponse);

        // Set up button click listeners
        setupButtonClick(R.id.cost_button, "Cost");
        setupButtonClick(R.id.comfort_button, "Comfort");
        setupButtonClick(R.id.experience_button, "Experience");
        setupButtonClick(R.id.vehicle_condition_button, "Vehicle Condition");
    }

    private void extractShopAndOwnerDetails(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray dataArray = jsonResponse.optJSONArray("data");

            if (dataArray != null && dataArray.length() > 0) {
                // ✅ Loop through all shops in "data" array
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject shopObj = dataArray.getJSONObject(i);
                    shopUserId = shopObj.optString("user_id", "N/A"); // Extract shop's user_id
                    shopAddress = shopObj.optString("shop_address", "N/A");
                    mobileNumber = shopObj.optString("mobile_number", "N/A");

                    // ✅ Extract user_id from the bikes array (if present)
                    if (shopObj.has("bikes")) {
                        JSONArray bikesArray = shopObj.getJSONArray("bikes");
                        if (bikesArray.length() > 0) {
                            JSONObject firstBike = bikesArray.getJSONObject(0);
                            shopUserId = firstBike.optString("user_id", shopUserId); // Get from bike if available
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ERROR", "JSON Parsing error: " + e.getMessage());
        }
    }

    private void setupButtonClick(int buttonId, String feature) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            selectedFeature = feature;
            navigateToFilteredBikes();
        });
    }

    private void navigateToFilteredBikes() {
        Intent intent = new Intent(bot.this, filtered_bikes.class);

        // Send all required data
        intent.putExtra("full_response", fullApiResponse);
        intent.putExtra("from_date", fromDate);
        intent.putExtra("from_time", fromTime);
        intent.putExtra("to_date", toDate);
        intent.putExtra("to_time", toTime);
        intent.putExtra("customer_user_id", customerUserId);

        // ✅ Send extracted shop details (shop owner ID now taken from `full_response`)
        intent.putExtra("user_id", shopUserId);
        intent.putExtra("shop_address", shopAddress);
        intent.putExtra("mobile_number", mobileNumber);
        intent.putExtra("selected_feature", selectedFeature);

        Log.d("DEBUG", "Navigating with: Feature=" + selectedFeature +
                ", Response=" + fullApiResponse +
                ", From Date=" + fromDate + ", From Time=" + fromTime +
                ", To Date=" + toDate + ", To Time=" + toTime +
                ", Customer ID=" + customerUserId +
                ", Shop User ID=" + shopUserId +
                ", Shop Address=" + shopAddress +
                ", Mobile Number=" + mobileNumber);

        startActivity(intent);
    }
}
