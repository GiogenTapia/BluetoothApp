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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity  {

    private static final int REQUEST_ENABLE_BT =0 ;
    private static final String TAG ="PRUEBA";
    private static final int REQUEST_DISABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter;
    Button btnEncender, btnApagar;
    ListView dispositivos;
    ArrayList<BluetoothDevice>  listBlueto = new ArrayList<>();
    ArrayAdapter<BluetoothDevice> arrayAdapter;
    private BluetoothService mBluetoothConnection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
       btnEncender = findViewById(R.id.btnEncender);
       btnApagar = findViewById(R.id.btnApagar);
       dispositivos = findViewById(R.id.listDispositivos);
       arrayAdapter = new ArrayAdapter<BluetoothDevice>(getApplicationContext(), android.R.layout.simple_list_item_1,listBlueto);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
           msg("Bluetooth is not available");

        }else{
           msg("Bluetooth is available");
        }
        if (bluetoothAdapter.isEnabled()){
            cargar();
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

        dispositivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //first cancel discovery because its very memory intensive.
                //mBluetoothAdapter.cancelDiscovery();
                BluetoothDevice device = listBlueto.get(position);
                //BluetoothDevice device = mPairedDevices.get(clickedItemIndex);

                Log.d(TAG, "onItemClick: You Clicked on a device.");
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                Log.d(TAG, "onItemClick: deviceName = " + deviceName);
                Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

                mBluetoothConnection = new BluetoothService(mHandler);
                Log.d(TAG, "Connecting with " + device.getName());
                //device.createBond();
                mBluetoothConnection.startClient(device);
            }
        });





    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            byte[] readBuf = (byte[]) msg.obj;
            int numberOfBytes = msg.arg1;

            // construct a string from the valid bytes in the buffer
            String readMessage = new String(readBuf, 0, numberOfBytes);
            Log.d(TAG, readMessage);
        }
    };


    public final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("JALAR","ANALIZANDO");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("JALAR",deviceName);
                listBlueto.add(device);
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
                    cargar();

                }
                break;
            case REQUEST_DISABLE_BT:
                if (resultCode == RESULT_OK){

                    msg("Bluetooth apagado");
                    listBlueto.clear();
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


public void cargar(){
    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
    if (pairedDevices.size() > 0) {
        // There are paired devices. Get the name and address of each paired device.
        for (BluetoothDevice device : pairedDevices) {
            String deviceName = device.getName();
            String deviceHardwareAddress = device.getAddress(); // MAC address
            listBlueto.add(device);
            arrayAdapter.notifyDataSetChanged();

        }
    }
}



}