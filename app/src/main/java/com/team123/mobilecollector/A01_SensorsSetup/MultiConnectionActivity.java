package com.team123.mobilecollector.A01_SensorsSetup;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleScanResult;
import com.team123.mobilecollector.A02_SessionsManager.SessionsManagerActivity;
import com.team123.mobilecollector.A04_3DEvaluation.EvaluationActivity;
import com.team123.mobilecollector.A05_TrainingManager.TrainingManagerActivity;
import com.team123.mobilecollector.R;
import com.team123.mobilecollector.bluetooth.MdsRx;
import com.team123.mobilecollector.bluetooth.RxBle;
import com.team123.mobilecollector.model.EnergyGetModel;
import com.team123.mobilecollector.model.MdsConnectedDevice;
import com.team123.mobilecollector.model.MdsDeviceInfoNewSw;
import com.team123.mobilecollector.model.MdsDeviceInfoOldSw;
import com.team123.mobilecollector.utils.ScannedDevicesAdapter;
import com.team123.mobilecollector.utils.ActivityOperations;
import com.polidea.rxandroidble.RxBleDevice;
import com.team123.mobilecollector.utils.ThrowableToastingAction;
import com.team123.mobilecollector.model.SensorConnected;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class MultiConnectionActivity extends AppCompatActivity implements ActivityOperations.DeviceSelectionListener, ActivityOperations.DeviceConnectListener {

    @BindView(R.id.multiConnection_start_session) TextView tv_start_session;
    @BindView(R.id.device_list_content) LinearLayout ly_device_list;
    @BindView(R.id.multiConnection_status) TextView tv_conecction_status;
    @BindView(R.id.multiConnection_refresh) ImageView img_refresh;

    //From fragment
    private BluetoothAdapter bluetoothAdapter;
    private ScannedDevicesAdapter scannedDevicesAdapter;
    private ActivityOperations.DeviceSelectionListener deviceSelectionListener;
    private RxBleClient rxBleClient;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private final String TAG = MultiConnectionActivity.class.getSimpleName();
    private final String LOG_TAG = MultiConnectionActivity.class.getSimpleName();
    private SensorSetupFragment sensorSetupFragment;
    private ScannedDevicesAdapter.DeviceViewHolder deviceViewHolder;
    public static ArrayList<SensorConnected> sensorsConnected = new ArrayList<SensorConnected>();
    private boolean isAddDevice1Pressed = false;
    private CompositeSubscription mCompositeSubscription;
    private SensorConnected currentSensorConnecting;
    private String operationType;


    @Override
    protected void onResume(){
        super.onResume();
        //startScanning(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_connection);
        ButterKnife.bind(this);

        //Getting extras parameters
        operationType = getIntent().getStringExtra("Type");

        if (getSupportActionBar() != null) {
            if(operationType.equals("3D")){
                getSupportActionBar().setTitle("3D - Sensors Setup");
                tv_start_session.setText("Start Evaluation");
            }else{
                if(operationType.equals("Training")){
                    getSupportActionBar().setTitle("Training - Sensors Setup");
                    tv_start_session.setText("Start Training");
                }else{
                    getSupportActionBar().setTitle("Recognition - Sensors Setup");
                    tv_start_session.setText("Start Recognition");
                }
            }

        }

        mCompositeSubscription = new CompositeSubscription();

        mCompositeSubscription.add(MdsRx.Instance.connectedDeviceObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MdsConnectedDevice>() {
                    @Override
                    public void call(MdsConnectedDevice mdsConnectedDevice) {
                        Boolean deviceConnectedNew = false;
                        Boolean deviceConnectedOld = false;
                        if (mdsConnectedDevice.getConnection() != null) {

                            if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoNewSw) {
                                MdsDeviceInfoNewSw mdsDeviceInfoNewSw = (MdsDeviceInfoNewSw) mdsConnectedDevice.getDeviceInfo();

                                MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(
                                        mdsDeviceInfoNewSw.getAddressInfoNew().get(0).getAddress(),
                                        mdsDeviceInfoNewSw.getDescription(),
                                        mdsDeviceInfoNewSw.getSerial(),
                                        mdsDeviceInfoNewSw.getSw()));
                                deviceConnectedNew = true;
                            } else if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoOldSw) {
                                MdsDeviceInfoOldSw mdsDeviceInfoOldSw = (MdsDeviceInfoOldSw) mdsConnectedDevice.getDeviceInfo();

                                MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(
                                        mdsDeviceInfoOldSw.getAddressInfoOld(),
                                        mdsDeviceInfoOldSw.getDescription(),
                                        mdsDeviceInfoOldSw.getSerial(),
                                        mdsDeviceInfoOldSw.getSw()));
                                deviceConnectedOld = true;
                            }
                            if(deviceConnectedNew || deviceConnectedOld){
                                //Exist at least one sensor connected
                                tv_conecction_status.setText("");
                                if(deviceViewHolder != null){
                                    deviceViewHolder.setState("Connected / " + currentSensorConnecting.getLocation());
                                }
                                tv_start_session.setEnabled(true);

                                //RecyclerView deviceList = findViewById(R.id.device_list);
                                //deviceList.setEnabled(true);
                            }

                        }
                    }
                }));

        tv_start_session.setEnabled(false);

        //from scanner fragment code
        deviceSelectionListener = (ActivityOperations.DeviceSelectionListener) this;

        this.registerReceiver(btReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        // Ask For Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enable so run
            bluetoothAdapter.enable();
        }

        // Capture instance of RxBleClient to make code look cleaner
        rxBleClient = RxBle.Instance.getClient();


        scannedDevicesAdapter = new ScannedDevicesAdapter(this, false);
        RecyclerView deviceList = this.findViewById(R.id.device_list);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.setAdapter(scannedDevicesAdapter);
        deviceList.setItemAnimator(null);

        // Listen for device selection
        Subscription selectionSubscription = scannedDevicesAdapter.deviceSelectionObservable()
                .subscribe(new Action1<RxBleDevice>() {
                    @Override
                    public void call(RxBleDevice rxBleDevice) {
                        deviceSelectionListener.onDeviceSelected(rxBleDevice);
                    }
                }, new ThrowableToastingAction(this));
        mCompositeSubscription.add(selectionSubscription);

        // Start scanning immediately
        startScanning(true);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.unsubscribe();
        mCompositeSubscription.clear();

        // Unregister BtReceiver
        this.unregisterReceiver(btReceiver);
    }

    @OnClick({R.id.multiConnection_start_session,R.id.multiConnection_refresh})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.multiConnection_start_session:

                if(sensorsConnected.size() > 0){
                    //Toast.makeText(this, sensorsConnected.size() + " device(s) connected", Toast.LENGTH_SHORT).show();
                    if(MovesenseConnectedDevices.getConnectedDevices().size() ==0){
                        Toast.makeText(this, MovesenseConnectedDevices.getConnectedDevices().size() + " device(s) connected", Toast.LENGTH_SHORT).show();

                    }else{
                        if(operationType.equals("3D")){
                            startActivity(new Intent(MultiConnectionActivity.this, EvaluationActivity.class));
                        }else{
                            if(operationType.equals("Training")){
                                startActivity(new Intent(MultiConnectionActivity.this, TrainingManagerActivity.class));
                            }else{
                                startActivity(new Intent(MultiConnectionActivity.this, SessionsManagerActivity.class));
                            }


                        }

                    }

                }else{
                    Toast.makeText(this, "Connect at least one device", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.multiConnection_refresh:
                checkLocationPermission2();
                break;
        }

    }




    @Override
    public void onDeviceSelected(RxBleDevice device) {
        Log.d(TAG, "onDeviceSelected: " + device.getName() + " " + device.getMacAddress() + " " + isAddDevice1Pressed);

        if(sensorsConnected.contains(new SensorConnected(device))){
            Toast.makeText(MultiConnectionActivity.this, "Device already setup", Toast.LENGTH_SHORT).show();
            return;
        }


        sensorSetupFragment = new SensorSetupFragment();
        sensorSetupFragment.setDevice(device);
        sensorSetupFragment.show(getSupportFragmentManager(), SensorSetupFragment.class.getName());


        //Only one specific location of sensor
        //onDeviceConnect(device,"Waist");

    }

    @Override
    public void onCancelConnection(){
        sensorSetupFragment.dismiss();
    }

    @Override
    public void onDeviceConnect(RxBleDevice device,String location){
        RecyclerView deviceList = this.findViewById(R.id.device_list);

        sensorSetupFragment.dismiss();

        if(device != null){
            Mds.builder().build(MultiConnectionActivity.this).connect(device.getMacAddress(), null);

            tv_conecction_status.setText("Connecting may take 10-30s - Please Wait...");

            deviceViewHolder = (ScannedDevicesAdapter.DeviceViewHolder)deviceList.findViewHolderForItemId(device.getMacAddress().hashCode());
            if(deviceViewHolder != null){
                deviceViewHolder.setState("Connecting...");

            }

            deviceList.setEnabled(false);
            //Add device
            currentSensorConnecting = new SensorConnected(device,location,true);
            sensorsConnected.add(currentSensorConnecting);


        } else {
            Toast.makeText(this, "Add device for connection", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    //From fragment
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    // Try starting scan again
                    startScanning(false);
                }
            }
        }
    }


    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            // It means the user has changed his bluetooth state.
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    // The user bluetooth is ready to use.

                    // start scanning again in case of ready Bluetooth
                    startScanning(false);
                    return;
                }

                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
                    // The user bluetooth is turning off yet, but it is not disabled yet.
                    return;
                }

                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    // The user bluetooth is already disabled.
                    return;
                }

            }
        }
    };

    private void startScanning(boolean checkPermission) {
        // Make sure we have location permission
        /*if (!checkLocationPermission()) {
            return;
        }*/
        if(checkPermission)
            checkLocationPermission();

        Log.d(LOG_TAG, "START SCANNING !!!");
        // Start scanning
        mCompositeSubscription.add(rxBleClient.scanBleDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxBleScanResult>() {
                    @Override
                    public void call(RxBleScanResult rxBleScanResult) {
                        Log.d(TAG, "call: ");
                        scannedDevicesAdapter.handleScanResult(rxBleScanResult);
                    }
                }, new ThrowableToastingAction(this)));
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if(Build.VERSION.SDK_INT >= 23) {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.title_location_permission)
                            .setMessage(R.string.text_location_permission)
                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                            MY_PERMISSIONS_REQUEST_LOCATION);
                                }
                            })
                            .create()
                            .show();

                } else {
                    // No explanation needed, we can request the permission.
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);
                }
            }
            return false;
        } else {
            return true;
        }
    }



    public void checkLocationPermission2() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }else{
            Toast.makeText(MultiConnectionActivity.this, "Location service is enabled", Toast.LENGTH_SHORT).show();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        //startScanning(false);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }



}
