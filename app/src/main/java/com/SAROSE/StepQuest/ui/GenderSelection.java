package com.SAROSE.StepQuest.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;

import com.SAROSE.StepQuest.R;
import com.SAROSE.StepQuest.Database.DatabaseHandler;

public class GenderSelection extends FragmentActivity implements View.OnClickListener{
    private CardView femalecardview,malecardview;
    private TextView textfemale,textmale;
    private Button nextbtn;
    public static String genderclicked,gendernotclicked;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.genderselection);
        initiateview();
        clickListner();
    }
    // calling Buttons, card views
    private void initiateview(){
        femalecardview=findViewById(R.id.cardviewfemale);
    malecardview=findViewById(R.id.cardviewmale);
    textfemale=findViewById(R.id.textviewfemale);
    textmale=findViewById(R.id.textviewmale);
    nextbtn=findViewById(R.id.nextbutton);
    nextbtn.setVisibility(View.GONE);

}
    private void clickListner(){
        femalecardview.setOnClickListener(this);
        malecardview.setOnClickListener(this);
        nextbtn.setOnClickListener(this);

}
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.cardviewfemale:
                femaleclick();
                break;
            case R.id.cardviewmale:
                maleclick();
                break;
            case R.id.nextbutton:
               navigatetonextactivity();
                break;

        }

    }

    // Navigate to login/signup page
    private void navigatetonextactivity() {
        DatabaseHandler databaseHandler= new DatabaseHandler(getApplicationContext());
       Intent intent= new Intent(this,LogSign.class);
       startActivity(intent);
    }

    // Color changing when gender selection
    private void femaleclick() {
        femalecardview.setBackgroundResource(R.drawable.imagecardfemale);
        textfemale.setTextColor(Color.DKGRAY);
        genderclicked="Female";
        gendernotclicked="Male";
        malecardview.setBackgroundResource(R.drawable.imagecard);
        textmale.setTextColor(Color.TRANSPARENT);
        nextbtn.setVisibility(View.VISIBLE);
    }
    private void maleclick() {
        malecardview.setBackgroundResource(R.drawable.imagecardmale);
        textmale.setTextColor(Color.DKGRAY);
        genderclicked="Male";
        gendernotclicked="Female";
        femalecardview.setBackgroundResource(R.drawable.imagecard);
        textfemale.setTextColor(Color.TRANSPARENT);
        nextbtn.setVisibility(View.VISIBLE);
    }
}
