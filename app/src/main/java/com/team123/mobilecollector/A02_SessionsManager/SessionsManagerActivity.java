package com.team123.mobilecollector.A02_SessionsManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.team123.mobilecollector.A01_SensorsSetup.MultiConnectionActivity;
import com.team123.mobilecollector.BaseActivity;
import com.team123.mobilecollector.R;
import com.team123.mobilecollector.bluetooth.MdsRx;
import com.team123.mobilecollector.model.EnergyGetModel;
import com.team123.mobilecollector.model.MdsConnectedDevice;
import com.team123.mobilecollector.model.SensorConnected;
import com.team123.mobilecollector.A00_MainView.MainViewActivity;
import com.team123.mobilecollector.utils.ConnectedDevicesAdapter;
import com.team123.mobilecollector.utils.ActivityOperations;
import com.team123.mobilecollector.utils.ThrowableToastingAction;
import com.team123.tensorflow.Estimate;
import com.team123.tensorflow.FeatureData;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class SessionsManagerActivity extends BaseActivity implements SessionsManagerContract.View,ActivityOperations.ActicitySelectionListener {

    @BindView(R.id.sessionManger_stop) TextView sessionManger_stop_tv;
    @BindView(R.id.sessionManger_activity_tv) TextView sessionManger_activity_tv;
    @BindView(R.id.sessionManger_img) ImageView sessionManger_img;


    private ArrayList<SensorConnected> listDevices;
    private CompositeSubscription mCompositeSubscription;
    private ConnectedDevicesAdapter connectedDevicesAdapter;
    private SessionsManagerPresenter mPresenter;
    private Boolean sessionStarted;
    private final String TAG = SessionsManagerActivity.class.getSimpleName();
    private ConnectedDevicesAdapter.DeviceConnectedViewHolder deviceConnectedViewHolder;
    private RecyclerView deviceList;
    private String activitySelected;
    private SessionsActivityFragment sessionsActivityFragment;
    private final String BATTERY_PATH_GET = "/System/Energy/Level";
    final Handler handler = new Handler();

    //Websocket
    private WebSocketClient mWebSocketClient;
    //private String uri_features =  "ws://immense-woodland-38027.herokuapp.com/api/socket"; //"ws://echo.websocket.org";
    //private String uri_features =  "ws://mteam123.azurewebsites.net/api/socket";
    private String uri_features = "ws://movesensesocket.herokuapp.com/api/socket";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions_manager);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Recognition Manager");
        }
        sessionStarted = true;
        mPresenter = new SessionsManagerPresenter(this);
        mCompositeSubscription = new CompositeSubscription();
        activitySelected = "..."; //By default

        //Creating item_movesense_connected foreach device
        connectedDevicesAdapter = new ConnectedDevicesAdapter(this);
        deviceList = this.findViewById(R.id.scroll_sensors_connected);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.setAdapter(connectedDevicesAdapter);
        deviceList.setItemAnimator(null);

        listDevices = MultiConnectionActivity.sensorsConnected;
        for(SensorConnected sensorItem: listDevices){
            //Get battery level
            getBatteyLevelFromSensor(sensorItem);
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
                                    startActivity(new Intent(SessionsManagerActivity.this, MainViewActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                }
                            }, 1000);
                        }
                    }
                }, new ThrowableToastingAction(this)));

        //Register ServiceReceiver
        LocalBroadcastManager.getInstance(SessionsManagerActivity.this).registerReceiver(
                mMessageReceiver, new IntentFilter("sensorDataCount"));
        //Start session service to subscribe and upload data from sensor
        StartSessionService();

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
        sessionManger_stop_tv.setText("Stop Recognition");
        sessionStarted = true;
        //sessionManger_activity_tv.setText("Activity: "+activitySelected);
        sessionManger_img.setImageResource(R.drawable.ic_estimate_24dp);


        Intent serviceIntent = new Intent(this,SessionsService.class);
        serviceIntent.putExtra("activitySelected",activitySelected);
        startService(serviceIntent);

        //Connect Socket
        connectWebSocket();
    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI(uri_features);
        } catch (URISyntaxException e) {
            Toast.makeText(SessionsManagerActivity.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //--->Enviar el mensaje a tensorflow formateado para obtener el resultado
                        if(message.length()>0 && !message.contains("{"))
                            estimateOnline(message);
                        else {
                            //Temporal
                            if (message.length() > 26) {
                                sessionManger_activity_tv.setText(message.substring(0, 25));
                            } else {
                                sessionManger_activity_tv.setText(message);
                            }
                        }

                        Log.i("Websocket", "Message: " + message);
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(SessionsManagerActivity.this, "Error websocket: " + e.toString(), Toast.LENGTH_SHORT).show();
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    private void disconnectWebSocket(){

        if(mWebSocketClient.isOpen()){
            mWebSocketClient.close();
        }

    }


    private void StopSessionService(){
        sessionManger_stop_tv.setText("Start Recognition");
        sessionStarted = false;

        for(SensorConnected sensorItem: listDevices){
            deviceConnectedViewHolder =
                    (ConnectedDevicesAdapter.DeviceConnectedViewHolder)deviceList.findViewHolderForItemId(sensorItem.getSensor()
                            .getMacAddress().hashCode());
            if(deviceConnectedViewHolder != null){
                deviceConnectedViewHolder.setRecord("0");
            }
        }
        stopService(new Intent(this, SessionsService.class));
        disconnectWebSocket();
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

    @OnClick({R.id.sessionManger_stop, R.id.sessionManger_img/*,R.id.sessionManger_upload*/})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sessionManger_stop:
                //Toast.makeText(this, "Wait just some hours!", Toast.LENGTH_SHORT).show();
                if(sessionStarted){
                    StopSessionService();
                }else{
                    StartSessionService();
                }
                break;
            /*case R.id.sessionManger_img:
                if(!sessionStarted){
                    sessionsActivityFragment = new SessionsActivityFragment();
                    sessionsActivityFragment.show(getSupportFragmentManager(), SessionsActivityFragment.class.getName());
                }else{
                    Toast.makeText(this, "Cannot change the activity while training is running!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sessionManger_upload:
                //Toast.makeText(this, "Espera por la promesa de Guille!", Toast.LENGTH_SHORT).show();
                estimateOnline();

                break;
            */
        }

    }

    private void estimateOnline(String values){
        Estimate testEstimate = new Estimate();
        //FeatureData data = testEstimate.extractFeaturesData(Estimate.getTestValues()); //Testing
        FeatureData data = testEstimate.extractFeaturesData(values);
        ArrayList<ArrayList<String>>  result = testEstimate.EstimateHA(data);
        if(result.size() > 0){
            //Toast.makeText(this, "Result: " + result.get(0).get(0), Toast.LENGTH_SHORT).show();
            //Result: 1:  0:expected 1:label_expected 2:prediction 3:label_prediction
            //sessionManger_activity_tv.setText(result.get(1).get(3) + " / Accuracy: "+testEstimate.getCurrentAccuracy() );
            sessionManger_activity_tv.setText(result.get(0).get(1));
        }
    }

    @Override
    public void setPresenter(SessionsManagerContract.Presenter presenter) {

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
                        SessionsManagerActivity.super.onBackPressed();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void onActivitySelected(String activity) {

        sessionsActivityFragment.dismiss();
        activitySelected = activity;
        sessionManger_activity_tv.setText("Activity: "+activity);
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
