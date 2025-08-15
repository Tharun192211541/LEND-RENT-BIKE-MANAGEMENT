package com.example.myapplication1;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class delete_bikes extends AppCompatActivity {

    LinearLayout bikesContainer;
    ImageView go_back;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remove_vehicles);

        bikesContainer = findViewById(R.id.reviewsContainer);
        go_back = findViewById(R.id.back);

        go_back.setOnClickListener(v -> finish());

        userId = getIntent().getStringExtra("user_id");

        if (userId != null) {
            fetchBikes(userId);
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchBikes(String userId) {
        String url = config.BASE_URL+"delete_bikes.php";

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", userId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(delete_bikes.this, "Failed to fetch bikes", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = Objects.requireNonNull(response.body()).string().trim();

                runOnUiThread(() -> {
                    try {
                        if (responseBody.isEmpty()) {
                            Toast.makeText(delete_bikes.this, "Empty response from server", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONArray dataArray = jsonObject.optJSONArray("data");

                        if (dataArray == null || dataArray.length() == 0) {
                            Toast.makeText(delete_bikes.this, "No bikes available", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        bikesContainer.removeAllViews();

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject bike = dataArray.getJSONObject(i);
                            String bikeName = bike.optString("Bike_Name", "Unknown");
                            String imageUrl=  bike.optString("bike_image_url", "Unknown");
                            String regNumber = bike.optString("Bike_Reg_number", "Unknown");
                            int number_of_ratings = bike.optInt("total_ratings", 0);
                            double overallRating = bike.optDouble("average_rating", 0.0);
                            String rent = bike.optString("rent_price", "0");
                            boolean is_active = bike.optBoolean("is_active", false);

                            addBikeToLayout(bikeName,imageUrl, regNumber, number_of_ratings, overallRating, rent, is_active);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(delete_bikes.this, "JSON Parsing Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void addBikeToLayout(String bikeNameStr,String imagepath, String regNumberStr, int number_of_ratings,
                                 double overallRating, String rentStr, boolean is_active) {
        View bikeView = LayoutInflater.from(this).inflate(R.layout.remove_vehicle_content, null);

        EditText bikeName = bikeView.findViewById(R.id.bike_name);
        TextView total_rat = bikeView.findViewById(R.id.total_rating);
        ImageView bike_pic = bikeView.findViewById(R.id.bike_image);
        RatingBar stars = bikeView.findViewById(R.id.MyRating);
        TextView numbers = bikeView.findViewById(R.id.count);
        EditText rent = bikeView.findViewById(R.id.rent);
        Button deleteButton = bikeView.findViewById(R.id.delete_button);
        String imageUrl = config.BASE_URL + imagepath;
        Glide.with(delete_bikes.this)
                .load(imageUrl)
                .error(R.drawable.scooty)
                .into(bike_pic);

        bikeName.setText(bikeNameStr);
        rent.setText(rentStr);
        total_rat.setText(String.valueOf(overallRating));
        stars.setRating((float) overallRating);
        numbers.setText(String.valueOf(number_of_ratings));

        if (is_active) {
            bikeName.setTextColor(getResources().getColor(android.R.color.black));
            bikeName.setTypeface(null, android.graphics.Typeface.BOLD);
            deleteButton.setOnClickListener(v -> deleteBike(regNumberStr, bikeView));
        } else {
            bikeName.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            deleteButton.setBackgroundColor(getResources().getColor(R.color.disabled_red));
            deleteButton.setOnClickListener(v ->
                    Toast.makeText(this, "This bike is already booked by a customer for the selected time slot.", Toast.LENGTH_LONG).show()
            );
        }

        bikesContainer.addView(bikeView);
    }

    private void deleteBike(String regNumber, View bikeView) {
        if (userId == null) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog before deleting
        new AlertDialog.Builder(this)
                .setTitle("Delete Bike")
                .setMessage("Are you sure you want to delete this bike?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Proceed with deletion
                    performBikeDeletion(regNumber, bikeView);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performBikeDeletion(String regNumber, View bikeView) {
        String url = config.BASE_URL+"delete_admin_bike_content.php";

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("bike_id", regNumber)
                .add("user_id", userId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(delete_bikes.this, "Failed to delete bike", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = Objects.requireNonNull(response.body()).string().trim();
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(res);
                        if (jsonResponse.optString("status").equals("success")) {
                            Toast.makeText(delete_bikes.this, "Bike deleted successfully", Toast.LENGTH_SHORT).show();
                            bikesContainer.removeView(bikeView);
                        } else {
                            Toast.makeText(delete_bikes.this, jsonResponse.optString("message", "Failed to delete bike"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(delete_bikes.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}