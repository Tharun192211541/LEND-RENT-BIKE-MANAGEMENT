package com.example.myapplication1;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class filtered_bikes extends AppCompatActivity {
    private String fromDate, fromTime, toDate, toTime, ShopAddress, MobileNumber, customer_user_id, owner_id;


    private float popupOverallRating = 0.0f;
    private int popupTotalRatings = 0;
    private double popupAvgCost = 0.0;
    private double popupAvgComfort = 0.0;
    private double popupAvgCondition = 0.0;
    private double popupAvgExperience = 0.0;
    private String popupBikeId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommended_vehicles);

        ImageView go_back = findViewById(R.id.back);
        go_back.setOnClickListener(v -> finish());

        // Get intent values
        fromDate = getIntent().getStringExtra("from_date");
        fromTime = getIntent().getStringExtra("from_time");
        toDate = getIntent().getStringExtra("to_date");
        toTime = getIntent().getStringExtra("to_time");
        ShopAddress = getIntent().getStringExtra("shop_address");
        MobileNumber = getIntent().getStringExtra("mobile_number");
        customer_user_id = getIntent().getStringExtra("customer_user_id");
        owner_id = getIntent().getStringExtra("user_id");

        GridLayout reviewsContainer = findViewById(R.id.reviewsContainer);

        // Get JSON response and selected filter from Intent
        String fullResponse = getIntent().getStringExtra("full_response");
        String selectedFeature = getIntent().getStringExtra("selected_feature");

        if (fullResponse == null || fullResponse.trim().isEmpty()) {
            Toast.makeText(this, "No response received", Toast.LENGTH_SHORT).show();
            Log.e("DEBUG", "No response received.");
            return;
        }

        Log.d("DEBUG", "Received API Response: " + fullResponse);
        Log.d("DEBUG", "Selected Feature: " + selectedFeature);

        try {
            JSONObject jsonResponse = new JSONObject(fullResponse);
            JSONArray dataArray = jsonResponse.getJSONArray("data");

            // List to store bike objects
            List<JSONObject> bikeList = new ArrayList<>();

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject shop = dataArray.getJSONObject(i);
                JSONArray bikesArray = shop.getJSONArray("bikes");

                for (int j = 0; j < bikesArray.length(); j++) {
                    bikeList.add(bikesArray.getJSONObject(j));
                }
            }

            // Step 1: Apply sorting based on selected feature
            if (selectedFeature != null) {
                switch (selectedFeature) {
                    case "Cost":
                        Collections.sort(bikeList, Comparator.comparingDouble(b -> b.optDouble("rent_price", Double.MAX_VALUE)));
                        break;
                    case "Comfort":
                        Collections.sort(bikeList, (b1, b2) -> Double.compare(b2.optDouble("avg_comfort", 0.0), b1.optDouble("avg_comfort", 0.0)));
                        break;
                    case "Vehicle Condition":
                        Collections.sort(bikeList, (b1, b2) -> Double.compare(b2.optDouble("avg_vehicle_condition", 0.0), b1.optDouble("avg_vehicle_condition", 0.0)));
                        break;
                    case "Experience":
                        Collections.sort(bikeList, (b1, b2) -> Double.compare(b2.optDouble("avg_overall_experience", 0.0), b1.optDouble("avg_overall_experience", 0.0)));
                        break;
                }
            }

            // Step 2: Filter only active bikes
            List<JSONObject> activeBikes = new ArrayList<>();
            for (JSONObject bike : bikeList) {
                if (bike.optInt("is_active", 0) == 1) {
                    activeBikes.add(bike);
                }
            }

            // Step 3: Display sorted and filtered bikes
            for (JSONObject bike : activeBikes) {
                // Inflate bike_content.xml
                LayoutInflater inflater = LayoutInflater.from(this);
                View bikeView = inflater.inflate(R.layout.bike_content, reviewsContainer, false);

                // Set layout parameters for grid item
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                bikeView.setLayoutParams(params);

                // Set values to UI
                TextView bikeName = bikeView.findViewById(R.id.bike_name);
                ImageView bike_pic = bikeView.findViewById(R.id.bike_image);
                TextView rent = bikeView.findViewById(R.id.rent);
                TextView totalRating = bikeView.findViewById(R.id.total_rating);
                RatingBar ratingBar = bikeView.findViewById(R.id.MyRating);
                TextView count = bikeView.findViewById(R.id.count);
                String imageUrl = config.BASE_URL + bike.optString("bike_image");
                Glide.with(filtered_bikes.this)
                        .load(imageUrl)
                        .error(R.drawable.scooty)
                        .into(bike_pic);

                String name = bike.optString("bike_name", "Unknown Bike");
                int rentPrice = bike.optInt("rent_price", 0);
                double averageRating = bike.optDouble("average_rating", 0.0);
                int totalRatings = bike.optInt("total_ratings", 0);
                LinearLayout ratings = bikeView.findViewById(R.id.ratings);

                bikeName.setText(name);
                bikeName.setTextColor(Color.BLACK);
                bikeName.setTypeface(null, Typeface.BOLD);
                bikeView.setOnClickListener(v -> {
                    Intent intent = new Intent(filtered_bikes.this, BikeDetails.class);
                    intent.putExtra("bike_id", bike.optString("bike_id", ""));
                    intent.putExtra("bike_name", bike.optString("bike_name", ""));
                    intent.putExtra("customer_user_id", customer_user_id);
                    intent.putExtra("bike_model", bike.optString("model", ""));
                    intent.putExtra("bike_colour", bike.optString("colour", ""));
                    intent.putExtra("mileage", bike.optString("mileage", ""));
                    intent.putExtra("rent_price", bike.optString("rent_price", ""));
                    intent.putExtra("rating", bike.optString("total_ratings", ""));
                    intent.putExtra("mobile_number", MobileNumber);
                    intent.putExtra("shop_address", ShopAddress);
                    intent.putExtra("user_id", owner_id);
                    intent.putExtra("from_date", fromDate);
                    intent.putExtra("from_time", fromTime);
                    intent.putExtra("to_date", toDate);
                    intent.putExtra("to_time", toTime);

                    startActivity(intent);
                });
                rent.setText("₹" + rentPrice + "/hr");
                totalRating.setText(String.valueOf(averageRating));
                ratingBar.setRating((float) averageRating);
                count.setText("(" + totalRatings + ")");


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ColorStateList gold = ColorStateList.valueOf(Color.parseColor("#FFD700"));
                    ratingBar.setProgressTintList(gold);
                    ratingBar.setSecondaryProgressTintList(gold);
                    ratingBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
                }

                // Show rating popup with per-bike data
                ratings.setOnClickListener(v -> {
                    popupOverallRating = (float) averageRating;;
                    popupTotalRatings = totalRatings;
                    popupAvgCost = bike.optDouble("avg_cost", 0.0);;
                    popupAvgComfort = bike.optDouble("avg_comfort", 0.0);;
                    popupAvgCondition = bike.optDouble("avg_vehicle_condition", 0.0);;
                    popupAvgExperience = bike.optDouble("avg_overall_experience", 0.0);;
                    popupBikeId = bike.optString("bike_id", "");
                    showRatingPopup();
                });


                reviewsContainer.addView(bikeView);
            }


        } catch (JSONException e) {
            Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
            Log.e("JSON_ERROR", "Parsing error: " + e.getMessage());
        }
    }

    private void showRatingPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(filtered_bikes.this);
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_rating, null);

        RatingBar popupRatingBar = popupView.findViewById(R.id.MyRating);
        TextView totalText = popupView.findViewById(R.id.count);
        TextView totalRatingsText = popupView.findViewById(R.id.ratings);
        ImageView closeBtn = popupView.findViewById(R.id.btn_close);
        TextView show_ratings = popupView.findViewById(R.id.see_ratings);
        show_ratings.setOnClickListener(v -> {
            Intent intent = new Intent(filtered_bikes.this, CustomerRatings.class);
            intent.putExtra("customer_user_id", customer_user_id);
            intent.putExtra("bike_id", popupBikeId);
            startActivity(intent);
        });

        ProgressBar progressCost = popupView.findViewById(R.id.progress_cost);
        ProgressBar progressComfort = popupView.findViewById(R.id.progress_comfort);
        ProgressBar progressCondition = popupView.findViewById(R.id.progress_condition);
        ProgressBar progressExperience = popupView.findViewById(R.id.progress_experience);

        TextView percentCost = popupView.findViewById(R.id.percent_cost);
        TextView percentComfort = popupView.findViewById(R.id.percent_comfort);
        TextView percentCondition = popupView.findViewById(R.id.percent_condition);
        TextView percentExperience = popupView.findViewById(R.id.percent_experience);

        if (popupTotalRatings == 0) {
            // Show "No ratings yet"
            totalText.setText("No ratings yet");
            totalRatingsText.setVisibility(View.GONE);
            popupRatingBar.setVisibility(View.GONE);

            progressCost.setVisibility(View.GONE);
            progressComfort.setVisibility(View.GONE);
            progressCondition.setVisibility(View.GONE);
            progressExperience.setVisibility(View.GONE);

            percentCost.setVisibility(View.GONE);
            percentComfort.setVisibility(View.GONE);
            percentCondition.setVisibility(View.GONE);
            percentExperience.setVisibility(View.GONE);
        } else {
            // Show normal rating popup
            popupRatingBar.setVisibility(View.VISIBLE);
            totalRatingsText.setVisibility(View.VISIBLE);

            popupRatingBar.setRating(popupOverallRating);
            totalText.setText(popupOverallRating + " out of 5");
            totalRatingsText.setText(popupTotalRatings + " ratings");

            int costPercent = (int) (popupAvgCost * 20);
            int comfortPercent = (int) (popupAvgComfort * 20);
            int conditionPercent = (int) (popupAvgCondition * 20);
            int experiencePercent = (int) (popupAvgExperience * 20);

            progressCost.setProgress(costPercent);
            progressComfort.setProgress(comfortPercent);
            progressCondition.setProgress(conditionPercent);
            progressExperience.setProgress(experiencePercent);

            percentCost.setText(costPercent + "%");
            percentComfort.setText(comfortPercent + "%");
            percentCondition.setText(conditionPercent + "%");
            percentExperience.setText(experiencePercent + "%");

            progressCost.setVisibility(View.VISIBLE);
            progressComfort.setVisibility(View.VISIBLE);
            progressCondition.setVisibility(View.VISIBLE);
            progressExperience.setVisibility(View.VISIBLE);

            percentCost.setVisibility(View.VISIBLE);
            percentComfort.setVisibility(View.VISIBLE);
            percentCondition.setVisibility(View.VISIBLE);
            percentExperience.setVisibility(View.VISIBLE);
        }

        AlertDialog dialog = builder.setView(popupView).create();
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
