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

public class CustomerLoginRegisterActivity extends AppCompatActivity {

    //Declare variable and buttons
    private Button  customerLogin, customerRegister;
    private EditText email, password;
    private TextView login_regis, clickregister;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference customerDatabaseReference;
    private String onlineCustomerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //initialilize firebase and database reference
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this, R.style.MyAlertDialogStyle);

        //initialize variable and buttons
        customerLogin = (Button) findViewById(R.id.btn_login);
        customerRegister = (Button) findViewById(R.id.register_btn);
        email = (EditText) findViewById(R.id.text_cust_email);
        password = (EditText) findViewById(R.id.text_cust_pass);
        login_regis = (TextView) findViewById(R.id.text_login_batch);
        clickregister = (TextView) findViewById(R.id.text_register);

        //invisible the register button
        customerRegister.setVisibility(View.INVISIBLE);
        customerRegister.setEnabled(false);

        clickregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerLogin.setVisibility(View.INVISIBLE);
                clickregister.setVisibility(View.INVISIBLE);
                login_regis.setText("REGISTERING CUSTOMER");

                customerRegister.setVisibility(View.VISIBLE);
                customerRegister.setEnabled(true);

            }
        });

        customerRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String customerEmail = email.getText().toString();
                String customerPassword = password.getText().toString();

                RegisterCustomer(customerEmail, customerPassword);


            }
        });


        customerLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String customerEmail = email.getText().toString();
                String customerPassword = password.getText().toString();


                LoginCustomer(customerEmail, customerPassword);

            }
        });



    }

    private void LoginCustomer(String Email, String Password)
    {
        if(TextUtils.isEmpty(Email) | (TextUtils.isEmpty(Password)))
        {
            Toast.makeText(this, "One of Field is empty...", Toast.LENGTH_SHORT).show();
        }


        else
        {
            loadingBar.setTitle("Customer Login");
            loadingBar.setMessage("Please Wait while we Check Your Credentials...");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(Email, Password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Login Successfull...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent customer_map_intent = new Intent(CustomerLoginRegisterActivity.this, CutomerMapActivity.class);
                                startActivity(customer_map_intent);
                            }else{
                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Login unSuccessfull...", Toast.LENGTH_SHORT).show();
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
                    loadingBar.setTitle("Customer Registration");
                    loadingBar.setMessage("Please Wait while we Register You...");
                    loadingBar.show();

                mAuth.createUserWithEmailAndPassword(mail, pwd)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                if(task.isSuccessful())
                                {
                                    //saving id in database reference unders users and customers
                                    onlineCustomerID = mAuth.getCurrentUser().getUid();
                                    customerDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(onlineCustomerID);
                                    customerDatabaseReference.setValue(true);
                                    loadingBar.dismiss();

                                    //sending customer to customer Map
                                    Intent customer_map_intent = new Intent(CustomerLoginRegisterActivity.this, CutomerMapActivity.class);
                                    startActivity(customer_map_intent);

                                    //toast
                                    Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Registered Successfull...", Toast.LENGTH_SHORT).show();


                                }else{
                                    loadingBar.dismiss();
                                    Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Registration unSuccessfull...", Toast.LENGTH_SHORT).show();
                                }

                             }
                        });
            }

    }




}
