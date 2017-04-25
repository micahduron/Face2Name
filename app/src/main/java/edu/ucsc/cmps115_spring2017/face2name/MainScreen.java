package edu.ucsc.cmps115_spring2017.face2name;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import edu.ucsc.cmps115_spring2017.face2name.Camera.CameraPreview;


public class MainScreen extends AppCompatActivity implements CameraPreview.PreviewCallbacks {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_screen);
    }

    @Override
    public void onStart() {
        super.onStart();

        mCameraPreview = (CameraPreview) findViewById(R.id.camera_preview);
    }

    @Override
    public void onResume() {
        super.onResume();

        mCameraPreview.init(CameraPreview.BACK_CAMERA, this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mCameraPreview.release();
    }

    @Override
    public void onCameraStart() {

    }

    @Override
    public void onCameraRelease() {

    }

    @Override
    public void onCameraReady() {
        mCameraPreview.startPreview();
    }

    private CameraPreview mCameraPreview;
}
