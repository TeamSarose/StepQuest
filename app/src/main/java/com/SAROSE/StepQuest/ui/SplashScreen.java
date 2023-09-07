package com.SAROSE.StepQuest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import com.SAROSE.StepQuest.Database.DatabaseHandler;
import com.SAROSE.StepQuest.R;


public class SplashScreen extends FragmentActivity {

    // Creating database object
    DatabaseHandler helper=new DatabaseHandler(this);

    String databasecheck="not null";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        runNextScreen();
        askForFullScreen();
    }
    // Asking permission for full screen
    private void askForFullScreen()
    {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // if DB is null it will fresh start
    // if its not it will direct to the home page
    private void runNextScreen() {
        String pass=helper.serachifnotnull();
        if (databasecheck.equals(pass)){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreen.this, ActivityMain.class));
                    finish();
                }
            }, 1500);

        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreen.this, Welcome.class));
                    finish();
                }
            }, 1500);

        }
    }

}

