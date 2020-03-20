package com.team123.mobilecollector.bluetooth;

import android.content.Context;

import com.polidea.rxandroidble.RxBleClient;

/**
 * Singleton wrapper for RxBleClient
 */
public enum RxBle {
    Instance;

    private RxBleClient client;

    public void initialize(Context context) {
        client = RxBleClient.create(context);
    }

    public RxBleClient getClient() {
        return client;
    }
}
