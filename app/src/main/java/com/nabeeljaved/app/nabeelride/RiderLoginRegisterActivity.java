package com.nabeeljaved.app.nabeelride;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RiderLoginRegisterActivity extends AppCompatActivity {

    //DECLARE VARIABLES
    Button driverLogin, driverRegister;
    EditText email, password;
    TextView driverStatus, clickregister;
    FirebaseAuth mAuth;
    ProgressDialog loadingBar;
    private DatabaseReference driverDatabaseReference;
    private String onlineDriverID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_login_register);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //INITIALIZE FIREBASE
        mAuth = FirebaseAuth.getInstance();


        //INITIALIZE VARIABLES
        loadingBar = new ProgressDialog(this, R.style.MyAlertDialogStyle);
        driverLogin = (Button) findViewById(R.id.btn_login);
        driverRegister = (Button) findViewById(R.id.register_btn);
        email = (EditText) findViewById(R.id.text_driv_email);
        password = (EditText) findViewById(R.id.text_driv_pass);
        driverStatus = (TextView) findViewById(R.id.text_login_batch);
        clickregister = (TextView) findViewById(R.id.text_register);

        //invisible the register button
        driverRegister.setVisibility(View.INVISIBLE);
        driverRegister.setEnabled(false);

        clickregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverLogin.setVisibility(View.INVISIBLE);
                clickregister.setVisibility(View.INVISIBLE);
                driverStatus.setText("REGISTERING DRIVER");

                driverRegister.setVisibility(View.VISIBLE);
                driverRegister.setEnabled(true);

            }
        });

        driverRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String customerEmail = email.getText().toString();
                String customerPassword = password.getText().toString();

                RegisterCustomer(customerEmail, customerPassword);

            }
        });

        driverLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String customerEmail = email.getText().toString();
                String customerPassword = password.getText().toString();

                LoginDriver(customerEmail, customerPassword);

            }
        });

    }

    private void LoginDriver(String customerEmail, String customerPassword)
    {

        if(TextUtils.isEmpty(customerEmail) | (TextUtils.isEmpty(customerPassword)))
        {
            Toast.makeText(this, "One of Field is empty...", Toast.LENGTH_SHORT).show();
        }


        else
        {
            loadingBar.setTitle("Driver Login");
            loadingBar.setMessage("Please Wait while we Check Your Credentials...");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(customerEmail, customerPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(RiderLoginRegisterActivity.this, "Driver Login Successfull...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent driver_map_intent = new Intent(RiderLoginRegisterActivity.this, DriverMapActivity.class);
                                startActivity(driver_map_intent);
                            }else{
                                Toast.makeText(RiderLoginRegisterActivity.this, "Driver Login unSuccessfull...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });
        }
    }


    public void RegisterCustomer(String mail, String pwd)
    {

        if(TextUtils.isEmpty(mail) | (TextUtils.isEmpty(pwd)))
        {
            Toast.makeText(this, "One of Field is empty...", Toast.LENGTH_SHORT).show();
        }


        else
        {
            loadingBar.setTitle("Driver Registration");
            loadingBar.setMessage("Please Wait while we Register You...");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(mail, pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                //saving database reference to databse
                                onlineDriverID = mAuth.getCurrentUser().getUid();
                                driverDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(onlineDriverID);
                                driverDatabaseReference.setValue(true);
                                loadingBar.dismiss();

                                //move to other activity
                                Intent driver_map_intent = new Intent(RiderLoginRegisterActivity.this, DriverMapActivity.class);
                                startActivity(driver_map_intent);

                                //display toast message
                                Toast.makeText(RiderLoginRegisterActivity.this, "Driver Registered Successfull...", Toast.LENGTH_SHORT).show();

                            }else{
                                Toast.makeText(RiderLoginRegisterActivity.this, "Driver Registration unSuccessfull...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });
        }

    }
}
