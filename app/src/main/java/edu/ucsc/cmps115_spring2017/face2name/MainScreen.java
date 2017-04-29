package edu.ucsc.cmps115_spring2017.face2name;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import edu.ucsc.cmps115_spring2017.face2name.Camera.CameraPreview;
import edu.ucsc.cmps115_spring2017.face2name.Camera.OrientationCapability;
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
        mCameraPreview.setCapabilities(new OrientationCapability(getWindowManager().getDefaultDisplay()));
        mLayerView = (LayerView) findViewById(R.id.layer_view);
        nameField = (EditText)findViewById(R.id.nameText);
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
        return (inputEvent == MotionEvent.ACTION_DOWN ||
                inputEvent == MotionEvent.ACTION_UP);
    }
    
    public boolean onTouchEvent(MotionEvent event) {

        //gets the coordinate of press event
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();
       // Log.d("PRESS", "GET_X: "+touchX+ " GET_Y: "+touchY);
       // Log.d("PRESS", "EVENT ACTION: "+event.getAction());
        if (isPressEvent(event.getActionMasked()))
        {
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

        // If we tapped on the screen and if we didn't tap in the text box
        if(event.getAction() == MotionEvent.ACTION_DOWN && !getLocationOnScreen(nameField).contains(touchX,touchY)) {

            // If the textbox is hidden, bring up the textbox and the keyboard
            if(nameField.getVisibility() == View.INVISIBLE) {
                nameField.setVisibility(View.VISIBLE);
                nameField.requestFocus();
                InputMethodManager input = (InputMethodManager) getSystemService(MainScreen.INPUT_METHOD_SERVICE);
                input.showSoftInput(nameField, InputMethodManager.SHOW_IMPLICIT);
            }

            // Otherwise, hide the textbox and close the keyboard
            else {
                InputMethodManager input = (InputMethodManager) getSystemService(MainScreen.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(nameField.getWindowToken(), 0);
                nameField.setVisibility(View.INVISIBLE);
                nameField.clearFocus();
            }
        }
        return true;
    }

    // Makes a rectangle so we can check if we tapped inside of our textbox
    protected Rect getLocationOnScreen(EditText mEditText) {
        Rect mRect = new Rect();
        int[] location = new int[2];

        mEditText.getLocationOnScreen(location);

        mRect.left = location[0];
        mRect.top = location[1];
        mRect.right = location[0] + mEditText.getWidth();
        mRect.bottom = location[1] + mEditText.getHeight();

        return mRect;
    }

    private EditText nameField;
    private CameraPreview mCameraPreview;
    private LayerView mLayerView;
}

