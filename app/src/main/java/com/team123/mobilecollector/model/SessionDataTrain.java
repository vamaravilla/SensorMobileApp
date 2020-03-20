package com.team123.mobilecollector.model;

import java.util.Date;

public class SessionDataTrain {

    public String sessionID;
    public String userID;
    public String time;
    //public int numberOfSensors;
    //public String sensorId;
    public String sensorPosition;
    //public Date timeStamp;
    public String activity;
    //public SensorData sensorData;
    private SensorData sensorData[];

    public SessionDataTrain(String userId, String sessionId){
        this.userID = userId;
        this.sessionID = sessionId;
        sensorData = new SensorData[129];
    }

    public void setSensorData(SensorData data, int position){
        sensorData[position-1] = data;
    }


    public void Overlap(){
        int overlap = 65;
        for(int i =0; i<65 ; i++){
            sensorData[i] = sensorData[(i+overlap)-1];
        }
    }

/*
    public String getRecordInfoString(){
        if(sensorData == null) return "";

        return  (new StringBuilder()
                .append(sessionID).append(",")
                .append(userID).append(",")
                .append(time).append(",")
                //.append(numberOfSensors).append(",")
                //.append(sensorId).append(",")
                //.append(sensorPosition).append(",")
                .append(activity).append(",")
                //.append(timeStamp.toString()).append(",")
                .append(sensorData.laccelerationX).append(",")
                .append(sensorData.laccelerationY).append(",")
                .append(sensorData.laccelerationZ).append(",")
                .append(sensorData.gyroX).append(",")
                .append(sensorData.gyroY).append(",")
                .append(sensorData.gyroZ)
        ).toString();
    }
    */




}
