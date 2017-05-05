package edu.ucsc.cmps115_spring2017.face2name;

/**
 * Created by micah on 5/4/17.
 */

public class AppStateMachine {
    public enum AppState {
        INIT,
        IDLE,
        SELECTED,
        ERROR
    }

    public interface Callbacks {
        void onAppStateChange(AppState oldState, AppState newState);
    }

    public AppStateMachine() {
        this(AppState.INIT, null);
    }

    public AppStateMachine(AppState initState) {
        this(initState, null);
    }

    public AppStateMachine(AppState initState, Callbacks callbacks) {
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
