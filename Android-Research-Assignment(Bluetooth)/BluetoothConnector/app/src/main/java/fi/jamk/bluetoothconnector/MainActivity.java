package fi.jamk.bluetoothconnector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private TextView switchStatus;
    private Switch mySwitch;
    ArrayAdapter pairedArrayAdapter;
    ArrayAdapter newDeviceArrayAdapter;
    BluetoothAdapter mBluetoothAdapter;
    ListView listViewPaired;
    ListView listViewSearch;
    final ArrayList<String> listPaired = new ArrayList<>();
    final ArrayList<String> listSearch = new ArrayList<>();
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        switchStatus = (TextView) findViewById(R.id.switchStatus);
        mySwitch = (Switch) findViewById(R.id.switch1);
        mySwitch.setChecked(false);


        mySwitch.setOnCheckedChangeListener(    new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked){
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                    startActivity(discoverableIntent);
                    switchStatus.setText("Device is currently visible");
                }else{
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1);
                    startActivity(discoverableIntent);
                    switchStatus.setText("Device is hidden");
                }
            }
        });


        //check the current state before we display the screen
        if (mySwitch.isChecked()){
            switchStatus.setText("Device is currently visible");
        }
        else {
            switchStatus.setText("Device is hidden");
        }

        listViewPaired = (ListView) findViewById(R.id.listViewPaired);
        listViewSearch = (ListView) findViewById(R.id.listViewSearch);
        //Create bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }


        //Check if bluetooth is enabled

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        getPaired();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem itemSwitch = menu.findItem(R.id.mySwitch);
        itemSwitch.setActionView(R.layout.use_switch);
        final Switch sw = (Switch) menu.findItem(R.id.mySwitch).getActionView().findViewById(R.id.action_switch);
        if (mBluetoothAdapter.isEnabled()){
            sw.setChecked(true);
        }
        sw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                }else{
                    mBluetoothAdapter.disable();
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_search:
                search();
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getPaired(){
        listPaired.clear();
        pairedArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listPaired);
        listViewPaired.setAdapter(pairedArrayAdapter);
        //query for paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                listPaired.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    public void search() {

        new SearchTask().execute();
    }

    public void setToList(){
        newDeviceArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listSearch);
        listViewSearch.setAdapter(newDeviceArrayAdapter);

        /*newDeviceArrayAdapter.clear();
        newDeviceArrayAdapter.insert(this, 0);
        newDeviceArrayAdapter.insert(android.R.layout.simple_list_item_1, 1);
        newDeviceArrayAdapter.insert(listSearch, 2);
        listViewSearch.setAdapter(newDeviceArrayAdapter);*/
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();


            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // Add the name and address to an array adapter to show in a ListView
                listSearch.add(device.getName() + "\n" + device.getAddress());

            }
        }
    };

    private class SearchTask extends AsyncTask<Void, Integer, Void>{
        @Override
        protected void onPreExecute(){
            //menu.getItem(1).setEnabled(false);
        }

        protected Void doInBackground(Void... params){
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... params){

        }

        protected void onPostExecute(Void params){
           setToList();
        }
    }
}
