package fi.jamk.signalstrength;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;


public class PersonalActivity extends Activity{

    private TelephonyManager mTelephonyManager;
    private MyPhoneStateListener mPhoneStatelistener;

    private String operatorName;

    private double lat = 0;
    private double lon = 0;

    protected int mGsmSignalStrength = 0;
    protected int mCdmaSignalStrength = 0;
    protected int mEvdoSignalStrength = 0;

    private TextView mOperatorNameTextView;
    private TextView mGsmTextView;
    private TextView mCdmaTextView;
    private TextView mEvdoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);


        TextView text = new TextView(this);
        text.setText("Signaalin vahvuus");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        text.setTextAppearance(this, android.R.style.TextAppearance_Material_Widget_ActionBar_Title_Inverse);
        toolbar.addView(text);

        // get TextViews
        mOperatorNameTextView = (TextView) findViewById((R.id.operatorNameTextView));
        mGsmTextView = (TextView)findViewById(R.id.gsmTextView);
        mCdmaTextView = (TextView)findViewById(R.id.cdmaTextView);
        mEvdoTextView = (TextView)findViewById(R.id.evdoTextView);

        mPhoneStatelistener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        operatorName = mTelephonyManager.getNetworkOperatorName();
        mOperatorNameTextView.setText("Operaattori: "+operatorName);

        mTelephonyManager.listen(mPhoneStatelistener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    }

    class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            mGsmSignalStrength = signalStrength.getGsmSignalStrength();
            mGsmSignalStrength = (2 * mGsmSignalStrength) - 113; // -> dBm

            mCdmaSignalStrength = signalStrength.getCdmaDbm();

            mEvdoSignalStrength = signalStrength.getGsmBitErrorRate();

            mGsmTextView.setText(Integer.toString(mGsmSignalStrength));
            mCdmaTextView.setText(Integer.toString(mCdmaSignalStrength));
            mEvdoTextView.setText(Integer.toString(mEvdoSignalStrength));
        }
    }
}
