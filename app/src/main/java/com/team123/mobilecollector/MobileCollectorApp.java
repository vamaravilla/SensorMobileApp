package com.team123.mobilecollector;

import android.app.Application;
import android.content.Context;

import com.team123.mobilecollector.bluetooth.BluetoothStatusMonitor;
import com.team123.mobilecollector.bluetooth.MdsRx;
import com.team123.mobilecollector.bluetooth.RxBle;
import com.team123.mobilecollector.utils.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Application for making all initializations
 */
public class MobileCollectorApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize RxBleWrapper
        RxBle.Instance.initialize(this);

        // Copy necessary configuration file to proper place
        copyRawResourceToFile(R.raw.kompostisettings, "KompostiSettings.xml");

        BluetoothStatusMonitor.INSTANCE.initBluetoothStatus();


        // Initialize MDS
        MdsRx.Instance.initialize(this);


    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }

    /**
     * Copy raw resource file to file.
     *
     * @param resourceId Resource id.
     * @param fileName   Target file name.
     */
    private void copyRawResourceToFile(int resourceId, String fileName) {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = getResources().openRawResource(resourceId);
            out = openFileOutput(fileName, Context.MODE_PRIVATE);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not copy configuration file to: " + fileName);
        } finally {
            Util.safeClose(out);
            Util.safeClose(in);
        }
    }
}
