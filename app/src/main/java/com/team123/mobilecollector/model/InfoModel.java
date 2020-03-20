package com.team123.mobilecollector.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InfoModel {

    @SerializedName("Name")
    private String name;

    @SerializedName("Description")
    private String description;

    @SerializedName("Mode")
    private int mode;

    @SerializedName("SwVersion")
    private String swVersion;

    private String productName;

    private String variant;

    private String hwCompatibilityId;

    @SerializedName("Serial")
    private String serial;

    private String pcbaSerial;

    private String sw;

    private String hw;

    private String additionalVersionInfo;

    private String manufacturerName;

    @SerializedName("addressInfo")
    private List<MdsAddressModel> addressInfoNew;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMode() {
        return mode;
    }

    public String getSwVersion() {
        return swVersion;
    }

    public String getProductName() {
        return productName;
    }

    public String getVariant() {
        return variant;
    }

    public String getHwCompatibilityId() {
        return hwCompatibilityId;
    }

    public String getSerial() {
        return serial;
    }

    public String getPcbaSerial() {
        return pcbaSerial;
    }

    public String getSw() {
        return sw;
    }

    public String getHw() {
        return hw;
    }

    public String getAdditionalVersionInfo() {
        return additionalVersionInfo;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public List<MdsAddressModel> getAddressInfoNew() {
        return addressInfoNew;
    }
}
