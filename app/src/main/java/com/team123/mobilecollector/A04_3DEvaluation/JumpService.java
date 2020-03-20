package com.team123.mobilecollector.A04_3DEvaluation;

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
import com.team123.mobilecollector.model.JumpCountModel;
import com.team123.mobilecollector.model.SensorConnected;
import com.team123.mobilecollector.model.SensorData;
import com.team123.mobilecollector.model.SessionData;
import com.team123.mobilecollector.utils.ActivityOperations;
import com.team123.mobilecollector.utils.FileOperations;
import com.team123.mobilecollector.utils.FormatHelper;
import com.team123.mobilecollector.utils.UploadProcessBackground;

import java.util.Calendar;

public class JumpService extends Service  {

    private final String IMU_PATH = "Sample/JumpCounter/JumpCount";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        suscribeSensors();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unSuscribeSensors();
    }

    private void suscribeSensors(){

        //Just one sensor for Jump
        SensorConnected sensorItem = MultiConnectionActivity.sensorsConnected.get(0);
        //sendDataSensorToActivity("Count",FormatHelper.formatContractToJson(sensorItem.getSerial(), IMU_PATH));
        //Toast.makeText(JumpService.this, FormatHelper.formatContractToJson(sensorItem.getSerial(), IMU_PATH), Toast.LENGTH_SHORT).show();
        MdsSubscription suscriptionJump = Mds.builder().build(this).subscribe("suunto://MDS/EventListener",
                FormatHelper.formatContractToJson(sensorItem.getSerial(), IMU_PATH), new MdsNotificationListener() {
                    @Override
                    public void onNotification(String s) {
                        JumpCountModel jumpCountModel = new Gson().fromJson(s, JumpCountModel.class);

                        //Print Json
                        sendDataSensorToActivity("Count",String.valueOf(jumpCountModel.getBody()));
                    }

                    @Override
                    public void onError(MdsException e) {
                        Toast.makeText(JumpService.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                        sendDataSensorToActivity("Count","Error suscription Jump");
                    }
                });


            sensorItem.setSuscripcionIMU(suscriptionJump);
    }


    private void sendDataSensorToActivity(String sensor,String value) {
        Intent intent = new Intent("sensorJump");
        intent.putExtra(sensor, value);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void unSuscribeSensors(){

        for(SensorConnected sensorItem: MultiConnectionActivity.sensorsConnected){
            sensorItem.unsuscribeAll();
            //sensorItem.imuNotifiCount = 0;
        }
    }

}
