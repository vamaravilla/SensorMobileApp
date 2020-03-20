package com.team123.mobilecollector.utils;

import com.polidea.rxandroidble.RxBleDevice;

/**
 * List of interfaces for Activities operations
 */
public class ActivityOperations {


    public interface DeviceSelectionListener {
        void onDeviceSelected(RxBleDevice device);

    }

    public interface DeviceConnectListener {
        void onDeviceConnect(RxBleDevice device,String location);
        void onCancelConnection();
    }

    public interface ActicitySelectionListener {
        void onActivitySelected(String activity);
        void onCancelSelection();
    }

    public interface KinesisLitener {
        void onUploadCompleted(String message);
    }

    public interface UserListener {
        void onChangeUser(String name);
        void onCancelSelection();
    }

}
