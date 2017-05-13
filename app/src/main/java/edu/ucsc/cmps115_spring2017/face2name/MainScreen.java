package edu.ucsc.cmps115_spring2017.face2name;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import edu.ucsc.cmps115_spring2017.face2name.AppStateMachine.AppState;
import edu.ucsc.cmps115_spring2017.face2name.Camera.CameraPreview;
import edu.ucsc.cmps115_spring2017.face2name.Camera.FaceDetectionCapability;
import edu.ucsc.cmps115_spring2017.face2name.Camera.FaceDetectionCapability.Face;
import edu.ucsc.cmps115_spring2017.face2name.Camera.OrientationCapability;
import edu.ucsc.cmps115_spring2017.face2name.Layer.LayerView;

public class MainScreen
        extends AppCompatActivity
        implements CameraPreview.PreviewCallbacks,
        FaceDetectionCapability.FaceDetectionListener,
        AppStateMachine.Callbacks
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main_screen);

        mStateMachine = new AppStateMachine(AppState.INIT, this);
    }

    @Override
    public void onStart() {
        super.onStart();

        mInputManager = (InputMethodManager) getSystemService(MainScreen.INPUT_METHOD_SERVICE);

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

        mStateMachine.setState(AppState.INIT);
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
        mStateMachine.setState(AppState.IDLE);
    }

    @Override
    public void onFaceDetection(Face[] faces) {
        LayerView.Drawer drawer = mLayerView.getDrawer();
        drawer.beginDrawing();
        drawer.clearScreen();

        RectF faceRect = new RectF();
        boolean tappedOnFace = false;

        for (final Face face : faces) {
            mFaceTransform.mapRect(faceRect, face.getRect());

            if(mStateMachine.getState() == AppState.SELECTED && faceRect.contains(mTouchX, mTouchY)) {
                tappedOnFace = true;
            }
            drawer.drawBox(faceRect);
        }
        drawer.endDrawing();

        if (mStateMachine.getState() == AppState.SELECTED) {
            mStateMachine.setState(tappedOnFace ? AppState.FACE_SELECTED : AppState.IDLE);
            // If the face is selected
            if (mStateMachine.getState() == AppState.FACE_SELECTED) {
                Log.d("Bitmap", "Face selected");
                Log.d("Bitmap", "Rect Width: " + Float.toString(faceRect.width()));
                Log.d("Bitmap", "Rect Height: " + Float.toString(faceRect.height()));
                previewBM = mCameraPreview.getBitmap();
                croppedBM = Bitmap.createBitmap(previewBM, (int) faceRect.left, (int) faceRect.top, (int)faceRect.width(), (int)faceRect.height());
                Log.d("Bitmap", "BM Width: " + Float.toString(croppedBM.getWidth()));
                Log.d("Bitmap", "BM Height: " + Float.toString(croppedBM.getHeight()));
                int a = previewBM.getPixel((int) faceRect.left + 5, (int) faceRect.top + 2);
                int b = croppedBM.getPixel(5,2);
                if (a==b){
                    Log.d("Bitmap", "same");
                }
                Bitmap emptyBitmap = Bitmap.createBitmap(croppedBM.getWidth(), croppedBM.getHeight(), croppedBM.getConfig());
                if (croppedBM.sameAs(emptyBitmap)) {
                    Log.d("Bitmap", "empty");
                }
            }

        }
    }

    @Override
    public void onAppStateChange(AppState oldState, AppState newState) {
        switch (newState) {
            case IDLE:
                if (oldState == AppState.SELECTED) break;
                if (oldState == AppState.IDLE) break;

                hideKeyboard();
                mCameraPreview.startPreview();
                mFaceDetector.startFaceDetection();
                break;
            case FACE_SELECTED:
                showKeyboard();
                mCameraPreview.stopPreview();
                mFaceDetector.stopFaceDetection();
                break;
        }
    }

    private void showKeyboard() {
        mName.setVisibility(View.VISIBLE);
        mInputManager.showSoftInput(mName, InputMethodManager.SHOW_IMPLICIT);
        mName.requestFocus();
    }

    private void hideKeyboard() {
        mName.setVisibility(View.INVISIBLE);
        mInputManager.hideSoftInputFromWindow(mName.getWindowToken(), 0);
        mName.clearFocus();
    }

    public boolean onTouchEvent(MotionEvent event) {
        // Ignore touch events during initialization.
        if (mStateMachine.getState() == AppState.INIT) return false;

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mStateMachine.getState() == AppState.IDLE) {
                mTouchX = (int) event.getX();
                mTouchY = (int) event.getY();

                mStateMachine.setState(AppState.SELECTED);
            } else if (mStateMachine.getState() == AppState.FACE_SELECTED && !getLocationOnScreen().contains(mTouchX, mTouchY)) {
                mStateMachine.setState(AppState.IDLE);
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

    private AppStateMachine mStateMachine;
    private EditText mName;
    private CameraPreview mCameraPreview;
    private OrientationCapability mOrientation;
    private FaceDetectionCapability mFaceDetector;
    private Matrix mFaceTransform;
    private LayerView mLayerView;
    private int mTouchX, mTouchY;
    private InputMethodManager mInputManager;
    private Bitmap previewBM;
    private Bitmap croppedBM;
}
