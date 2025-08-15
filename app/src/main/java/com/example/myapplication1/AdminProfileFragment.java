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
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class AdminProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImage;
    private EditText userName, age, mobile, email, email_text, store_name, store_address, state, city;
    private Button editProfileBtn, logoutBtn;
    private String user_id;
    private boolean isEditing = false;
    private Uri selectedImageUri = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_admin, container, false);

        if (getArguments() != null) {
            user_id = getArguments().getString("user_id", "Unknown");
        }

        profileImage = view.findViewById(R.id.imageView);
        userName = view.findViewById(R.id.full_name);
        age = view.findViewById(R.id.age);
        email = view.findViewById(R.id.mail);
        mobile = view.findViewById(R.id.mobile);
        store_name = view.findViewById(R.id.store_name);
        store_address = view.findViewById(R.id.store_address);
        state = view.findViewById(R.id.state);
        city = view.findViewById(R.id.city);
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
        store_name.setEnabled(true);
        mobile.setEnabled(true);
        store_address.setEnabled(true);
        state.setEnabled(true);
        city.setEnabled(true);
        profileImage.setEnabled(true);
        email_text.setVisibility(View.VISIBLE);
        email_text.setText("Email can't be edited");
        editProfileBtn.setText("Save");
    }

    private void disableEditing() {
        isEditing = false;
        userName.setEnabled(false);
        age.setEnabled(false);
        store_name.setEnabled(false);
        mobile.setEnabled(false);
        email.setEnabled(false);
        store_address.setEnabled(false);
        state.setEnabled(false);
        city.setEnabled(false);
        email_text.setVisibility(View.GONE);
        profileImage.setEnabled(false);
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
                URL url = new URL(config.BASE_URL+"fetch_admin.php");
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
                    store_name.setText(data.getString("Store_name"));
                    email.setText(data.getString("email"));
                    mobile.setText(data.getString("Mobile_number"));
                    store_address.setText(data.getString("Store_Address"));
                    state.setText(data.getString("State"));
                    city.setText(data.getString("City"));

                    String imageUrl = config.BASE_URL + data.getString("profile_photo");
                    Glide.with(AdminProfileFragment.this)
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
                URL url = new URL(config.BASE_URL+"admin_edit.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                writeFormField(outputStream, boundary, "user_id", user_id);
                writeFormField(outputStream, boundary, "Full_Name", userName.getText().toString());
                writeFormField(outputStream, boundary, "Age", age.getText().toString());
                writeFormField(outputStream, boundary, "email", email.getText().toString());
                writeFormField(outputStream, boundary, "Mobile_number", mobile.getText().toString());
                writeFormField(outputStream, boundary, "Store_name", store_name.getText().toString());
                writeFormField(outputStream, boundary, "Store_Address", store_address.getText().toString());
                writeFormField(outputStream, boundary, "City", city.getText().toString());
                writeFormField(outputStream, boundary, "State", state.getText().toString());

                if (selectedImageUri != null) {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedImageUri);
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    outputStream.writeBytes("--" + boundary + "\r\n");
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"profile_photo\"; filename=\"profile.jpg\"\r\n");
                    outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n");

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.writeBytes("\r\n");
                    inputStream.close();
                }

                outputStream.writeBytes("--" + boundary + "--\r\n");
                outputStream.flush();
                outputStream.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                return response.toString();
            } catch (FileNotFoundException e) {
                return "{\"status\":\"error\",\"message\":\"File not found\"}";
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
                    Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    disableEditing();
                } else {
                    Toast.makeText(getActivity(), "Update failed: " + jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "Error parsing server response", Toast.LENGTH_SHORT).show();
            }
        }

        private void writeFormField(DataOutputStream outputStream, String boundary, String fieldName, String fieldValue) throws Exception {
            outputStream.writeBytes("--" + boundary + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n");
            outputStream.writeBytes(fieldValue + "\r\n");
        }
    }
}
