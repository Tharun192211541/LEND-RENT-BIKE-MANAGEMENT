package com.example.myapplication1; // Ensure package name matches

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class CustomerAdminChoose extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_admin_choose); // Ensure this XML file exists

        // Navigate to MainActivity when rd3eveivdiie is clicked
        findViewById(R.id.rd3eveivdiie).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomerAdminChoose.this, AdminRegister.class));
            }
        });

        // Navigate to AdminRegister when r8l4ms9xfda7 is clicked
        findViewById(R.id.r8l4ms9xfda7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomerAdminChoose.this, MainActivity.class));
            }
        });

        // Navigate to Login when r6zj9plfs6 is clicked
        findViewById(R.id.r6zj9plfs6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomerAdminChoose.this, Login.class));
            }
        });
    }
}
