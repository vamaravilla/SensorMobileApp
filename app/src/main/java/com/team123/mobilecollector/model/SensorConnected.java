package com.team123.mobilecollector.model;

import com.movesense.mds.MdsSubscription;
import com.polidea.rxandroidble.RxBleDevice;

public class SensorConnected {

    private RxBleDevice RxBleDevice;
    private String location;
    private boolean connected;
    private String Id;
    private String serial;
    private MdsSubscription suscripcionIMU;
    public Integer imuNotifiCount;
    private int batteryLevel;

    public SensorConnected(RxBleDevice device){
        RxBleDevice = device;
        connected = false;
        if(device != null) {
            this.Id = device.getMacAddress();
            this.serial = device.getName().replace("Movesense ","");
        }
        imuNotifiCount=0;
    }
    public SensorConnected(RxBleDevice device,String location, Boolean connectedStatus){
        this.RxBleDevice = device;
        this.location = location;
        this.connected = connectedStatus;
        if(device != null) {
            this.Id = device.getMacAddress();
            this.serial = device.getName().replace("Movesense ","");
        }
        imuNotifiCount=0;
    }
    public MdsSubscription getSuscripcionIMU() {
        return suscripcionIMU;
    }

    public void setSuscripcionIMU(MdsSubscription suscripcionIMU) {
        this.suscripcionIMU = suscripcionIMU;
    }

    public void setSensor(RxBleDevice device){
        this.RxBleDevice = device;

        if(device != null) {
            this.Id = device.getMacAddress();
            this.serial = device.getName().replace("Movesense ","");
        }
        imuNotifiCount=0;
    }

    public RxBleDevice getSensor(){
        return this.RxBleDevice;
    }


    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation(){
        return this.location;
    }

    public void setConnectedStatus(Boolean connected){
        this.connected = connected;
    }

    public Boolean getConectedStatus(){
        return this.connected;
    }

    public String getId(){
        return this.Id;
    }

    public String getSerial(){
        return this.serial;
    }

    public int getBatteryLevel(){
        return this.batteryLevel;
    }
    public void setBatteryLevel(int level){
        this.batteryLevel = level;
    }
    public void unsuscribeAll(){

        if(this.suscripcionIMU != null){
            this.suscripcionIMU.unsubscribe();
        }
    }
    @Override
    public boolean equals(Object obj) {
        SensorConnected sensor = (SensorConnected)obj;
        return (sensor.getId() == this.Id);
    }
}
