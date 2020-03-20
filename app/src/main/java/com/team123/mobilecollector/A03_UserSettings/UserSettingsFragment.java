package com.team123.mobilecollector.A03_UserSettings;


import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.team123.mobilecollector.A00_MainView.MainViewActivity;
import com.team123.mobilecollector.R;
import com.team123.mobilecollector.utils.ActivityOperations;


public class UserSettingsFragment extends DialogFragment {

    private TextView buttonSave;
    private EditText et_user_name;
    private TextView buttonCancel;
    private TextView tv_messages;
    private ActivityOperations.UserListener callback;

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            dialog.getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;
            int height = 900;//metrics.heightPixels;
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
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);


        buttonSave = view.findViewById(R.id.userSettings_save);
        et_user_name = view.findViewById(R.id.et_user_name);
        et_user_name.setText(MainViewActivity.USER_NAME);
        buttonCancel = view.findViewById(R.id.userSettings_cancel_save);
        tv_messages = view.findViewById(R.id.tv_messages);

        Activity activity = getActivity();
        if (activity instanceof ActivityOperations.UserListener) {
            callback = (ActivityOperations.UserListener) activity;
        } else {
            throw new IllegalArgumentException("Containing Activity "+ activity +" does not implement UserListener");
        }

        buttonSave.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(et_user_name.getText().toString().equals("")){
                    tv_messages.setText("Username cannot be empty");
                    return;
                }else{

                    Activity parentAntivity = getActivity();
                    if(parentAntivity instanceof ActivityOperations.UserListener){
                        callback= (ActivityOperations.UserListener)parentAntivity;
                        callback.onChangeUser(et_user_name.getText().toString());
                    }
                }
            }
        });

        buttonCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                callback.onCancelSelection();
            }
        });

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

}

