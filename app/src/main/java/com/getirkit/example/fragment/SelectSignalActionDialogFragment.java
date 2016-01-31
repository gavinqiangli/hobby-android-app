package com.getirkit.example.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.getirkit.example.R;

import java.util.Calendar;

/**
 * Dialog for selecting what action to be done for the signal.
 */
public class SelectSignalActionDialogFragment extends DialogFragment {
    public static final String TAG = SelectSignalActionDialogFragment.class.getSimpleName();

    public interface SelectSignalActionDialogFragmentListener {
        void onSelectSignalActionSend();
        void onSelectSignalActionSchedule(); // added by eqiglii 2015-11-05
        void onSelectSignalActionEdit();
    }

    private SelectSignalActionDialogFragmentListener selectSignalActionDialogFragmentListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            selectSignalActionDialogFragmentListener = (SelectSignalActionDialogFragmentListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement SelectSignalActionDialogFragmentListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final CharSequence[] items = {
                getString(R.string.send),
                getString(R.string.edit),
                getString(R.string.cancel),
                // added by eqiglii 2015-11-05
                getString(R.string.schedule),
                // ended by eqiglii 2015-11-05
        };

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: { // Send
                        dialog.dismiss();
                        if (selectSignalActionDialogFragmentListener != null) {
                            selectSignalActionDialogFragmentListener.onSelectSignalActionSend();
                        }
                        break;
                    }
                    case 1: { // Edit
                        dialog.dismiss();
                        if (selectSignalActionDialogFragmentListener != null) {
                            selectSignalActionDialogFragmentListener.onSelectSignalActionEdit();
                        }
                        break;
                    }
                    case 2: { // Cancel
                        dialog.dismiss();
                        break;
                    }
                    // added by eqiglii 2015-11-05
                    case 3: { // Schedule
                        dialog.dismiss();
                        if (selectSignalActionDialogFragmentListener != null) {
                            selectSignalActionDialogFragmentListener.onSelectSignalActionSchedule();
                        }
                        break;
                    }

                }
            }
        });

        return builder.create();
    }
}
