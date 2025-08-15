package com.example.myapplication1;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AboutFragment extends Fragment {
    private EditText feedback;
    private String user_id;
    private ImageView send;
    private static final String REGISTER_URL = config.BASE_URL+"feedback.php";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        if (getArguments() != null) {
            user_id = getArguments().getString("user_id", null);
        }

        feedback = view.findViewById(R.id.feed);
        send = view.findViewById(R.id.sen);

        send.setOnClickListener(v -> {
            if (user_id == null || user_id.equalsIgnoreCase("Unknown") || user_id.trim().isEmpty()) {
                showLoginRequiredPopup();
            } else {
                sendFeedback();
            }
        });

        return view;
    }

    private void sendFeedback() {
        String feedbackText = feedback.getText().toString().trim();
        if (feedbackText.isEmpty()) {
            showPopup("Error", "Please enter feedback.", false);
            return;
        }

        executorService.execute(() -> {
            String result = sendFeedbackToServer(user_id, feedbackText);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> showPopup("Feedback Status", result, true));
            }
        });
    }

    private String sendFeedbackToServer(String userId, String feedbackText) {
        try {
            URL url = new URL(REGISTER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String postData = "user_id=" + userId + "&feedback=" + feedbackText;
            OutputStream os = conn.getOutputStream();
            os.write(postData.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getString("message");
            } else {
                return "Error submitting feedback.";
            }
        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }

    private void showPopup(String title, String message, boolean clearOnDismiss) {
        if (getActivity() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            if (clearOnDismiss) {
                feedback.setText("");
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                if (clearOnDismiss) {
                    feedback.setText("");
                }
            }
        }, 3000);
    }

    private void showLoginRequiredPopup() {
        if (getActivity() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Login Required");
        builder.setMessage("Please login to submit feedback.");
        builder.setCancelable(false);

        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
