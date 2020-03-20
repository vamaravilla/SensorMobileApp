package com.team123.mobilecollector.utils;


import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.team123.mobilecollector.bluetooth.MdsRx;

import no.nordicsemi.android.dfu.DfuServiceInitiator;

public class DfuUtil {

    private static final String TAG = DfuUtil.class.getSimpleName();

    private static final String DFU_PATH = "/System/Mode";
    private static final String DFU_CONTRACT = "{\"NewState\":12}";
    private static final String BATTERY_PATH = "/System/Energy/Level";
    private static final String INFO_PATH = "/Info";

    public static void runDfuModeOnConnectedDevice(Context context, final MdsResponseListener responseListener) {
        String connectedDeviceSerial = Util.getVisibleSerial(MovesenseConnectedDevices.getConnectedRxDevice(0).getName());
        Log.e(TAG, "runDfuModeOnConnectedDevice: " + connectedDeviceSerial);

        if (connectedDeviceSerial == null || connectedDeviceSerial.isEmpty()) {
            Log.e(TAG, "No connected Devices");
            return;
        }

        Mds.builder().build(context).put(MdsRx.SCHEME_PREFIX +
                        connectedDeviceSerial + DFU_PATH,
                DFU_CONTRACT, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String data) {
                        responseListener.onSuccess(data);
                    }

                    @Override
                    public void onError(MdsException error) {
                        responseListener.onError(error);
                    }
                });
    }

    public static String incrementMacAddress(String addressToIncrement) {
        String[] macSegments = addressToIncrement.split(":");
        String lastSegmentofMacAdress = macSegments[5];
        int value = Integer.parseInt(lastSegmentofMacAdress, 16);
        value++;
        String incrementedSegment = Integer.toHexString(value);
        StringBuilder sb = new StringBuilder();

        // For will build mac address without last segment ( length -1 )
        for (int i = 0; i < macSegments.length - 1; i++) {
            sb.append(macSegments[i]).append(":");
        }
        // Add incremented segment to old address
        if (value < 10) {
            sb.append("0");
            sb.append(incrementedSegment.toUpperCase());
        } else {
            sb.append(incrementedSegment.toUpperCase());
        }

        return sb.toString();
    }

    public static void runDfuServiceUpdate(Context context, String macAddress, String deviceName,
                                           Uri fileStreamUri, String filePath) {
        Log.d(TAG, "runDfuServiceUpdate: macAddress: " + macAddress + " deviceName: " + deviceName
                + " fileStreamUri: " + fileStreamUri + " filePath: " + filePath);

        if (macAddress == null || macAddress.equals("")) {
            Toast.makeText(context, "Mac address not valid", Toast.LENGTH_LONG).show();
            Log.e(TAG, "runDfuServiceUpdate: Mac address not valid");
            return;
        }

        DfuServiceInitiator mServiceInitiator = new DfuServiceInitiator(macAddress)
                .setDeviceName(deviceName)
                .setKeepBond(false)
                .setForceDfu(false)
                .setPacketsReceiptNotificationsEnabled(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                .setPacketsReceiptNotificationsValue(DfuServiceInitiator.DEFAULT_PRN_VALUE)
                .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);

        mServiceInitiator.setZip(fileStreamUri, filePath);
        //mServiceInitiator.start(context, DfuService.class);
    }

    public static void getBatteryStatus(Context context, final MdsResponseListener responseListener) {
        Log.d(TAG, "getBatteryStatus() ");
        if (MovesenseConnectedDevices.getConnectedDevices().size() <= 0) {
            Log.e(TAG, "No connected Devices");
            return;
        }
        String connectedDeviceSerial = MovesenseConnectedDevices.getConnectedDevices().get(0).getSerial();

        Mds.builder().build(context).get(MdsRx.SCHEME_PREFIX +
                        connectedDeviceSerial + BATTERY_PATH,
                null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String data) {
                        responseListener.onSuccess(data);
                    }

                    @Override
                    public void onError(MdsException error) {
                        responseListener.onError(error);
                    }
                });
    }

    public static void getDfuAddress(Context context, MdsResponseListener mdsResponseListener) {
        Log.d(TAG, "getDfuAddress: ");
        if (mdsResponseListener == null) {
            Log.e(TAG, "getDfuAddress() MdsResponseListener null");
            return;
        }
        if (MovesenseConnectedDevices.getConnectedDevices().size() <= 0) {
            Log.e(TAG, "getDfuAddress() No connected Devices");
            return;
        }
        String connectedDeviceSerial = MovesenseConnectedDevices.getConnectedDevices().get(0).getSerial();

        Mds.builder().build(context).get(MdsRx.SCHEME_PREFIX + connectedDeviceSerial
                + INFO_PATH, null, mdsResponseListener);
    }
}
