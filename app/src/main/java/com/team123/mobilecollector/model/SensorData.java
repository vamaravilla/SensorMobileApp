package com.team123.mobilecollector.model;

public class SensorData {

    public double laccelerationX;
    public double laccelerationY;
    public double laccelerationZ;

    public double gyroX;
    public double gyroY;
    public double gyroZ;

    public void setAcc(double dX,double dY, double dZ){
            this.laccelerationX = dX;
            this.laccelerationY = dY;
            this.laccelerationZ = dZ;
    }
    public void setGyro(double dX,double dY, double dZ){
        this.gyroX = dX;
        this.gyroY = dY;
        this.gyroZ = dZ;
    }
}

