package com.team123.tensorflow;

public class FeatureData {
    private double [][]features;
    private int[]types;
    private int numFeatures;
    private int numSamples;
    private int numTypes;

    public int getNumTypes() {
        return numTypes;
    }
    public void setNumTypes(int numTypes) {
        this.numTypes = numTypes;
    }
    public int getNumSamples() {
        return numSamples;
    }
    public void setNumSamples(int numSamples) {
        this.numSamples = numSamples;
    }
    public int getNumFeatures() {
        return numFeatures;
    }
    public void setNumFeatures(int numFeatures) {
        this.numFeatures = numFeatures;
    }
    public double[][] getFeatures() {
        return features;
    }
    public void setFeatures(double[][] features) {
        this.features = features;
    }
    public int[] getTypes() {
        return types;
    }
    public void setTypes(int[] types) {
        this.types = types;
    }
}
