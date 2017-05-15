package edu.ucsc.cmps115_spring2017.face2name.Identity;

import android.graphics.Bitmap;

/**
 * Created by micah on 4/29/17.
 */

public final class Identity {
    public long key;
    public String name;
    public Bitmap image;

    public Identity(long keyVal, String nameVal, Bitmap imageVal) {
        key = keyVal;
        name = nameVal;
        image = imageVal;
    }
}
