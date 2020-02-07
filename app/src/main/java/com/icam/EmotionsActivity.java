package com.icam;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.icam.helpers.TensorFlowClassifier;

public class EmotionsActivity extends BaseActivity {

    private static final String TAG = EmotionsActivity.class.getSimpleName();
    private static final int PIXEL_WIDTH = 48;

    public static void start(Context context){
        Intent intent = new Intent(context, EmotionsActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setViewType(EMOTION_VIEW);
        super.onCreate(savedInstanceState);
        loadModel();
        artButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArtActivity.start(EmotionsActivity.this);
                EmotionsActivity.this.finish();
            }
        });
        artButton.setVisibility(View.VISIBLE);
    }

    private void loadModel() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier=TensorFlowClassifier.getInstance(getAssets(), "CNN",
                            "opt_em_convnet_5000.pb", "labels.txt", PIXEL_WIDTH,
                            "input", "output_50", true, 7);

                } catch (final Exception e) {
                    //if they aren't found, throw an error!
                    throw new RuntimeException("Error initializing classifiers!", e);
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
