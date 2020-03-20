package com.team123.mobilecollector.model;


import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class JumpCountModel {

    @SerializedName("Body")
    private final int count;

    public int getBody() {
        return count;
    }

    public JumpCountModel(int body) {
        count = body;
    }

    @Override
    public String toString() {
        return "JumpCountModel{" +
                "Count=" + count +
                '}';
    }

}
