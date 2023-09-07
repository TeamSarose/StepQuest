package com.SAROSE.StepQuest.BottomNavigation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Bundle;

import androidx.fragment.app.Fragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.SAROSE.StepQuest.Components.SensorListener;
import com.SAROSE.StepQuest.R;
import com.SAROSE.StepQuest.ui.TotalStepsHome;
import com.SAROSE.StepQuest.util.Util;
import com.SAROSE.StepQuest.Database.DatabaseHandler;


import org.eazegraph.lib.charts.PieChart;

import org.eazegraph.lib.models.PieModel;

import java.text.NumberFormat;

import java.util.Locale;

public class FragmentHome extends Fragment  implements SensorEventListener{

public static NumberFormat formatter=NumberFormat.getInstance(Locale.getDefault());
private ImageView levels;
private TextView stepsView,totalView,averageView,calories,stepsleft;
private PieModel sliceGoal,sliceCurrent;
 private PieChart pg;
 public static int totalstepsgoal=0;
 private ProgressBar progressBar;
 private int todayoffset, total_start, goal,since_boot, totaldays, goalreach;
    private boolean showSteps = true;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

            getActivity().startService(new Intent(getActivity(), SensorListener.class));  // Starting Sensor service
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,  final  ViewGroup container,
                            final  Bundle savedInstanceState) {

        final View view= inflater.inflate(R.layout.fragment_home,null); // View the Home fragment

       // Assigning variables
        levels=view.findViewById(R.id.levels);
        stepsView=view.findViewById(R.id.stepsinpiechart);
       totalView=view.findViewById(R.id.total);
       averageView=view.findViewById(R.id.average);
       progressBar=view.findViewById(R.id.progressBar);
       stepsleft=view.findViewById(R.id.stepsleft);
      calories=view.findViewById(R.id.calories);
      pg=view.findViewById(R.id.graph);

      // Load Total steps fragment
        levels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadtotalstepshomeFragment();
            }
        });

        // Onclick For PieChart
      setPiechart();
      pg.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              showSteps = !showSteps;
              stepsDistanceChanges();
          }
      });

        return view;
    }

    // Resume DB
    @Override
    public void onResume() {
        super.onResume();
        DatabaseHandler db = DatabaseHandler.getInstance(getActivity());


        // read todays offset
        todayoffset = db.getSteps(Util.getToday());

        // Save data temporary
        SharedPreferences prefs =
                getActivity().getSharedPreferences("StepQuest", Context.MODE_PRIVATE);

        // retrieves the existing step count
        goal = prefs.getInt("goal", (int) MyProfile.DEFAULT_GOAL);
        since_boot = db.getCurrentSteps();
        int pauseDifference = since_boot - prefs.getInt("pauseCount", since_boot);

        // register a sensorlistener to live update the UI if a step is taken
        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        // Checking sensor available or not
        if (sensor == null) {
            new AlertDialog.Builder(getActivity()).setTitle(R.string.no_sensor)
                    .setMessage(R.string.no_sensor_explain)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(final DialogInterface dialogInterface) {
                            getActivity().finish();
                        }
                    }).setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).create().show();
        } else {
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
        }

        since_boot -= pauseDifference;

        total_start = db.getTotalWithoutToday();
        totaldays = db.getDays();

        db.close();

        stepsDistanceChanges();
    }

    // Update steps distance according to the user preference
    private void stepsDistanceChanges() {
        if(showSteps){
            ((TextView) getView().findViewById(R.id.unit)).setText(getString(R.string.steps));

        }else{
            String unit=getActivity().getSharedPreferences("StepQuest",Context.MODE_PRIVATE)
                    .getString("stepsize_unit",MyProfile.DEFAULT_STEP_UNIT);
            if(unit.equals("cm")){
                unit="km";
            }else{
                unit="mile";
            }
            ((TextView) getView().findViewById(R.id.unit)).setText(unit);
        }
         updatePie();

    }

    // Pause the sensor and update the current Steps While Minimize
    @Override
    public void onPause() {
        super.onPause();
        try{
            SensorManager sm=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);


        } catch (Exception e) {

            e.printStackTrace();
        }
        DatabaseHandler db= DatabaseHandler.getInstance(getActivity());
        db.saveCurrentSteps(since_boot);
        db.close();
    }

    // Update pie view based on today's steps value
    private void updatePie() {

        int steps_today=Math.max(todayoffset+since_boot,0);
        sliceCurrent.setValue(steps_today);
        if(goal-steps_today>0){
            if(pg.getData().size()==1){
                pg.addPieSlice(sliceGoal);
            }
            sliceGoal.setValue(goal-steps_today);}
            else{
                pg.clearChart();
                pg.addPieSlice(sliceCurrent);
            }
            pg.update();

            // Kcal Calculation
            if(showSteps){
                stepsView.setText(formatter.format(steps_today));
                double kcal=steps_today*0.04;
                calories.setText(formatter.format(kcal));
                totalView.setText(formatter.format(total_start+steps_today));
                averageView.setText(formatter.format((total_start+steps_today)/totaldays));
                totalstepsgoal=total_start+steps_today;

                // Update Level Icons
                if(totalstepsgoal<3000){
                    levels.setBackgroundColor(Color.GRAY);
                    levels.setImageResource(R.drawable.editthree);
                    goalreach=3000;
                }
                if(totalstepsgoal>=3000 && totalstepsgoal<7000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.editthree);
                    goalreach=7000;
                }
                if(totalstepsgoal>=7000 && totalstepsgoal<10000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.editseven);
                    goalreach=10000;
                }
                if(totalstepsgoal>=10000 && totalstepsgoal<14000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.edit10);
                    goalreach=14000;
                }
                if(totalstepsgoal>=14000 && totalstepsgoal<20000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.editfort);
                    goalreach=20000;
                }
                if(totalstepsgoal>=20000 && totalstepsgoal<30000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.edittwenty);
                    goalreach=30000;
                }
                if(totalstepsgoal>=30000 && totalstepsgoal<40000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.editthirty);
                    goalreach=40000;
                }
                if(totalstepsgoal>=40000 && totalstepsgoal<60000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.editforty);
                    goalreach=60000;
                }
                if(totalstepsgoal>=60000 && totalstepsgoal<70000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.editforty);
                    goalreach=700000;
                }

                // Progress bar Calculation
                float set=((totalstepsgoal*100)/goalreach);
                int b=(int)Math.round(set);
                stepsleft.setText(formatter.format(goalreach-totalstepsgoal));
                progressBar.setProgress(b);
            }

            // View Distance on KM
            else{
                SharedPreferences prefs=getActivity().getSharedPreferences("StepQuest",Context.MODE_PRIVATE);
                float stepsize=prefs.getFloat("stepsize_value",MyProfile.DEFAULT_STEP_SIZE);
                float distance_today=steps_today*stepsize;
                float distance_total=(steps_today+total_start)*stepsize;
                if(prefs.getString("stepsize_unit",MyProfile.DEFAULT_STEP_UNIT).equals("cm"))
                {
                    distance_today/=100000;
                    distance_total/=100000;

                }else{
                    distance_today /= 5280;
                    distance_total /= 5280;

                }
                stepsView.setText(formatter.format(distance_today));
                totalView.setText(formatter.format(distance_total));
                averageView.setText(formatter.format(distance_total / totaldays));
                totalstepsgoal=total_start+steps_today;
                if(totalstepsgoal<3000){
                    levels.setBackgroundColor(Color.GRAY);
                    levels.setImageResource(R.drawable.editthree);
                    goalreach=3000;
                }
                if(totalstepsgoal>=3000 && totalstepsgoal<7000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.editthree);
                    goalreach=7000;
                }
                if(totalstepsgoal>=7000 && totalstepsgoal<10000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.editseven);
                    goalreach=10000;
                }
                if(totalstepsgoal>=10000 && totalstepsgoal<14000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.edit10);
                    goalreach=14000;
                }
                if(totalstepsgoal>=14000 && totalstepsgoal<20000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.editfort);
                    goalreach=20000;
                }
                if(totalstepsgoal>=20000 && totalstepsgoal<30000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.edittwenty);
                    goalreach=30000;
                }
                if(totalstepsgoal>=30000 && totalstepsgoal<40000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.editthirty);
                    goalreach=40000;
                }
                if(totalstepsgoal>=40000 && totalstepsgoal<60000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.editforty);
                    goalreach=60000;
                }
                if(totalstepsgoal>=60000 && totalstepsgoal<70000){
                    levels.setBackgroundColor(Color.BLUE);
                    levels.setImageResource(R.drawable.editforty);
                    goalreach=700000;
                }
                float set=((totalstepsgoal*100)/goalreach);
                int b=(int)Math.round(set);
                stepsleft.setText(formatter.format(goalreach-totalstepsgoal));
                progressBar.setProgress(b);
            }



    }

    // SetPieChart View/Animation
    private void setPiechart() {
        sliceCurrent=new PieModel(0, Color.parseColor("#69F0AE"));
        pg.addPieSlice(sliceCurrent);
        sliceGoal=new PieModel(MyProfile.DEFAULT_GOAL, Color.parseColor("#40C4FF"));
        pg.addPieSlice(sliceGoal);
        pg.setDrawValueInPie(false);
        pg.setUsePieRotation(true);
        pg.startAnimation();
    }

    // Load Total Step Fragment In to the Home Page
    private void loadtotalstepshomeFragment() {
        Fragment newFragment=new TotalStepsHome();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment,newFragment)
                .commit();
    }

    // Update Steps Count and pie chart on sensor change
    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.values[0]>Integer.MAX_VALUE || event.values[0]==0){
            todayoffset=-(int) event.values[0];
            DatabaseHandler db= DatabaseHandler.getInstance(getActivity());
            db.insertNewDay(Util.getToday(),(int)event.values[0]);
            db.close();

        }
        since_boot=(int)event.values[0];
        updatePie();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
     //will not change
    }
}