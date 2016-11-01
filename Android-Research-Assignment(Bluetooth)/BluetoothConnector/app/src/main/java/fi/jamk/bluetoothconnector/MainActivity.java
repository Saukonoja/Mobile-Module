package fi.jamk.bluetoothconnector;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;



public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_VISIBLE = 1;
    private static final int RES_CODE =  2;
    private TextView visibleStatus;
    private TextView deviceNameView;
    private Switch enableSwitch;
    private LinearLayout layOut;
    private ArrayAdapter pairedArrayAdapter;
    private ArrayAdapter newDeviceArrayAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private ListView listViewPaired;
    private ListView listViewSearch;
    private Map<String, BluetoothDevice> hashMapPaired = new HashMap<>();
    private final ArrayList<String> listPaired = new ArrayList<>();
    private Map<String, BluetoothDevice> hashMapNew = new HashMap<>();
    private final ArrayList<String> listSearch = new ArrayList<>();
    private String deviceName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        visibleStatus = (TextView) findViewById(R.id.deviceStatus);
        deviceNameView = (TextView) findViewById(R.id.deviceName);

        listViewPaired = (ListView) findViewById(R.id.listViewPaired);
        listViewSearch = (ListView) findViewById(R.id.listViewSearch);

        deviceNameView.setText(deviceName = mBluetoothAdapter.getName());

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mPairReceiver, intent);
        getPaired();
        search();

        listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String phoneInfo = listSearch.get(position).toString();
                String address = phoneInfo.substring(phoneInfo.length() - 17);

                BluetoothDevice device = hashMapNew.get(address);
                pairDevice(device);
                showToast("Pairing...");
            }
        });

        listViewPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String phoneInfo = listPaired.get(position).toString();
                String address = phoneInfo.substring(phoneInfo.length() - 17);

                BluetoothDevice device = hashMapPaired.get(address);

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    unPairDevice(device);
                }
            }
        });

    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unPairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem itemSwitch = menu.findItem(R.id.action_bluetoothSwitch);
        itemSwitch.setActionView(R.layout.use_switch);
        enableSwitch = (Switch) menu.findItem(R.id.action_bluetoothSwitch).getActionView().findViewById(R.id.bluetoothSwitch);
        layOut = (LinearLayout) findViewById(R.id.linearLayout);
        if (mBluetoothAdapter.isEnabled()){
            enableSwitch.setChecked(true);
            layOut.setVisibility(View.VISIBLE);
            visibleStatus.setText("Showing to all nearby devices.");
        }
        enableSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                        startActivityForResult(discoverableIntent, REQUEST_ENABLE_VISIBLE);
                    }
                }else{
                    layOut.setVisibility(View.INVISIBLE);
                    mBluetoothAdapter.disable();
                }
            }
        });
        return true;
    }

    protected void SendFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, RES_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_VISIBLE) {
            if (resultCode == 1){
                mBluetoothAdapter.enable();
                visibleStatus.setText("Showing to all nearby devices.");
                layOut.setVisibility(View.VISIBLE);
            }
            if (resultCode == RESULT_CANCELED) {
                enableSwitch.setChecked(false);
            }
        }

        if (requestCode == RES_CODE) {

            try {
                Uri selectedImage = data.getData();

                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("*/*");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"Share File"});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "File Name");
                emailIntent.putExtra(Intent.EXTRA_STREAM, selectedImage);
                startActivity(Intent.createChooser(emailIntent, "Share File"));

            } catch (Exception e) {

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_search:
                search();
                return  true;
            case R.id.action_rename:
                ChangeDeviceName();
                return  true;
            case R.id.action_send_file:
                SendFile();
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void ChangeDeviceName(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change device name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deviceName = input.getText().toString();
                mBluetoothAdapter.setName(deviceName);
                deviceNameView.setText(deviceName = mBluetoothAdapter.getName());
                showToast("Changed name to " + deviceName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
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
                hashMapPaired.put(device.getAddress(), device);
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

                for (String address : listSearch){
                    if (address.contains(device.getAddress())){
                        return;
                    }
                }

                hashMapNew.put(device.getAddress(), device);
                listSearch.add(device.getName() +"\n"+ device.getAddress());
                Collections.sort(listSearch, String.CASE_INSENSITIVE_ORDER);
                listViewSearch.setAdapter(newDeviceArrayAdapter);
            }
        }
    };

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    showToast("Paired");
                    search();
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    showToast("Unpaired");
                    search();
                }
            }
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}