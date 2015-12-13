package com.example.piotrek.easypres;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class BrowseActivity extends AppCompatActivity {
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

    public void fillListView(){
        pdfAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, pdfPaths);
        lv.setAdapter(pdfAdapter);
    }

    public void openPdf(String path){
        File f = new File(path);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.fromFile(f), "application/pdf");
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
    }

}
