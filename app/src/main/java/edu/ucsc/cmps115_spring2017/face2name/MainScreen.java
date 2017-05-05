package edu.ucsc.cmps115_spring2017.face2name;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import edu.ucsc.cmps115_spring2017.face2name.Camera.CameraPreview;
import edu.ucsc.cmps115_spring2017.face2name.Camera.FaceDetectionCapability;
import edu.ucsc.cmps115_spring2017.face2name.Camera.FaceDetectionCapability.Face;
import edu.ucsc.cmps115_spring2017.face2name.Camera.OrientationCapability;
import edu.ucsc.cmps115_spring2017.face2name.Layer.LayerView;

public class MainScreen
       extends AppCompatActivity
       implements CameraPreview.PreviewCallbacks,
                  FaceDetectionCapability.FaceDetectionListener
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
    }

    @Override
    public void onStart() {
        super.onStart();

        mOrientation = new OrientationCapability(getWindowManager().getDefaultDisplay());
        mFaceDetector = new FaceDetectionCapability(this);
        
        mCameraPreview = (CameraPreview) findViewById(R.id.camera_preview);
        mCameraPreview.setCapabilities(mOrientation, mFaceDetector);

        mLayerView = (LayerView) findViewById(R.id.layer_view);
        mName = (EditText)findViewById(R.id.name_text);
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
        mCameraPreview.stopPreview();
        mFaceDetector.stopFaceDetection();
    }

    @Override
    public void onCameraError(Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onPreviewReady() {
        mFaceTransform = FaceDetectionCapability.getFaceTransform(
                mOrientation.getCameraAngle(),
                mLayerView.getWidth(),
                mLayerView.getHeight()
        );
        mCameraPreview.startPreview();
        mFaceDetector.startFaceDetection();
    }

    @Override
    public void onFaceDetection(Face[] faces) {
        LayerView.Drawer drawer = mLayerView.getDrawer();
        drawer.beginDrawing();
        drawer.clearScreen();

        RectF faceRect = new RectF();

        for (final Face face : faces) {
            mFaceTransform.mapRect(faceRect, face.getRect());

            drawer.drawBox(faceRect);
        }
        drawer.endDrawing();
    }

    /*
    This helper function checks the input event against a few other events that might occur
    that night be associated with a press.
    */
    private boolean isPressEvent(int inputEvent){
        return (inputEvent == MotionEvent.ACTION_DOWN ||
                inputEvent == MotionEvent.ACTION_UP);
    }
    
    public boolean onTouchEvent(MotionEvent event) {
        //gets the coordinate of press event
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        // If we tapped on the screen and if we didn't tap in the text box
        if(event.getAction() == MotionEvent.ACTION_DOWN && !getLocationOnScreen().contains(touchX,touchY)) {

            // If the textbox is hidden, bring up the textbox and the keyboard
            if(mName.getVisibility() == View.INVISIBLE) {
                mName.setVisibility(View.VISIBLE);
                mName.requestFocus();
                InputMethodManager input = (InputMethodManager) getSystemService(MainScreen.INPUT_METHOD_SERVICE);
                input.showSoftInput(mName, InputMethodManager.SHOW_IMPLICIT);
            }

            // Otherwise, hide the textbox and close the keyboard
            else {
                InputMethodManager input = (InputMethodManager) getSystemService(MainScreen.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(mName.getWindowToken(), 0);
                mName.setVisibility(View.INVISIBLE);
                mName.clearFocus();
            }
        }
        return true;
    }

    // Makes a rectangle so we can check if we tapped inside of our textbox
    private Rect getLocationOnScreen( ) {
        Rect tempRect = new Rect();
        int[] location = new int[2];

        mName.getLocationOnScreen(location);

        tempRect.left = location[0];
        tempRect.top = location[1];
        tempRect.right = location[0] + mName.getWidth();
        tempRect.bottom = location[1] + mName.getHeight();

        return tempRect;
    }

    private EditText mName;
    private CameraPreview mCameraPreview;
    private OrientationCapability mOrientation;
    private FaceDetectionCapability mFaceDetector;
    private Matrix mFaceTransform;
    private LayerView mLayerView;
}
