package fi.jamk.signalstrength;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.LinearLayout;
import android.widget.TextView;


public class AboutDialogFragment extends DialogFragment{


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        LinearLayout layout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        params.setMargins(50,50,50,50); //substitute parameters for left, top, right, bottom
        layout.setLayoutParams(params);

        TextView messageTextView = new TextView(getActivity());
        messageTextView.setText(R.string.dialog_about);

        params.setMargins(50,50,50,50); //substitute parameters for left, top, right, bottom
        messageTextView.setLayoutParams(params);

        TextView linkTextView = new TextView(getActivity());

        linkTextView.setText(Html.fromHtml("<b>Täältä löytyy signaalien raakadatat: </b>" +
                "<a href=\"http://84.251.189.202:8080/sonera\">Raakadata</a> "));
        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());

        params.setMargins(50,50,50,50); //substitute parameters for left, top, right, bottom
        linkTextView.setLayoutParams(params);

        layout.addView(messageTextView);
        layout.addView(linkTextView);

        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setTitle(R.string.about);

        // Setting Positive "OK" Button
        alertDialogBuilder.setPositiveButton("OK", null);

        return alertDialogBuilder.create();
    }
}
