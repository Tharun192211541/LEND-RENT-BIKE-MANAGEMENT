package com.example.myapplication1;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final String user_id;

    public ViewPagerAdapter(@NonNull FragmentActivity activity, String user_id) {
        super(activity);
        this.user_id = user_id;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new Home_Fragment();
                break;
            case 1:
                fragment = new Cart_Fragment();
                break;
            case 2:
                fragment = new AboutFragment();
                break;
            case 3:
                fragment = new ProfileFragment();
                break;
            default:
                fragment = new Home_Fragment();
        }

        // Pass user_id to fragment via Bundle
        Bundle bundle = new Bundle();
        bundle.putString("user_id", user_id);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 4; // Home, Cart, About, Profile
    }
}
