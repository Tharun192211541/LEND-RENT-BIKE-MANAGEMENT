package com.example.myapplication1;

import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class admin_user_details extends AppCompatActivity {

    private TextView name,conta,addres;
    LinearLayout orderID,customer_data,Lender_content;
    TextView customer_name,customer_mobile,customer_address,customer_profession,customer_workaddress,customer_proof;

    boolean isLenderVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_cart_user_details); // Connecting XML layout

        name = findViewById(R.id.User_name);
        conta = findViewById(R.id.contact);
        addres = findViewById(R.id.addressText);
        orderID = findViewById(R.id.order_details);
        customer_data = findViewById(R.id.Lender);
        Lender_content = findViewById(R.id.Lender_content);
        Lender_content.setVisibility(GONE);
        customer_name = findViewById(R.id.customer_name_text);
        customer_mobile = findViewById(R.id.mobile);
        customer_profession = findViewById(R.id.profession);
        customer_workaddress = findViewById(R.id.work_address);
        customer_proof = findViewById(R.id.proof);
        customer_address = findViewById(R.id.address);
        // Find view AFTER setContentView

        // Safely retrieve the customer name
        String rentId = getIntent().getStringExtra("rent_id");
        String customerId = getIntent().getStringExtra("customer_id");
        String customerName = getIntent().getStringExtra("customer_name");
        String ownerId = getIntent().getStringExtra("owner_id");
        String bikeId = getIntent().getStringExtra("bike_id");
        String fromDate = getIntent().getStringExtra("from_date");
        String fromTime = getIntent().getStringExtra("from_time");
        String toDate = getIntent().getStringExtra("to_date");
        String toTime = getIntent().getStringExtra("to_time");
        String totalRent = getIntent().getStringExtra("total_rent");
        String mobile = getIntent().getStringExtra("mobile");
        String address = getIntent().getStringExtra("address");
        String profession = getIntent().getStringExtra("profession");
        String workplace = getIntent().getStringExtra("workplace");
        String selectedIDProof = getIntent().getStringExtra("selectedIDproof");
        String filePath = getIntent().getStringExtra("file_path");


        name.setText(customerName);
        conta.setText("Mobile : " + mobile);
        addres.setText(address);
        customer_name.setText(customerName);
        customer_mobile.setText(mobile);
        customer_profession.setText(profession);
        customer_workaddress.setText(workplace);
        customer_proof.setText(Html.fromHtml("<u>View Proof</u>"));

        customer_address.setText(address);

        orderID.setOnClickListener(v -> {
            Intent intent = new Intent(admin_user_details.this, receipt1.class);
            intent.putExtra("rent_id", rentId);
            String customerUserId = getIntent().getStringExtra("customer_user_id");
            intent.putExtra("user_id", customerUserId);
            startActivity(intent);
        });
        customer_data.setOnClickListener(v -> {
            if (isLenderVisible) {
                Lender_content.setVisibility(GONE);
                isLenderVisible = false;
            } else {
                Lender_content.setVisibility(View.VISIBLE);
                isLenderVisible = true;
            }
        });
        customer_proof.setOnClickListener(v -> {
            Intent intent = new Intent(admin_user_details.this, imageView.class);
            intent.putExtra("file_path", filePath);
            startActivity(intent);
        });


    }
}
