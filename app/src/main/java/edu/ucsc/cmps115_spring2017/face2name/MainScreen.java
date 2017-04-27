package edu.ucsc.cmps115_spring2017.face2name;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.app.AlertDialog;
import android.util.Log;

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


    /*
    This helper function checks the input event against a few other events that might occur
    that night be associated with a press.
    */
    private boolean isPressEvent(int inputEvent){

        /*
        Because MotionEvent constants are not supported in the lower versions of API
        That we are supporting, this constants are defined manually at the bottom of
        MainScreen.java
        */
        return (inputEvent ==  ACTION_BUTTON_PRESS||
                inputEvent == ACTION_BUTTON_RELEASE ||
                inputEvent == ACTION_DOWN ||
                inputEvent == ACTION_UP);
    }
    
    public boolean onTouchEvent(MotionEvent event) {

        //gets the coordinate of press event
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();


        if (isPressEvent(event.getAction()))
        {
            // try/catch block because getRectPoint throws an IllegalArgumentException
            try{
                //returns the coordinates of the red rectangle
                int leftXCoordinate = mLayerView.getRectPoint(LayerView.coordinate_sections.LEFT_X);
                int rightXCoordinate = mLayerView.getRectPoint(LayerView.coordinate_sections.RIGHT_X);
                int upperYCoordinate = mLayerView.getRectPoint(LayerView.coordinate_sections.UPPER_Y);
                int lowerYCoordinate = mLayerView.getRectPoint(LayerView.coordinate_sections.LOWER_Y);

                //If the press was within the X bounds and within the Y bounds, continue
                if ((touchX >= leftXCoordinate && touchX <= rightXCoordinate) &&
                        (touchY >= upperYCoordinate && touchY <= lowerYCoordinate)) {

                   //build dialong
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                    // set title
                    alertDialogBuilder.setTitle("Rectangle Pressed");

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            }
            catch(IllegalArgumentException iArg){
                    Log.d("ILLEGALARGUMENT", "IllegalArgumentException: " + iArg);
            }
        }
        return true;
    }

    private CameraPreview mCameraPreview;
    private LayerView mLayerView;

    //Custom constants as a replacement for MotionEvent.constants
    private static final int ACTION_DOWN = 0;
    private static final int ACTION_UP = 1;
    private static final int ACTION_BUTTON_PRESS = 11;
    private static final int ACTION_BUTTON_RELEASE = 12;


}

