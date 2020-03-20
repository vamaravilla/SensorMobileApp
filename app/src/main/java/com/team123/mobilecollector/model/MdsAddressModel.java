package com.team123.mobilecollector.model;


public class MdsAddressModel {

    private final String name;
    private final String address;

    public MdsAddressModel(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
