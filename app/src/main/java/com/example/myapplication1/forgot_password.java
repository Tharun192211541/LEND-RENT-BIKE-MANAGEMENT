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
import java.io.OutputStream;
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

public class forgot_password extends AppCompatActivity {

    private EditText emailEditText;
    private Button sendOtpButton;
    private static final String EMAIL_SENDER = "lendnrent1@gmail.com";
    private static final String APP_PASSWORD = "kdhq vkhi qaln zvld"; // Your App Password

    private String generatedOtp;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        emailEditText = findViewById(R.id.emailEditText);
        sendOtpButton = findViewById(R.id.button3);

        sendOtpButton.setOnClickListener(v -> validateAndSendOtp());
    }

    private void validateAndSendOtp() {
        userEmail = emailEditText.getText().toString().trim();
        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        sendOtpEmail(userEmail);
    }

    private void sendOtpEmail(String recipientEmail) {
        generatedOtp = String.valueOf(new Random().nextInt(900000) + 100000);
        new Thread(() -> {
            boolean sent = sendEmail(recipientEmail, "Your OTP for Password Reset",
                    "Your OTP is: " + generatedOtp + "\nDo not share this code with anyone.");
            runOnUiThread(() -> {
                if (sent) {
                    Toast.makeText(forgot_password.this, "OTP sent to your email", Toast.LENGTH_SHORT).show();
                    showOtpDialog();
                } else {
                    Toast.makeText(forgot_password.this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private boolean sendEmail(String toEmail, String subject, String body) {
        try {
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
                Toast.makeText(forgot_password.this, "OTP Verified", Toast.LENGTH_SHORT).show();
                new Thread(() -> fetchCredentialsFromServer(userEmail)).start();
            } else {
                Toast.makeText(forgot_password.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void fetchCredentialsFromServer(String email) {
        try {
            URL url = new URL(config.BASE_URL+"forgot_password.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String postData = "mail=" + email;
            OutputStream os = conn.getOutputStream();
            os.write(postData.getBytes());
            os.flush();
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            String response = responseBuilder.toString();
            JSONObject jsonObject = new JSONObject(response);
            runOnUiThread(() -> handleServerResponse(jsonObject));

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(forgot_password.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void handleServerResponse(JSONObject jsonObject) {
        try {
            if (jsonObject.getString("status").equals("success")) {
                JSONObject data = jsonObject.getJSONObject("data");
                String userId = data.getString("user_id");
                String password = data.getString("password");
                String role = data.getString("role");

                Intent intent = new Intent(forgot_password.this, LoginCredentials.class);
                intent.putExtra("user_email", userEmail);
                intent.putExtra("user_id", userId);
                intent.putExtra("password", password);
                intent.putExtra("user_role", role);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(forgot_password.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(forgot_password.this, "Invalid response format", Toast.LENGTH_SHORT).show();
        }
    }
}
