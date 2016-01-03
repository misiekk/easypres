package com.example.piotrek.easypres;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ShowSlideActivity extends AppCompatActivity implements Runnable {

    private ImageView imgView;
    private Button prevSlide, nextSlide;

    private Socket socket = null;
    DataOutputStream dataOutputStream = null;
    private static final String address = "192.168.0.12";
    private static final int port = 1755;
    private static final int REQUEST_WIFI_ENABLE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_slide);

        imgView = (ImageView) findViewById(R.id.imageView);
        prevSlide = (Button) findViewById(R.id.buttonPrev);
        nextSlide = (Button) findViewById(R.id.buttonNext);


    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try{
            dataOutputStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void enableBTDialog() {
        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled()){
            //Log.d("P2P WIFI", "SUPPORTED :)");
            /*Intent enableWIFIIntent = new Intent(wifi.ac);
            startActivityForResult(enableWIFIIntent, REQUEST_WIFI_ENABLE);*/
        }
    }
   /* protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_WIFI_ENABLE) {
            if (resultCode == RESULT_OK) {
                //prepareBitmaps(pathToFile);
                sendMsg();
            }
            else{
                finish();
            }
        }
    }*/

    @Override
    public void run(){
        prepareSocket();
    }

    public void prepareSocket(){
        try {
            socket = new Socket(address, port);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (UnknownHostException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        sendMsg();

    }

    public void sendMsg(){
        try {
            dataOutputStream.writeUTF("Tralalalala");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}
