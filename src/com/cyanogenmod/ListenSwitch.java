package com.cyanogenmod.dockaudio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.UEventObserver;
import android.util.Log;

public class ListenSwitch extends Service {
    private static final String LOG_TAG = "DockAudio";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mUEventObserver.startObserving("DEVPATH=/class/switch/emuconn");
        Log.i(LOG_TAG, "Dock Audio service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "Dock Audio service stopped");
    }


    /*
     * Listens for uevent messages from the kernel
     */
    private final UEventObserver mUEventObserver = new UEventObserver() {
        @Override
        public void onUEvent(UEventObserver.UEvent event) {
            Log.i(LOG_TAG, "DockAudio UEVENT: " + event.toString());

            String name = event.get("SWITCH_NAME");
            String state = event.get("SWITCH_STATE");

            Intent intent;
            if ("0".equals(state) || "No Device".equals(name)) {
                intent = new Intent("com.cyanogenmod.dockaudio.DISABLE_AUDIO");
            } if ("Mono out".equals(name) || "Stereo out".equals(name)) {
                intent = new Intent("com.cyanogenmod.dockaudio.ENABLE_ANALOG_AUDIO");
            } else { //if ("SPDIF audio out".equals(name)) {
                intent = new Intent("com.cyanogenmod.dockaudio.DIGITAL_AUDIO");
            }

            startActivity(intent);
        }
    };
}
