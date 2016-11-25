package fi.jamk.signalstrength;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;


public class TrackingDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class to create a Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_tracking_title)
                .setMessage(R.string.dialog_tracking_title)
                .setPositiveButton(R.string.dialog_ok, null);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
