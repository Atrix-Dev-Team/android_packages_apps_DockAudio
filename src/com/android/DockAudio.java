package com.android.dockaudio;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.lang.Thread;

/**************************************************
 * Intents ------------------------> Numeric code *
 *                                                *
 * MOT_BASIC_DOCK_AUDIO_PLUG ------> 0x800        *
 * MOT_SMART_DOCK_SPDIF_PLUG ------> 0x400        *
 * EXTDISP_STATUS_DISPLAY (HDMI) --> 0x1000       *
 *                                                *
 * libaudio routing ---------------> Numeric code *
 * HDMI or S/PDIF -----------------> 0x400        *
 * EMU stereo (car dock) ----------> 0x800        *
 *                                                *
 **************************************************/


public class DockAudio extends BroadcastReceiver {
    private static final String LOG_TAG = "DockAudio";

    private static final int AUDIO_OUT_ANALOG = 0x800;
    private static final int AUDIO_OUT_SPDIF  = 0x400;
    private static final int FOR_DOCK         = 3;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "Got intent from dock");

        if(intent.getExtras().containsKey("android.intent.extra.DOCK_STATE")){
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int state = intent.getExtras().getInt("android.intent.extra.DOCK_STATE",1);

            if (state == 0) {
                Log.i(LOG_TAG, "Removed from dock!");
                AudioSystem.setForceUse(FOR_DOCK, 0);
                AudioSystem.setDeviceConnectionState(AUDIO_OUT_SPDIF, 0, "");
                AudioSystem.setDeviceConnectionState(AUDIO_OUT_ANALOG, 0, "");
                am.setParameters("DockState=0;routing=2");
            } else if (state == 1) {
                /* Sleep a second to let whisper and the kernel to catch up if
                   the audio switch didn't trigger yet */
                try { Thread.sleep(1000); } catch (Exception e) {}

                /* emuconn indicates if we should use analog out (1) or digital out (0) */
                int emuconn;
                try {
                    emuconn = (new FileInputStream(new File("/sys/class/switch/emuconn/state"))).read();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Problem detecting kind of dock. Defaulting to Standard Dock");
                    emuconn = 1;
                }

                if(emuconn == 0){
                    Log.i(LOG_TAG, "Docked on desktop HD Dock!");
                    am.setParameters("DockState=1;routing=1024");
                    AudioSystem.setForceUse(FOR_DOCK, AUDIO_OUT_SPDIF);
                    AudioSystem.setDeviceConnectionState(AUDIO_OUT_SPDIF, 1, "");
               } else {
                    Log.i(LOG_TAG, "Docked on desktop Standard Dock!");
                    am.setParameters("DockState=1;routing=2048");
                    AudioSystem.setForceUse(FOR_DOCK, AUDIO_OUT_ANALOG);
                    AudioSystem.setDeviceConnectionState(AUDIO_OUT_ANALOG, 1, "");
               }
            } else {
                Log.i(LOG_TAG, "Docked on car!");
                am.setParameters("DockState=2;routing=2048");
                AudioSystem.setForceUse(FOR_DOCK, AUDIO_OUT_ANALOG);
                AudioSystem.setDeviceConnectionState(AUDIO_OUT_ANALOG, 1, "");
            }
        }
    }
}
