package com.srstudios.blurtest;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    RenderScript mRS;
    ScriptIntrinsicBlur scriptBlur;
    Allocation screenshot, blurred;
    TextureView blurredView;
    ImageView blur, noblur;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRS = RenderScript.create(this);
        scriptBlur = ScriptIntrinsicBlur.create(mRS, Element.RGBA_8888(mRS));
        scriptBlur.setRadius(16);


        blur = (ImageView) findViewById(R.id.blur);
        noblur = (ImageView) findViewById(R.id.noblur);

        noblur.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                noblur.getViewTreeObserver().removeOnPreDrawListener(this);
                noblur.buildDrawingCache();

                blurView(blur);
                return true;
            }
        });

    }

    Bitmap makeScreenShot(View v) {
        //Make a screenshot of the image
        v.setDrawingCacheEnabled(true);
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);

        return b;
    }



    void newView(View originalView, View newView) {
        originalView.setTag(newView);

        newView.setLayoutParams(new LinearLayout.LayoutParams(originalView.getLayoutParams()));

        ViewGroup parent = (ViewGroup) originalView.getParent();
        int index = parent.indexOfChild(originalView);
        parent.removeView(originalView);
        parent.addView(newView, index);
    }


    void blurView(View v) {

        Bitmap viewScreenshot = makeScreenShot(v);

        // specifies where to store the screenshot and the temporary blurred image
        screenshot = Allocation.createFromBitmap(mRS, viewScreenshot);

        // specifies where to store the blur results
        blurred = Allocation.createTyped(mRS, screenshot.getType(), Allocation.USAGE_SCRIPT | Allocation.USAGE_IO_OUTPUT);


        blurredView = new TextureView(this);
        blurredView.setOpaque(false);
        blurredView.setSurfaceTextureListener(surfaceTextureListener);

        newView(v, blurredView);

    }


    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            // Once the surface is ready, execute the blur
            blurred.setSurface(new Surface(surfaceTexture));

            scriptBlur.setInput(screenshot);
            scriptBlur.forEach(blurred);

            blurred.ioSend();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

}