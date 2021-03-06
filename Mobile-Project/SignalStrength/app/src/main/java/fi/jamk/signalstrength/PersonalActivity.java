package fi.jamk.signalstrength;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;


public class PersonalActivity extends AppCompatActivity {

    //variables
    private TelephonyManager mTelephonyManager;
    private MyPhoneStateListener mPhoneStatelistener;

    private String operatorName;

    protected int mGsmSignalStrength = 0;
    protected int mCdmaSignalStrength = 0;
    protected int mEvdoSignalStrength = 0;

    private TextView mPersonalInfoTextView;
    private TextView mOperatorNameTextView;
    private TextView mGsmTextView;
    private TextView mCdmaTextView;
    private TextView mEvdoTextView;
    private TextView mGsmTextViewColor;
    private TextView mCdmaTextViewColor;
    private TextView mEvdoTextViewColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        //initializing toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //setting back button visible
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //listener for clicking back button
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // get TextViews
        mPersonalInfoTextView = (TextView) findViewById(R.id.personalInfoTextView);
        mOperatorNameTextView = (TextView) findViewById((R.id.operatorNameTextView));
        mGsmTextView = (TextView)findViewById(R.id.gsmTextView);
        mCdmaTextView = (TextView)findViewById(R.id.cdmaTextView);
        mEvdoTextView = (TextView)findViewById(R.id.evdoTextView);
        mGsmTextViewColor = (TextView) findViewById(R.id.gsmTextViewColor);
        mCdmaTextViewColor = (TextView) findViewById(R.id.cdmaTextViewColor);
        mEvdoTextViewColor = (TextView) findViewById(R.id.evdoTextViewColor);

        mPhoneStatelistener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        operatorName = mTelephonyManager.getNetworkOperatorName();
        mOperatorNameTextView.setText("Operaattori: "+ operatorName);

        mTelephonyManager.listen(mPhoneStatelistener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        mPersonalInfoTextView.setPaintFlags(mPersonalInfoTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    //function to listen phone's signal strengths
    class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            mGsmSignalStrength = signalStrength.getGsmSignalStrength();
            mGsmSignalStrength = (2 * mGsmSignalStrength) - 113; // -> dBm

            mCdmaSignalStrength = signalStrength.getCdmaDbm();

            mEvdoSignalStrength = signalStrength.getGsmBitErrorRate();

            mGsmTextView.setText("GSM " + Integer.toString(mGsmSignalStrength) + " dBm");
            mCdmaTextView.setText("CDMA " + Integer.toString(mCdmaSignalStrength) + " dBm");
            mEvdoTextView.setText("EVDO " + Integer.toString(mEvdoSignalStrength) + " dBm");

            if (mGsmSignalStrength > -65) {
                mGsmTextViewColor.setText("PERFECT");mGsmTextViewColor.setBackgroundResource(R.color.green);
            }

            if (mGsmSignalStrength >= -79 && mGsmSignalStrength <= -65) {
                mGsmTextViewColor.setText("GOOD");mGsmTextViewColor.setBackgroundResource(R.color.yellow);
            }

            if (mGsmSignalStrength >= -89 && mGsmSignalStrength <= -80) {
                mGsmTextViewColor.setText("FAIR");mGsmTextViewColor.setBackgroundResource(R.color.orange);
            }

            if (mGsmSignalStrength >= -99 && mGsmSignalStrength <= -90) {
                mGsmTextViewColor.setText("POOR");mGsmTextViewColor.setBackgroundResource(R.color.magenta);
            }

            if (mGsmSignalStrength <= -100) {
                mGsmTextViewColor.setText("NO SIGNAL");mGsmTextViewColor.setBackgroundResource(R.color.red);
            }

            if (mCdmaSignalStrength > - 65) {
                mCdmaTextViewColor.setText("PERFECT");mCdmaTextViewColor.setBackgroundResource(R.color.green);
            }

            if (mCdmaSignalStrength >= -79 && mCdmaSignalStrength <= -65) {
                mCdmaTextViewColor.setText("GOOD");mCdmaTextViewColor.setBackgroundResource(R.color.yellow);
            }

            if (mCdmaSignalStrength >= -89 && mCdmaSignalStrength <= -80) {
                mCdmaTextViewColor.setText("FAIR");mCdmaTextViewColor.setBackgroundResource(R.color.orange);
            }

            if (mCdmaSignalStrength >= -99 && mCdmaSignalStrength <= -90) {
                mCdmaTextViewColor.setText("POOR");mCdmaTextViewColor.setBackgroundResource(R.color.magenta);
            }

            if (mGsmSignalStrength <= -100 || mCdmaSignalStrength <= -100) {
                mCdmaTextViewColor.setText("NO SIGNAL");mCdmaTextViewColor.setBackgroundResource(R.color.red);
            }
        }
    }
}
