package com.example.piotrek.easypres;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class ShowSlideActivity extends AppCompatActivity {

    private ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_slide);

        Bundle bun = getIntent().getExtras();
        String path = bun.getString("@string/pathToPdf");   // przekazanie adresu do pliku pdf

        imgView = (ImageView) findViewById(R.id.imageView);
        prepareBitmaps(path);

    }

    public void prepareBitmaps(String path){
        File f = new File(path);
        int imgViewWidth = imgView.getWidth(),
                imgViewHeight = imgView.getHeight();/*
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

}
