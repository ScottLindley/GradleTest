package com.lindleydev.scott.canvasapp;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Scott on 1/28/17.
 */
public class DrawingThread extends Thread implements SensorEventListener{
    private static final int SHAKE_THRESHOLD = 900;
    private DrawingView mView;
    private Sensor mAccelSensor;
    private SensorManager mSensorManager;
    private long mCurrentTime;
    private long mLastTime;
    private float[] mXYZ;
    private float[] mLastXYZ;
    private float[] mNextPosition;
    private float mTimeDiff;
    private int mViewWidth;
    private int mViewHeight;
    private boolean mRun;
    private LinkedList<Circle> mCircles;

    public DrawingThread(DrawingView view) {
        mView = view;
        mViewWidth = view.getWidth();
        mViewHeight = view.getHeight();
        mRun = true;
        mCircles = mView.getCircles();
    }

    @Override
    public void run() {
        super.run();
        mXYZ = new float[3];
        mLastXYZ = new float[3];
        mNextPosition = new float[2];
        setUpAccelerometer();
        if (mView.getSimNumber() == 1) {
            startSim1Loop();
        } else {
            startSim2Loop();
        }
    }

    private void startSim1Loop(){
        while(mRun) {
            mView.beginDrawing();
            for (int i=0; i<mCircles.size(); i++) {
                mView.getPaint().setColor(Color.argb(255,
                        mCircles.get(i).getColor()[0],
                        mCircles.get(i).getColor()[1],
                        mCircles.get(i).getColor()[2]));
                mView.drawCircle(mCircles.get(i).getX(),
                        mCircles.get(i).getY(),
                        mCircles.get(i).getRadius());
                calculateNewVelocity(mCircles.get(i));
                calculateNewPosition(mCircles.get(i));
//                checkForBallCollision(mCircles.get(i));
                if (!checkForBorderCollision(mCircles.get(i))) {
                    mCircles.get(i).setX(mNextPosition[0]);
                    mCircles.get(i).setY(mNextPosition[1]);
                }
                mLastTime = mCurrentTime;
            }
            mView.commitDrawing();
            mCircles.add(mCircles.get(0));
            mCircles.remove(0);
            Log.d(TAG, "size = "+mCircles.size());
        }
        mSensorManager.unregisterListener(this);
    }

    private void startSim2Loop(){
        while(mRun){
            mView.beginDrawing();
            List<Pointer> pointers = mView.getPointers();
            for (int i=0; i<pointers.size(); i++){
                try {
                    Pointer p = pointers.get(i);
                    int[] color = p.getColor();
                    mView.getPaint().setColor(Color.argb(130, color[0], color[1], color[2]));
                    mView.drawCircle(p.getX(), p.getY(), p.getRadius());
                    p.setRadius(p.getRadius()+3);
                } catch (ArrayIndexOutOfBoundsException e){

                }
            }
            for (int i=0; i<mCircles.size(); i++){
                Circle c = mCircles.get(i);
                int[] color = c.getColor();
                mView.getPaint().setColor(Color.argb(130, color[0], color[1], color[2]));
                if (c.getRadius() > 0) {
                    mView.drawCircle(c.getX(), c.getY(), c.getRadius());
                }
                double baseSpeed = c.getFallSpeedFactor();
                setNewXY(c, (float) baseSpeed);
            }
            mView.commitDrawing();
        }
        mSensorManager.unregisterListener(this);
    }


    private void setNewXY(Circle c, float baseSpeed) {
        float speedX = baseSpeed * mXYZ[0] / 10;
        float speedY = baseSpeed * mXYZ[1] / 10;

        boolean atTop = c.getY()-c.getRadius() <= 0;
        boolean atBottom = c.getY()+c.getRadius() >= mViewHeight;
        boolean atRight = c.getX()+c.getRadius() >= mViewWidth;
        boolean atLeft = c.getX()-c.getRadius() <= 0;

        if (!atRight && speedX < 0) {
            c.setX(c.getX() - speedX);
        }
        if (!atLeft && speedX > 0) {
            c.setX(c.getX() - speedX);
        }
        if (!atTop && speedY < 0){
            c.setY(c.getY() + speedY);
        }
        if (!atBottom && speedY > 0){
            c.setY(c.getY() + speedY);
        }
    }

    private void calculateNewVelocity(Circle c){
        mCurrentTime = System.currentTimeMillis();

        mTimeDiff = mCurrentTime - mLastTime;
        mTimeDiff /= 2;

        c.setVelocity(new float[]{c.getVelocity()[0] + (mXYZ[0]*mTimeDiff)/100
                , c.getVelocity()[1] + (mXYZ[1]*mTimeDiff)/100});
    }


    private void calculateNewPosition(Circle c){
        // x = x0 + v0t + 1/2at^2
        float timeDiffSQR = mTimeDiff * mTimeDiff;

        mNextPosition[0] = (c.getX() + (c.getVelocity()[0]*mTimeDiff) + ((1/2)*mXYZ[0]*timeDiffSQR)
                        + ((1/6)*mLastXYZ[0]*timeDiffSQR*mTimeDiff));
        mNextPosition[1] = (c.getY() + (c.getVelocity()[1]*mTimeDiff) + ((1/2)*mXYZ[1]*timeDiffSQR)
                + ((1/6)*mLastXYZ[1]*timeDiffSQR*mTimeDiff));
    }

    private void checkForBallCollision(Circle c1){
        float dxC1 = c1.getRadius();
        float dyC1 = c1.getRadius();
        for (int i=0; i<mCircles.size(); i++){
            if (Math.abs(mNextPosition[0] - mView.getCircleArray().get(i).getX()) <= dxC1*2
                    && Math.abs(mNextPosition[1] - mView.getCircleArray().get(i).getY()) <= dyC1*2
                    && c1.getID() != mView.getCircleArray().get(i).getID()){
                calculateBallCollision(c1, mView.getCircleArray().get(i));
            }
        }
    }

    private void calculateBallCollision(Circle c1, Circle c2){
        // v'1 = [(m1*v1)-(m2*v2)]/[(m1*v1)+(m2*v2)] * v1
        // v'2 = (2*m1*v1)/[(m1*v1)+(m2*v2)] * v1
        c1.setVelocity(new float[]{
                ((c1.getVelocity()[0] - c2.getVelocity()[0])/(c1.getVelocity()[0] + c2.getVelocity()[0]))*c1.getVelocity()[0]*(8/10),
                ((c1.getVelocity()[1] - c2.getVelocity()[1])/(c1.getVelocity()[1] + c2.getVelocity()[1]))*c1.getVelocity()[1]*(8/10)});
        calculateNewPosition(c1);

    }

    private boolean checkForBorderCollision(Circle c){
        boolean[] borders = checkForBorders(c);
        if (borders[0] && borders[1] && borders[2] && borders[3]){
            return false;
        }
        if(c.getVelocity()[0] < 0) {
            if (borders[0]) {
                c.setVelocity(new float[]{(float) (c.getVelocity()[0] * (-0.62)), c.getVelocity()[1]});
            } else {
                if (!borders[2]){
                    c.setX(mNextPosition[0]);
                }
            }
        }
        if (c.getVelocity()[1] < 0) {
            if (borders[1]){
                c.setVelocity(new float[]{c.getVelocity()[0], (float) (c.getVelocity()[1]*(-0.62))});
            } else {
                if (!borders[3]){
                    c.setY(mNextPosition[1]);
                }
            }
        }
        if (c.getVelocity()[0] > 0){
            if (borders[2]) {
                c.setVelocity(new float[]{(float) (c.getVelocity()[0]*(-0.62)), c.getVelocity()[1]});
            } else {
                if (!borders[0]){
                    c.setX(mNextPosition[0]);
                }
            }
        }
        if (c.getVelocity()[1] > 0){
            if (borders[3]){
                //not at bottom border with positive y velocity
                c.setVelocity(new float[]{c.getVelocity()[0], (float) (c.getVelocity()[1]*(-0.62))});
            } else {
                if (!borders[1]){
                    c.setY(mNextPosition[1]);
                }
            }
        }
        calculateNewPosition(c);
        return true;
//        Log.d(TAG, "velocity = "+c.getVelocity()[1]);
    }

    private boolean[] checkForBorders(Circle c){
        boolean[] borders = new boolean[4];
        borders[0] = mNextPosition[0]-c.getRadius() <= 0;
        borders[1] = mNextPosition[1]-c.getRadius() <= 0;
        borders[2] = mNextPosition[0]+c.getRadius() >= mViewWidth;
        borders[3] = mNextPosition[1]+c.getRadius() >= mViewHeight;
        return borders;
    }



    private void setUpAccelerometer(){
        mSensorManager =
                (SensorManager) mView.getContext().getSystemService(Context.SENSOR_SERVICE);
        mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

//            Log.d(TAG, x + ", " + y + ", " + z);

            if (mView.getSimNumber() == 2) {
                float[] values = sensorEvent.values;
                float x = values[0];
                float y = values[1];
                mCurrentTime = System.currentTimeMillis();
                long timeDiff = mCurrentTime - mLastTime;

                float lastX = mXYZ[0];
                float lastY = mXYZ[1];


                float phoneSpeed = Math.abs(x + y - lastX - lastY) / timeDiff * 1000;
                if (phoneSpeed > SHAKE_THRESHOLD) {
                    Log.d(TAG, "onSensorChanged: " + phoneSpeed);
                    mCircles.clear();
                }

                mLastTime = mCurrentTime;
            }
            mLastXYZ[0] = mXYZ[0];
            mLastXYZ[1] = mXYZ[1];

            if (mView.getSimNumber() == 1){
                mXYZ[0] = sensorEvent.values[0] * -1f;
            } else {
                mXYZ[0] = sensorEvent.values[0];
            }
            mXYZ[1] = sensorEvent.values[1];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public boolean isRunning() {
        return mRun;
    }

    public void setRunning(boolean mRun) {
        this.mRun = mRun;
    }



}
