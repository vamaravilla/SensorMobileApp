package com.team123.mobilecollector.utils;

import android.os.Environment;
import android.media.MediaScannerConnection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import android.util.Log;

public class FileOperations {

    public static File sessionFile;

    public static File createFileForKinesisClient(){
        // Get the directory for the user's public docuements directory.
        File path =
                Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DOCUMENTS + "/Movesense/kinesis");


        // Make sure the path directory exists.
        if(!path.exists())
        {
            // Make it, if it doesn't exit
            path.mkdirs();
        }



        return path;
    }

    public static Boolean createFile(String name){
        String header = "sessionID,userID,startTime,activity,accX,accY,accZ,gyroX,gyroY,gyroZ";

        // Get the directory for the user's public docuements directory.
        final File path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Movesense/");

        // Make sure the path directory exists.
        if(!path.exists())
        {
            // Make it, if it doesn't exit
            path.mkdirs();
        }

        sessionFile = new File(path,  name +".csv");

        try
        {
            sessionFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(sessionFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            BufferedWriter bWriter = new BufferedWriter(myOutWriter);

            bWriter.write(header);
            bWriter.newLine();

            bWriter.close();
            myOutWriter.close();
            fOut.flush();
            fOut.close();
            return true;
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
            return false;
        }
    }
    public static File  returnFile(){
        return sessionFile;
    }
    public static void  writeSessionData(String value){

        try{

            FileOutputStream fOut = new FileOutputStream(sessionFile,true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            BufferedWriter bWriter = new BufferedWriter(myOutWriter);

            bWriter.write(value);
            bWriter.newLine();
            //System.out.println("imprimiendo en archivo: " + sessionFile +  " value : " + value);
            bWriter.close();
            myOutWriter.close();
            fOut.flush();
            fOut.close();

        } catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }

}
