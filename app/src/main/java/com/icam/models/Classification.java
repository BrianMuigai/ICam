package com.icam.models;

import java.util.Map;

public class Classification {

    //conf is the output
    private float conf;
    //input label
    private String label;
    private Map<String,Float> predictions;

    public Classification() {
        this.conf = -1.0F;
        this.label = null;
    }

    public void update(float conf, String label) {
        this.conf = conf;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public float getConf() {
        return conf;
    }

    public void putPredictions(Map<String, Float> classificationData) {
        predictions = classificationData;
    }

    public Map<String, Float> getPredictions(){
        return predictions;
    }
}
