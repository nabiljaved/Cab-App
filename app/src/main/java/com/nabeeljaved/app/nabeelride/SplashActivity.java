package com.nabeeljaved.app.nabeelride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        Thread thread = new Thread(){

            @Override
            public void run()
            {
                try{

                    sleep(7000);

                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {

                    Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                    startActivity(intent);

                }
            }
        };

        thread.start();


    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
