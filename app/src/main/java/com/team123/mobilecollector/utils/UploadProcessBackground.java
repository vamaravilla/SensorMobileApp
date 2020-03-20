package com.team123.mobilecollector.utils;

import android.os.AsyncTask;

import com.amazonaws.AmazonClientException;
import com.team123.kinesisclient.MobileCollectorKinesisClient;

public class UploadProcessBackground  extends AsyncTask<String, Void, String> {

    //Call back interface
    public ActivityOperations.KinesisLitener delegate = null;
    public MobileCollectorKinesisClient kinesisClient;

    public UploadProcessBackground(ActivityOperations.KinesisLitener listener,MobileCollectorKinesisClient client) {
        //Assigning call back interfacethrough constructor
        delegate = listener;
        kinesisClient = client;
    }

    @Override
    protected String doInBackground(String... params) {
        String message;
        kinesisClient.submitAllRecords();

        try {
            kinesisClient.submitAllRecords();
            message = "success";
        } catch (AmazonClientException ex) {
            message = ex.getMessage();
        }
        return message;

    }

    @Override
    protected void onPostExecute(String result) {
        delegate.onUploadCompleted(result);
    }
}
