package com.team123.mobilecollector.A05_TrainingManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.team123.mobilecollector.A00_MainView.MainViewActivity;
import com.team123.mobilecollector.A01_SensorsSetup.MultiConnectionActivity;
import com.team123.mobilecollector.BaseActivity;
import com.team123.mobilecollector.R;
import com.team123.mobilecollector.bluetooth.MdsRx;
import com.team123.mobilecollector.model.EnergyGetModel;
import com.team123.mobilecollector.model.MdsConnectedDevice;
import com.team123.mobilecollector.model.SensorConnected;
import com.team123.mobilecollector.utils.ActivityOperations;
import com.team123.mobilecollector.utils.ConnectedDevicesAdapter;
import com.team123.mobilecollector.utils.ThrowableToastingAction;
import com.team123.tensorflow.Estimate;
import com.team123.tensorflow.FeatureData;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class TrainingManagerActivity extends BaseActivity implements TrainingManagerContract.View,ActivityOperations.ActicitySelectionListener {

    @BindView(R.id.sessionManger_start) TextView sessionManger_start_tv;
    //@BindView(R.id.sessionManger_activity_tv) TextView sessionManger_activity_tv;
    //@BindView(R.id.sessionManger_img) ImageView sessionManger_img;
    @BindView(R.id.sessionManger_stop) TextView sessionManger_stop_tv;
    @BindView(R.id.sessionManger_savingmethod_tv) TextView sessionManger_savingmethod_tv;
    @BindView(R.id.sessionManger_activity_tv) TextView sessionManger_activity_tv;


    private ArrayList<SensorConnected> listDevices;
    private CompositeSubscription mCompositeSubscription;
    private ConnectedDevicesAdapter connectedDevicesAdapter;
    private TrainingManagerPresenter mPresenter;
    private Boolean sessionStarted;
    private final String TAG = TrainingManagerActivity.class.getSimpleName();
    private ConnectedDevicesAdapter.DeviceConnectedViewHolder deviceConnectedViewHolder;
    private RecyclerView deviceList;
    private String activitySelected;
    private TrainingActivityFragment sessionsActivityFragment;
    private final String BATTERY_PATH_GET = "/System/Energy/Level";
    final Handler handler = new Handler();
    private final String GYRO_CONFIG_PATH = "/Meas/Gyro/Config";
    private final String LINEAR_CONFIG_PATH = "/Meas/Acc/Config";
    private final String range = "\"config\":{\"GRange\":";
    private final String rangeValue = "16";
    private Boolean filemode = false;
    private RadioGroup radioGroup;
    private String savingMethod;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_manager);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Training Manager");
        }
        sessionStarted = false;
        mPresenter = new TrainingManagerPresenter(this);
        mCompositeSubscription = new CompositeSubscription();
        activitySelected = "..."; //By default

        //Creating item_movesense_connected foreach device
        connectedDevicesAdapter = new ConnectedDevicesAdapter(this);
        deviceList = this.findViewById(R.id.scroll_sensors_connected);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.setAdapter(connectedDevicesAdapter);
        deviceList.setItemAnimator(null);

        //Find radio group
        radioGroup = this.findViewById(R.id.rg_session_activities);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked)
                {
                    activitySelected = checkedRadioButton.getText().toString();
                    //StopSessionService();
                }
            }
        });

        listDevices = MultiConnectionActivity.sensorsConnected;
        for(SensorConnected sensorItem: listDevices){
            //Get battery level
            getBatteyLevelFromSensor(sensorItem);
            //getACCRangeFromSensor(sensorItem);
            //getGyroRangeFromSensor(sensorItem);
            connectedDevicesAdapter.handleAddSensor(sensorItem);
        }


        mCompositeSubscription.add(MdsRx.Instance.connectedDeviceObservable()
                .subscribe(new Action1<MdsConnectedDevice>() {
                    @Override
                    public void call(MdsConnectedDevice mdsConnectedDevice) {
                        if (mdsConnectedDevice.getConnection() == null) {

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(TrainingManagerActivity.this, MainViewActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                }
                            }, 1000);
                        }
                    }
                }, new ThrowableToastingAction(this)));

        //Register ServiceReceiver
        LocalBroadcastManager.getInstance(TrainingManagerActivity.this).registerReceiver(
                mMessageReceiver, new IntentFilter("sensorDataCount"));


        //Start session service to subscribe and upload data from sensor
        //StartSessionService();
        //Select saving method
        sessionsActivityFragment = new TrainingActivityFragment();
        sessionsActivityFragment.show(getSupportFragmentManager(), TrainingActivityFragment.class.getName());

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String valueMessage;
            // Get extra data included in the Intent
            valueMessage = intent.getStringExtra("sensorx");
            String[] values = valueMessage.split("/");

            if(values != null && values.length == 2){
                deviceConnectedViewHolder =
                        (ConnectedDevicesAdapter.DeviceConnectedViewHolder) deviceList.findViewHolderForItemId(values[0].hashCode());
                if (deviceConnectedViewHolder != null) {
                    deviceConnectedViewHolder.setRecord(values[1]);
                }
            }
        }
    };

    private void StartSessionService(){

        if(activitySelected.equals("...")){
            int radioCheckedId = radioGroup.getCheckedRadioButtonId();
            RadioButton radioCheked = radioGroup.findViewById(radioCheckedId);
            activitySelected = radioCheked.getText().toString();

            //sessionsActivityFragment = new TrainingActivityFragment();
            //sessionsActivityFragment.show(getSupportFragmentManager(), TrainingActivityFragment.class.getName());
        }
        sessionManger_activity_tv.setText("Current activity: "+ activitySelected);

        //sessionManger_stop_tv.setText("Stop Training");
        sessionStarted = true;
        //sessionManger_activity_tv.setText("Activity: "+activitySelected);
        //sessionManger_img.setImageResource(R.drawable.ic_walk_24dp);
        Intent serviceIntent = new Intent(this, TrainingService.class);
        serviceIntent.putExtra("activitySelected",activitySelected);
        serviceIntent.putExtra("savingMethod",savingMethod);
        startService(serviceIntent);

    }

    private void StopSessionService(){
        //sessionManger_stop_tv.setText("Start Training");
        sessionStarted = false;
        sessionManger_activity_tv.setText("Current activity: "+ activitySelected);

        for(SensorConnected sensorItem: listDevices){
            deviceConnectedViewHolder =
                    (ConnectedDevicesAdapter.DeviceConnectedViewHolder)deviceList.findViewHolderForItemId(sensorItem.getSensor()
                            .getMacAddress().hashCode());
            if(deviceConnectedViewHolder != null){
                deviceConnectedViewHolder.setRecord("0");
            }
        }
        stopService(new Intent(this, TrainingService.class));

    }

    private void getBatteyLevelFromSensor(SensorConnected sensor){
        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX +
                        sensor.getSerial() + BATTERY_PATH_GET,
                null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String s) {

                        EnergyGetModel energyGetModel = new Gson().fromJson(s, EnergyGetModel.class);
                        sensor.setBatteryLevel(energyGetModel.content);

                        deviceConnectedViewHolder =
                                (ConnectedDevicesAdapter.DeviceConnectedViewHolder) deviceList.findViewHolderForItemId(sensor.getSensor()
                                        .getMacAddress().hashCode());
                        if (deviceConnectedViewHolder != null) {

                            deviceConnectedViewHolder.setBattery(String.valueOf(energyGetModel.content));
                        }
                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }

    private void getACCRangeFromSensor(SensorConnected sensor){
        /*Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX +
                        sensor.getSerial() + LINEAR_CONFIG_PATH,
                       range + rangeValue +"}", new
                        MdsResponseListener() {*/

        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX +
                        sensor.getSerial() + LINEAR_CONFIG_PATH,null, new
                        MdsResponseListener() {
                    @Override
                    public void onSuccess(String s) {
                        Log.e(TAG, "config acc: " + s);

                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }
    private void getGyroRangeFromSensor(SensorConnected sensor){
        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX +
                sensor.getSerial() + GYRO_CONFIG_PATH,null, new
                MdsResponseListener() {
                    @Override
                    public void onSuccess(String s) {
                        Log.e(TAG, "config gyro: " + s);

                    }

                    @Override
                    public void onError(MdsException e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }
    @OnClick({R.id.sessionManger_start, R.id.sessionManger_stop})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sessionManger_start:
                //Toast.makeText(this, "Wait just some hours!", Toast.LENGTH_SHORT).show();
                if(!sessionStarted){
                    StartSessionService();
                }else{
                    StopSessionService();
                    StartSessionService();
                    Toast.makeText(this, "Session is already started", Toast.LENGTH_SHORT).show();
                }
                break;
            /*case R.id.sessionManger_img:
                if(!sessionStarted){
                    sessionsActivityFragment = new TrainingActivityFragment();
                    sessionsActivityFragment.show(getSupportFragmentManager(), TrainingActivityFragment.class.getName());
                }else{
                    Toast.makeText(this, "Cannot change the activity while training is running!", Toast.LENGTH_SHORT).show();
                }
                break;
                */
            case R.id.sessionManger_stop:

                if(sessionStarted){
                    StopSessionService();
                }else{
                    Toast.makeText(this, "Session is already stopped", Toast.LENGTH_SHORT).show();
                }

                break;

        }

    }


    @Override
    public void setPresenter(TrainingManagerContract.Presenter presenter) {

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage(R.string.disconnect_dialog_text)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "Disconnecting...");

                        if(sessionStarted) {
                            StopSessionService();
                        }
                        int numSensor = 0;
                        for(SensorConnected sensorItem: listDevices){
                            sensorItem.unsuscribeAll();
                            BleManager.INSTANCE.disconnect(MovesenseConnectedDevices.getConnectedRxDevice(numSensor));
                            numSensor++;
                        }
                        MultiConnectionActivity.sensorsConnected.clear();
                        TrainingManagerActivity.super.onBackPressed();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void onActivitySelected(String method) {

        sessionsActivityFragment.dismiss();
        //sessionManger_activity_tv.setText("Activity: "+activity);
        savingMethod =method;
        sessionManger_savingmethod_tv.setText("Saving method: "+ savingMethod);
        sessionManger_activity_tv.setText("Current activity: "+ activitySelected);
        //StartSessionService();
    }

    @Override
    public void onCancelSelection() {
        sessionsActivityFragment.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(sessionStarted) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String NOTIFICATION_CHANNEL_ID = "channel_id_01";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

                // Configure the notification channel.
                notificationChannel.setDescription("Mobile collector Channel");
                notificationChannel.enableLights(true);
                //notificationChannel.setLightColor(Color.RED);
                //notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                //notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }


            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker("Hearty365")
                    .setContentTitle("Mobile Collectorn")
                    .setContentText("Training is running... "+activitySelected)
                    .setContentInfo("Info");

            notificationManager.notify(/*notification id*/1, notificationBuilder.build());

        }
    }
}
