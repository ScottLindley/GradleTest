package com.lindleydev.scott.canvasapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class Sim2Activity extends AppCompatActivity {
    private DrawingView mDrawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
    }

    @Override
    protected void onResume() {
        setContentView(R.layout.activity_drawing);
        mDrawingView = (DrawingView)findViewById(R.id.drawing_view);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDrawingView.getThread() != null && mDrawingView.getThread().isRunning()) {
            mDrawingView.getThread().setRunning(false);
        }
    }
}
