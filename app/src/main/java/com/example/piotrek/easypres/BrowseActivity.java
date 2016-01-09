package com.example.piotrek.easypres;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.File;
import java.util.ArrayList;
import android.graphics.pdf.PdfRenderer;
import android.widget.Toast;


public class BrowseActivity extends AppCompatActivity {
    ArrayList<String> ptxPaths;
    ArrayAdapter<String> ptxAdapter;
    ArrayList<String> pdfPaths;
    ArrayAdapter<String> pdfAdapter;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Choose file...");
        //getSupportActionBar().hide();
        lv = (ListView) findViewById(R.id.listView);

        Environment.getDataDirectory().getAbsolutePath();
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d("External dir = ", rootPath);
        File root = new File(rootPath);
        pdfPaths = new ArrayList<String>();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(),"Clicked " + position,Toast.LENGTH_SHORT).show();
                String path = pdfPaths.get(position);
                openPdf(path);
            }
        });

        processDir(root, pdfPaths);
        fillListView();
    }

    public void processDir(File f, ArrayList<String> list)
    {
        if(f == null)
            return;

        if(f.isFile()){
            processFile(f, list);
        }
        else if(f.isDirectory()){
            File[] fileList = f.listFiles();
            if(fileList != null){
                for(int j=0; j<fileList.length; ++j){
                    processDir(fileList[j], list);
                }
            }
        }
    }

    public void processFile(File f, ArrayList<String> list)
    {
        if(!f.getName().endsWith(".pdf"))
            return;
        list.add(f.getAbsolutePath());
    }

    public void fillListView(){
        pdfAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, pdfPaths);
        lv.setAdapter(pdfAdapter);
    }

    public void openPdf(String path){
        showAlertBox(path);
    }

    public void showAlertBox(final String path){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose wisely");
        builder.setMessage("Show slides or start Bluetooth Transmission?");
        builder.setPositiveButton("Show slides", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getApplicationContext(), "To be implemented ;)", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), RemoteBluetooth.class);
                i.putExtra("pathToPdf", path);
                i.putExtra("bt", false);
                startActivity(i);
            }
        });
        builder.setNegativeButton("Start transmission", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(getApplicationContext(), RemoteBluetooth.class);
                i.putExtra("pathToPdf", path);
                i.putExtra("bt", true);
                startActivity(i);
            }
        });
        builder.show();
    }

}
