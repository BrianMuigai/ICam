package com.icam.models;

import android.graphics.PointF;

import com.google.android.gms.vision.face.Contour;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.Map;

public class FaceEmotions {

    public static final String ANGRY = "Angry";
    public static final String DISGUST = "Disgust";
    public static final String FEAR = "Fear";
    public static final String HAPPY = "Happy";
    public static final String SAD = "Sad";
    public static final String SUPRISE = "Surprise";
    public static final String NUTRAL = "Neutral";

    private String emotion;
    private float conf;
    private Face face;
    private Map<String, Float> predictions;

    public Face getFace() {
        return face;
    }

    public void setFace(Face face) {
        this.face = face;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public float getConf() {
        return conf;
    }

    public void setConf(float conf) {
        this.conf = conf;
    }

    public Map<String, Float> getPredictions() {
        return predictions;
    }

    public void setPredictions(Map<String, Float> predictions) {
        this.predictions = predictions;
    }
}
