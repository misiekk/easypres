package com.example.piotrek.easypres;

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

public class BrowseActivity extends AppCompatActivity {
    ArrayList<String> pdfPaths;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d("zzzzzz", Environment.getExternalStorageDirectory().getAbsolutePath());
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File root = new File(rootPath);
        pdfPaths = new ArrayList<String>();

        processDir(root, pdfPaths);

        for(int i=0; i<pdfPaths.size(); ++i){
            String temp = pdfPaths.get(i);
            Log.d(Integer.toString(i), temp);
        }
        /*try
        {
            new FileInputStream("pres.pptx");
            //XMLSlideShow ppt = new XMLSlideShow(new FileInputStream("pres.pptx"));
        }
        catch(FileNotFoundException e)
        {

        }*/

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
        //Log.d("FILE", f.getAbsolutePath());
        list.add(f.getAbsolutePath());
    }

}
