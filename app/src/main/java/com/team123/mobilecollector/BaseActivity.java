package com.team123.mobilecollector;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.team123.mobilecollector.bluetooth.BluetoothStatusMonitor;
import com.team123.mobilecollector.bluetooth.RxBle;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BaseActivity extends AppCompatActivity {

    private final String TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        BluetoothStatusMonitor.INSTANCE.bluetoothStatusSubject
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer == BluetoothAdapter.STATE_ON) {
                        Log.d(TAG, "call: BluetoothAdapter.STATE_ON");

                        RxBle.Instance.getClient().scanBleDevices()
                                .takeUntil(Observable.timer(5, TimeUnit.SECONDS))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(rxBleScanResult -> Log.d(TAG, "scan: "),
                                        throwable -> Log.e(TAG, "scanBleDevices() error ", throwable));
                    } else if (integer == BluetoothAdapter.STATE_OFF) {
                        Log.d(TAG, "call: BluetoothAdapter.STATE_OFF");
                    }
                }, throwable -> Log.e(TAG, "call bluetoothStatusSubject: ", throwable));
    }
}
