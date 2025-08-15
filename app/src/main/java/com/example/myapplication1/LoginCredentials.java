package com.example.myapplication1; // Replace with your actual package name

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginCredentials extends AppCompatActivity {

    private TextView userIdTextView, passwordTextView;
    private Button loginbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_credentials); // Ensure this XML file exists

        // Initialize UI elements
        userIdTextView = findViewById(R.id.username_text);
        passwordTextView = findViewById(R.id.password_text);
        loginbutton  = findViewById(R.id.button3);
        // Get data from Intent
        String userId = getIntent().getStringExtra("user_id");
        String password = getIntent().getStringExtra("password");

        // Display data
        if (userId != null && password != null) {
            userIdTextView.setText(userId);
            passwordTextView.setText(password);
        } else {
            Toast.makeText(this, "No credentials received", Toast.LENGTH_SHORT).show();
        }
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginCredentials.this, Login.class);
                startActivity(intent);
                finish(); // Closes current activity
            }
        });
    }
}


