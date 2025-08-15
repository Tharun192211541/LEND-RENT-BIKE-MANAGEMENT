package com.example.myapplication1;

import static android.app.PendingIntent.getActivity;
import static android.app.ProgressDialog.show;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class BookingData extends AppCompatActivity {

    private EditText userName, age, mobile, address, gender, profession, workplace, email;
    private TextView email_text, termsText;
    private Button rentNowBtn;
    private Spinner idProofSpinner;
    private CheckBox checkTerms;

    private String selectedIdProof = "";
    private String user_id;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView imagePreview, back;
    private TextView selectImageBtn;
    private Uri selectedImageUri = null;

    private String file_path = ""; // Placeholder

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_user_data);

        // Initialize Views
        userName = findViewById(R.id.full_name);
        age = findViewById(R.id.age);
        gender = findViewById(R.id.gender);
        email = findViewById(R.id.mail);
        mobile = findViewById(R.id.mobile);
        address = findViewById(R.id.address);
        profession = findViewById(R.id.profession);
        workplace = findViewById(R.id.work_address);
        rentNowBtn = findViewById(R.id.editprofile);
        email_text = findViewById(R.id.email_text);
        idProofSpinner = findViewById(R.id.id_proof_spinner);
        checkTerms = findViewById(R.id.checkTerms);
        back = findViewById(R.id.riz1elq9hmsq);
        termsText = findViewById(R.id.termsText);


        rentNowBtn.setText("Rent Now");


        // Spinner setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Aadhar Card", "PAN Card", "Driving Licence", "Passport"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        idProofSpinner.setAdapter(adapter);

        idProofSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedIdProof = adapterView.getItemAtPosition(i).toString();
                imagePreview = findViewById(R.id.imagePreview);
                selectImageBtn = findViewById(R.id.click);
                selectImageBtn.setOnClickListener(v -> openImageChooser());

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedIdProof = "";
            }
        });

        // Get customer ID
        user_id = getIntent().getStringExtra("customer_user_id");
        if (!TextUtils.isEmpty(user_id)) {
            fetchUserData(user_id);
        } else {
            showLoginRequiredPopup();
            }

        boolean[] isTermsAccepted = {false}; // Class-level or final array inside onCreate

// Terms text click opens dialog
        termsText.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(BookingData.this);
            View view = getLayoutInflater().inflate(R.layout.terms_and_conditions, null);

            TextView titleView = new TextView(BookingData.this);
            titleView.setText("Terms & Conditions");
            titleView.setPadding(40, 30, 40, 20);
            titleView.setTextSize(18);
            titleView.setTextColor(Color.BLACK);
            titleView.setTypeface(null, Typeface.BOLD);
            builder.setCustomTitle(titleView);

            builder.setView(view);

            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                isTermsAccepted[0] = true;
                checkTerms.setChecked(true); // Check only after OK
            });

            AlertDialog dialog = builder.create();

            dialog.setOnShowListener(dialogInterface -> {
                Window window = dialog.getWindow();
                if (window != null) {
                    DisplayMetrics metrics = BookingData.this.getResources().getDisplayMetrics();
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                    layoutParams.copyFrom(window.getAttributes());
                    layoutParams.width = (int) (metrics.widthPixels * 0.85);
                    layoutParams.height = (int) (metrics.heightPixels * 0.60);
                    window.setAttributes(layoutParams);
                    window.setBackgroundDrawableResource(android.R.color.white);
                }

                Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setEnabled(false);

                ScrollView scrollView = view.findViewById(R.id.scrollView);
                scrollView.setScrollbarFadingEnabled(false);

                scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
                    View lastChild = scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int diff = lastChild.getBottom() - (scrollView.getHeight() + scrollView.getScrollY());
                    if (diff <= 0) {
                        okButton.setEnabled(true);
                    }
                });
            });

            dialog.show();
        });

// Prevent user from checking checkbox manually
        checkTerms.setOnClickListener(v -> {
            if (!isTermsAccepted[0]) {
                checkTerms.setChecked(false); // Prevent check
                Toast.makeText(BookingData.this, "Please read Terms and Conditions to accept", Toast.LENGTH_SHORT).show();
            }
        });

// Still keep color update logic on actual check/uncheck
        checkTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int color = isChecked ? android.R.color.holo_green_dark : R.color.cement;
            checkTerms.setTextColor(ContextCompat.getColor(BookingData.this, color));
        });


        back.setOnClickListener(v -> finish());


        rentNowBtn.setOnClickListener(v -> {
            if (!checkTerms.isChecked()) {
                Toast.makeText(this, "Please agree to the terms and conditions", Toast.LENGTH_SHORT).show();
            } else if (validateInputs()) {
                submitUserData();
            }
        });
    }
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
            file_path = getRealPathFromURI(imageUri);
        }
    }
    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    private boolean validateInputs() {
        if (userName.getText().toString().trim().isEmpty()) {
            userName.setError("Name is required");
            return false;
        }

        if (age.getText().toString().trim().isEmpty()) {
            age.setError("Age is required");
            return false;
        }

        if (gender.getText().toString().trim().isEmpty()) {
            gender.setError("Gender is required");
            return false;
        }

        String emailVal = email.getText().toString().trim();
        if (emailVal.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()) {
            email.setError("Valid Email is required");
            return false;
        }

        String mobileVal = mobile.getText().toString().trim();
        if (mobileVal.isEmpty() || !Pattern.matches("\\d{10}", mobileVal)) {
            mobile.setError("Valid 10-digit Mobile number is required");
            return false;
        }

        if (address.getText().toString().trim().isEmpty()) {
            address.setError("Address is required");
            return false;
        }

        return true;
    }

    private void fetchUserData(String userId) {
        new FetchUserDataTask().execute(userId);
    }

    private class FetchUserDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(config.BASE_URL+"fetch_customer.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("user_id=" + URLEncoder.encode(params[0], "UTF-8"));
                writer.flush();
                writer.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                return sb.toString();
            } catch (Exception e) {
                Log.e("FetchUserDataTask", "Error: " + e.getMessage());
                return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                if ("success".equals(jsonObject.optString("status"))) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    userName.setText(data.optString("Full_Name", ""));
                    age.setText(data.optString("Age", ""));
                    gender.setText(data.optString("Gender", ""));
                    email.setText(data.optString("email", ""));
                    mobile.setText(data.optString("Mobile_number", ""));
                    address.setText(data.optString("Address", ""));
                    profession.setText(data.optString("Profession", ""));
                    workplace.setText(data.optString("Work_Address", ""));

                    String idProof = data.optString("ID_Proof", "");
                    int spinnerPos = ((ArrayAdapter<String>) idProofSpinner.getAdapter()).getPosition(idProof);
                    if (spinnerPos >= 0) idProofSpinner.setSelection(spinnerPos);

                }
            } catch (JSONException e) {
                Log.e("FetchUserData", "JSON parsing error: " + e.getMessage());
                showAlertDialog("Error", "Failed to parse server response");
            }
        }
    }
    // Only relevant changes shown (retain other existing parts of onCreate, validation, etc.)

    private void submitUserData() {
        new MultipartUploadTask().execute();
    }

    private class MultipartUploadTask extends AsyncTask<Void, Void, String> {
        AlertDialog progressDialog;
        boolean isError = false;
        String errorMessage = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new AlertDialog.Builder(BookingData.this)
                    .setCancelable(false)
                    .setMessage("Submitting your booking. Please wait...")
                    .create();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String boundary = "===" + System.currentTimeMillis() + "===";
            String LINE_FEED = "\r\n";

            try {
                URL url = new URL(config.BASE_URL+"userbooking_details.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                DataOutputStream request = new DataOutputStream(conn.getOutputStream());

                writeTextPart(request, boundary, "customer_id", user_id);
                writeTextPart(request, boundary, "customer_name", userName.getText().toString().trim());
                writeTextPart(request, boundary, "owner_id", getIntent().getStringExtra("owner_id"));
                writeTextPart(request, boundary, "bike_id", getIntent().getStringExtra("bike_id"));
                writeTextPart(request, boundary, "from_date", getIntent().getStringExtra("from_date"));
                writeTextPart(request, boundary, "from_time", getIntent().getStringExtra("from_time"));
                writeTextPart(request, boundary, "to_date", getIntent().getStringExtra("to_date"));
                writeTextPart(request, boundary, "to_time", getIntent().getStringExtra("to_time"));
                writeTextPart(request, boundary, "total_rent", String.valueOf(getIntent().getIntExtra("total_rent", 0)));
                writeTextPart(request, boundary, "mobile", mobile.getText().toString().trim());
                writeTextPart(request, boundary, "address", address.getText().toString().trim());
                writeTextPart(request, boundary, "profession", profession.getText().toString().trim());
                writeTextPart(request, boundary, "workplace", workplace.getText().toString().trim());
                writeTextPart(request, boundary, "selectedIDproof", selectedIdProof);

                // Upload image
                if (imageUri != null) {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    String fileName = "id_proof.jpg";

                    request.writeBytes("--" + boundary + LINE_FEED);
                    request.writeBytes("Content-Disposition: form-data; name=\"id_proof\"; filename=\"" + fileName + "\"" + LINE_FEED);
                    request.writeBytes("Content-Type: image/jpeg" + LINE_FEED);
                    request.writeBytes(LINE_FEED);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        request.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    request.writeBytes(LINE_FEED);
                }

                // End of multipart
                request.writeBytes("--" + boundary + "--" + LINE_FEED);
                request.flush();
                request.close();

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    isError = true;
                    errorMessage = "Server returned response code: " + responseCode;
                    return "";
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                conn.disconnect();

                return response.toString();

            } catch (Exception e) {
                isError = true;
                errorMessage = e.getMessage();
                Log.e("UploadError", "Exception: " + e.getMessage());
                return "";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (isError) {
                showAlertDialog("Upload Failed", errorMessage);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if ("success".equalsIgnoreCase(jsonObject.optString("status"))) {
                        String bookingId = jsonObject.optString("rent_id", "");
                        Toast.makeText(BookingData.this, "Booking submitted successfully!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(BookingData.this, receipt.class);
                        intent.putExtra("rent_id", bookingId);
                        intent.putExtra("user_id", user_id); // Passing rent_id
                        startActivity(intent);
                    } else {
                        showAlertDialog("Server Error", jsonObject.optString("message", "Unknown error"));
                    }
                } catch (JSONException e) {
                    showAlertDialog("Response Error", "Invalid server response");
                }
            }
        }
        private void writeTextPart(DataOutputStream request, String boundary, String paramName, String paramValue) throws Exception {
            String LINE_FEED = "\r\n";
            request.writeBytes("--" + boundary + LINE_FEED);
            request.writeBytes("Content-Disposition: form-data; name=\"" + paramName + "\"" + LINE_FEED);
            request.writeBytes("Content-Type: text/plain; charset=UTF-8" + LINE_FEED);
            request.writeBytes(LINE_FEED);
            request.writeBytes(paramValue + LINE_FEED);
        }

    }
        private void showAlertDialog(String title, String message) {
            new AlertDialog.Builder(BookingData.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        }
    private void showLoginRequiredPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BookingData.this);
        builder.setTitle("Login Required");
        builder.setMessage("Please login to continue booking.");
        builder.setCancelable(false); // Prevent dialog from being dismissed accidentally

        builder.setPositiveButton("Login", (dialog, which) -> {
            // Navigate to Login Activity
            Intent intent = new Intent(BookingData.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Optional: Clears back stack
            startActivity(intent);
            finish(); // Close BookingData activity
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            finish(); // Optionally close current screen if user cancels
        });

        builder.show();
    }


}
