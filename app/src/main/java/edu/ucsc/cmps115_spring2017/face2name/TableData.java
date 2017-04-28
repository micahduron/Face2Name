package edu.ucsc.cmps115_spring2017.face2name;

import android.provider.BaseColumns;

/**
 * Created by evanlouie on 4/20/17.
 */

public class TableData extends Object {
    public TableData() {

    }

    public static abstract class TableInfo implements BaseColumns {
        public static final String USER_NAME = "user_name";
        public static final String USER_ID = "user_id";
        public static final String USER_PASS = "user_pass";
        public static final String DATABASE_NAME = "user_info";
        public static final String TABLE_NAME = "reg_info";
    }

}