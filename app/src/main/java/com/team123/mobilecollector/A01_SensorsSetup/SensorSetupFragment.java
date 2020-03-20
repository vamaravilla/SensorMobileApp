package com.team123.mobilecollector.A01_SensorsSetup;


import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.view.View.OnClickListener;
import com.polidea.rxandroidble.RxBleDevice;
import com.team123.mobilecollector.R;
import com.team123.mobilecollector.utils.ActivityOperations;


public class SensorSetupFragment extends DialogFragment {

    private RxBleDevice sensor;
    private TextView buttonConnect;
    private TextView buttonCancel;
    private ActivityOperations.DeviceConnectListener callback;
    private RadioGroup radioGroup;

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            /*int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT; */

            DisplayMetrics metrics = new DisplayMetrics();
            dialog.getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;
            int height = 1400;//metrics.heightPixels;
            if(Build.VERSION.SDK_INT >= 22) {
                dialog.getWindow().setElevation(5);
            }
            dialog.getWindow().setLayout((int)(width*0.9),height/*(int)(height*0.7)*/);
            //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Util.getColorWithAlpha(Color.BLACK,0.3f)));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setup, container, false);


        buttonConnect = view.findViewById(R.id.multiConnection_connect);
        buttonCancel = view.findViewById(R.id.multiConnection_cancel);

        radioGroup = view.findViewById(R.id.rg_location_sensor);

        Activity activity = getActivity();
        if (activity instanceof ActivityOperations.DeviceConnectListener) {
            callback = (ActivityOperations.DeviceConnectListener) activity;
        } else {
            throw new IllegalArgumentException("Containing Activity "+ activity +" does not implement DeviceConnectListener");
        }

        buttonConnect.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                int radioCheckedId = radioGroup.getCheckedRadioButtonId();
                RadioButton radioCheked = radioGroup.findViewById(radioCheckedId);
                String location = radioCheked.getText().toString();
                callback.onDeviceConnect(sensor,location);
            }
        });
        buttonCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                callback.onCancelConnection();
            }
        });

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    public void setDevice(RxBleDevice device){
        this.sensor = device;
    }
}

