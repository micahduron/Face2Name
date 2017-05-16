package edu.ucsc.cmps115_spring2017.face2name;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.ucsc.cmps115_spring2017.face2name.AppStateMachine.AppState;
import edu.ucsc.cmps115_spring2017.face2name.Camera.AutoFocusCapability;
import edu.ucsc.cmps115_spring2017.face2name.Camera.CameraPreview;
import edu.ucsc.cmps115_spring2017.face2name.Camera.FaceDetectionCapability;
import edu.ucsc.cmps115_spring2017.face2name.Camera.FaceDetectionCapability.Face;
import edu.ucsc.cmps115_spring2017.face2name.Camera.OrientationCapability;
import edu.ucsc.cmps115_spring2017.face2name.Identity.Identity;
import edu.ucsc.cmps115_spring2017.face2name.Identity.IdentityStorage;
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

        mIdentityStorage = new IdentityStorage(this);
        // NOTE: This is here for testing purposes. Will be removed before release.
        mIdentityStorage.clearIdentities();

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
        AutoFocusCapability autoFocus = new AutoFocusCapability();

        mCameraPreview = (CameraPreview) findViewById(R.id.camera_preview);
        mCameraPreview.setCapabilities(mOrientation, mFaceDetector, autoFocus);

        mLayerView = (LayerView) findViewById(R.id.layer_view);
        mNameBox = (EditText)findViewById(R.id.name_text);

        mLayerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mCameraPreview.isReady()) return false;
                if (event.getActionMasked() != MotionEvent.ACTION_DOWN) return true;

                switch (mStateMachine.getState()) {
                    case IDLE:
                        if (mFaceDetector.getNumDetectedFaces() > 0) {
                            mStateMachine.setState(AppState.SCREEN_TAPPED);
                        }
                        break;
                    case SCREEN_PAUSED:
                        mSelectedFace = getSelectedFace((int) event.getX(), (int) event.getY());

                        mStateMachine.setState(mSelectedFace == null ? AppState.IDLE : AppState.FACE_SELECTED);
                        break;
                    case FACE_SELECTED:
                        RectF prevSelectedFace = mSelectedFace;
                        mSelectedFace = getSelectedFace((int) event.getX(), (int) event.getY());

                        if (mSelectedFace == null) {
                            mStateMachine.setState(AppState.IDLE);
                        } else if (!mSelectedFace.equals(prevSelectedFace)) {
                            mStateMachine.setState(AppState.FACE_SELECTED);
                        }
                        break;
                }
                return true;
            }
        });
        mNameBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mCurrentIdentity.name = v.getText().toString();
                    mIdentityStorage.storeIdentity(mCurrentIdentity);

                    mStateMachine.setState(AppState.SCREEN_PAUSED);

                    return true;
                }
                return false;
            }
        });
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
        if (mStateMachine.getState() == AppState.IDLE || mStateMachine.getState() == AppState.INIT) {
            mStateMachine.setState(AppState.IDLE);
        }
    }

    @Override
    public void onFaceDetection(Face[] faces) {
        if (mStateMachine.getState() == AppState.SCREEN_TAPPED) {
            for (final Face face : faces) {
                RectF faceRect = new RectF();
                mFaceTransform.mapRect(faceRect, face.getRect());

                mFaceRegions.add(faceRect);
            }
            mStateMachine.setState(AppState.SCREEN_PAUSED);
        }
    }

    @Override
    public void onAppStateChange(AppState oldState, AppState newState) {
        switch (newState) {
            case IDLE:
                mFaceRegions.clear();
                clearFaceRegions();

                hideNameBox();
                mCameraPreview.startPreview();
                mFaceDetector.startFaceDetection();
                break;
            case SCREEN_PAUSED:
                if (oldState == AppState.FACE_SELECTED) {
                    hideNameBox();
                }
                drawFaceRegions();
                mCameraPreview.stopPreview();
                mFaceDetector.stopFaceDetection();
                break;
            case FACE_SELECTED:
                Identity ident = mIdentityStorage.getIdentity(mCurrentIdentity);

                showNameBox(ident != null ? ident.name : null);
                break;
        }
    }

    private void showNameBox(String name) {
        mNameBox.setText(name);
        mNameBox.setVisibility(View.VISIBLE);

        if (name == null) {
            mInputManager.showSoftInput(mNameBox, InputMethodManager.SHOW_IMPLICIT);
            mNameBox.requestFocus();
        }
    }

    private void hideNameBox() {
        mNameBox.setVisibility(View.INVISIBLE);
        mInputManager.hideSoftInputFromWindow(mNameBox.getWindowToken(), 0);
        mNameBox.clearFocus();
    }

    private void drawFaceRegions() {
        LayerView.Drawer drawer = mLayerView.getDrawer();
        drawer.beginDrawing();
        drawer.clearScreen();

        for (final RectF faceRect : mFaceRegions) {
            drawer.drawBox(faceRect);
        }
        drawer.endDrawing();
    }

    private RectF getSelectedFace(int x, int y) {
        for (final RectF faceRect : mFaceRegions) {
            if (faceRect.contains(x, y)) {
                return faceRect;
            }
        }
        return null;
    }

    private void clearFaceRegions() {
        LayerView.Drawer drawer = mLayerView.getDrawer();
        drawer.beginDrawing();
        drawer.clearScreen();
        drawer.endDrawing();
    }

    // Returns a bitmap cropped to the rectangle's dimensions
    private Bitmap getBM(RectF faceRect){
        mPreviewBitmap = mPreviewBitmap == null ? mCameraPreview.getBitmap() : mCameraPreview.getBitmap(mPreviewBitmap);

        return  Bitmap.createBitmap(mPreviewBitmap, (int) faceRect.left, (int) faceRect.top, (int)faceRect.width(), (int)faceRect.height());
    }

    private AppStateMachine mStateMachine;
    private EditText mNameBox;
    private CameraPreview mCameraPreview;
    private OrientationCapability mOrientation;
    private FaceDetectionCapability mFaceDetector;
    private Matrix mFaceTransform;
    private LayerView mLayerView;
    private InputMethodManager mInputManager;
    private Bitmap mPreviewBitmap;
    private List<RectF> mFaceRegions = new ArrayList<>();
    private RectF mSelectedFace;
    // NOTE: Initialized to a test value.
    private Identity mCurrentIdentity = new Identity(42, null, null);
    private IdentityStorage mIdentityStorage;
}
