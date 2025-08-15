package com.example.myapplication1;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Objects;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CustomerRatings extends AppCompatActivity {

    LinearLayout reviewsContainer;
    ImageButton go_back;
    String bikeId;
    TextView No_re;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_reviews);

        reviewsContainer = findViewById(R.id.reviewsContainer);
        go_back = findViewById(R.id.back);
        No_re = findViewById(R.id.review_text);


        go_back.bringToFront();

        go_back.setOnClickListener(v -> {
            finish(); // Close the activity
        });

        bikeId = getIntent().getStringExtra("bike_id");

        if (bikeId != null) {
            fetchRatings(bikeId);
        } else {
            Toast.makeText(this, "Bike ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchRatings(String bikeId) {
        String url = config.BASE_URL+"user_ratings.php?bike_id=" + bikeId;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(CustomerRatings.this, "Failed to fetch ratings", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = Objects.requireNonNull(response.body()).string();

                try {
                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONArray dataArray = jsonObject.getJSONArray("reviews");

                    runOnUiThread(() -> {
                        Log.d("DEBUG", "Review count: " + dataArray.length());

                        reviewsContainer.removeAllViews();

                        if (dataArray.length() == 0) {
                            No_re.setVisibility(View.VISIBLE);
                            reviewsContainer.setVisibility(View.GONE);
                        } else {
                            No_re.setVisibility(View.GONE);
                            reviewsContainer.setVisibility(View.VISIBLE);
                        }

                        for (int i = 0; i < dataArray.length(); i++) {
                            try {
                                JSONObject review = dataArray.getJSONObject(i);
                                String name = review.optString("user_name", "Anonymous");
                                String reviewText = review.optString("review", "No review");
                                float rating = (float) review.optDouble("overall", 0.0);
                                addReviewToLayout(name, reviewText, rating);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(CustomerRatings.this, "Error parsing data", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void addReviewToLayout(String name, String reviewText, float rating) {
        View reviewView = LayoutInflater.from(this).inflate(R.layout.customer_reviews_content, null);

        TextView bikeName = reviewView.findViewById(R.id.bike_name);
        TextView content = reviewView.findViewById(R.id.content);
        TextView count = reviewView.findViewById(R.id.count);
        RatingBar ratingBar = reviewView.findViewById(R.id.MyRating);

        bikeName.setText(name);
        content.setText(reviewText);
        count.setText(String.format("Rated %.1f out of 5", rating));
        ratingBar.setRating(rating);

        reviewsContainer.addView(reviewView);
    }
}