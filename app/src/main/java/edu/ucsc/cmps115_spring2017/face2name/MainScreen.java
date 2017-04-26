package edu.ucsc.cmps115_spring2017.face2name;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import edu.ucsc.cmps115_spring2017.face2name.Camera.CameraPreview;
import edu.ucsc.cmps115_spring2017.face2name.Camera.OrientationCapability;
import edu.ucsc.cmps115_spring2017.face2name.Camera.OrientationCapability.OrientationSetting;

import edu.ucsc.cmps115_spring2017.face2name.Layer.LayerView;


public class MainScreen extends AppCompatActivity implements CameraPreview.PreviewCallbacks{

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
        mLayerView = (LayerView) findViewById(R.id.layer_view);

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



    public boolean onTouchEvent(MotionEvent event) {
        int l_x = mLayerView.getRectPoint("Left X");
        int r_x = mLayerView.getRectPoint("Right X");
        int u_y = mLayerView.getRectPoint("Upper Y");
        int l_y = mLayerView.getRectPoint("Lower Y");

        int touchX = (int) event.getX();
        int touchY = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
            if ((touchX >= l_x && touchX <= r_x) && (touchY >= l_y && touchY <= u_y)) {
                System.out.println("Touched Rectangle, start activity.");
            }
        }
        return true;
    }

    private CameraPreview mCameraPreview;
    private LayerView mLayerView;

}

