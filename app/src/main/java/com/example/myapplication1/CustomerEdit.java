package com.example.myapplication1;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CustomerEdit extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private String user_id; // Store user_id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cutomer_edit);  // Ensure correct layout

        // Retrieve user_id from Intent
        user_id = getIntent().getStringExtra("user_id");

        viewPager = findViewById(R.id.ViewPager1);
        tabLayout = findViewById(R.id.tab_layout);

        // Pass user_id to ViewPagerAdapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, user_id);
        viewPager.setAdapter(adapter);

        // Tab Titles & Icons (Ensure these resources exist)
        final String[] tabTitles = {"Home", "Cart", "About", "Profile"};
        final int[] tabIcons = {
                R.drawable.home_icon,
                R.drawable.cart_icon,
                R.drawable.a_icon,
                R.drawable.person
        };

        // Attach TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
            tab.setIcon(tabIcons[position]);
        }).attach();
    }
}
