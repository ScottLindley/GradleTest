package com.lindleydev.scott.canvasapp;


/**
 * Created by Scott on 1/28/17.
 */
public class Pointer implements Comparable<Pointer>{
    private int mId;
    private float mX;
    private float mY;
    private float radius = 1;
    private int[] mColor;

    public Pointer(int id, float x, float y, int[] color) {
        mId = id;
        mX = x;
        mY = y;
        mColor = color;
    }

    public int getId() {
        return mId;
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setX(float x) {
        mX = x;
    }

    public void setY(float y) {
        mY = y;
    }

    public int[] getColor() {
        return mColor;
    }


    @Override
    public int compareTo(Pointer pointer) {
        return mId - pointer.getId();
    }
}
