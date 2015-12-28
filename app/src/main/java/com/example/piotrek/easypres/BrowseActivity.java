package com.example.piotrek.easypres;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;

import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import android.graphics.pdf.PdfRenderer;


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
        lv = (ListView) findViewById(R.id.listView);

        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
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
        Intent i = new Intent(this, ShowSlideActivity.class);
        i.putExtra("@string/pathToPdf", path);
        startActivity(i);
    }

}
