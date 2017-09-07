package com.lindleydev.scott.canvasapp;

/**
 * Created by Scott Lindley on 1/28/2017.
 */

public class Circle {

    private float mX;
    private float mY;
    private int mID;
    private float mRadius = 1;
    private float[] mVelocity;
    private double fallSpeedFactor;
    private int[] mColor;

    public Circle(int id, float x, float y, float radius, int[] color) {
        mID = id;
        mX = x;
        mY = y;
        mRadius = radius;
        mColor = color;
        fallSpeedFactor = Math.random() * 10;
        mVelocity = new float[]{0, 0};
    }

    public float getX() {
        return mX;
    }

    public void setX(float x) {
        mX = x;
    }

    public float getY() {
        return mY;
    }

    public void setY(float y) {
        mY = y;
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
    }

    public int[] getColor() {
        return mColor;
    }

    public double getFallSpeedFactor() {
        return fallSpeedFactor;
    }

    public void setFallSpeedFactor(double factor){
        fallSpeedFactor = factor;
    }

    public float[] getVelocity(){
        return mVelocity;
    }

    public void setVelocity(float[] velocity){
        mVelocity = velocity;
    }

    public int getID(){
        return mID;
    }
}
