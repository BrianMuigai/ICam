package com.icam.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.icam.models.Classification;
import com.icam.models.FaceEmotions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class EmotionDetector extends Detector<FaceEmotions> {

    private static final String TAG = EmotionDetector.class.getSimpleName();
    private Detector<Face> mDelegate;
    private TensorFlowClassifier mClassifier;
    private Map<Integer, FaceEmotions> emotionsMap = new HashMap<>();
    private Context mContext;

    public EmotionDetector(Context context, Detector<Face> delegate, TensorFlowClassifier classifier) {
        mDelegate = delegate;
        mClassifier = classifier;
        mContext = context;
    }

    @Override
    public SparseArray<FaceEmotions> detect(Frame frame) {
        SparseArray<FaceEmotions> emotionFaces = new SparseArray<>();
        SparseArray<Face> detectedFaces = mDelegate.detect(frame);

        for (int i = 0; i < detectedFaces.size(); i++) {
            Face face = detectedFaces.valueAt(i);

            FaceEmotions faceEmotions = new FaceEmotions();
            faceEmotions.setConf(0);
            faceEmotions.setEmotion("Unknown emotion");
            if (!emotionsMap.containsKey(face.getId())){
                if (face.getIsSmilingProbability() > 0.8f){
                    faceEmotions.setConf(face.getIsSmilingProbability());
                    faceEmotions.setEmotion(FaceEmotions.HAPPY);
                }
                faceEmotions.setFace(face);
                emotionFaces.append(face.getId(), faceEmotions);
            }else{
                emotionFaces.append(face.getId(), emotionsMap.get(face.getId()));
            }

            classify(face, frame);
        }
        return emotionFaces;
    }

    private void classify(final Face face, final Frame frame){
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] data = frame.getGrayscaleImageData().array();
                float centerX = face.getPosition().x + face.getWidth() / 2.0f;
                float centerY = face.getPosition().y + face.getHeight() / 2.0f;
                float offsetX = face.getWidth() / 2.0f;
                float offsetY = face.getHeight() / 2.0f;

                // draw a box around the face
                float left = centerX - offsetX;
                float right = centerX + offsetX;
                float top = centerY - offsetY;
                float bottom = centerY + offsetY;

                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (image == null){
                    YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,
                            frame.getMetadata().getWidth(), frame.getMetadata().getHeight(), null);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(new Rect((int)left, (int)top, (int)(right), (int)(bottom)),
                            100, outputStream);
                    byte[] yData = outputStream.toByteArray();
                    image = BitmapFactory.decodeByteArray(yData, 0, yData.length);
                }
                Bitmap bitmap = Bitmap.createScaledBitmap(
                        image,
                        48,
                        48,
                        true
                );
                //Initialize the intArray with the same size as the number of pixels on the image
                int[] pixelarray = new int[bitmap.getWidth() * bitmap.getHeight()];
                //copy pixel data from the Bitmap into the 'intArray' array
                bitmap.getPixels(pixelarray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//                float[] normalized_pixels = new float[pixelarray.length];
//                for (int i=0; i < pixelarray.length; i++) {
//                    // 0 for white and 255 for black
//                    int pix = pixelarray[i];
//                    int b = pix & 0xff;
//                    //  normalized_pixels[i] = (float)((0xff - b)/255.0);
//                    // normalized_pixels[i] = (float)(b/255.0);
//                    normalized_pixels[i] = (float)(b);
//
//                }
                try {
                    final Classification res = mClassifier.recognize(pixelarray);
                    FaceEmotions faceEmotions = new FaceEmotions();
                    faceEmotions.setFace(face);
                    //if it can't classify, output a 0
                    if (res.getLabel() == null) {
                        faceEmotions.setEmotion("?");
                    } else {
                        faceEmotions.setConf(res.getConf());
                        faceEmotions.setEmotion(res.getLabel());
                        faceEmotions.setPredictions(res.getPredictions());
                        Log.i(TAG, "Emotion: " + res.getLabel() + " confidence: " + res.getConf());
                    }
                    emotionsMap.put(face.getId(), faceEmotions);
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }).start();
    }
}
