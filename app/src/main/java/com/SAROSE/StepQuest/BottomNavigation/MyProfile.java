package com.SAROSE.StepQuest.BottomNavigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.SAROSE.StepQuest.Components.SensorListener;
import com.SAROSE.StepQuest.Database.DatabaseHandler;
import com.SAROSE.StepQuest.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;


public class MyProfile extends Fragment implements AdapterView.OnItemSelectedListener {
    public static final float DEFAULT_STEP_SIZE = Locale.getDefault() == Locale.US ? 2.5f : 75f;;
    public static final String DEFAULT_STEP_UNIT =Locale.getDefault() == Locale.US ? "ft" : "cm" ;
    public static final float DEFAULT_GOAL = 10000;
    private LinearLayout setgoal,export,importl;
    private TextView goal;
    public static int Position;
    private Spinner spinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view= inflater.inflate(R.layout.fragment_my_profile,null);
        setgoal=view.findViewById(R.id.setgoallayout);
       importl=view.findViewById(R.id.importlayout);
        export=view.findViewById(R.id.exportlayout);
        goal=view.findViewById(R.id.goaltext);

       setgoal.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               loadsetgoal();
           }
       });

         importl.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if(hasexternalpermissio()){
                     importCsv();
                 }
                 else if(Build.VERSION.SDK_INT>=23){
                     requestPermission(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
                 }
                 else{
                     Toast.makeText(getActivity(), ""+R.string.permission_external_storage, Toast.LENGTH_SHORT).show();
                 }
             }
         });
        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasexternalpermissio()){
                    exportCsv();
                }
                else if(Build.VERSION.SDK_INT>=23){
                    requestPermission(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
                }
                else{
                    Toast.makeText(getActivity(), ""+R.string.permission_external_storage, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }


    private void exportCsv() {
        // Check Storage available
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            final File f = new File(Environment.getExternalStorageDirectory(), "StepQuest.csv");

            // check already backuped or not
            if (f.exists()) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.file_already_exists)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            // Overwrite the file
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                writetoFile(f);
                            }

                        }) .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                }).create().show();
                return;

                //Write new file
            } else{
                writetoFile(f);
            }
        }
        else{
            // If the storage not available
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.error_external_storage_not_available)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    }).create().show();

        }

    }

    // Function of creating new File
    @SuppressLint("SuspiciousIndentation")
    private void writetoFile(final File f) {
        BufferedWriter out;
        try{
            f.createNewFile();
            out=new BufferedWriter(new FileWriter(f));
        } catch (IOException e) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.error_file, e.getMessage()))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    }).create().show();
            e.printStackTrace();
            return;
        }
        // Get Date and steps from DB and Write into CSV file
        DatabaseHandler db=DatabaseHandler.getInstance(getActivity());
        Cursor c= db.query(new String[]{"date","steps"},"date>0",null,null,null,"date",null);
        try{
            if(c!=null&& c.moveToFirst()){
                while(!c.isAfterLast()){
                    out.append(c.getString(0)).append(";")
                            .append(String.valueOf(Math.max(0,c.getInt(1)))).append("\n");
                    c.moveToNext();
                }
            }
           out.flush();
            out.close();
            // if DB doesnt have Data...
        } catch (Exception e) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.error_file, e.getMessage()))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    }).create().show();
            e.printStackTrace();
            return;

        }finally{
         if(c!=null) c.close();
            db.close();

        }
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.data_saved, f.getAbsolutePath()))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                }).create().show();
    }

    // Importing CSV File
    private void importCsv() {
        // Check Storage available
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File f = new File(Environment.getExternalStorageDirectory(), "StepQuest.csv");
            // check readability and existing
            if (!f.exists() || !f.canRead()) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.file_cant_read, f.getAbsolutePath()))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }

                        }).create().show();
                return;
            }

            // read each lines from the csv file and split each line by ";" and add date and steps to the database
            DatabaseHandler db = DatabaseHandler.getInstance(getActivity());
            String line;
            String[] data;
            int ignore = 0, inserted = 0, overwritten = 0;
            BufferedReader in;
            try {
                in = new BufferedReader(new FileReader(f));
                while ((line = in.readLine()) != null) {
                    data = line.split(";");
                    try {
                        if (db.insertDayFromBackup(Long.valueOf(data[0]),
                                Integer.valueOf(data[1]))) {
                            inserted++;
                        } else {
                            overwritten++;
                        }
                    } catch (Exception e) {
                        ignore++;
                    }}
                    in.close();
                }

            // Error If file cannot access
             catch (IOException e) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.error_file,e.getMessage()))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }

                        }).create().show();
                e.printStackTrace();
                return;

            } finally {
                db.close();
            }

            // Show Summary of the imported file
            String message = getString(R.string.entries_imported, inserted + overwritten);
            if (overwritten > 0)
                message += "\n\n" + getString(R.string.entries_overwritten, overwritten);

            if (ignore > 0)
                message += "\n\n" + getString(R.string.entries_ignored, ignore);
            new AlertDialog.Builder(getActivity())
                    .setMessage(message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    }).create().show();

        }
        else{
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.error_external_storage_not_available)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    }).create().show();

        }
    }
    // Permission for accessing storage
    private boolean hasexternalpermissio() {
             return getActivity().getPackageManager().checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,getActivity().getPackageName())==PackageManager.PERMISSION_GRANTED;
    }


    // set goal steps
    private void loadsetgoal() {
        final SharedPreferences prefs= getActivity().getSharedPreferences("StepQuest", Context.MODE_PRIVATE);
        final AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        final NumberPicker np = new NumberPicker(getActivity());
        np.setMinValue(1);
        np.setMaxValue(100000);
        np.setValue(prefs.getInt("goal",10000));
        builder.setView(np);
        builder.setTitle(R.string.set_goal);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            // Once we set the goal sensor will start count step
            @Override
            public void onClick(DialogInterface dialog, int which) {
                np.clearFocus();
                prefs.edit().putInt("goal",np.getValue()).commit();
                goal.setText(getString(R.string.goal_summary,np.getValue()));
                dialog.dismiss();
                getActivity().startService(new Intent(getActivity(), SensorListener.class).putExtra("updateNotificationState",true));
            }
        });
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });
        // LoadSpinner Menue Function
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                  Position=position;
                  spinner.setSelection(Position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // if we don't select anything Nothing will happen

    }

    // request permission depending on the API level
    public static void requestPermission(final Activity a, final String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            a.requestPermissions(permissions, 42);
        }
    }
}