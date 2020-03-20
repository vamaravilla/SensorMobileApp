package com.team123.mobilecollector.model;

public class SessionDataTrainSimple {

    public String sessionID;
    public String userID;
    public String time;
    //public int numberOfSensors;
    //public String sensorId;
    public String sensorPosition;
    //public Date timeStamp;
    public String activity;
    public SensorData sensorData;


    public SessionDataTrainSimple(String userId, String sessionId){
        this.userID = userId;
        this.sessionID = sessionId;
    }


    public String getRecordInfoString(){
        if(sensorData == null) return "";

        return  (new StringBuilder()
                .append(sessionID).append(",")
                .append(userID).append(",")
                .append(time).append(",")
                //.append(numberOfSensors).append(",")
                //.append(sensorId).append(",")
                .append(sensorPosition).append(",")
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





}
