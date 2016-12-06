package fi.jamk.signalstrength;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.Manifest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    //variables
    private static final int GPS_CHECK = 1;
    private TelephonyManager mTelephonyManager;
    private MyPhoneStateListener mPhoneStatelistener;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final int REQUEST_LOCATION = 1;
    private double lat = 0;
    private double lon = 0;
    protected int mGsmSignalStrength = 0;
    protected int mCdmaSignalStrength = 0;
    protected int mEvdoSignalStrength = 0;
    private int mLteSignalStrength = 0;
    private String operatorName;
    private boolean IsTracking;
    private Switch switchTracking;
    private Menu menu;
    private MenuItem menuTracking;
    private UUID uuid;

    private LocationManager manager;
    private boolean statusOfGPS;


    //private PowerManager.WakeLock wl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.menu = menu;
        //setting toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // ini UUID this would be different for each "session"
        uuid = UUID.randomUUID();

        //initialize google api
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mPhoneStatelistener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        operatorName = mTelephonyManager.getNetworkOperatorName();
        mTelephonyManager.listen(mPhoneStatelistener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        switchTracking = new Switch(MainActivity.this);
        IsTracking = true;
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //Ini wakelock to send data when screen off NOTE: keeps te CPU running but system switcesh off GPS so location doesn't change
        //PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        //wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "On");
        //wl.acquire();
    }



    //functions for handling google api connection
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
        //This callback is important for handling errors.
    }

    //called when location is changed
    @Override
    public void onLocationChanged(Location location) {
        if (IsTracking) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            Date date = new Date();
            //DataLocation object for temporary data storage
            DataLocation dl = new DataLocation(uuid, date, lat, lon, mGsmSignalStrength, mCdmaSignalStrength, mEvdoSignalStrength, mLteSignalStrength);

            if (operatorName.equals("Sonera") ){
                //String uri = "http://84.251.189.202:8080/signals/sonera"; // Timppas Home Ubuntu
                String uri = "http://54.157.28.32:8080/signals/sonera"; // Ubuntu on AWS
                testPost(dl, uri);
            }else if (operatorName.equals("Dna")) {
                String uri = "http://84.251.189.202:8080/signals/dna";
                testPost(dl, uri);
            }else if (operatorName.equals("Saunalahti")){
                String uri = "http://84.251.189.202:8080/signals/saunalahti";
                testPost(dl, uri);
            }

            // float speed = location.getSpeed(); // This could maybe be used to set interval according to users traveling speed
        }
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //google api start and stop
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

    // checking that we have permissions for getting location
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

    //result for checking permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // accepted, start getting location information
                    startGettingLocation();
                } else {
                    // denied
                    Toast.makeText(this, "Location access denied by the user!", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //function to start get phones location with interval time
    private void startGettingLocation() {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(3000); // Update location every three seconds

            // now permission is granted, but we need to check it
            int hasLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasLocationPermission == PackageManager.PERMISSION_GRANTED) {
                // start requesting location changes
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
            }
    }

    //initializing options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menuTracking = menu.findItem(R.id.enable_tracking);

        if (statusOfGPS){
            switchTracking.setChecked(true);
            menuTracking.setTitle("Paikannus (päällä)");
        }else{
            switchTracking.setChecked(false);
            menuTracking.setTitle("Paikannus (pois)");
        }
        return true;
    }

    //handler for optionsitemselection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.user_info) {
            Intent intent = new Intent(MainActivity.this,PersonalActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.enable_tracking) {

            switchTracking.setTextOn("päälle");
            switchTracking.setTextOff("pois");

            final LinearLayout linearLayout = new LinearLayout(MainActivity.this);
            linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
            linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            linearLayout.addView(switchTracking);

            AlertDialog.Builder myDialog = new AlertDialog.Builder(MainActivity.this);
            myDialog.setTitle("Paikannus")
            .setMessage("Aseta paikannus päälle tai pois.")
            .setView(linearLayout)
            .setOnCancelListener(
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            linearLayout.removeAllViews();
                        }
                    })
            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    linearLayout.removeAllViews();
                }
            });

            myDialog.show();

            //switch checked listener
            switchTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        IsTracking = true;
                        statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        if (!statusOfGPS){
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_CHECK);
                        }
                        menuTracking.setTitle("Paikannus (päällä)");
                    }else{
                        IsTracking = false;
                        menuTracking.setTitle("Paikannus (pois)");
                    }
                }
            });

            return true;
        }

        if (id == R.id.about) {
            AboutDialogFragment eDialog = new AboutDialogFragment();
            eDialog.show(getFragmentManager(), "about");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //function for switching between tabs
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new SoneraFragment();
                case 1:
                    return new DNAFragment();
                case 2:
                    return new SaunalahtiFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    //method for getting signal strengths
    class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            mGsmSignalStrength = signalStrength.getGsmSignalStrength();
            mGsmSignalStrength = (2 * mGsmSignalStrength) - 113; // -> dBm
            mCdmaSignalStrength = signalStrength.getCdmaDbm();
            mEvdoSignalStrength = signalStrength.getEvdoDbm();
            mLteSignalStrength = getLTE();
        }
    }

    public void testPost(DataLocation dl, String uri){
        HttpRequestTask task = new HttpRequestTask(dl, uri);
        task.execute();
    }
    //get 4G strength NOTE: seems to always return -13 so does not work as intended(or my provider has extremely stable LTE network)
    public int getLTE(){
        TelephonyManager lteTelephony;
        lteTelephony = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        int asu = lteTelephony.NETWORK_TYPE_LTE;
        int dbm = asu - 140;
        return dbm;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (requestCode == GPS_CHECK) {
            if (!statusOfGPS){
               switchTracking.setChecked(false);
            }
        }
    }
}