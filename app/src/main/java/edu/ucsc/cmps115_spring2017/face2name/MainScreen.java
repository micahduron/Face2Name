package edu.ucsc.cmps115_spring2017.face2name;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
import java.util.ArrayList;
import java.util.List;

import edu.ucsc.cmps115_spring2017.face2name.AppStateMachine.AppState;
import edu.ucsc.cmps115_spring2017.face2name.CV.FaceRecognition;
import edu.ucsc.cmps115_spring2017.face2name.Camera.AutoFocusCapability;
import edu.ucsc.cmps115_spring2017.face2name.Camera.CameraPreview;
import edu.ucsc.cmps115_spring2017.face2name.Camera.FaceDetectionCapability;
import edu.ucsc.cmps115_spring2017.face2name.Camera.FaceDetectionCapability.Face;
import edu.ucsc.cmps115_spring2017.face2name.Camera.OrientationCapability;
import edu.ucsc.cmps115_spring2017.face2name.Identity.Identity;
import edu.ucsc.cmps115_spring2017.face2name.Identity.IdentityStorage;
import edu.ucsc.cmps115_spring2017.face2name.Layer.LayerView;
import edu.ucsc.cmps115_spring2017.face2name.Utils.Image;
import edu.ucsc.cmps115_spring2017.face2name.Utils.Rectangle;

public class MainScreen
        extends AppCompatActivity
        implements CameraPreview.PreviewCallbacks,
        AppStateMachine.Callbacks {
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Failed to load library.");
        }
    }

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
        mFaceDetector = new FaceDetectionCapability();
        AutoFocusCapability autoFocus = new AutoFocusCapability();

        mCameraPreview = (CameraPreview) findViewById(R.id.camera_preview);
        mCameraPreview.setCapabilities(mOrientation, mFaceDetector, autoFocus);

        mLayerView = (LayerView) findViewById(R.id.layer_view);
        mNameBox = (EditText) findViewById(R.id.name_text);

        //mIdentityList = mStorage.dumpIdentities();
        //mFaceRecognizer.initialize(mIdentityList);

        mLayerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mCameraPreview.isReady()) return false;
                if (event.getActionMasked() != MotionEvent.ACTION_DOWN) return true;

                switch (mStateMachine.getState()) {
                    case IDLE:
                        if (mFaceDetector.getNumDetectedFaces() > 0) {
                            mStateMachine.setState(AppState.SCREEN_PAUSED);
                        }
                        break;
                    case SCREEN_PAUSED:
                        mSelectedFace = getSelectedFace((int) event.getX(), (int) event.getY());

                        mStateMachine.setState(mSelectedFace == null ? AppState.IDLE : AppState.FACE_SELECTED);
                        break;
                    case FACE_SELECTED:
                        Rectangle prevSelectedFace = mSelectedFace;
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
    public void onAppStateChange(AppState oldState, AppState newState) {
        Log.d("State change", "Old: " + oldState.toString() + ", New: " + newState.toString());

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
                // This has to be called before .stopFaceDetection() because that call will clear
                // the list of detected faces.
                getFaceRegions();

                mCameraPreview.stopPreview();
                mFaceDetector.stopFaceDetection();

                drawFaceRegions();
                break;
            case FACE_SELECTED:
                if (mSelectedFace == null) {
                    Log.d("Face", "Face is null");
                }
                //getBM(mSelectedFace);
                mFaceRecognizer.addFace(getBM(mSelectedFace));
                Log.d("Face Selected", "Added face");
                //mRecognition.identify(getBM(mSelectedFace));

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

    private void getFaceRegions() {
        Face[] faceList = mFaceDetector.getFaces();
        if (faceList == null) return;

        Rectangle visibleRegion = new Rectangle(mLayerView.getBoundingRect());

        for (final Face face : faceList) {
            Rectangle faceRect = new Rectangle();

            mFaceTransform.mapRect(faceRect, face.getRect());
            faceRect.scale(1.5f);

            if (visibleRegion.contains(faceRect)) {
                mFaceRegions.add(faceRect);
            }
        }
    }

    private void drawFaceRegions() {
        LayerView.Drawer drawer = mLayerView.getDrawer();
        drawer.beginDrawing();
        drawer.clearScreen();

        for (final Rectangle faceRect : mFaceRegions) {
            drawer.drawBox(faceRect);
        }
        drawer.endDrawing();
    }

    private Rectangle getSelectedFace(int x, int y) {
        for (final Rectangle faceRect : mFaceRegions) {
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

    // Returns an Image of the cropped bitmap to the rectangle's dimensions
    private Image getBM(Rectangle faceRect) {
        mPreviewBitmap = (mPreviewBitmap == null ? mCameraPreview.getBitmap() : mCameraPreview.getBitmap(mPreviewBitmap));
        mCroppedBitmap = Bitmap.createBitmap(mPreviewBitmap, (int) faceRect.left, (int) faceRect.top, (int) faceRect.width(), (int) faceRect.height());
        //Log.d("Face", Integer.toString(mPreviewBitmap.getPixel((int)faceRect.left,(int)faceRect.top)));
        //Log.d("Face", Integer.toString(mCroppedBitmap.getPixel(0,0)));
        Bitmap emptyBitmap = Bitmap.createBitmap(mCroppedBitmap.getWidth(), mCroppedBitmap.getHeight(), mCroppedBitmap.getConfig());
        if (mCroppedBitmap.sameAs(emptyBitmap)) {
            Log.d("Face", "Cropped bitmap is empty");
        }
        Log.d("Face", "Bitmap cropped");
        mFaceImage = new Image(mCroppedBitmap);
        //mFaceImage.toBitmap();
        if (mFaceImage == null) {
            Log.d("Face", "Image is null");
        }
        Log.d("Face Selected", "Image returned");
        return mFaceImage;
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
    private Bitmap mCroppedBitmap;
    private List<Rectangle> mFaceRegions = new ArrayList<>();
    private Rectangle mSelectedFace;
    private IdentityStorage mStorage;
    private FaceRecognition mFaceRecognizer;
    private FaceRecognition mRecognition;
    private List<Identity> mIdentityList = new ArrayList<>();
    private Image mFaceImage;
    // NOTE: Initialized to a test value.
    private Identity mCurrentIdentity = new Identity(42, null, null);
    private IdentityStorage mIdentityStorage;
}