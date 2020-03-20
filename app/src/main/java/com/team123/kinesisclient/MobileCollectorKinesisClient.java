package com.team123.kinesisclient;

import java.io.File;
import java.nio.charset.Charset;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisRecorder;
import com.amazonaws.regions.Regions;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

import com.team123.mobilecollector.model.SessionData;
import com.team123.mobilecollector.model.SessionDataTrain;

/**
 * Kinesis simple client, that uses a KinesisRecorder to store data before sending it to the AWS
 * Kinesis stream.
 */
public class MobileCollectorKinesisClient {
    private final KinesisRecorder recorder;
    private final String streamName;
    private final Gson gsonSerializer;

    /**
     * Creates an instance of the MobileCollectorKinesisClient.
     * @param recordsDirectory  Points to a directory to store the records temporarily. It must be empty.
     * @param accessKey         AWS user access key.
     * @param secretKey         AWS user secret key.
     * @param streamName        Kinesis stream name.
     */
    public MobileCollectorKinesisClient(File recordsDirectory, String accessKey, String secretKey, String streamName)
    {
        this.streamName = streamName;
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSCredentialsProvider credentialsProvider = new StaticCredentialsProvider(credentials);
        Regions region = Regions.US_EAST_2;
        recorder = new KinesisRecorder(recordsDirectory, region, credentialsProvider);

        gsonSerializer = (new GsonBuilder()).setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
    }

    /**
     * Saves a single SessionData record locally.
     * @param data  SessionData record to be saved.
     */
    public void saveRecord(SessionData data) {
        recorder.saveRecord(serializeToJsonBinary(data), streamName);
    }

    /**
     * Saves a single SessionData record locally.
     * @param data  SessionData record to be saved.
     */
    public void saveRecordTrain(SessionDataTrain data) {
        recorder.saveRecord(serializeToJsonBinary(data), streamName);
    }

    /**
     * Submits all the stored records to the Kinesis stream. If they are submitted successfully, the
     * records are deleted from the temporary directory.
     * @throws AmazonClientException  Error thrown
     */
    public synchronized void submitAllRecords() throws AmazonClientException {
        recorder.submitAllRecords();
    }

    /**
     * Serializes a SessionData object to a JSON string, then to a byte array.
     * @param data  SessionData object.
     * @return      SessionData object serialized as JSON, as a byte array.
     */
    private byte[] serializeToJsonBinary(SessionData data) {
        String json = gsonSerializer.toJson(data);
        return json.getBytes(Charset.forName("UTF-8"));
    }

    /**
     * Serializes a SessionData object to a JSON string, then to a byte array.
     * @param data  SessionData object.
     * @return      SessionData object serialized as JSON, as a byte array.
     */
    private byte[] serializeToJsonBinary(SessionDataTrain data) {
        String json = gsonSerializer.toJson(data);
        return json.getBytes(Charset.forName("UTF-8"));
    }
}
