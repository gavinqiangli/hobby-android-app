package com.getirkit.example.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.getirkit.irkit.IRKit;
import com.getirkit.irkit.IRSignal;
import com.getirkit.irkit.net.IRAPICallback;
import com.getirkit.irkit.net.IRAPIError;
import com.getirkit.irkit.net.IRAPIResult;
import com.getirkit.irkit.net.IRInternetAPIService;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by eqiglii on 2015/10/30.
 */
public class AlarmReceiver extends BroadcastReceiver
{
    public static final String TAG = MainActivity.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent)
    {
        /*the solution was to use WakeLocker. That should be done (preferably as the 1st thing in the receiver),
        or the device will wake up when the alarm is received, but will fall asleep again before context.startActivity(newIntent); is called.
         */
        WakeLocker.acquire(context);

        // here you can start an activity or service depending on your need

        Log.d("alarm", "received");
        // first need to identify which file shall be read for that particular alarm
        String filename = intent.getExtras().getString("filename");

        // then read the persisted "selectedSignalPosition" from the file created by "onSelectSignalActionSchedule()"
        String readfromFile = ""; // for store the "selectedSignalPosition"
        try {
            InputStream inputStream = context.openFileInput(filename);

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
        int selectedSignalPosition = Integer.valueOf(readfromFile);

        // then execute "onSelectSignalActionSend()"
        if (selectedSignalPosition == -1) {
            return;
        }


        // Must initialize IRKit first
        IRKit irkit = IRKit.sharedInstance();

        // If apiKey argument is not provided, read it from AndroidManifest.xml
        String apiKey = irkit.getIRKitAPIKey();

        irkit.init(context);
        irkit.registerClient(apiKey);

        // call onSelectSignalActionSend() to send signal
        final IRSignal signal = IRKit.sharedInstance().signals.get(selectedSignalPosition);
        if (signal != null) {
            IRKit.sharedInstance().sendSignal(signal, new IRAPIResult() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError(IRAPIError error) {
                    String msg = "Error sending " + signal.getName() + ": " + error.message;
                    Log.e(TAG, msg);
                    //Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onTimeout() {
                    String msg = "Error sending " + signal.getName() + ": timeout";
                    Log.e(TAG, msg);
                    //Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }


            // Show the toast  like in above screen shot
        Toast.makeText(context, "Alarm Triggered and Signal Sent", Toast.LENGTH_LONG).show();

        // it would also be neat to call WakeLocker.release(); once your alarm has done its thing.
        WakeLocker.release();
    }

}
