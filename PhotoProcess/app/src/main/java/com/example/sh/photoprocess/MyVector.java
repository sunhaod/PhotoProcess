package com.example.sh.photoprocess;

public class MyVector {
    private float x;
    private float y;
    public MyVector() {}
    public MyVector(float x,float y) {
        this.x = x;
        this.y = y;
    }
    public float getX(){
        return x;
    }
    public float getY(){
        return y;
    }
    public void setXY(float x, float y){
        this.x = x;
        this.y = y;
    }
    public MyVector minus(MyVector a){
       return new MyVector(this.x - a.x,this.y-a.y);
    }
}
