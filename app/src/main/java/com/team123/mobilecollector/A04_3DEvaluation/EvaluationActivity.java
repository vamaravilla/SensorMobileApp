package com.team123.mobilecollector.A04_3DEvaluation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.team123.Avatar2.UnityPlayerGenerator;
import com.team123.mobilecollector.A00_MainView.MainViewActivity;
import com.team123.mobilecollector.A01_SensorsSetup.MultiConnectionActivity;
import com.team123.mobilecollector.R;
import com.team123.mobilecollector.bluetooth.MdsRx;
import com.team123.mobilecollector.model.LastJumpHeightModel;
import com.team123.mobilecollector.model.MdsConnectedDevice;
import com.team123.mobilecollector.model.SensorConnected;
import com.team123.mobilecollector.utils.ThrowableToastingAction;
import com.unity3d.player.UnityPlayer;

import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;


public class EvaluationActivity extends AppCompatActivity {

    private LinearLayout ly_unity;
    private UnityPlayer mUnityPlayer ;
    private TextView tv_star_stop;
    private TextView tv_exit;
    private TextView tv_status;
    private final String IMU_PATH = "/Sample/JumpCounter/LastJumpHeight";
    private SensorConnected sensor;
    private CompositeSubscription mCompositeSubscriptionD;
    private final String TAG = EvaluationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3d_evaluation);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("3D Evaluation");
        }

        //Display unity player
        DisplayMetrics metrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;

        mUnityPlayer = UnityPlayerGenerator.CreateUnityPlayer(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (height-300));
        mUnityPlayer.requestFocus();
        //mUnityPlayer.UnitySendMessage("aj", "Play", "Avatar1");

        ly_unity = this.findViewById(R.id.unity_player);
        ly_unity.addView(mUnityPlayer.getView(), 0, lp);

        tv_star_stop = findViewById(R.id.evaluation3d_start_stop);
        tv_exit = findViewById(R.id.evaluation3d_exit);
        tv_status = findViewById(R.id.evaluation3d_status);
        tv_star_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tv_star_stop.setText(R.string.start_evaluation_text);

                //mUnityPlayer.destroy();
                //mUnityPlayer.start();
            }
        });
        tv_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        //Checking sensor connected
        if(MultiConnectionActivity.sensorsConnected.size() == 0){
            //Toast.makeText(this, "Nos device connected", Toast.LENGTH_SHORT).show();
            tv_status.setText("No device connected");
        }else{
            sensor = MultiConnectionActivity.sensorsConnected.get(0);
            mCompositeSubscriptionD = new CompositeSubscription();
            mCompositeSubscriptionD.add(MdsRx.Instance.connectedDeviceObservable()
                    .subscribe(new Action1<MdsConnectedDevice>() {
                        @Override
                        public void call(MdsConnectedDevice mdsConnectedDevice) {
                            if (mdsConnectedDevice.getConnection() == null) {

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(new Intent(EvaluationActivity.this, MainViewActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    }
                                }, 1000);
                            }
                        }
                    }, new ThrowableToastingAction(this)));


            //Register ServiceReceiver
            LocalBroadcastManager.getInstance(EvaluationActivity.this).registerReceiver(
                    mMessageReceiverX, new IntentFilter("sensorJump"));
            //Start session service to subscribe and upload data from sensor
            StartSessionService();
        }
    }


    private BroadcastReceiver mMessageReceiverX = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String valueMessage;
            // Get extra data included in the Intent
            valueMessage = intent.getStringExtra("Count");
            if(valueMessage.equals("") || valueMessage == null){
                //tv_status.setText("Empty response");
                Toast.makeText(EvaluationActivity.this, "Empty response", Toast.LENGTH_SHORT).show();
            }else {

                getLastJumpHeightFromSensor();
                //gameObject, functionName, funcParam
                mUnityPlayer.UnitySendMessage("aj", "Play", "Jump");
                Toast.makeText(EvaluationActivity.this, "Jump: "+valueMessage, Toast.LENGTH_SHORT).show();

            }
        }
    };

    private void StartSessionService(){
        Intent serviceIntent = new Intent(this,JumpService.class);
        //serviceIntent.putExtra("activitySelected",activitySelected);
        startService(serviceIntent);
    }


    private void StopSessionService(){

        stopService(new Intent(this, JumpService.class));

    }

    private void getLastJumpHeightFromSensor(){
        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX +
                        sensor.getSerial() + IMU_PATH,
                null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String s) {

                        if(s.equals("") || s == null){
                            tv_status.setText("Empty response");
                        }else {
                            LastJumpHeightModel lastJumpHeightModel = new Gson().fromJson(s, LastJumpHeightModel.class);

                            tv_status.setText("Last jump height: " + String.valueOf(lastJumpHeightModel.getBody()*100) + " cm");
                        }
                    }

                    @Override
                    public void onError(MdsException e) {
                        tv_status.setText(e.getMessage());
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }


    // Quit Unity
    @Override protected void onDestroy ()
    {
        super.onDestroy();
        mUnityPlayer.destroy();

    }

    // Pause Unity
    @Override protected void onPause()
    {
        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override protected void onResume()
    {
        super.onResume();
        mUnityPlayer.resume();
    }

    @Override protected void onStart()
    {
        super.onStart();
        mUnityPlayer.start();
    }

    @Override protected void onStop()
    {
        super.onStop();
        mUnityPlayer.stop();
    }

    // Low Memory Unity
    @Override public void onLowMemory()
    {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL)
        {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage(R.string.disconnect_dialog_text)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        StopSessionService();

                        int numSensor = 0;
                        for(SensorConnected sensorItem: MultiConnectionActivity.sensorsConnected){
                            //sensorItem.unsuscribeAll();
                            BleManager.INSTANCE.disconnect(MovesenseConnectedDevices.getConnectedRxDevice(numSensor));
                            numSensor++;
                        }
                        MultiConnectionActivity.sensorsConnected.clear();
                        EvaluationActivity.super.onBackPressed();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }


}
