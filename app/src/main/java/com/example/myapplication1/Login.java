package com.example.myapplication1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity {
    EditText usernameInput, passwordInput;
    TextView loginButton, skip_page, forgot_password;
    TextView errorMessage;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        usernameInput = findViewById(R.id.ruew2utpeok9);
        passwordInput = findViewById(R.id.rz0qcadxv6rh);
        loginButton = findViewById(R.id.r6cmadzdghb);
        errorMessage = findViewById(R.id.errorMessage);
        forgot_password = findViewById(R.id.forgot);
        skip_page = findViewById(R.id.skip);

        skip_page.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, CustomerEdit.class));
        });

        forgot_password.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, forgot_password.class));
        });


        findViewById(R.id.rribfbk0negs).setOnClickListener(v -> {
            startActivity(new Intent(Login.this, CustomerAdminChoose.class));
        });

        loginButton.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            } else {
                authenticateUser(username, password);
            }
        });
    }
    private void authenticateUser(String username, String password) {
        new Thread(() -> {
            try {
                URL url = new URL(config.BASE_URL + "login.php");
                // Use your PC's IP// Emulator localhost
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("user_id=" + username + "&password=" + password);
                writer.flush();
                writer.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    runOnUiThread(() -> handleResponse(response.toString()));
                } else {
                    runOnUiThread(() -> {
                        errorMessage.setText("Server Error: " + responseCode);
                        Toast.makeText(Login.this, "Server Error: " + responseCode, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    errorMessage.setText("Network Error");
                    Toast.makeText(Login.this, "Network Error", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void handleResponse(String response) {
        try {
            if (!response.trim().startsWith("{")) {
                errorMessage.setText("Invalid response from server: " + response);
                return;
            }

            JSONObject jsonResponse = new JSONObject(response);
            String status = jsonResponse.optString("status", "fail");
            String message = jsonResponse.optString("message", "Unknown error");
            String role = jsonResponse.optString("role", "");
            String user_id = jsonResponse.optString("user_id", "");

            if ("success".equals(status) && "customer".equals(role)) {
                Intent Intent = new Intent(Login.this, CustomerEdit.class);
                Intent.putExtra("user_id", user_id);
                startActivity(Intent);
                finish();
            }
            else {
                errorMessage.setText(message);
            }
            if ("success".equals(status) && "admin".equals(role)) {
                Intent Intent = new Intent(Login.this, AdminEdit.class);
                Intent.putExtra("user_id", user_id);
                startActivity(Intent);
                finish();
            }
            else {
                errorMessage.setText(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage.setText("Error parsing response: " + response);
        }
    }
}
