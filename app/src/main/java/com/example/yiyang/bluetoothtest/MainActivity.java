package com.example.yiyang.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_ENABLE_BT = 1234;
    private Button btnGetBTA;
    private TextView tvDeviceName;
    private Button btnEnableBT;
    private BluetoothAdapter btAdapter;
    private TextView tvBTEnable;
    private ListView lvPairedDevices;
    private Button btnGetPairedDevices;
    private ArrayAdapter<String> arrDeviceList;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Bluetooth Enabled!!", Toast.LENGTH_LONG).show();
                tvBTEnable.setText("BT Enabled!");
                btnEnableBT.setText("Disable BT in APP");

            } else {
                Toast.makeText(MainActivity.this, "Operation Canceled!!", Toast.LENGTH_LONG).show();
                tvBTEnable.setText("BT Disabled!");
                btnEnableBT.setText("Enable BT");

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGetBTA = (Button) findViewById(R.id.btnGetBTA);
        tvDeviceName = (TextView) findViewById(R.id.tvDeviceName);
        btnEnableBT = (Button) findViewById(R.id.btnEnableBT);
        tvBTEnable = (TextView) findViewById(R.id.tvBTEnable);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        lvPairedDevices = (ListView) findViewById(R.id.lvPairedDevices);
        btnGetPairedDevices = (Button) findViewById(R.id.btnGetPairedDevices);


        arrDeviceList = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1);
        arrDeviceList.add("Non");
        lvPairedDevices.setAdapter(arrDeviceList);

        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        lvPairedDevices.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, ((TextView)view).getText(), Toast.LENGTH_LONG).show();

                return true;
            }
        });

        btnGetPairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    arrDeviceList.clear();
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        if (device.getAddress().length() > 0)
                            arrDeviceList.add(device.getName() + "\n" + device.getAddress());
                    }

                }
            }
        });

        btnGetBTA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btAdapter == null)
                    Toast.makeText(MainActivity.this, "No BluetoothAdapter!!", Toast.LENGTH_LONG).show();
                else {
                    Toast.makeText(MainActivity.this, "BT Device Name: " + btAdapter.getName().toString(), Toast.LENGTH_LONG).show();
                    tvDeviceName.setText(btAdapter.getName().toString());
                }
            }
        });

        btnEnableBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btnEnableBT.getText().equals("Disable BT in APP")) {
                    btAdapter.disable();
                    btnEnableBT.setText("Enable BT");

                } else {
                    if (!btAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//                        btAdapter.enable();

                    } else {
                        tvBTEnable.setText("BT Enabled!");
                        btnEnableBT.setText("Disable BT in APP");
                    }
                }
            }
        });

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        tvBTEnable.setText("Bluetooth off");
                        btnEnableBT.setText("Enable BT");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        tvBTEnable.setText("Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        tvBTEnable.setText("Bluetooth on");
                        btnEnableBT.setText("Disable BT in APP");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        tvBTEnable.setText("Turning Bluetooth on...");
                        break;
                }
            }
        }
    };


}
