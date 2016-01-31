package com.getirkit.example.activity;

/**
 * Created by eqiglii on 2016/1/27.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.getirkit.example.R;
import com.getirkit.example.adapter.SignalListAdapter;
import com.getirkit.example.fragment.SelectScheduleActionDialogFragment;
import com.getirkit.irkit.IRKit;

public class ScheduleActivity extends AppCompatActivity
    implements SelectScheduleActionDialogFragment.SelectScheduleActionDialogFragmentListener {

    public static final String TAG = ScheduleActivity.class.getSimpleName();


    TimePicker myTimePicker;
    Button buttonstartSetDialog;
    TextView textAlarmPrompt;
    ListView scheduleListView;

    TimePickerDialog timePickerDialog;

    private int selectedSignalPosition = -1;
    private String signalName = "";
    private String click_filename = "";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle args = intent.getExtras();

        // Use savedInstanceState if it exists
        if (savedInstanceState != null) {
            args = savedInstanceState;
        }

        setContentView(R.layout.activity_schedule);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (args == null) {
            throw new IllegalArgumentException("extras are not passed via Intent");
        }
        selectedSignalPosition = args.getInt("selectedSignalPosition");
        signalName = args.getString("signalName");
        if (signalName == "" || selectedSignalPosition == -1) {
            throw new IllegalArgumentException("signal attribute is not passed via Intent");
        }

        textAlarmPrompt = (TextView)findViewById(R.id.alarmprompt);

        buttonstartSetDialog = (Button)findViewById(R.id.startSetDialog);
        buttonstartSetDialog.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                textAlarmPrompt.setText("");
                openTimePickerDialog(true);

            }
        });

        displaySchedule();

    }


    private void openTimePickerDialog(boolean is24r){
        Calendar calendar = Calendar.getInstance();

        timePickerDialog = new TimePickerDialog(
                ScheduleActivity.this,
                onTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                is24r);
        timePickerDialog.setTitle("Set Alarm Schedule");

        timePickerDialog.show();

    }

    OnTimeSetListener onTimeSetListener
            = new OnTimeSetListener(){

        @Override
        public void onTimeSet(TimePicker view, int hourofDay, int minute) {

            addSchedule(hourofDay, minute);
        };

    };

    public void addSchedule(int hour, int minute) {

        if (selectedSignalPosition == -1) {
            return;
        }

        // 1. Wake up the device to fire the alarm at approximately 6:00 a.m., and repeat once a day at the same time:

        // e.g. Set the alarm to start at approximately 6:30 a.m.
        int scheduleTime = hour*100 + minute; // scheduled time must be unique in order to avoid multiple alarm conflicts
        Calendar calNow = Calendar.getInstance();
        Calendar calSet = (Calendar) calNow.clone();

        calSet.set(Calendar.HOUR_OF_DAY, hour);
        calSet.set(Calendar.MINUTE, minute);
        calSet.set(Calendar.SECOND, 0);
        calSet.set(Calendar.MILLISECOND, 0);

        if(calSet.compareTo(calNow) <= 0){
            //Today Set time passed, count to tomorrow
            calSet.add(Calendar.DATE, 1);
        }

        // Use AlarmManager for managing alarms
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        int alarmId = (selectedSignalPosition + 1) * scheduleTime; // identity of the alarm, must be unique

        // the alarm shall identify a file that is going to persist the "selectedSignalPosition" value, for that particular alarm
        String filename = signalName + "," + String.valueOf(scheduleTime); // filename is "signalName,630"
        intent.putExtra("filename", filename);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, alarmId, intent, 0);
        alarmMgr.setRepeating (AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        // 2. Persist the "selectedSignalPosition" into the alarm-identified-specific file
        String string = String.valueOf(selectedSignalPosition); // filecontent is "selectedSignalPosition"
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        textAlarmPrompt.setText(
                "\n\n***\n"
                        + "Alarm is set@ " + calSet.getTime() + "\n"
                        + "***\n");

        // refresh the schedule listview
        displaySchedule();
    }

    public void displaySchedule() {

        // Read the alarm list from the private file
        final String[] filenames = fileList();

        // Display the view list
        scheduleListView = (ListView) findViewById(R.id.schedule__listview);

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, filenames);

        scheduleListView.setAdapter(adapter);
        scheduleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemValue = (String) scheduleListView.getItemAtPosition(position);
                click_filename = itemValue;

                SelectScheduleActionDialogFragment dialog = new SelectScheduleActionDialogFragment();
                dialog.show(getSupportFragmentManager(), "SelectScheduleActionDialogFragment");
            }
        });
    }

    @Override
    public void onSelectScheduleActionDelete() {

        if (click_filename.isEmpty()){
            Log.e(TAG, "File name is empty, nothing to be deleted");
            return;
        }

        // first split the filename and get the scheduleTime
        int scheduleTime = 0;
        if (click_filename.contains(",")) {
            String[] parts = click_filename.split(","); // filename is "signalName,630"
            String part1 = parts[0]; // signalName
            String part2 = parts[1]; // 630
            scheduleTime = Integer.valueOf(part2);
        } else {
            try {
                scheduleTime = Integer.valueOf(click_filename); // // filename is "630"
            }
            catch(NumberFormatException e) {
                System.out.println("parse value is not valid : " + e);
            }
        }


        // then read the persisted "selectedSignalPosition" from the file created by "onSelectSignalActionSchedule()"
        String readfromFile = ""; // for store the "selectedSignalPosition"
        try {
            InputStream inputStream = openFileInput(click_filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                readfromFile = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }

        // pass the value to "selectedSignalPosition"
        int signalPosition = Integer.valueOf(readfromFile);

        // constract the alarmId
        int alarmId = (signalPosition + 1) * scheduleTime; // identity of the alarm, must be unique

        // Use AlarmManager for managing alarms
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, alarmId, intent, 0);
        // If the alarm has been set, cancel it.
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }

        // Also delete the file persisted for that scheduler
        File dir = getFilesDir();
        File file = new File(dir, click_filename);
        boolean deleted = file.delete();
        Log.v("log_tag", "file deleted: " + deleted);

        // refresh the schedule listview
        displaySchedule();
    }

    private void closeAndFinish() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.schedule_activity_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("back_to_home", true);
            setResult(RESULT_CANCELED, resultIntent);

            finish();
            return true;
        } else if (id == R.id.activity_schedule__action_close) {
            Intent resultIntent = new Intent();
            Bundle args = new Bundle();
            args.putString("action", "close");
            resultIntent.putExtras(args);
            setResult(RESULT_OK, resultIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

        }
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }
}
