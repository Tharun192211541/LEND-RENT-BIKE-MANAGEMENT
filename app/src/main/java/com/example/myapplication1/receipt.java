package com.example.myapplication1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class receipt extends AppCompatActivity {

    TextView name, contact, address, profession, workAddress, regNumber,
            fromDate, fromTime, toDate, toTime, totalRent;
    ImageView back;


    String rentId, URL = config.BASE_URL+"receipt.php";
    String user_id;// Change this URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receipt);

        // Get rent_id from intent
        rentId = getIntent().getStringExtra("rent_id");
        user_id = getIntent().getStringExtra("user_id");


        // Initialize TextViews
        name = findViewById(R.id.name);
        contact = findViewById(R.id.contact);
        address = findViewById(R.id.address);
        profession = findViewById(R.id.profession);
        workAddress = findViewById(R.id.work_address);
        regNumber = findViewById(R.id.regnumber);
        fromDate = findViewById(R.id.from_date);
        fromTime = findViewById(R.id.from_time);
        toDate = findViewById(R.id.to_date);
        toTime = findViewById(R.id.to_time);
        totalRent = findViewById(R.id.totalRentText);
        back = findViewById(R.id.riz1elq9hmsq);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(receipt.this, CustomerEdit.class);
            intent.putExtra("user_id", user_id);
            startActivity(intent);
        });

        fetchReceiptFromServer(rentId);
    }

    private void fetchReceiptFromServer(String rentId) {
        StringRequest request = new StringRequest(Request.Method.POST,  URL + "?rent_id=" + rentId,
                response -> {
                    try {
                        JSONObject responseObj = new JSONObject(response);
                        if (responseObj.getString("status").equals("success")) {
                            JSONObject receipt = responseObj.getJSONObject("receipt");

                            name.setText(receipt.getString("customer_name"));
                            contact.setText(receipt.getString("mobile"));
                            address.setText(receipt.getString("address"));
                            profession.setText(receipt.getString("profession"));
                            workAddress.setText(receipt.getString("workplace"));
                            regNumber.setText(receipt.getString("bike_id"));
                            fromDate.setText(receipt.getString("from_date"));
                            fromTime.setText(receipt.getString("from_time"));
                            toDate.setText(receipt.getString("to_date"));
                            toTime.setText(receipt.getString("to_time"));
                            totalRent.setText("₹ " + receipt.getString("total_rent"));

                        } else {
                            Toast.makeText(this, "Receipt not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Volley Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> data = new HashMap<>();
                data.put("rent_id", rentId);
                return data;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
