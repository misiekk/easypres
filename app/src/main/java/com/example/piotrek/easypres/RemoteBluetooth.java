package com.example.piotrek.easypres;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Message;
import android.os.ParcelFileDescriptor;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class RemoteBluetooth extends AppCompatActivity implements Observer{
    public static final int EXIT = 0;
    public static final int NEXT_SLIDE = 3;
    public static final int PREV_SLIDE = 4;
    public static final int READ_COMMAND_OK_RESPONSE = 5;
    public static final int READ_FILE_RESPONSE = 6;
    public static final int READ_NUMBER_RESPONSE = 7;
    public static final int READ_COMMAND_NOT_OK_RESPONSE = 8;
    public static final String PREFIX_SLIDE = "#S";
    public static final String PREFIX_COUNT_OF_SLIDES = "#I";
    public static final String PREFIX_EOF = "#E";
    private static final int REQUEST_BT_ENABLE = 1;

    private static final int FILES_TRANSMITTED = 12;
    private BluetoothAdapter adapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Button prevSlide = null, nextSlide = null;
    private ImageView imgVActual = null, imgVPrev = null, imgVNext = null;
    private static String address = "44:6D:57:EE:67:C2";
    private String pathToFile = "";
    private ArrayList<Bitmap> bitmaps = null;
    private int actualSlide = 0;
    private int slidesCount = 0;
    private boolean isBtTransmission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_bluetooth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bitmaps = new ArrayList<Bitmap>();
        prevSlide = (Button) findViewById(R.id.buttonPrev);
        nextSlide = (Button) findViewById(R.id.buttonNext);
        Bundle bun = getIntent().getExtras();
        pathToFile = bun.getString("pathToPdf");
        isBtTransmission = bun.getBoolean("bt");
        prevSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSlide(PREV_SLIDE);
            }
        });

        nextSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSlide(NEXT_SLIDE);
            }
        });

        imgVActual = (ImageView) findViewById(R.id.imageViewActual);
        imgVPrev = (ImageView) findViewById(R.id.imageViewPrev);
        imgVNext = (ImageView) findViewById(R.id.imageViewNext);
        changeButtonState(false);
        getSupportActionBar().setTitle("Loading slides...");
        Log.d("imgVActual: ", Integer.toString(imgVActual.getWidth()) + "x" + Integer.toString(imgVActual.getHeight()));
    }

    public void startBTAdapter(){
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
        }
        else{
            prepareBitmapsAndSendSlides(pathToFile);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isBtTransmission){
            startBTAdapter();
            if(adapter == null){
                Log.d("ADAPTER IS NULL!", "ERROR NULL!");
                finish();
            }
            enableBTDialog();
        }
        else{
            prepareBitmaps(pathToFile);
        }

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        reset();
        bitmaps.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        startBTAdapter();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BT_ENABLE) {
            if (resultCode == RESULT_OK) {
                prepareBitmapsAndSendSlides(pathToFile);
            }
            else{
                finish();
            }
        }
    }

    public synchronized void startTransmission(final byte[] buf){
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
        final BluetoothSocket socket;

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        adapter.cancelDiscovery();
        new Thread(new Runnable(){
            public void run(){
                //OutputStream out = null;
                try {
                    btSocket.connect();
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
              try {
                  if(outStream == null){
                      Log.d("OUTSTREAM ", "IS NULL!");
                      return;
                  }
                  Log.d("STARTING ", "WRITING TO SOCKET");
                  //outStream.write("#F".getBytes(), 0, 2);     // #F - new file incoming
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
                  outStream.close();
                  btSocket.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
        }).start();
    }

    public void prepareBitmaps(String path){
        try {
            Log.d("PATH = ", path);
            File f = new File(path);
            int imgViewWidth = imgVActual.getWidth(),
                    imgViewHeight = imgVActual.getHeight();
            /*int imgViewWidth = 200,
                    imgViewHeight = 160;*/
            Log.d("imgViewWidth = ", Integer.toString(imgViewWidth));
            Log.d("imgViewHeight = ", Integer.toString(imgViewHeight));
            PdfRenderer pdfRend = new PdfRenderer(ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY));
            Log.d("getPageCount = ", Integer.toString(pdfRend.getPageCount()));
            //ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
            for(int i=0; i<pdfRend.getPageCount(); ++i){
                PdfRenderer.Page p = pdfRend.openPage(i);
                Bitmap b = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Bitmap.Config.ARGB_8888);
                //Matrix m = imgVActual.getImageMatrix();
                //Rect rect = new Rect(0, 0, imgViewWidth, imgViewHeight);
                p.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                //pdfRend.openPage(i).render(b, rect, m, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                bitmaps.add(b);
                p.close();
            }

            pdfRend.close();
            /*
            imgView.setImageMatrix(m);
            imgView.setImageBitmap(b);
            imgView.invalidate();*/
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        /*
        String[] paths = {"/storage/sdcard1/bluetooth/1prez.jpg",
                "/storage/sdcard1/bluetooth/2prez.jpg",
                "/storage/sdcard1/bluetooth/3prez.jpg"};
        for(String e : paths){
            Bitmap bmp = BitmapFactory.decodeFile(e);
            bitmaps.add(bmp);
        }
        slidesCount = bitmaps.size();
        changeButtonState(true);
        // show first slide*/
        slidesCount = bitmaps.size();
        changeButtonState(true);
        showSlides(0);
    }

    public void prepareBitmapsAndSendSlides(String path){
        try {
            ArrayList<byte[]> tab = new ArrayList<byte[]>();
            Log.d("PATH = ", path);
            File f = new File(path);
            PdfRenderer pdfRend = new PdfRenderer(ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY));
            Log.d("getPageCount = ", Integer.toString(pdfRend.getPageCount()));
            for(int i=0; i<pdfRend.getPageCount(); ++i){
                PdfRenderer.Page p = pdfRend.openPage(i);
                Bitmap b = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Bitmap.Config.ARGB_8888);
                //Matrix m = imgVActual.getImageMatrix();
                //Rect rect = new Rect(0, 0, imgViewWidth, imgViewHeight);
                p.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                //pdfRend.openPage(i).render(b, rect, m, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                bitmaps.add(b);
                p.close();
                ByteArrayOutputStream s = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.JPEG, 50, s);
                byte[] bytes = s.toByteArray();
                tab.add(bytes);
            }

            pdfRend.close();
            /*
            imgView.setImageMatrix(m);
            imgView.setImageBitmap(b);
            imgView.invalidate();*/
            slidesCount = bitmaps.size();

            startTransmission(tab);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
/*
        String[] paths = {"/storage/sdcard1/bluetooth/1prez.jpg",
                "/storage/sdcard1/bluetooth/2prez.jpg",
                "/storage/sdcard1/bluetooth/3prez.jpg"};
        ArrayList<byte[]> tab = new ArrayList<byte[]>();
        for(String e : paths){
            Bitmap bmp = BitmapFactory.decodeFile(e);
            bitmaps.add(bmp);
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, s);
            byte[] bytes = s.toByteArray();
            tab.add(bytes);
        }*/

        //tab.clear();
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
            imgView.setImageMatrix(m);
            imgView.setImageBitmap(b);
            imgView.invalidate();*//*
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public void startTransmission(final ArrayList<byte[]> tab){
        reset();
        BluetoothDevice device = adapter.getRemoteDevice(address);
        Log.d("MAC ADDRESS = ", device.getAddress());
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        adapter.cancelDiscovery();

        class TransmitFiles extends Observable implements Runnable{
            TransmitFiles(Observer o){
                this.addObserver(o);
            }

            @Override
            public void run() {
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
                    if(!btSocket.isConnected()){
                        btSocket.connect();
                    }
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

                try {
                    if(outStream == null){
                        Log.d("OUTSTREAM ", "IS NULL!");
                        return;
                    }
                    Log.d("STARTING ", "WRITING TO SOCKET");
                    InputStream in = btSocket.getInputStream();

                    // first, send number of slides to be transmitted
                    int slidesCount = tab.size();
                    outStream.write(PREFIX_COUNT_OF_SLIDES.getBytes());   // #I - start of msg
                    outStream.write(Integer.toString(slidesCount).getBytes());
                    outStream.flush();
                    int responseCode = in.read();
                    if(responseCode == READ_NUMBER_RESPONSE){
                        // now we can send slides one by one
                        for(byte[] b : tab){
                            outStream.write(b);
                            outStream.write(PREFIX_EOF.getBytes());   // #E - end of file
                            outStream.flush();

                            // now wait for response...
                            int response = in.read();
                            if(response == READ_FILE_RESPONSE){
                                Log.d("Got response", "Let's continue");
                            }
                            else{
                                throw new IOException("Wrong response code from server!");
                            }
                        }
                    }
                    Log.d("WRITTEN TO SOCKET", "YEAH");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.setChanged();
                this.notifyObservers(FILES_TRANSMITTED);
            }
        }

        Thread t = new Thread(new TransmitFiles(this));
        t.start();

    }

    public void sendSlide(final int param){
        // update imageView and send request
        calculateActualSlide(param);

        if(isBtTransmission) {
            try {
                InputStream in = btSocket.getInputStream();
                if (!btSocket.isConnected()) {
                    btSocket.connect();
                }
                int bCount = PREFIX_SLIDE.getBytes().length;
                switch (param) {
                    case PREV_SLIDE:
                        int psCount = Integer.toString(PREV_SLIDE).getBytes().length;
                        byte[] temp_ps = new byte[bCount + psCount];
                        System.arraycopy(PREFIX_SLIDE.getBytes(), 0, temp_ps, 0, bCount);
                        System.arraycopy(Integer.toString(PREV_SLIDE).getBytes(), 0, temp_ps, bCount, psCount);
                        outStream.write(temp_ps);
                        break;
                    case NEXT_SLIDE:
                        int nsCount = Integer.toString(NEXT_SLIDE).getBytes().length;
                        byte[] temp_ns = new byte[bCount + nsCount];
                        System.arraycopy(PREFIX_SLIDE.getBytes(), 0, temp_ns, 0, bCount);
                        System.arraycopy(Integer.toString(NEXT_SLIDE).getBytes(), 0, temp_ns, bCount, nsCount);
                        outStream.write(temp_ns);
                        break;
                    default:
                        break;
                }
                outStream.flush();
                int commandOk = in.read();
                if (commandOk == READ_COMMAND_OK_RESPONSE) {
                    Log.d("COMMAND", "OK");
                } else if (commandOk == READ_COMMAND_NOT_OK_RESPONSE) {
                    sendSlide(param);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void calculateActualSlide(int param){
        switch (param){
            case PREV_SLIDE:
                if(actualSlide > 0) {
                    actualSlide--;
                }
                break;
            case NEXT_SLIDE:
                if(actualSlide < slidesCount-1){
                    actualSlide++;
                }
                break;

            default:
                break;
        }
        showSlides(actualSlide);


    }

    public void reset(){
        try {
            if (outStream != null) {
                outStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        outStream = null;
        try {
            if (btSocket != null) {
                btSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        btSocket = null;

    }

    public void changeButtonState(boolean state){
        prevSlide.setEnabled(state);
        nextSlide.setEnabled(state);
    }

    @Override
    public void update(Observable observable, Object data) {
        if((Integer)data == FILES_TRANSMITTED){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeButtonState(true);
                    // show first slide
                    showSlides(0);
                }
            });
        }
    }

    public void showSlides(int param){
        Bitmap bmpAct = bitmaps.get(param);
        imgVActual.setImageBitmap(bmpAct);

        if(param > 0){
            Bitmap bmpPrev = bitmaps.get(param-1);
            imgVPrev.setImageBitmap(bmpPrev);
        }
        else{
            imgVPrev.setImageBitmap(null);
        }
        if(param < slidesCount-1){
            Bitmap bmpNext = bitmaps.get(param+1);
            imgVNext.setImageBitmap(bmpNext);
        }
        else{
            imgVNext.setImageBitmap(null);
        }

        // update title
        getSupportActionBar().setTitle("Slide " + Integer.toString(actualSlide+1) +"/" + Integer.toString(slidesCount));
    }
}
