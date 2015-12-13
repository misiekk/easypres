package com.example.piotrek.easypres;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
//import org.apache.poi.xslf.*;
//import org.apache.poi.xslf.usermodel.XMLSlideShow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Button butBt = (Button) findViewById(R.id.buttonBt);
        Button butWifi = (Button) findViewById(R.id.buttonWifi);
        Button butBrowse = (Button) findViewById(R.id.buttonBrowse);

        setSupportActionBar(toolbar);

    }

    public enum SupportedPowerPointFiles{
        PPTX
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /* Obsluga przycisku BT */
    public void butBtHandle(View v)
    {

    }
    /* Obsluga przycisku Wifi */
    public void butWifiHandle(View v)
    {

    }
    /* Obsluga przycisku Browse */
    public void butBrowseHandle(View v)
    {
        Intent i = new Intent(this, BrowseActivity.class);
        startActivity(i);
    }


}
