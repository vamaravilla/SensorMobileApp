package com.team123.mobilecollector.model;


import com.google.gson.annotations.SerializedName;

public class TemperatureSubscribeModel {

    @SerializedName("Body")
    public final Body mBody;

    @SerializedName("Uri")
    public final String mUri;


    public TemperatureSubscribeModel(Body body, String uri) {
        mBody = body;
        mUri = uri;
    }

    public class Body {
        @SerializedName("Timestamp")
        public final long timestamp;

        @SerializedName("Measurement")
        public final double measurement;

        public Body(long timestamp, double measurement) {
            this.timestamp = timestamp;
            this.measurement = measurement;
        }


        public long getTimestamp() {
            return timestamp;
        }

        public double getMeasurement() {
            return measurement;
        }
    }

    public Body getBody() {
        return mBody;
    }

    public String getUri() {
        return mUri;
    }
}
