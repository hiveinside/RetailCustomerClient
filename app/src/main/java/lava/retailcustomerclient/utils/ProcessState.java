package lava.retailcustomerclient.utils;

/**
 * Created by Mridul on 6/9/2016.
 */
public class ProcessState {
    private static ProcessState instance = null;

    private ProcessState() {
        currentState = STATE_NOT_STARTED;
    }

    public static ProcessState getInstance() {
        if (instance == null) {
            instance = new ProcessState();
        }
        return instance;
    }

    public static final int STATE_NOT_STARTED                   = 0;
    public static final int STATE_CONNECTED                     = 101;
    public static final int STATE_GETTING_APPSLIST              = 102;
    public static final int STATE_DONE_GETTING_APPSLIST         = 103;
    public static final int STATE_DOWNLOADING_APKS              = 104;
    public static final int STATE_DONE_DOWNLOADING_APKS         = 105;
    public static final int STATE_INSTALLING_APKS               = 106;
    public static final int STATE_DONE_INSTALLING_APKS          = 107;
    public static final int STATE_COLLECTING_DEVICE_DATA        = 108;
    public static final int STATE_DONE_COLLECTING_DEVICE_DATA   = 109;
    public static final int STATE_SUBMITTING_DATA               = 110;
    public static final int STATE_DONE_SUBMITTING_DATA          = 111;

    private static int currentState;

    public static int getState() {
        return currentState;
    }

    public static void setState(int newState) {
        currentState = newState;
    }
}
