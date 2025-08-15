package com.example.myapplication1; // Replace with your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.myapplication1.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Cart_Fragment extends Fragment {

    private LinearLayout currentBookingsGrid, previousBookingsGrid;
    private ViewGroup emptyCartContainer;
    private View bookingsContentContainer;
    private String customer_user_id;
    private final String BASE_URL = config.BASE_URL+"customer_bookings.php";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cart, container, false);

        currentBookingsGrid = rootView.findViewById(R.id.current_bookings_grid);
        previousBookingsGrid = rootView.findViewById(R.id.previous_bookings_grid);
        emptyCartContainer = rootView.findViewById(R.id.empty_cart_container);
        bookingsContentContainer = rootView.findViewById(R.id.bookings_content_container);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("user_id")) {
            customer_user_id = intent.getStringExtra("user_id");
        }

        fetchBookingDataFromServer();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchBookingDataFromServer();
    }

    private void fetchBookingDataFromServer() {
        String finalUrl = BASE_URL + "?customer_user_id=" + customer_user_id;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, finalUrl, null,
                response -> {
                    try {
                        JSONArray currentBookingsArray = response.getJSONArray("current_bookings");
                        JSONArray previousBookingsArray = response.getJSONArray("previous_bookings");

                        displayBookings(currentBookingsArray, currentBookingsGrid, R.layout.customer_cart_content);
                        displayBookings(previousBookingsArray, previousBookingsGrid, R.layout.customer_cart_content);

                        if ((currentBookingsArray == null || currentBookingsArray.length() == 0) &&
                                (previousBookingsArray == null || previousBookingsArray.length() == 0)) {
                            showEmptyCartLayout();
                        } else {
                            showBookingsLayout();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Server Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private void displayBookings(JSONArray bookingsArray, LinearLayout layout, int layoutId) {
        layout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (bookingsArray == null || bookingsArray.length() == 0) return;

        for (int i = 0; i < bookingsArray.length(); i++) {
            try {
                JSONObject booking = bookingsArray.getJSONObject(i);
                View bookingView = inflater.inflate(layoutId, layout, false);

                TextView bikeNameView = bookingView.findViewById(R.id.bike_name);
                ImageView bike_pic = bookingView.findViewById(R.id.bike_image);
                TextView rentView = bookingView.findViewById(R.id.rent);
                String imageUrl = config.BASE_URL + booking.optString("bike_image");
                Glide.with(Cart_Fragment.this)
                        .load(imageUrl)
                        .error(R.drawable.scooty)
                        .into(bike_pic);

                String bikeName = booking.optString("bike_name", "N/A");
                String rent_id = booking.optString("rent_id", "N/A");
                String totalRent = booking.optString("total_rent", "N/A");
                String bikeId = booking.optString("bike_id", "N/A");
                String mileage = booking.optString("bike_mileage", "N/A");
                String color = booking.optString("bike_colour", "N/A");
                String bike_model = booking.optString("bike_model", "N/A");
                String owner_name = booking.optString("owner_name", "N/A");
                String owner_mobile = booking.optString("owner_mobile", "N/A");
                String store_name = booking.optString("store_name", "N/A");
                String store_address = booking.optString("store_address", "N/A");

                bikeNameView.setText(bikeName);
                rentView.setText("Total Rent: " + totalRent + " ₹");

                bookingView.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), Customer_cart_previous.class);
                    intent.putExtra("bike_name", bikeName);
                    intent.putExtra("bike_id", bikeId);
                    intent.putExtra("total_rent", totalRent);
                    intent.putExtra("bike_image", booking.optString("bike_image"));
                    intent.putExtra("mileage", mileage);
                    intent.putExtra("color", color);
                    intent.putExtra("bike_model", bike_model);
                    intent.putExtra("rent_id", rent_id);
                    intent.putExtra("customer_user_id", customer_user_id);
                    intent.putExtra("owner_name", owner_name);
                    intent.putExtra("owner_mobile", owner_mobile);
                    intent.putExtra("store_name", store_name);
                    intent.putExtra("store_address", store_address);
                    intent.putExtra("booking_type", layout == currentBookingsGrid ? "current" : "previous");
                    startActivity(intent);
                });

                layout.addView(bookingView);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showEmptyCartLayout() {
        bookingsContentContainer.setVisibility(View.GONE);
        emptyCartContainer.setVisibility(View.VISIBLE);
        emptyCartContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View emptyView = inflater.inflate(R.layout.empty_cart, emptyCartContainer, false);
        emptyCartContainer.addView(emptyView);
    }

    private void showBookingsLayout() {
        emptyCartContainer.setVisibility(View.GONE);
        bookingsContentContainer.setVisibility(View.VISIBLE);
    }
}
