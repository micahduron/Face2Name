package edu.ucsc.cmps115_spring2017.face2name.CameraCapabilities;

import android.graphics.Bitmap;
import android.hardware.Camera;

/**
 * Created by micah on 4/21/17.
 */

public interface CameraCapability {
    void onAttach(Camera camera);

    void onPreviewFrame(Bitmap bitmap, Camera camera);

    void onRelease(Camera camera);
}
