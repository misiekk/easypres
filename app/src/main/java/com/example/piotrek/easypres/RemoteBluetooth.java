package com.example.piotrek.easypres;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class RemoteBluetooth extends AppCompatActivity {
    private BluetoothAdapter adapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BT_ENABLE = 1;
    TextView txt;
    private Button prevSlide, nextSlide;
    private static String address = "44:6D:57:EE:67:C2";
    private String pathToFile = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_bluetooth);
        txt = (TextView) findViewById(R.id.txtView);
        prevSlide = (Button) findViewById(R.id.buttonPrev);
        nextSlide = (Button) findViewById(R.id.buttonNext);
        Bundle bun = getIntent().getExtras();
        pathToFile = bun.getString("pathToPdf");

        prevSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSlide(0);
            }
        });

        nextSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSlide(1);
            }
        });
    }

    public void startBTAdapter()
    {
        //String info = "Starting Bluetooth Adapter...";
        //txt.setText(info);
        adapter = BluetoothAdapter.getDefaultAdapter();
        //Log.d("ADAPTER!", "STARTING!");
        if(adapter == null) {
            Toast.makeText(this, "Bluetooth not available!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void enableBTDialog(){
        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
            /*
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth not enabled!");
            builder.setMessage("Press OK to enable Bluetooth");
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.enable();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), "Disabling Bluetooth...", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            builder.show();*/
        }
        else{
            prepareBitmaps(pathToFile);
            //doTransmit();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        startBTAdapter();
        if(adapter == null){
            Log.d("ADAPTER IS NULL!", "ERROR NULL!");
            finish();
        }
        enableBTDialog();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        try {
            //txt.setText(info4);
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*if(adapter.isEnabled()) {
            adapter.disable();
            Toast.makeText(this, "Disabling Bluetooth...", Toast.LENGTH_SHORT).show();
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        startBTAdapter();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BT_ENABLE) {
            if (resultCode == RESULT_OK) {
                prepareBitmaps(pathToFile);
                //doTransmit();
            }
            else{
                finish();
            }
        }
    }

    public void startTransmission(final byte[] buf){
        if(buf == null){
            Log.d("BUFFER IS NULL", "!!!");
            return;
        }
        BluetoothDevice device = adapter.getRemoteDevice(address);
        Log.d("MAC ADDRESS = ", device.getAddress());
        String info1 = "Creating socket...";
        String info2 = info1 + "\n" + "Connecting...";
        String info3 = info2 + "\n" + "Writing to stream...";
        final String info4 = info3 + "\n" + "Closing socket...";
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);

            txt.setText(info1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        adapter.cancelDiscovery();
        new Thread(new Runnable(){
            public void run(){
                try {
                    btSocket.connect();
                    //txt.setText(info2);
                } catch (IOException e) {
                    try {
                        btSocket.close();
                        //Toast.makeText(getApplicationContext(), "Could not connect!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }

                try {
                    if(!btSocket.isConnected()){
                        btSocket.connect();
                    }
                    //Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show();
                    outStream = btSocket.getOutputStream();
                    Log.d("BTSOCKET ", Boolean.toString(btSocket.isConnected()));
                    String info = "";
                    if (outStream != null) {
                        info = "EXISTS";
                    } else {
                        info = "NOT EXISTS";
                    }
                    Log.d("OUTSTREAM ", info);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //txt.setText(info3);

              try {
                  if(outStream == null){
                      Log.d("OUTSTREAM ", "IS NULL!");
                      return;
                  }
                  Log.d("STARTING ", "WRITING TO SOCKET");
                  outStream.write("#F".getBytes());     // #F - new file incoming
                  outStream.write(buf);
                  //outStream.write("#".getBytes());
                  outStream.flush();
                 /* try{
                      Thread.currentThread().sleep(100, 0);
                  }
                  catch (InterruptedException e)
                  {
                      e.printStackTrace();
                  }
                  outStream.write("#E".getBytes());
                  outStream.flush();*/
                  //Toast.makeText(getApplicationContext(), "Data written to socket!", Toast.LENGTH_SHORT).show();

                  Log.d("WRITTEN TO SOCKET", "YEAH");
              }
              catch (IOException e) {
                  e.printStackTrace();
              }
              try {
                  //txt.setText(info4);
                  btSocket.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
        }).start();
    }

    public void prepareBitmaps(String path){
        Bitmap bmp = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, s);
        byte[] bytes = s.toByteArray();
        startTransmission(bytes);

        /*
        try {
            PdfRenderer pdfRend = new PdfRenderer(ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY));
            ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
            for(int i=0; i<pdfRend.getPageCount(); ++i){
                Bitmap b = Bitmap.createBitmap(imgViewWidth, imgViewHeight, Bitmap.Config.ARGB_8888);
                Matrix m = imgView.getImageMatrix();
                Rect rect = new Rect(0, 0, imgViewWidth, imgViewHeight);
                pdfRend.openPage(i).render(b, rect, m, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                bitmaps.add(b);
            }
           /* imgView.setImageMatrix(m);
            imgView.setImageBitmap(b);
            imgView.invalidate();*//*
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/
    }


    public void doTransmit(){
        BluetoothDevice device = adapter.getRemoteDevice(address);

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendSlide(final int param){
        doTransmit();
        try{
            if(!btSocket.isConnected()){
                btSocket.connect();
            }
            //outStream.write("#C".getBytes());
            switch (param) {
                case 0: // prev
                    outStream.write(2);
                    break;
                case 1: // next
                    outStream.write(3);
                    break;
                default:
                    break;
            }

            outStream.flush();
        } catch (Exception e){
            e.printStackTrace();
        }
        endTransmission();
    }

    public void endTransmission(){
        try {
            //txt.setText(info4);
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
