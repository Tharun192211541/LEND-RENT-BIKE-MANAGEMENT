package com.example.myapplication1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdminRegister extends AppCompatActivity {

    private EditText fullName, shopName, shopAddress, zipCode, mobileNumber, email, password, confirmPassword;
    private TextView registerButton;
    private static final String REGISTER_URL = config.BASE_URL+"admin.php"; // Replace with actual server URL

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_register);

        // Initialize UI elements
        fullName = findViewById(R.id.rtjqal6c8ut);
        shopName = findViewById(R.id.r2qvaj6nteki);
        shopAddress = findViewById(R.id.rlcq1i21qfsg);
        zipCode = findViewById(R.id.rtdwzw2ct8oe);
        mobileNumber = findViewById(R.id.rtrife9wh7wp);
        email = findViewById(R.id.rwdu8drsdxbs);
        password = findViewById(R.id.r4wdrblupbpm);
        confirmPassword = findViewById(R.id.rxer9d2isf7s);
        registerButton = findViewById(R.id.rxma7gn4vc3b);

        // Set OnClickListener for Register Button
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String fullNameText = fullName.getText().toString().trim();
        String shopNameText = shopName.getText().toString().trim();
        String shopAddressText = shopAddress.getText().toString().trim();
        String zipCodeText = zipCode.getText().toString().trim();
        String mobileNumberText = mobileNumber.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String confirmPasswordText = confirmPassword.getText().toString().trim();

        // Validate input fields
        if (fullNameText.isEmpty() || shopNameText.isEmpty() || shopAddressText.isEmpty() ||
                zipCodeText.isEmpty() || mobileNumberText.isEmpty() || emailText.isEmpty() ||
                passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
            showToast("All fields are required");
            return;
        }

        if (!passwordText.equals(confirmPasswordText)) {
            showToast("Passwords do not match");
            return;
        }

        // Perform registration request in a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(REGISTER_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);

                    // Prepare POST data with all necessary fields
                    String postData = "Full_name=" + fullNameText +
                            "&shop_name=" + shopNameText +
                            "&shop_address=" + shopAddressText +
                            "&zip_code=" + zipCodeText +
                            "&Mobile_number=" + mobileNumberText +
                            "&email=" + emailText +
                            "&password=" + passwordText +
                            "&confirm_password=" + confirmPasswordText;

                    // Send request
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(postData);
                    writer.flush();
                    writer.close();

                    // Read response
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String status = jsonResponse.getString("status");
                    String message = jsonResponse.getString("message");

                    if (status.equals("success")) {
                        JSONObject userData = jsonResponse.getJSONObject("data");
                        String userId = userData.getString("user_id");
                        String userPassword = userData.getString("password");

                        // Navigate to LoginCredentialsActivity
                        Intent intent = new Intent(AdminRegister.this, LoginCredentials.class);
                        intent.putExtra("user_id", userId);
                        intent.putExtra("password", userPassword);
                        startActivity(intent);
                        finish();
                    } else {
                        showToast(message);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Error: " + e.getMessage());
                }
            }
        }).start();
    }

    private void showToast(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AdminRegister.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
