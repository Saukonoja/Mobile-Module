package fi.jamk.signalstrength;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;


public class AboutDialogFragment extends DialogFragment{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // We use the Builder class to create a Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.about)
                .setMessage(R.string.dialog_about)
                .setPositiveButton(R.string.dialog_ok, null);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
