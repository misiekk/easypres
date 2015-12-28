package com.example.piotrek.easypres;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class RemoteBluetooth extends Activity{
    private BluetoothAdapter adapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static String address = "44:6D:57:EE:67:C2";
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        startBTAdapter();
    }
    public void startBTAdapter()
    {
        adapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("ADAPTER!", "STARTING!");
        if(adapter == null) {
            Toast.makeText(this, "Bluetooth not available!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();

        if(adapter == null){
            Log.d("ADAPTER IS NULL!", "ERROR NULL!");
            finish();
        }
        if (!adapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }
    }
        @Override
        public void onResume() {
            super.onResume();

        BluetoothDevice device = adapter.getRemoteDevice(address);
        Log.d("MAC ADDRESS = ", device.getAddress());

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        adapter.cancelDiscovery();
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }

        try {
            outStream = btSocket.getOutputStream();
            Log.d("BTSOCKET ", Boolean.toString(btSocket.isConnected()));
            String info ="";
            if(outStream != null){
                info = "EXISTS";
            }
            else{
                info = "NOT EXISTS";
            }
            Log.d("OUTSTREAM ", info);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String msg = "Siema siema!";
        byte[] bytes = msg.getBytes();

        try{
            outStream.write(bytes);
        }
        catch(IOException e){
            e.printStackTrace();
        }


        try {
            btSocket.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent i){
        if(resultCode == Activity.RESULT_OK){

        }
    }



}
