package com.team123.mobilecollector.model;


import com.google.gson.annotations.SerializedName;

public class EcgInfoResponse {

    @SerializedName("Content")
    public final Content mContent;


    public EcgInfoResponse(Content content) {
        mContent = content;
    }

    public static class Content {
        @SerializedName("AvailableSampleRates")
        public final int ranges[];

        @SerializedName("CurrentSampleRate")
        public final int currentSampleRate;

        public Content(int[] ranges, int currentSampleRate) {
            this.ranges = ranges;
            this.currentSampleRate = currentSampleRate;
        }
    }
}
