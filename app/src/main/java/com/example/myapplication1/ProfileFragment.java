package com.example.myapplication1;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImage;
    private Uri selectedImageUri = null;
    private EditText userName, age, mobile, address, gender, profession, workplace, email, email_text;
    private Button editProfileBtn, logoutBtn;
    private String user_id;
    private boolean isEditing = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (getArguments() != null) {
            user_id = getArguments().getString("user_id", "Unknown");
        }

        profileImage = view.findViewById(R.id.imageView);
        userName = view.findViewById(R.id.full_name);
        age = view.findViewById(R.id.age);
        gender = view.findViewById(R.id.gender);
        email = view.findViewById(R.id.mail);
        mobile = view.findViewById(R.id.mobile);
        address = view.findViewById(R.id.address);
        profession = view.findViewById(R.id.profession);
        workplace = view.findViewById(R.id.work_address);
        editProfileBtn = view.findViewById(R.id.editprofile);
        logoutBtn = view.findViewById(R.id.logout);
        email_text = view.findViewById(R.id.email_text);

        fetchUserData(user_id);

        editProfileBtn.setOnClickListener(v -> {
            if (isEditing) {
                saveProfileChanges();
            } else {
                enableEditing();
            }
        });

        profileImage.setOnClickListener(v -> {
            if (isEditing) {
                openGallery();
            }
        });


        logoutBtn.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), Login.class));
            getActivity().finish();
        });

        return view;
    }

    private void enableEditing() {
        isEditing = true;
        userName.setEnabled(true);
        age.setEnabled(true);
        gender.setEnabled(true);
        mobile.setEnabled(true);
        address.setEnabled(true);
        profession.setEnabled(true);
        workplace.setEnabled(true);
        profileImage.setEnabled(true);
        email_text.setVisibility(View.VISIBLE);
        email_text.setText("Email can't be edited");
        editProfileBtn.setText("Save");
    }

    private void disableEditing() {
        isEditing = false;
        userName.setEnabled(false);
        age.setEnabled(false);
        gender.setEnabled(false);
        mobile.setEnabled(false);
        email.setEnabled(false);
        address.setEnabled(false);
        profession.setEnabled(false);
        workplace.setEnabled(false);
        profileImage.setEnabled(false);
        email_text.setVisibility(View.GONE);
        editProfileBtn.setText("Edit Profile");
    }

    private void fetchUserData(String userId) {
        new FetchUserDataTask().execute(userId);
    }

    private void saveProfileChanges() {
        new UpdateProfileTask().execute();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profileImage.setImageURI(selectedImageUri);
        }
    }

    private class FetchUserDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            try {
                URL url = new URL(config.BASE_URL+"fetch_customer.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write("user_id=" + URLEncoder.encode(userId, "UTF-8"));
                writer.flush();
                writer.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } catch (Exception e) {
                return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");

                if (status.equals("success")) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    userName.setText(data.getString("Full_Name"));
                    age.setText(String.valueOf(data.getInt("Age")));
                    gender.setText(data.getString("Gender"));
                    email.setText(data.getString("email"));
                    mobile.setText(data.getString("Mobile_number"));
                    address.setText(data.getString("Address"));
                    profession.setText(data.getString("Profession"));
                    workplace.setText(data.getString("Work_Address"));
                    String imageUrl = config.BASE_URL + data.getString("profile_photo");
                    Glide.with(ProfileFragment.this)
                            .load(imageUrl)
                            .error(R.drawable.person)
                            .into(profileImage);
                } else if (status.equals("not_found") && jsonObject.has("email")) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Profile Update Pending")
                            .setMessage("Your profile update is still pending. Please fill it now.")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                    email.setText(jsonObject.getString("email"));
                } else if (status.equals("error")) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("User Not Found")
                            .setMessage("User not found. Please log in again.")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> {
                                Intent intent = new Intent(getActivity(), Login.class);
                                startActivity(intent);
                                getActivity().finish();
                            })
                            .show();
                } else {
                    Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "JSON Parsing Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdateProfileTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String boundary = "*****" + System.currentTimeMillis() + "*****";
                URL url = new URL(config.BASE_URL+"customer_edit.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                OutputStream outputStream = connection.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);

                writeFormField(writer, boundary, "Full_Name", userName.getText().toString());
                writeFormField(writer, boundary, "Age", age.getText().toString());
                writeFormField(writer, boundary, "Gender", gender.getText().toString());
                writeFormField(writer, boundary, "Mobile_number", mobile.getText().toString());
                writeFormField(writer, boundary, "email", email.getText().toString());
                writeFormField(writer, boundary, "Address", address.getText().toString());
                writeFormField(writer, boundary, "Profession", profession.getText().toString());
                writeFormField(writer, boundary, "Work_Address", workplace.getText().toString());

                writer.flush();

                if (selectedImageUri != null) {
                    String fileName = "profile.jpg";
                    writeFileField(outputStream, boundary, "profile_photo", fileName, selectedImageUri);
                    outputStream.flush();
                }

                writer.write("--" + boundary + "--\r\n");
                writer.flush();
                writer.close();
                outputStream.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                return response.toString();

            } catch (Exception e) {
                return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
            }
        }
        private void writeFormField(OutputStreamWriter writer, String boundary, String fieldName, String fieldValue) throws Exception {
            writer.write("--" + boundary + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n");
            writer.write(fieldValue + "\r\n");
        }
        private void writeFileField(OutputStream outputStream, String boundary, String fieldName, String fileName, Uri imageUri) throws Exception {
            String mimeType = getActivity().getContentResolver().getType(imageUri);
            outputStream.write(("--" + boundary + "\r\n").getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
            outputStream.write(("Content-Type: " + mimeType + "\r\n\r\n").getBytes());

            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.write("\r\n".getBytes());
            inputStream.close();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getString("status").equals("success")) {
                    Toast.makeText(getActivity(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    disableEditing();
                } else {
                    Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}