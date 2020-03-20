package com.team123.mobilecollector.A00_MainView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.Toast;

import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.team123.mobilecollector.A02_SessionsManager.SessionsManagerActivity;
import com.team123.mobilecollector.A03_UserSettings.UserSettingsFragment;
import com.team123.mobilecollector.A04_3DEvaluation.EvaluationActivity;
import com.team123.mobilecollector.A05_TrainingManager.TrainingManagerActivity;
import com.team123.mobilecollector.R;
import com.team123.mobilecollector.A01_SensorsSetup.MultiConnectionActivity;
import com.team123.mobilecollector.utils.ActivityOperations;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;



public class MainViewActivity extends AppCompatActivity implements ActivityOperations.UserListener {

    private final String TAG = MainViewActivity.class.getSimpleName();
    private UserSettingsFragment userSettingsFragment;

    public static final String PREFS_NAME = "TEAM123";
    public static  String USER_NAME;

    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,

    };

    @BindView(R.id.mainView_sensorsSetup_L1) RelativeLayout mMainViewMultiConnectionLl;
    @BindView(R.id.mainView_user) TextView mainView_user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
        ButterKnife.bind(this);
        checkPermissions();
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        MainViewActivity.USER_NAME = settings.getString("USER", "Movesense User");

        updateUserName();
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something
            }
            return;
        }
    }
    public void updateUserName(){

        mainView_user.setText("Welcome: "+MainViewActivity.USER_NAME);
    }

    @OnClick({R.id.mainView_sensorsSetup_L1, R.id.mainView_sessionsManager_Ll, R.id.mainView_settings_Ll,R.id.mainView_telemetry_Ll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.mainView_sensorsSetup_L1:
                //startActivity(new Intent(MainViewActivity.this, MultiConnectionActivity.class));

                //Intent multiConnectionIntent = new Intent(MainViewActivity.this, MultiConnectionActivity.class);
                //multiConnectionIntent.putExtra("Type","Training");
                //startActivity(multiConnectionIntent);

                if(MultiConnectionActivity.sensorsConnected.size() > 0){
                    if(MovesenseConnectedDevices.getConnectedDevices().size() ==0){
                        Toast.makeText(this, MovesenseConnectedDevices.getConnectedDevices().size() + " device(s) connected, please go to Sensors Setup.", Toast.LENGTH_SHORT).show();
                    }else{
                        startActivity(new Intent(MainViewActivity.this, TrainingManagerActivity.class));
                    }
                }else{
                    Intent sessionManagerIntent = new Intent(MainViewActivity.this, MultiConnectionActivity.class);
                    sessionManagerIntent.putExtra("Type","Training");
                    startActivity(sessionManagerIntent);
                }

                break;
            case R.id.mainView_sessionsManager_Ll:
                //noImplementedFeactureDialog();
                if(MultiConnectionActivity.sensorsConnected.size() > 0){
                    if(MovesenseConnectedDevices.getConnectedDevices().size() ==0){
                        Toast.makeText(this, MovesenseConnectedDevices.getConnectedDevices().size() + " device(s) connected, please go to Sensors Setup.", Toast.LENGTH_SHORT).show();
                    }else{
                        startActivity(new Intent(MainViewActivity.this, SessionsManagerActivity.class));
                    }
                }else{
                    Intent sessionManagerIntent = new Intent(MainViewActivity.this, MultiConnectionActivity.class);
                    sessionManagerIntent.putExtra("Type","Recognition");
                    startActivity(sessionManagerIntent);
                }
                break;
            case R.id.mainView_settings_Ll:
                userSettingsFragment = new UserSettingsFragment();
                userSettingsFragment.show(getSupportFragmentManager(), UserSettingsFragment.class.getName());

                break;
            case R.id.mainView_telemetry_Ll:
                //noImplementedFeactureDialog();
                if(MultiConnectionActivity.sensorsConnected.size() > 0){
                    if(MovesenseConnectedDevices.getConnectedDevices().size() ==0){
                        Toast.makeText(this, MovesenseConnectedDevices.getConnectedDevices().size() + " device(s) connected, please go to Sensors Setup.", Toast.LENGTH_SHORT).show();
                    }else{
                        startActivity(new Intent(MainViewActivity.this, EvaluationActivity.class));
                    }
                }else{
                    Intent evaluationIntent = new Intent(MainViewActivity.this, MultiConnectionActivity.class);
                    evaluationIntent.putExtra("Type","3D");
                    startActivity(evaluationIntent);
                }
                break;
        }
    }

    //Temporary message while all the features are implemented.
    private void noImplementedFeactureDialog(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainViewActivity.this).create();
        alertDialog.setTitle("Please wait");
        alertDialog.setMessage("This feature will be implemented soon.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onChangeUser(String name) {

        if(MainViewActivity.USER_NAME != name){
            MainViewActivity.USER_NAME = name;
            SharedPreferences settings = getSharedPreferences(MainViewActivity.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor edit = settings.edit();

            edit.putString("USER",name);
            edit.apply();
            edit.commit();

            updateUserName();
        }

        userSettingsFragment.dismiss();
    }

    @Override
    public void onCancelSelection() {
        userSettingsFragment.dismiss();
    }


    /**
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    **/
}
