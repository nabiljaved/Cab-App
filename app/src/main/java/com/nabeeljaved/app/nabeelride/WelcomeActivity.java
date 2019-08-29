package com.nabeeljaved.app.nabeelride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    Button driver, customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        driver = (Button) findViewById(R.id.btn);
        customer = (Button) findViewById(R.id.btn2);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("WELCOME");

        driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customerlogin = new Intent(WelcomeActivity.this, RiderLoginRegisterActivity.class);
                startActivity(customerlogin);
            }
        });


        customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent driverlogin = new Intent(WelcomeActivity.this, CustomerLoginRegisterActivity.class);
                startActivity(driverlogin);
            }
        });



    }
}
