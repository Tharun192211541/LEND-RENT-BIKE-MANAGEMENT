package com.example.myapplication1;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class DisplayBikesActivity extends AppCompatActivity {

    private GridLayout bikeGrid;
    private ImageView Back;
    private JSONArray bikeData;

    private Button btnScooty, btnGearBike, btnBicycle;
    private TextView storeNameText;

    private String fromDate, fromTime, toDate, toTime, ShopAddress, MobileNumber, customer_user_id, owner_id,bikeId;
    int is_active;

    // Variables to pass to rating popup
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
        setContentView(R.layout.display_bikes);

        bikeGrid = findViewById(R.id.content);
        btnScooty = findViewById(R.id.btnscooty);
        btnGearBike = findViewById(R.id.btngearbike);
        btnBicycle = findViewById(R.id.btnbicycle);
        storeNameText = findViewById(R.id.storeNameText);
        Back = findViewById(R.id.back);

        // Get store name
        String storeName = getIntent().getStringExtra("shop_name");
        if (storeName != null && !storeName.isEmpty()) {
            storeNameText.setText(storeName);
        }

        // Get additional details from intent
        fromDate = getIntent().getStringExtra("from_date");
        fromTime = getIntent().getStringExtra("from_time");
        toDate = getIntent().getStringExtra("to_date");
        toTime = getIntent().getStringExtra("to_time");
        ShopAddress = getIntent().getStringExtra("shop_address");
        MobileNumber = getIntent().getStringExtra("mobile_number");
        customer_user_id = getIntent().getStringExtra("customer_user_id");
        owner_id = getIntent().getStringExtra("user_id");


        // Set default button text color
        btnScooty.setTextColor(Color.WHITE);
        btnGearBike.setTextColor(Color.WHITE);
        btnBicycle.setTextColor(Color.WHITE);

        // Get bike data from intent
        String bikeDataStr = getIntent().getStringExtra("bike_data");
        try {
            bikeData = new JSONArray(bikeDataStr);
        } catch (JSONException e) {
            e.printStackTrace();
            showEmptyMessage("Error loading bike data");
            return;
        }

        // Display all bikes initially
        displayBikesByCategory("All");

        // Filter buttons
        btnScooty.setOnClickListener(view -> {
            highlightSelectedButtonText(btnScooty);
            displayBikesByCategory("Scooty");
        });

        btnGearBike.setOnClickListener(view -> {
            highlightSelectedButtonText(btnGearBike);
            displayBikesByCategory("Gear Bike");
        });

        btnBicycle.setOnClickListener(view -> {
            highlightSelectedButtonText(btnBicycle);
            displayBikesByCategory("Bicycle");
        });

        // Back button
        Back.setOnClickListener(v -> finish());
    }

    private void displayBikesByCategory(String category) {
        bikeGrid.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        boolean bikeFound = false;

        for (int i = 0; i < bikeData.length(); i++) {
            try {
                JSONObject bike = bikeData.getJSONObject(i);
                String bikeCategory = bike.optString("Category", "");

                if (category.equals("All") || bikeCategory.equalsIgnoreCase(category)) {
                    String bikeName = bike.optString("bike_name", "N/A");
                    String rent = bike.optString("rent_price", "N/A");
                    int number_of_ratings = bike.optInt("total_ratings", 0);
                    double overallRating = bike.optDouble("average_rating", 0.0);
                    double avgCost = bike.optDouble("avg_cost", 0.0);
                    double avgComfort = bike.optDouble("avg_comfort", 0.0);
                    double avgCondition = bike.optDouble("avg_vehicle_condition", 0.0);
                    double avgExperience = bike.optDouble("avg_overall_experience", 0.0);
                    int is_active = bike.optInt("is_active",0);

                    View bikeView = inflater.inflate(R.layout.bike_content, bikeGrid, false);
                    EditText bikeNameView = bikeView.findViewById(R.id.bike_name);
                    ImageView bike_pic = bikeView.findViewById(R.id.bike_image);
                    EditText rentView = bikeView.findViewById(R.id.rent);
                    TextView rating = bikeView.findViewById(R.id.count);
                    TextView total1 = bikeView.findViewById(R.id.total_rating);
                    RatingBar Myrating = bikeView.findViewById(R.id.MyRating);
                    LinearLayout ratings = bikeView.findViewById(R.id.ratings);
                    String imageUrl = config.BASE_URL + bike.optString("bike_image");
                    Glide.with(DisplayBikesActivity.this)
                            .load(imageUrl)
                            .error(R.drawable.scooty)
                            .into(bike_pic);

                    // Set values
                    bikeNameView.setText(bikeName);
                    if (is_active == 1) {
                        bikeNameView.setTextColor(Color.BLACK);
                        bikeNameView.setTypeface(null, Typeface.BOLD);
                        bikeView.setOnClickListener(v -> {
                            Intent intent = new Intent(DisplayBikesActivity.this, BikeDetails.class);
                            intent.putExtra("bike_id", bike.optString("bike_id", ""));
                            intent.putExtra("bike_name", bike.optString("bike_name", ""));
                            intent.putExtra("bike_model", bike.optString("model", ""));
                            intent.putExtra("bike_colour", bike.optString("colour", ""));
                            intent.putExtra("mileage", bike.optString("milage", ""));
                            intent.putExtra("rent_price", bike.optString("rent_price", ""));
                            intent.putExtra("rating", bike.optString("total_ratings", ""));
                            intent.putExtra("bike_image",bike.optString("bike_image"));

                            intent.putExtra("mobile_number", MobileNumber);
                            intent.putExtra("shop_address", ShopAddress);
                            intent.putExtra("user_id", owner_id);
                            intent.putExtra("customer_user_id", customer_user_id);
                            intent.putExtra("from_date", fromDate);
                            intent.putExtra("from_time", fromTime);
                            intent.putExtra("to_date", toDate);
                            intent.putExtra("to_time", toTime);

                            startActivity(intent);
                        });
                    } else {
                        bikeNameView.setTextColor(Color.RED);
                        bikeNameView.setTypeface(null, Typeface.NORMAL);
                        bikeView.setOnClickListener(v -> {
                            Toast.makeText(getApplicationContext(), "This bike is already booked in your selected time range", Toast.LENGTH_SHORT).show();

                        });
                    }
                    rentView.setText(rent + " ₹/hr");
                    rating.setText("(" + number_of_ratings + ")");
                    Myrating.setRating((float) overallRating);
                    total1.setText(String.valueOf(overallRating));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ColorStateList gold = ColorStateList.valueOf(Color.parseColor("#FFD700"));
                        Myrating.setProgressTintList(gold);
                        Myrating.setSecondaryProgressTintList(gold);
                        Myrating.setProgressBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
                    }

                    // Show rating popup with per-bike data
                    ratings.setOnClickListener(v -> {
                        popupOverallRating = (float) overallRating;
                        popupTotalRatings = number_of_ratings;
                        popupAvgCost = avgCost;
                        popupAvgComfort = avgComfort;
                        popupAvgCondition = avgCondition;
                        popupAvgExperience = avgExperience;
                        popupBikeId = bike.optString("bike_id", "");
                        showRatingPopup();
                    });
                    // Navigate to details
                    bikeGrid.addView(bikeView);
                    bikeFound = true;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (!bikeFound) {
            showEmptyMessage("No bikes found for this category.");
        }
    }

    private void showRatingPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayBikesActivity.this);
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_rating, null);

        RatingBar popupRatingBar = popupView.findViewById(R.id.MyRating);
        TextView totalText = popupView.findViewById(R.id.count);
        TextView totalRatingsText = popupView.findViewById(R.id.ratings);
        ImageView closeBtn = popupView.findViewById(R.id.btn_close);
        TextView show_ratings = popupView.findViewById(R.id.see_ratings);
        show_ratings.setOnClickListener(v -> {
            Intent intent = new Intent(DisplayBikesActivity.this, CustomerRatings.class);
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

    private void showEmptyMessage(String message) {
        TextView emptyView = new TextView(this);
        emptyView.setText(message);
        emptyView.setTextSize(16);
        emptyView.setPadding(20, 20, 20, 20);
        bikeGrid.addView(emptyView);
    }

    private void highlightSelectedButtonText(Button selectedButton) {
        btnScooty.setTextColor(Color.WHITE);
        btnGearBike.setTextColor(Color.WHITE);
        btnBicycle.setTextColor(Color.WHITE);
        selectedButton.setTextColor(Color.RED);
    }
}
