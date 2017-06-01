package edu.ucsc.cmps115_spring2017.face2name;

/**
 * Created by micah on 5/4/17.
 */

public class AppStateMachine {
    public enum AppState {
        /**
         * The app is within this state from when the app is started to when the camera is first
         * initialized.
         */
        INIT,
        /**
         * The camera is streaming preview images to the screen.
         */
        IDLE,
        /**
         * The screen has been tapped by the user.
         */
        SCREEN_TAPPED,
        /**
         * The camera preview is paused.
         */
        SCREEN_PAUSED,
        /**
         * The user has tapped a face on the screen.
         */
        FACE_SELECTED,
        /**
         * A generic error state.
         */
        ERROR
    }

    interface Callbacks {
        void onAppStateChange(AppState oldState, AppState newState);
    }

    AppStateMachine() {
        this(AppState.INIT, null);
    }

    AppStateMachine(AppState initState) {
        this(initState, null);
    }

    AppStateMachine(AppState initState, Callbacks callbacks) {
        mState = initState;
        mCallbacks = callbacks;
    }

    public AppState getState() {
        return mState;
    }

    public void setState(AppState newState) {
        AppState oldState = getState();

        mState = newState;

        if (mCallbacks != null) {
            mCallbacks.onAppStateChange(oldState, newState);
        }
    }

    private AppState mState;
    private Callbacks mCallbacks;
}
