package fi.jamk.kaademo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;


public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private Context mContext;
    private KaaActivity kaa;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private final int REQUEST_LOCATION = 1;

    private TelephonyManager mTelephonyManager;
    private MyPhoneStateListener mPhoneStatelistener;

    private String operatorName;

    private double lat = 0;
    private double lon = 0;

    protected int mGsmSignalStrength = 0;
    protected int mCdmaSignalStrength = 0;
    protected int mEvdoSignalStrength = 0;

    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mOperatorNameTextView;
    private TextView mGsmTextView;
    private TextView mCdmaTextView;
    private TextView mEvdoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        kaa = new KaaActivity();
        mContext = this;

        kaa.kaaStart(mContext);

        // get TextViews
        mLatitudeTextView = (TextView) findViewById(R.id.latitudeTextView);
        mLongitudeTextView = (TextView) findViewById(R.id.longitudeTextView);
        mOperatorNameTextView = (TextView) findViewById((R.id.operatorNameTextView));
        mGsmTextView = (TextView)findViewById(R.id.gsmTextView);
        mCdmaTextView = (TextView)findViewById(R.id.cdmaTextView);
        mEvdoTextView = (TextView)findViewById(R.id.evdoTextView);

        // build Google Play Services Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mPhoneStatelistener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        operatorName = mTelephonyManager.getNetworkOperatorName();
        mOperatorNameTextView.setText("Operator name: "+operatorName);

        mTelephonyManager.listen(mPhoneStatelistener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    }

    @Override
    public void onConnected(Bundle bundle) {
        // Connected to Google Play services.
        checkPermissions();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection has been interrupted.
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // This callback is important for handling errors.
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();

        // show location in TextViews
        mLatitudeTextView.setText("Latitude: " + lat);
        mLongitudeTextView.setText("Longitude: " + lon);

        kaa.sendLog(operatorName, lat, lon, mGsmSignalStrength, mCdmaSignalStrength, mEvdoSignalStrength);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void checkPermissions() {
        // check permission
        int hasLocationPermission = ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        // permission is not granted yet
        if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
            // ask it -> a dialog will be opened
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            // permission is already granted, start get location information
            startGettingLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // accepted, start getting location information
                    startGettingLocation();
                } else {
                    // denied
                    Toast.makeText(this, "Location access denied by the user!", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startGettingLocation() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000); // Update location every second

        // now permission is granted, but we need to check it
        int hasLocationPermission = ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // start requesting location changes
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
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