package com.example.myapplication1;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExploreResults extends AppCompatActivity {

    private LinearLayout parentLayout;
    private static final String API_URL = config.BASE_URL+"explore_results.php";
    private static final String _URL = config.BASE_URL + "fetch_shop_timings.php";
    private JSONObject fullApiResponse; // Declare globally
// Replace with your IP


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explore_results);

        parentLayout = findViewById(R.id.content);
        findViewById(R.id.back).setOnClickListener(v -> finish());

        String zipCode = getIntent().getStringExtra("zip_code");
        String customer_user_id = getIntent().getStringExtra("user_id");
        String fromDate = getIntent().getStringExtra("from_date");
        String fromTime = getIntent().getStringExtra("from_time");
        String toDate = getIntent().getStringExtra("to_date");
        String toTime = getIntent().getStringExtra("to_time");



        if (zipCode != null && !zipCode.trim().isEmpty()) {
            fetchData(zipCode, fromDate, fromTime, toDate, toTime);
            fetchData1(zipCode, fromDate, fromTime, toDate, toTime);
        } else {
            showErrorMessage("Invalid ZIP Code");
        }
        setupFilterIcon();
    }

    private JSONArray allBikes = new JSONArray();  // Move to class level

 // Store API response globally

    private void fetchData1(String zipCode, String fromDate, String fromTime, String toDate, String toTime) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String formattedFromDate = formatDate(fromDate);
        String formattedFromTime = formatTime(fromTime);
        String formattedToDate = formatDate(toDate);
        String formattedToTime = formatTime(toTime);

        StringBuilder urlBuilder = new StringBuilder(API_URL + "?zip_code=" + zipCode);
        if (formattedFromDate != null && formattedFromTime != null &&
                formattedToDate != null && formattedToTime != null) {
            urlBuilder.append("&from_date=").append(formattedFromDate)
                    .append("&from_time=").append(formattedFromTime)
                    .append("&to_date=").append(formattedToDate)
                    .append("&to_time=").append(formattedToTime);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlBuilder.toString(), null,
                response -> {
                    Log.d("API_RESPONSE", "Response: " + response.toString());

                    // ✅ Store full response globally
                    fullApiResponse = response;

                    setupFilterIcon();
                },
                error -> {
                    Log.e("API_ERROR", "Failed to connect to server: " + error.getMessage());
                });

        queue.add(request);
    }

    private void setupFilterIcon() {
        LinearLayout filterIcon = findViewById(R.id.recom_bot);
        filterIcon.setOnClickListener(v -> {
            Log.d("DEBUG", "Filter Image Clicked!");

            Intent intent = new Intent(ExploreResults.this, bot.class);

            if (fullApiResponse != null) {
                intent.putExtra("full_response", fullApiResponse.toString());
                intent.putExtra("from_date", getIntent().getStringExtra("from_date"));
                intent.putExtra("from_time", getIntent().getStringExtra("from_time"));
                intent.putExtra("to_date", getIntent().getStringExtra("to_date"));
                intent.putExtra("to_time", getIntent().getStringExtra("to_time"));
                intent.putExtra("customer_user_id", getIntent().getStringExtra("user_id"));

                try {
                    JSONArray dataArray = fullApiResponse.optJSONArray("data");
                    if (dataArray != null && dataArray.length() > 0) {
                        JSONObject shopObj = dataArray.getJSONObject(0);
                        intent.putExtra("user_id", shopObj.optString("user_id", "N/A"));
                        intent.putExtra("shop_address", shopObj.optString("shop_address", "N/A"));
                        intent.putExtra("mobile_number", shopObj.optString("mobile_number", "N/A"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("DEBUG", "Navigating with full response: " + fullApiResponse.toString());
            } else {
                Log.e("DEBUG", "Full API Response is NULL!");
            }

            startActivity(intent);
        });
    }



    private void fetchData(String zipCode, String fromDate, String fromTime, String toDate, String toTime) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String formattedFromDate = formatDate(fromDate);
        String formattedFromTime = formatTime(fromTime);
        String formattedToDate = formatDate(toDate);
        String formattedToTime = formatTime(toTime);

        StringBuilder urlBuilder = new StringBuilder(API_URL + "?zip_code=" + zipCode);
        if (formattedFromDate != null && formattedFromTime != null &&
                formattedToDate != null && formattedToTime != null) {
            urlBuilder.append("&from_date=").append(formattedFromDate)
                    .append("&from_time=").append(formattedFromTime)
                    .append("&to_date=").append(formattedToDate)
                    .append("&to_time=").append(formattedToTime);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlBuilder.toString(), null,
                response -> {
                    try {
                        String status = response.optString("status", "error");
                        if ("success".equalsIgnoreCase(status)) {
                            JSONArray dataArray = response.optJSONArray("data");
                            if (dataArray == null || dataArray.length() == 0) {
                                showErrorMessage("No results found");
                            } else {
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject shopObj = dataArray.getJSONObject(i);
                                    String shopName = shopObj.optString("shop_name", "N/A");
                                    String userId = shopObj.optString("user_id", "N/A");
                                    String shopAddress = shopObj.optString("shop_address", "N/A");
                                    String mobileNumber = shopObj.optString("mobile_number", "N/A");
                                    String isActive = shopObj.optString("is_active", "0"); // ✅ NEW

                                    JSONArray bikes = shopObj.optJSONArray("bikes");

                                    int totalRatings = 0;
                                    double totalRatingSum = 0.0;
                                    double totalAvgCost = 0.0;
                                    double totalAvgComfort = 0.0;
                                    double totalAvgVehicleCondition = 0.0;
                                    double totalAvgOverallExperience = 0.0;
                                    int countRatedBikes = 0;

                                    if (bikes != null && bikes.length() > 0) {
                                        for (int j = 0; j < bikes.length(); j++) {
                                            JSONObject bike = bikes.getJSONObject(j);
                                            int bikeRatings = bike.optInt("total_ratings", 0);
                                            double bikeRating = bike.optDouble("average_rating", 0.0);

                                            totalRatings += bikeRatings;
                                            totalRatingSum += bikeRatings * bikeRating;

                                            double avgCost = bike.optDouble("avg_cost", 0.0);
                                            double avgComfort = bike.optDouble("avg_comfort", 0.0);
                                            double avgVehicleCondition = bike.optDouble("avg_vehicle_condition", 0.0);
                                            double avgOverallExperience = bike.optDouble("avg_overall_experience", 0.0);

                                            if (bikeRatings > 0) {
                                                totalAvgCost += avgCost;
                                                totalAvgComfort += avgComfort;
                                                totalAvgVehicleCondition += avgVehicleCondition;
                                                totalAvgOverallExperience += avgOverallExperience;
                                                countRatedBikes++;
                                            }
                                        }
                                    }

                                    double averageRating = (totalRatings != 0) ? (totalRatingSum / totalRatings) : 0.0;
                                    double avgCost = (countRatedBikes != 0) ? (totalAvgCost / countRatedBikes) : 0.0;
                                    double avgComfort = (countRatedBikes != 0) ? (totalAvgComfort / countRatedBikes) : 0.0;
                                    double avgVehicleCondition = (countRatedBikes != 0) ? (totalAvgVehicleCondition / countRatedBikes) : 0.0;
                                    double avgOverallExperience = (countRatedBikes != 0) ? (totalAvgOverallExperience / countRatedBikes) : 0.0;

                                    addShopEntry(shopName, userId, shopAddress, mobileNumber,
                                            totalRatings, averageRating, avgCost, avgComfort,
                                            avgVehicleCondition, avgOverallExperience, bikes, isActive);
                                }
                            }
                        } else {
                            showErrorMessage("No data available");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showErrorMessage("Error parsing server data");
                    }
                },
                error -> {
                    error.printStackTrace();
                    showErrorMessage("Failed to connect to server");
                });

        queue.add(request);
    }

    private String formatDate(String inputDate) {
        try {
            if (inputDate == null || inputDate.isEmpty()) return null;
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return inputDate;
        }
    }

    private String formatTime(String inputTime) {
        try {
            if (inputTime == null || inputTime.isEmpty()) return null;
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date time = inputFormat.parse(inputTime);
            return outputFormat.format(time);
        } catch (Exception e) {
            e.printStackTrace();
            return inputTime;
        }
    }

    private void addShopEntry(String shopName, String userId, String shopAddress, String mobileNumber,
                              int totalRatings, double overallRating, double finalAvgCost,
                              double finalAvgComfort, double finalAvgVehicleCondition,
                              double finalAvgOverallExperience, JSONArray bikeArray, String isActive) {

        LayoutInflater inflater = LayoutInflater.from(this);
        View shopView = inflater.inflate(R.layout.explore_results_contnet, parentLayout, false);

        EditText shopNameField = shopView.findViewById(R.id.shop_name);
        EditText userStatusField = shopView.findViewById(R.id.Status);
        ImageView status_bar = shopView.findViewById(R.id.shop_stat);

// set shop name
        shopNameField.setText(shopName);

// fetch status and show timing on image click
        userStatusField.setText("Loading...");

        fetchShopStatus(userId, fullResponse -> {
            if (fullResponse == null) {
                userStatusField.setText("Error");
                userStatusField.setTextColor(Color.GRAY);
                return;
            }

            try {
                // ✅ Display current open/close status
                String status = fullResponse.optString("current_status", "Unknown");
                userStatusField.setText(status);
                if (status.equalsIgnoreCase("Open")) {
                    userStatusField.setTextColor(Color.parseColor("#4CAF50")); // Green
                } else {
                    userStatusField.setTextColor(Color.parseColor("#F44336")); // Red
                }

                // ✅ Set up click listener for showing weekly timings popup
                status_bar.setOnClickListener(v -> {
                    try {
                        JSONObject shopTimings = fullResponse.getJSONObject("shop_timings");
                        LayoutInflater inflater1 = LayoutInflater.from(ExploreResults.this);
                        View dialogView = inflater1.inflate(R.layout.show, null);
                        TableLayout tableLayout = dialogView.findViewById(R.id.timings_table);

                        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

                        for (String day : days) {
                            JSONObject dayObj = shopTimings.getJSONObject(day);
                            boolean isClosed = dayObj.getBoolean("is_closed");

                            TableRow row = new TableRow(ExploreResults.this);
                            TextView dayText = new TextView(ExploreResults.this);
                            TextView timeText = new TextView(ExploreResults.this);

                            dayText.setText(day);
                            dayText.setPadding(10, 10, 10, 10);
                            timeText.setPadding(10, 10, 10, 10);

                            if (isClosed) {
                                timeText.setText("Closed");
                            } else {
                                String from = formatTime12Hour(dayObj.getString("open_time"));
                                String to = formatTime12Hour(dayObj.getString("close_time"));
                                timeText.setText(from + " - " + to);
                            }

                            row.addView(dayText);
                            row.addView(timeText);
                            tableLayout.addView(row);
                        }

                        new AlertDialog.Builder(ExploreResults.this)
                                .setTitle("Weekly Shop Timings")
                                .setView(dialogView)
                                .setPositiveButton("OK", null)
                                .show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ExploreResults.this, "Failed to load timings", Toast.LENGTH_SHORT).show();
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
                userStatusField.setText("Error");
                userStatusField.setTextColor(Color.GRAY);
            }
        });



        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 20);
        shopView.setLayoutParams(params);

        shopView.setOnClickListener(v -> {
            Intent intent = new Intent(ExploreResults.this, DisplayBikesActivity.class);
            intent.putExtra("shop_name", shopName);
            intent.putExtra("user_id", userId);
            intent.putExtra("shop_address", shopAddress);
            intent.putExtra("mobile_number", mobileNumber);
            intent.putExtra("bike_data", bikeArray != null ? bikeArray.toString() : "[]");
            intent.putExtra("from_date", getIntent().getStringExtra("from_date"));
            intent.putExtra("from_time", getIntent().getStringExtra("from_time"));
            intent.putExtra("to_date", getIntent().getStringExtra("to_date"));
            intent.putExtra("to_time", getIntent().getStringExtra("to_time"));
            intent.putExtra("totalRatings", totalRatings);
            intent.putExtra("overall_rating", overallRating);
            intent.putExtra("avg_cost", finalAvgCost);
            intent.putExtra("avg_comfort", finalAvgComfort);
            intent.putExtra("avg_condition", finalAvgVehicleCondition);
            intent.putExtra("avg_experience", finalAvgOverallExperience);
            intent.putExtra("customer_user_id", getIntent().getStringExtra("user_id"));
            intent.putExtra("is_active", isActive);
            startActivity(intent);
        });

        parentLayout.addView(shopView);
    }
    private void fetchShopStatus(String userId, final StatusCallback callback) {
        String url = _URL + "?user_id=" + userId;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        callback.onStatusReceived(response); // 🔁 pass full JSON
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onStatusReceived(null); // ❗handle null case
                    }
                },
                error -> {
                    error.printStackTrace();
                    callback.onStatusReceived(null);
                });

        queue.add(request);
    }

    interface StatusCallback {
        void onStatusReceived(JSONObject fullResponse);
    }
    private String formatTime12Hour(String time24) {
        try {
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = sdf24.parse(time24);
            return sdf12.format(date);
        } catch (Exception e) {
            return time24; // fallback
        }
    }



    private void showErrorMessage(String message) {
        TextView errorTextView = new TextView(this);
        errorTextView.setText(message);
        errorTextView.setTextSize(16);
        errorTextView.setPadding(20, 20, 20, 20);
        parentLayout.addView(errorTextView);
    }
}
