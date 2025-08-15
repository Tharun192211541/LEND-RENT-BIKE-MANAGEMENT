package com.example.myapplication1;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CLenderFeedback extends AppCompatActivity {

    RatingBar owner_rating;
    EditText feedbackEditText;
    Button submitButton;
    String URL = config.BASE_URL+"customer_lender_feedback.php"; // Update IP if needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lender_feedback);

        owner_rating = findViewById(R.id.rating);
        feedbackEditText = findViewById(R.id.feed);
        submitButton = findViewById(R.id.logout); // Rename ID properly in XML if needed

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float ownerRating = owner_rating.getRating();

                String feedback = feedbackEditText.getText().toString().trim();

                String rentId = getIntent().getStringExtra("rent_id");
                String userId = getIntent().getStringExtra("user_id");
                String owner_name =  getIntent().getStringExtra("owner_name");

                // Basic validation
                if (feedback.isEmpty()) {
                    Toast.makeText(CLenderFeedback.this, "Please enter your feedback.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (rentId == null || userId == null) {
                    Toast.makeText(CLenderFeedback.this, "Missing rent ID or user ID.", Toast.LENGTH_SHORT).show();
                    return;
                }

                sendFeedbackToServer(ownerRating, feedback, userId, rentId);
            }
        });
    }

    private void sendFeedbackToServer(float ownerRating ,String feedback, String userId, String rentId) {
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
                                Toast.makeText(CLenderFeedback.this, message, Toast.LENGTH_LONG).show();
                                owner_rating.setRating(0);
                                feedbackEditText.setText("");
                            } else {
                                showErrorDialog(message);
                            }

                        } catch (JSONException e) {
                            Toast.makeText(CLenderFeedback.this, "Invalid response from server.", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(CLenderFeedback.this, "Network timeout. Please check your connection.", Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            Toast.makeText(CLenderFeedback.this, "Authentication error.", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(CLenderFeedback.this, "Server error. Try again later.", Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(CLenderFeedback.this, "Network error. Please try again.", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(CLenderFeedback.this, "Error parsing response.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(CLenderFeedback.this, "Unexpected error occurred.", Toast.LENGTH_LONG).show();
                        }
                        Log.e("VolleyError", error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("rating", String.valueOf(ownerRating));
                params.put("review", feedback);
                params.put("customer_id", userId);
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
