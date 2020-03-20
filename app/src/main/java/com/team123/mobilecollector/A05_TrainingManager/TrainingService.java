package com.team123.mobilecollector.A05_TrainingManager;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.team123.mobilecollector.model.SessionDataTrain;
import com.team123.mobilecollector.model.SessionDataTrainSimple;
import com.team123.mobilecollector.utils.ActivityOperations;
import com.team123.mobilecollector.utils.FileOperations;
import com.team123.mobilecollector.utils.FormatHelper;
import com.team123.mobilecollector.utils.UploadProcessBackground;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Calendar;

public class TrainingService  extends Service  implements ActivityOperations.KinesisLitener {

    private final String IMU_PATH = "Meas/IMU6/52";
    private SessionDataTrain sessionData;
    private SessionDataTrainSimple sessionDataSimple;
    private MobileCollectorKinesisClient kinesisClient;
    private String accessKey="AKIAI7CICRUNIDQVUFXQ";
    private String secretKey="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";
    private String streamName="movesense-train-stream";
    private String activitySelected;
    private boolean online;
    private boolean fileOk;
    //private final String LINEAR_ACC_PATH = "Meas/Acc/13";
    //private final String ANGULAR_VELOCITY_PATH = "Meas/Gyro/13";
    //private final String MAGNETIC_FIELD_PATH = "Meas/Magn/13";
    //private final String TEMPERATURE_PATH = "Meas/Temp";
    private int window = 129; //Readings
    private int overlap = 64;
    private int windowCount = 0;
    NetworkInfo activeNetwork;
    private String savingMethod;

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

        //ConnectivityM anager
        ConnectivityManager cm =
                (ConnectivityManager) this.getBaseContext().getSystemService(this.getBaseContext().CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();
        online = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();


        //Getting extras (parameters from activity)
        Bundle extras = intent.getExtras();
        if(extras == null){
            activitySelected ="";
        }else{
            activitySelected = (String) extras.get("activitySelected");
            savingMethod = (String) extras.get("savingMethod");
        }
        //Basic data for session
        sessionData = new SessionDataTrain(
                MainViewActivity.USER_NAME,
                android.text.format.DateFormat.format("yyyyMMddhhmmss", new java.util.Date()).toString());
        sessionDataSimple = new SessionDataTrainSimple(
                MainViewActivity.USER_NAME,
                android.text.format.DateFormat.format("yyyyMMddhhmmss", new java.util.Date()).toString());
        //sessionData.numberOfSensors = MultiConnectionActivity.sensorsConnected.size();
        sessionData.time =  Long.toString(System.currentTimeMillis());
        sessionDataSimple.time = sessionData.time;
        sessionData.activity = activitySelected;
        sessionDataSimple.activity = activitySelected;

        //Check saving method
        online = isOnline();
        if(!online){
            //CSV File
            if(FileOperations.createFile("s"+sessionData.sessionID) == false){
                Toast.makeText(TrainingService.this, "Error creating file session", Toast.LENGTH_SHORT).show();
                fileOk = false;
            }else{
                Toast.makeText(TrainingService.this, "No internet: Session has started recording in CSV File", Toast.LENGTH_SHORT).show();
                fileOk = true;
            }
        }else{
            Toast.makeText(TrainingService.this, "Internet available: Session has started to upload data", Toast.LENGTH_SHORT).show();
        }


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
                            Toast.makeText(TrainingService.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
            sensorItem.setSuscripcionIMU(suscriptionIMU);
        }
    }

    private void saveDataSuscriptionIMU(SensorConnected sensor,SensorData sesorData){

        /*
        // OLD WAY
        sessionData.sensorData = sesorData;
        sessionData.time =  Long.toString(System.currentTimeMillis());

        sensor.imuNotifiCount++;

        //Update count in activity
        sendDataSensorToActivity("sensorx",sensor.getSensor().getMacAddress()+"/"+ sensor.imuNotifiCount);

        if(online){
            //We do not write csv file because we use kinesis client instead
            kinesisClient.saveRecordTrain(sessionData);
            uploadKinesisData();
        }else{
            //Write Csv
            if(fileOk)
                FileOperations.writeSessionData(sessionData.getRecordInfoString());
        }

        */


        if(online) {
            sensor.imuNotifiCount += 1;
            sessionData.setSensorData(sesorData, sensor.imuNotifiCount);

            if (sensor.imuNotifiCount == window) {
                windowCount++;
                //Update count in activity
                sendDataSensorToActivity("sensorx", sensor.getSensor().getMacAddress() + "/" + windowCount);
                //We do not write csv file because we use kinesis client instead
                sessionData.sensorPosition = sensor.getLocation();
                kinesisClient.saveRecordTrain(sessionData);
                uploadKinesisData();

                //New window
                sessionData.time = Long.toString(System.currentTimeMillis());
                sessionData.Overlap();
                sensor.imuNotifiCount -= overlap;
            }

        }else{
            sessionDataSimple.sensorData = sesorData;
            sessionDataSimple.time =  Long.toString(System.currentTimeMillis());

            sensor.imuNotifiCount++;

            //Update count in activity
            sendDataSensorToActivity("sensorx",sensor.getSensor().getMacAddress()+"/"+ sensor.imuNotifiCount);
                //Write Csv
            if(fileOk)
                    FileOperations.writeSessionData(sessionDataSimple.getRecordInfoString());

        }

    }

    private void uploadKinesisData(){

        UploadProcessBackground asyncTaskUpload =new UploadProcessBackground(this,kinesisClient);
        asyncTaskUpload.execute();

    }

    // TCP/HTTP/DNS (depending on the port, 53=DNS, 80=HTTP, etc.)
    public boolean isOnline() {

        if(savingMethod.equals("AWS")){
            return true;
        }else{
            return false;
        }
        /*try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(sockaddr, timeoutMs);
            sock.close();

            return true;
        }
        catch (ConnectException e){ return false; }
        catch (IOException e) { return false; }
        catch (RuntimeException e) { return false; }
        catch (Exception e) { return false; }

        */

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
            Toast.makeText(TrainingService.this, "Upload failed: " + message, Toast.LENGTH_SHORT).show();
        }

        /*if(message.equals("success")) {
            uploadKinesisData();
        }else{
            Toast.makeText(SessionsService.this, "Upload failed: " + message, Toast.LENGTH_SHORT).show();
        }*/
    }
}
