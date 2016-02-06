package com.getirkit.example.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

/**
 * Created by eqiglii on 2016/2/6.
 * Start an Alarm When the Device Boots
 By default, all alarms are canceled when a device shuts down.
 To prevent this from happening, you can design your application to automatically restart a repeating alarm if the user reboots the device.
 This ensures that the AlarmManager will continue doing its task without the user needing to manually restart the alarm.
 */
public class BootReceiver extends BroadcastReceiver {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            Log.d("boot alarm", "received");

            // Set the alarm here.
            // Read the alarm list from the private file
            String[] filenames = context.fileList();

            for (int i = 0; i < filenames.length ; i++) {
                recreateAlarmFromFile(context, filenames[i]);
            }

            // Show the toast  like in above screen shot
            Toast.makeText(context, "Boot Alarm Triggered and Signal Sent", Toast.LENGTH_LONG).show();


        }
    }


    public void recreateAlarmFromFile(Context context, String inputfilename) {

        // 1. read the file
        if (inputfilename.isEmpty()){
            Log.e(TAG, "File name is empty, nothing to be recreated");
            return;
        }

        // first split the filename and get the scheduleTime
        int scheduleTime = 0;
        if (inputfilename.contains(",")) {
            String[] parts = inputfilename.split(","); // filename is "signalName,630"
            String part1 = parts[0]; // signalName
            String part2 = parts[1]; // 630
            try {
                scheduleTime = Integer.valueOf(part2);
            }
            catch(NumberFormatException e) {
                System.out.println("parse value is not valid : " + e);
                return;
            }
        } else {
            try {
                scheduleTime = Integer.valueOf(inputfilename); // // filename is "630"
            }
            catch(NumberFormatException e) {
                System.out.println("parse value is not valid : " + e);
                return;
            }
        }

        // then read the persisted "selectedSignalPosition" from the file created by "onSelectSignalActionSchedule()"
        String readfromFile = ""; // for store the "selectedSignalPosition"
        try {
            InputStream inputStream = context.openFileInput(inputfilename);

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
        int signalPosition = -1;
        try {
            signalPosition = Integer.valueOf(readfromFile);
        }
        catch(NumberFormatException e) {
            System.out.println("parse value is not valid : " + e);
            return;
        }

        // 2. add the schedule
        if (signalPosition == -1) {
            return;
        }

        // e.g. Set the alarm to start at approximately 6:30 a.m.
        Calendar calNow = Calendar.getInstance();
        Calendar calSet = (Calendar) calNow.clone();

        int hour = scheduleTime / 100;  // scheduleTime is 630, 1630, 1600, 600
        int minute = scheduleTime - hour * 100;
        calSet.set(Calendar.HOUR_OF_DAY, hour);
        calSet.set(Calendar.MINUTE, minute);
        calSet.set(Calendar.SECOND, 0);
        calSet.set(Calendar.MILLISECOND, 0);

        if(calSet.compareTo(calNow) <= 0){
            //Today Set time passed, count to tomorrow
            calSet.add(Calendar.DATE, 1);
        }

        // Use AlarmManager for managing alarms
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        int alarmId = (signalPosition + 1) * scheduleTime; // identity of the alarm, must be unique

        // the alarm shall identify a file that is going to persist the "selectedSignalPosition" value, for that particular alarm
        intent.putExtra("filename", inputfilename);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, alarmId, intent, 0);
        alarmMgr.setRepeating (AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

    }

}