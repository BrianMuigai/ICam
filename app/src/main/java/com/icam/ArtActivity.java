package com.icam;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


public class ArtActivity extends BaseActivity {

    private static final String TAG = "FaceActivity";

    public static void start(Context context){
        Intent intent = new Intent(context, ArtActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setViewType(ART_VIEW);
        super.onCreate(savedInstanceState);
        emrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EmotionsActivity.start(ArtActivity.this);
                ArtActivity.this.finish();
            }
        });
        emrButton.setVisibility(View.VISIBLE);
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

//    private void showLicenseScreen() {
//        startActivity(new Intent(this, OssLicensesMenuActivity.class));
//    }

}