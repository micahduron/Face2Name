package edu.ucsc.cmps115_spring2017.face2name.CV;

import android.content.Context;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.ucsc.cmps115_spring2017.face2name.R;
import edu.ucsc.cmps115_spring2017.face2name.Utils.Image;
import edu.ucsc.cmps115_spring2017.face2name.Utils.Rectangle;

/**
 * Created by micah on 5/29/17.
 */

public class EyeDetector {
    public EyeDetector(Context context) throws IOException {
        mEyeClassifier = new ImageClassifier(context, R.raw.haarcascade_eye_tree_eyeglasses);
    }

    public List<Rectangle> detect(Image image) {
        MatOfRect rectMat = new MatOfRect();

        mEyeClassifier.detectMultiScale(image.getMat(), rectMat);

        Rect[] eyeRects = rectMat.toArray();
        List<Rectangle> result = new ArrayList<>(eyeRects.length);

        for (final Rect eyeRect : eyeRects) {
            result.add(new Rectangle(eyeRect));
        }
        return result;
    }

    private ImageClassifier mEyeClassifier;
}
