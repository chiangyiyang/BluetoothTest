package com.example.yiyang.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_ENABLE_BT = 1234;
    private static final int REQUEST_ENABLE_BT_DISCOVERABLE = 1235;
    private static final int BT_MESSAGE_READ = 1236;
    private static final String BT_SDP_SERVICE_NAME = "BluetoothTest";
    //    private static final String BT_SDP_UUID = "5cdbe98a-6fdd-11e6-8b77-86f30ca893d3";
    private static final String BT_SDP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothAdapter btAdapter;
    private Button btnGetBTA;
    private Button btnEnableBT;
    private Button btnGetPairedDevices;
    private TextView tvDeviceName;
    private TextView tvBTEnable;
    private ListView lvPairedDevices;
    private ListView lvDiscoveredDevices;
    private ArrayAdapter<String> arrPairedDeviceList;
    private ArrayAdapter<String> arrDiscoveredDeviceList;
    private Button btnDiscoverBT;
    private Button btnDiscoverable;
    private TextView tvDiscoverable;
    private ConnectedThread connectedThread;
    private AcceptThread acceptThread;
    private Button btnListen;
    private TextView tvListen;
    private Set<BluetoothDevice> pairedDevices;
    private List<BluetoothDevice> discoveredDevices;
    private Button btnSayHello;
    private EditText etSayHello;
    private Button btnDisconnect;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectedThread.cancel();
    }

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
        } else if (requestCode == REQUEST_ENABLE_BT_DISCOVERABLE) {

            if (resultCode == RESULT_CANCELED) {
                tvDiscoverable.setText("BT UNDISCOVERABLE!");
            } else {
                tvDiscoverable.setText("BT DISCOVERABLE!");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        fineViews();
        setListeners();


        arrPairedDeviceList = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1);
        arrPairedDeviceList.add("Non");
        lvPairedDevices.setAdapter(arrPairedDeviceList);

        arrDiscoveredDeviceList = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1);
        arrDiscoveredDeviceList.add("Non");
        lvDiscoveredDevices.setAdapter(arrDiscoveredDeviceList);

        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

    }

    private void setListeners() {

        btnDiscoverBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btAdapter.startDiscovery();
                arrDiscoveredDeviceList.clear();
                discoveredDevices = new ArrayList<BluetoothDevice>();

            }
        });

        lvPairedDevices.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String data = ((TextView) view).getText().toString();
                String[] items = data.split("\n");
                BluetoothDevice btDevice = null;
                for (BluetoothDevice btd :
                        pairedDevices) {
                    if (btd.getAddress().equals(items[1])) {
                        btDevice = btd;
                        break;
                    }
                }

                if (btDevice != null) {
                    ConnectThread connectThread = new ConnectThread(btDevice);
                    connectThread.start();
                    Toast.makeText(MainActivity.this, "Connect " + btDevice.getName(), Toast.LENGTH_LONG).show();
                }

                return true;
            }
        });

        lvDiscoveredDevices.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String data = ((TextView) view).getText().toString();
                String[] items = data.split("\n");
                BluetoothDevice btDevice = null;
                for (BluetoothDevice btd :
                        discoveredDevices) {
                    if (btd.getAddress().equals(items[1])) {
                        btDevice = btd;
                        break;
                    }
                }

                if (btDevice != null) {
                    ConnectThread connectThread = new ConnectThread(btDevice);
                    connectThread.start();
                    Toast.makeText(MainActivity.this, "Connect " + btDevice.getName(), Toast.LENGTH_LONG).show();
                }

                return false;
            }
        });

        btnGetPairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pairedDevices = btAdapter.getBondedDevices();
                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    arrPairedDeviceList.clear();
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        if (device.getAddress().length() > 0)
                            arrPairedDeviceList.add(device.getName() + "\n" + device.getAddress());
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

        btnDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent discoverableIntent = new
                        Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//                startActivity(discoverableIntent);
                startActivityForResult(discoverableIntent, REQUEST_ENABLE_BT_DISCOVERABLE);

            }
        });

        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnListen.getText().equals("Listen")) {
                    acceptThread = new AcceptThread();
                    acceptThread.start();
                    btnListen.setText("Stop Listen");
                    tvListen.setText("Listening");
                } else {
                    acceptThread.cancel();
                    acceptThread = null;
                    btnListen.setText("Listen");
                    tvListen.setText("");
                }
            }
        });


        btnSayHello.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] data = etSayHello.getText().toString().getBytes();
                connectedThread.write(data);
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectedThread != null) connectedThread.cancel();
            }
        });
    }

    private void fineViews() {
        btnGetBTA = (Button) findViewById(R.id.btnGetBTA);
        btnEnableBT = (Button) findViewById(R.id.btnEnableBT);
        btnGetPairedDevices = (Button) findViewById(R.id.btnGetPairedDevices);
        tvDeviceName = (TextView) findViewById(R.id.tvDeviceName);
        tvBTEnable = (TextView) findViewById(R.id.tvBTEnable);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        lvPairedDevices = (ListView) findViewById(R.id.lvPairedDevices);
        lvDiscoveredDevices = (ListView) findViewById(R.id.lvDiscoveredDevices);
        btnDiscoverBT = (Button) findViewById(R.id.btnDiscoverBT);
        btnDiscoverable = (Button) findViewById(R.id.btnDiscoverable);
        tvDiscoverable = (TextView) findViewById(R.id.tvDiscoverable);
        btnListen = (Button) findViewById(R.id.btnListen);
        tvListen = (TextView) findViewById(R.id.tvListen);
        btnSayHello = (Button) findViewById(R.id.btnSayHello);
        etSayHello = (EditText) findViewById(R.id.etSayHello);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                arrDiscoveredDeviceList.add(device.getName() + "\n" + device.getAddress());
                discoveredDevices.add(device);

            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
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


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // BT_SDP_UUID is the app's UUID string, also used by the client code
                tmp = btAdapter.listenUsingRfcommWithServiceRecord(BT_SDP_SERVICE_NAME
                        , UUID.fromString(BT_SDP_UUID));
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // BT_SDP_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(BT_SDP_UUID));
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BT_MESSAGE_READ:
                    String data = new String((byte[]) msg.obj).trim();
                    Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


    private void manageConnectedSocket(BluetoothSocket socket) {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
//        tvListen.setText("Connected");
//        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();
    }


}
