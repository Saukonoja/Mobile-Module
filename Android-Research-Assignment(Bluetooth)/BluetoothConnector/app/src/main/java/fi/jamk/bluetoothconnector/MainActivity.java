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
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static android.R.id.list;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private TextView visibleStatus;
    private Switch visibleSwitch;
    ArrayAdapter pairedArrayAdapter;
    ArrayAdapter newDeviceArrayAdapter;
    BluetoothAdapter mBluetoothAdapter;
    ListView listViewPaired;
    ListView listViewSearch;
    final ArrayList<String> listPaired = new ArrayList<>();
    final ArrayList<String> listSearch = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        visibleStatus = (TextView) findViewById(R.id.switchStatus);
        visibleSwitch = (Switch) findViewById(R.id.switch1);
        visibleSwitch.setChecked(false);

        visibleSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked){
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                    startActivity(discoverableIntent);
                    visibleStatus.setText("Device is currently visible");
                }else{
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1);
                    startActivity(discoverableIntent);
                    visibleStatus.setText("Device is hidden");
                }
            }
        });

        listViewPaired = (ListView) findViewById(R.id.listViewPaired);
        listViewSearch = (ListView) findViewById(R.id.listViewSearch);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        getPaired();

        listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String phoneInfo = listSearch.get(position).toString();
                String address = phoneInfo.substring(phoneInfo.length() - 17);
                Context context = getApplicationContext();

                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, address, duration);
                toast.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem itemSwitch = menu.findItem(R.id.action_bluetoothSwitch);
        itemSwitch.setActionView(R.layout.use_switch);
        final Switch sw = (Switch) menu.findItem(R.id.action_bluetoothSwitch).getActionView().findViewById(R.id.bluetoothSwitch);
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
        getPaired();
        listSearch.clear();
        try {
             Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    mBluetoothAdapter.startDiscovery();
                }
            });
            t.start(); // spawn thread
            t.join();  // wait for thread to finish

            newDeviceArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listSearch);
        }catch (InterruptedException e){
            Log.e("BluetoothDemo","Thread crash");
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                for (String address : listPaired){
                    if (address.contains(device.getAddress())){
                       return;
                    }
                }
                listSearch.add(device.getName() + "\n" + device.getAddress());
                Collections.sort(listSearch, String.CASE_INSENSITIVE_ORDER);
                listViewSearch.setAdapter(newDeviceArrayAdapter);
            }
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}