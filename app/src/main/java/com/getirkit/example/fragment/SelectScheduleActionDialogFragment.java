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
 * Dialog for selecting what action to be done for the schedule.
 */
public class SelectScheduleActionDialogFragment extends DialogFragment {
    public static final String TAG = SelectScheduleActionDialogFragment.class.getSimpleName();

    public interface SelectScheduleActionDialogFragmentListener {
        void onSelectScheduleActionDelete();
    }

    private SelectScheduleActionDialogFragmentListener selectScheduleActionDialogFragmentListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            selectScheduleActionDialogFragmentListener = (SelectScheduleActionDialogFragmentListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement SelectScheduleActionDialogFragmentListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final CharSequence[] items = {
                getString(R.string.delete),
                getString(R.string.cancel),
        };

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: { // Delete
                        dialog.dismiss();
                        if (selectScheduleActionDialogFragmentListener != null) {
                            selectScheduleActionDialogFragmentListener.onSelectScheduleActionDelete();
                        }
                        break;
                    }
                    case 1: { // Cancel
                        dialog.dismiss();
                        break;
                    }
                }
            }
        });

        return builder.create();
    }
}
