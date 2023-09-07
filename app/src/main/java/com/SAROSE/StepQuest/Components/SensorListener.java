
package com.SAROSE.StepQuest.Components;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import com.SAROSE.StepQuest.Database.DatabaseHandler;
import com.SAROSE.StepQuest.BuildConfig;
import com.SAROSE.StepQuest.Widget.WidgetUpdateService;
import com.SAROSE.StepQuest.util.Util;


public class SensorListener extends Service implements SensorEventListener {

    public final static int NOTIFICATION_ID = 1;
    private final static long MICROSECONDS_IN_ONE_MINUTE = 60000000;
    private final static long SAVE_OFFSET_TIME = AlarmManager.INTERVAL_HOUR;
    private final static int SAVE_OFFSET_STEPS = 500;

    private static int steps;
    private static int lastSaveSteps;
    private static long lastSaveTime;


    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy) {
        // nobody knows what happens here step value might magically decrease
        // when this method is called...
    }

    // The code checks if the first value in the event.values array  is greater than the maximum value of an integer
    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (event.values[0] > Integer.MAX_VALUE) {

            return;
        } else {
            steps = (int) event.values[0];
            updateIfNecessary();
        }
    }


    // Steps wil be save every 100 steps or Every 1 hour
    private boolean updateIfNecessary() {
        if (steps > lastSaveSteps + SAVE_OFFSET_STEPS ||
                (steps > 0 && System.currentTimeMillis() > lastSaveTime + SAVE_OFFSET_TIME)) {

            DatabaseHandler db = DatabaseHandler.getInstance(this);
            if (db.getSteps(Util.getToday()) == Integer.MIN_VALUE) {
                int pauseDifference = steps -
                        getSharedPreferences("StepQuest", Context.MODE_PRIVATE)
                                .getInt("pauseCount", steps);
                db.insertNewDay(Util.getToday(), steps - pauseDifference);
                if (pauseDifference > 0) {
                    // update pauseCount for the new day
                    getSharedPreferences("StepQuest", Context.MODE_PRIVATE).edit()
                            .putInt("pauseCount", steps).commit();
                }
            }
            db.saveCurrentSteps(steps);
            db.close();
            lastSaveSteps = steps;
            lastSaveTime = System.currentTimeMillis();
            WidgetUpdateService.enqueueUpdate(this);
            return true;
        } else {
            return false;
        }
    }


    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    // Register sensor on start
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        reRegisterSensor();

        // restart service every hour to save the current step count
        long nextUpdate = Math.min(Util.getTomorrow(),
                System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        // Restart service in 500 ms
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                .set(AlarmManager.RTC, System.currentTimeMillis() + 500, PendingIntent
                        .getService(this, 3, new Intent(this, SensorListener.class), 0));
    }

    // Check if Sensor is Working every 500ms
    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Registering Step Count Sensor
    private void reRegisterSensor() {
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        try {
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (BuildConfig.DEBUG) {
            if (sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size() < 1) return;

        }

        // enable batching with delay of max 5 min
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_NORMAL, (int) (5 * MICROSECONDS_IN_ONE_MINUTE));
    }
}
