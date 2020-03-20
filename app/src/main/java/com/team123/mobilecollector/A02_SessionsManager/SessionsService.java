package com.team123.mobilecollector.A02_SessionsManager;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsSubscription;
import com.team123.kinesisclient.MobileCollectorKinesisClient;
import com.team123.mobilecollector.A00_MainView.MainViewActivity;
import com.team123.mobilecollector.A01_SensorsSetup.MultiConnectionActivity;
import com.team123.mobilecollector.model.ImuModel;
import com.team123.mobilecollector.model.SensorConnected;
import com.team123.mobilecollector.model.SensorData;
import com.team123.mobilecollector.model.SessionData;
import com.team123.mobilecollector.utils.ActivityOperations;
import com.team123.mobilecollector.utils.FileOperations;
import com.team123.mobilecollector.utils.FormatHelper;
import com.team123.mobilecollector.utils.UploadProcessBackground;

import java.util.Calendar;

public class SessionsService  extends Service  implements ActivityOperations.KinesisLitener {

    private final String IMU_PATH = "Meas/IMU6/52";
    private SessionData sessionData;
    private MobileCollectorKinesisClient kinesisClient;
    private String accessKey="AKIAI7CICRUNIDQVUFXQ";
    private String secretKey="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";
    private String streamName="movesense-test-stream";
    //private String activitySelected;
    private int window = 129; //Readings
    private int overlap = 64;
    private int windowCount = 0;
    //private final String LINEAR_ACC_PATH = "Meas/Acc/13";
    //private final String ANGULAR_VELOCITY_PATH = "Meas/Gyro/13";
    //private final String MAGNETIC_FIELD_PATH = "Meas/Magn/13";
    //private final String TEMPERATURE_PATH = "Meas/Temp";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Create a kinesis client
        kinesisClient = new MobileCollectorKinesisClient(
                FileOperations.createFileForKinesisClient(),
                accessKey, secretKey, streamName);

        //Getting extras (parameters from activity)
        /*Bundle extras = intent.getExtras();
        if(extras == null){
            activitySelected ="";
        }else{
            activitySelected = (String) extras.get("activitySelected");
        }*/

        //Basic data for session
        sessionData = new SessionData(
                MainViewActivity.USER_NAME,
                android.text.format.DateFormat.format("yyyyMMddhhmmss", new java.util.Date()).toString());
        //sessionData.numberOfSensors = MultiConnectionActivity.sensorsConnected.size();
        sessionData.time =  Long.toString(System.currentTimeMillis());
        //sessionData.activity = activitySelected;


        suscribeSensors();
        //uploadKinesisData();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for(SensorConnected sensorItem: MultiConnectionActivity.sensorsConnected) {
            sensorItem.imuNotifiCount = 0;
        }
        unSuscribeSensors();
    }

    private void suscribeSensors(){

        //For each sensor create suscription for Linear Acceleration and Gyro
        for(SensorConnected sensorItem: MultiConnectionActivity.sensorsConnected) {
            //Reset count
            sensorItem.imuNotifiCount = 0;
            //For Linear Acc and Gyro
            MdsSubscription suscriptionIMU = Mds.builder().build(this).subscribe("suunto://MDS/EventListener",
                    FormatHelper.formatContractToJson(sensorItem.getSerial(), IMU_PATH), new MdsNotificationListener() {
                        @Override
                        public void onNotification(String s) {
                            ImuModel IMUData = new Gson().fromJson(s, ImuModel.class);
                            if (IMUData.getBody().getArrayAcc() != null && IMUData.getBody().getArrayGyro() != null) {
                                //Save data
                                SensorData sensorData = new SensorData();
                                sensorData.laccelerationX = IMUData.getBody().getArrayAcc()[0].getX();
                                sensorData.laccelerationY = IMUData.getBody().getArrayAcc()[0].getY();
                                sensorData.laccelerationZ = IMUData.getBody().getArrayAcc()[0].getZ();
                                sensorData.gyroX = IMUData.getBody().getArrayGyro()[0].getX();
                                sensorData.gyroY = IMUData.getBody().getArrayGyro()[0].getY();
                                sensorData.gyroZ = IMUData.getBody().getArrayGyro()[0].getZ();
                                saveDataSuscriptionIMU(sensorItem, sensorData);
                            }
                        }

                        @Override
                        public void onError(MdsException e) {
                            Toast.makeText(SessionsService.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
            sensorItem.setSuscripcionIMU(suscriptionIMU);
        }
    }

    private void saveDataSuscriptionIMU(SensorConnected sensor,SensorData sesorData){

        /*
        deviceConnectedViewHolder =
                (ConnectedDevicesAdapter.DeviceConnectedViewHolder) deviceList.findViewHolderForItemId(sensor.getSensor()
                        .getMacAddress().hashCode());
        if (deviceConnectedViewHolder != null) {

            deviceConnectedViewHolder.setRecord(sensor.imuNotifiCount.toString());
        }
        */

        sensor.imuNotifiCount += 1;
        sessionData.setSensorData(sesorData,sensor.imuNotifiCount);
        //sessionData.sensorId = sensor.getSerial();
        //sessionData.sensorPosition = sensor.getLocation();
        //sessionData.timeStamp = Calendar.getInstance().getTime();
        //sessionData.sensorData = sesorData;

        if(sensor.imuNotifiCount == window){
            windowCount++;
            //Update count in activity
            sendDataSensorToActivity("sensorx",sensor.getSensor().getMacAddress()+"/"+ windowCount);
            //We do not write csv file because we use kinesis client instead
            kinesisClient.saveRecord(sessionData);
            uploadKinesisData();

            //New window
            sessionData.time =  Long.toString(System.currentTimeMillis());
            sessionData.Overlap();
            sensor.imuNotifiCount -= overlap;
        }



    }

    private void uploadKinesisData(){

        UploadProcessBackground asyncTaskUpload =new UploadProcessBackground(this,kinesisClient);
        asyncTaskUpload.execute();

    }


    private void unSuscribeSensors(){

        for(SensorConnected sensorItem: MultiConnectionActivity.sensorsConnected){
            sensorItem.unsuscribeAll();
            sensorItem.imuNotifiCount = 0;
        }
    }

    private void sendDataSensorToActivity(String sensor,String value) {
        Intent intent = new Intent("sensorDataCount");
        intent.putExtra(sensor, value);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onUploadCompleted(String message) {
        if(!message.equals("success")) {
            Toast.makeText(SessionsService.this, "Upload failed: " + message, Toast.LENGTH_SHORT).show();
        }

        /*if(message.equals("success")) {
            uploadKinesisData();
        }else{
            Toast.makeText(SessionsService.this, "Upload failed: " + message, Toast.LENGTH_SHORT).show();
        }*/
    }
}
