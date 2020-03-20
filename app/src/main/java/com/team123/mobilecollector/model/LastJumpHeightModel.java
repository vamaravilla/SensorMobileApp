package com.team123.mobilecollector.model;


import com.google.gson.annotations.SerializedName;

public class LastJumpHeightModel {

    @SerializedName("Content")
    private final float height;

    public float getBody() {
        return height;
    }

    public LastJumpHeightModel(int body) {
        height = body;
    }

    @Override
    public String toString() {
        return "LastJumpHeightModel{" +
                "Height=" + height +
                '}';
    }

}
