package com.example.myapplication1;  // Replace with your package name

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import androidx.appcompat.app.AppCompatActivity;

public class imageView extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_show);

        ImageView proofImage = findViewById(R.id.proof_image);

        // Get the file path from the Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("file_path")) {
            String filePath = intent.getStringExtra("file_path");
            String fullImageUrl = config.BASE_URL + filePath;

            // Load image using Glide
            Glide.with(this)
                    .load(fullImageUrl)
                    .into(proofImage);
        }
    }
}
