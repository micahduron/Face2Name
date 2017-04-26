package edu.ucsc.cmps115_spring2017.face2name;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import edu.ucsc.cmps115_spring2017.face2name.Camera.CameraPreview;
import edu.ucsc.cmps115_spring2017.face2name.Camera.OrientationCapability;
import edu.ucsc.cmps115_spring2017.face2name.Camera.OrientationCapability.OrientationSetting;


public class MainScreen extends AppCompatActivity implements CameraPreview.PreviewCallbacks {
    Context ctx = this;
    DatabaseOperations db = new DatabaseOperations(ctx);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_screen);
    }

    @Override
    public void onStart() {
        super.onStart();

        mCameraPreview = (CameraPreview) findViewById(R.id.camera_preview);
        mCameraPreview.setCapabilities(new OrientationCapability(OrientationSetting.PORTRAIT));
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
    public void onCameraError(Exception ex) {

    }

    @Override
    public void onPreviewReady() {
        mCameraPreview.startPreview();
    }

    private CameraPreview mCameraPreview;

    public void insert (View v) {
        db.insertInfo(db);
    }
}
