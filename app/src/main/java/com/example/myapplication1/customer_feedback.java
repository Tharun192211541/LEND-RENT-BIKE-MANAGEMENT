package com.example.myapplication1;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class customer_feedback extends AppCompatActivity {

    RatingBar ratingCost, ratingComfort, ratingCondition, ratingOverallExperience;
    EditText feedbackEditText;
    ImageView bike_pic;
    Button submitButton;
    String URL = config.BASE_URL+"vehicle_rating.php"; // Update IP if needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_rating);

        ratingCost = findViewById(R.id.rating_cost);
        bike_pic = findViewById(R.id.bike_image);
        ratingComfort = findViewById(R.id.rating_comfort);
        ratingCondition = findViewById(R.id.rating_condition);
        ratingOverallExperience = findViewById(R.id.rating_overall);
        feedbackEditText = findViewById(R.id.feed);
        submitButton = findViewById(R.id.logout);
        String imageUrl = config.BASE_URL + getIntent().getStringExtra("bike_image");
        Glide.with(customer_feedback.this)
                .load(imageUrl)
                .error(R.drawable.scooty)
                .into(bike_pic);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float costRating = ratingCost.getRating();
                float comfortRating = ratingComfort.getRating();
                float conditionRating = ratingCondition.getRating();
                float overallExperience = ratingOverallExperience.getRating();

                float overallRating = (costRating + comfortRating + conditionRating + overallExperience) / 4.0f;
                String feedback = feedbackEditText.getText().toString().trim();

                String rentId = getIntent().getStringExtra("rent_id");
                String userId = getIntent().getStringExtra("user_id");

                // Basic validation
                if (feedback.isEmpty()) {
                    Toast.makeText(customer_feedback.this, "Please enter your feedback.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (rentId == null || userId == null) {
                    Toast.makeText(customer_feedback.this, "Missing rent ID or user ID.", Toast.LENGTH_SHORT).show();
                    return;
                }

                sendFeedbackToServer(costRating, comfortRating, conditionRating, overallExperience, overallRating, feedback, userId, rentId);
            }
        });
    }

    private void sendFeedbackToServer(float cost, float comfort, float condition, float overallExperience, float overall, String feedback, String userId, String rentId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ServerResponse", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            if (status.equalsIgnoreCase("success")) {
                                Toast.makeText(customer_feedback.this, message, Toast.LENGTH_LONG).show();
                                ratingCost.setRating(0);
                                ratingComfort.setRating(0);
                                ratingCondition.setRating(0);
                                ratingOverallExperience.setRating(0);
                                feedbackEditText.setText("");
                            } else {
                                    showErrorDialog(message);
                                }

                        } catch (JSONException e) {
                            Toast.makeText(customer_feedback.this, "Invalid response from server.", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(customer_feedback.this, "Network timeout. Please check your connection.", Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            Toast.makeText(customer_feedback.this, "Authentication error.", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(customer_feedback.this, "Server error. Try again later.", Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(customer_feedback.this, "Network error. Please try again.", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(customer_feedback.this, "Error parsing response.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(customer_feedback.this, "Unexpected error occurred.", Toast.LENGTH_LONG).show();
                        }
                        Log.e("VolleyError", error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("cost", String.valueOf(cost));
                params.put("comfort", String.valueOf(comfort));
                params.put("vehicle_condtion", String.valueOf(condition)); // Match server spelling
                params.put("overall_experience", String.valueOf(overallExperience));
                params.put("overall", String.valueOf(overall));
                params.put("review", feedback);
                params.put("user_id", userId);
                params.put("rent_id", rentId);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Submission Error")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", null)
                .show();
    }
}
