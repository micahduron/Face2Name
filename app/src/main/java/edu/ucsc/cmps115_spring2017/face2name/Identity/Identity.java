package edu.ucsc.cmps115_spring2017.face2name.Identity;


/**
 * Created by micah on 4/29/17.
 */

import edu.ucsc.cmps115_spring2017.face2name.Utils.Image;

/**
 * An {@code Identity} object contains the identifying details of an individual.
 */
public final class Identity {
    /**
     * A value that uniquely identifies an individual.
     */
    public long key;
    /**
     * The name of the individual.
     */
    public String name;
    /**
     * A bitmap image of the individual's face.
     */
    public Image image;

    public Identity(long keyVal, String nameVal, Image imageVal) {
        key = keyVal;
        name = nameVal;
        image = imageVal;
    }
}
