package com.example.myapplication1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentAdminStore extends Fragment {
    private String user_id;
    Button updatetime;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_store, container, true);

        // Fixed parameter
        if (getArguments() != null) {
            user_id = getArguments().getString("user_id", "Unknown");
        }
        updatetime = view.findViewById(R.id.upda);
        updatetime.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), shop_status.class);
            intent.putExtra("user_id", user_id);
            startActivity(intent);
        });

        ImageView addBikeButton = view.findViewById(R.id.add_vehicle);
        addBikeButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddBike.class);
            intent.putExtra("user_id", user_id);
            startActivity(intent);
        });

        ImageView deleteBikeButton = view.findViewById(R.id.remove);
        deleteBikeButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), delete_bikes.class);
            intent.putExtra("user_id", user_id);
            startActivity(intent);
        });


        ImageView requestsButton = view.findViewById(R.id.requests);
        requestsButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), OwnerCustomers.class);
            intent.putExtra("user_id", user_id);
            startActivity(intent);
        });
        ImageView updateButton = view.findViewById(R.id.manage);
        updateButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), update_bikes.class);
            intent.putExtra("user_id", user_id);
            startActivity(intent);
        });
        ImageView rejectButton = view.findViewById(R.id.delete_current);
        rejectButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), delete_customers.class);
            intent.putExtra("user_id", user_id);
            startActivity(intent);
        });
        return view;
    }
}
