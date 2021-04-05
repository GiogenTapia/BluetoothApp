package com.giogen.bluetoothapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT =0 ;
    private static final int REQUEST_DISABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter;
    Button btnEncender, btnApagar;
    ListView dispositivos;
    ArrayList<String> listString = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
       btnEncender = findViewById(R.id.btnEncender);
       btnApagar = findViewById(R.id.btnApagar);
       dispositivos = findViewById(R.id.listDispositivos);
       arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,listString);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
           msg("Bluetooth is not available");

        }else{
           msg("Bluetooth is available");
        }

        dispositivos.setAdapter(arrayAdapter);

        //Boton de encendido
        btnEncender.setOnClickListener(l ->{
            if (!bluetoothAdapter.isEnabled()){
                msg("Encendiendo Bluetooth...");
                Intent bluetooth = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(bluetooth,REQUEST_ENABLE_BT);

            }else{
                msg("Bluetooth ya esta encendido");
            }
        });

        //Boton de apagado
        btnApagar.setOnClickListener(l ->{
            if (bluetoothAdapter.isEnabled()){
                msg("Desconectando");
                bluetoothAdapter.disable();
            }else{
                msg("Ya se encuentra desactivado");
            }

        });






    }

    public final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("JALAR","ANALIZANDO");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("JALAR",deviceName);
                listString.add(deviceName);
                arrayAdapter.notifyDataSetChanged();
                msg(deviceName);

            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK){
                    msg("Bluetooth encendido");
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0) {
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address
                            listString.add(deviceName);
                            arrayAdapter.notifyDataSetChanged();

                        }
                    }
                }
                break;
            case REQUEST_DISABLE_BT:
                if (resultCode == RESULT_OK){

                    msg("Bluetooth apagado");
                    listString.clear();
                    arrayAdapter.notifyDataSetChanged();
                }else{
                    msg("Ya lo tienes apagado");

                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionmenu,menu);
       return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                if (bluetoothAdapter.isEnabled()){

                    if (bluetoothAdapter.isDiscovering()) {
                        // El Bluetooth ya está en modo discover, lo cancelamos para iniciarlo de nuevo
                        bluetoothAdapter.cancelDiscovery();
                    }
                    bluetoothAdapter.startDiscovery();
                    Log.d("JALAR","ENTRO");

                }else {
                    msg("Encienda el bluetooth");
                }
                break;

            case R.id.item2:
                if (bluetoothAdapter.isEnabled()){
                    Intent discoverableIntent =
                            new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);
                    msg("Visible por 300 segundos");
                }else {
                    msg("Encienda el Bluetooth");
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void msg(String ms){
        Toast.makeText(this,ms,Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


/*
    private class AcceptThread extends Thread {
        private static final String TAG = "CALIS" ;
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    mmServerSocket.close();
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

 */
}