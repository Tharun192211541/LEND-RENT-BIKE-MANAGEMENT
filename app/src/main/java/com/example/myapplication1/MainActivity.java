package com.example.myapplication1;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private static final String REGISTER_URL = config.BASE_URL+"customer.php";

    private static final String EMAIL_SENDER = "lendnrent1@gmail.com";
    private static final String APP_PASSWORD = "kdhq vkhi qaln zvld";

    private String generatedOtp;
    private String userEmail, userPassword, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.button3);

        registerButton.setOnClickListener(v -> validateAndSendOtp());
    }

    private void validateAndSendOtp() {
        userEmail = emailEditText.getText().toString().trim();
        userPassword = passwordEditText.getText().toString().trim();
        confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userPassword.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!userPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        sendOtpEmail(userEmail);
    }

    private void sendOtpEmail(String recipientEmail) {
        generatedOtp = String.valueOf(new Random().nextInt(900000) + 100000);
        new Thread(() -> {
            boolean sent = sendEmail(recipientEmail, "Your OTP for Verification",
                    "Your OTP is: " + generatedOtp + "\nPlease do not share it.");
            runOnUiThread(() -> {
                if (sent) {
                    Toast.makeText(MainActivity.this, "OTP sent to your email", Toast.LENGTH_SHORT).show();
                    showOtpDialog();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private boolean sendEmail(String toEmail, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_SENDER, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_SENDER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showOtpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter OTP");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String enteredOtp = input.getText().toString().trim();
            if (enteredOtp.equals(generatedOtp)) {
                Toast.makeText(MainActivity.this, "OTP Verified", Toast.LENGTH_SHORT).show();
                registerUser();
            } else {
                Toast.makeText(MainActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void registerUser() {
        new Thread(() -> {
            try {
                URL url = new URL(REGISTER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String postData = "mail=" + userEmail + "&password=" + userPassword + "&confirm_password=" + confirmPassword;

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(postData);
                writer.flush();
                writer.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String status = jsonResponse.getString("status");
                String message = jsonResponse.getString("message");

                if (status.equals("success")) {
                    String userId = jsonResponse.getJSONObject("data").getString("user_id");
                    String password = jsonResponse.getJSONObject("data").getString("password");

                    Intent intent = new Intent(MainActivity.this, LoginCredentials.class);
                    intent.putExtra("user_id", userId);
                    intent.putExtra("password", password);
                    startActivity(intent);
                    finish();
                } else {
                    showToast(message);
                }

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error: " + e.getMessage());
            }
        }).start();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
    }
}
