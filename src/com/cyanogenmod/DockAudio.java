package com.cyanogenmod.dockaudio;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Bundle;
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

    public int getDockState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("DockState",0);
        return prefs.getInt("DockState", 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Bundle extras = intent.getExtras();

        if(extras != null && extras.containsKey("android.intent.extra.DOCK_STATE")){
            int mDockState = extras.getInt("android.intent.extra.DOCK_STATE",1);
            Log.i(LOG_TAG, (mDockState != 0) ? "Device docked!" : "Device undocked!");
            SharedPreferences prefs = context.getSharedPreferences("DockState",0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("DockState", mDockState);
            editor.commit();

            /* When we undock the phone, first the audio is disconnected from the switch
               but DockState is non-zero. This is needed to actually disable the audio
               routing completely */
            if(mDockState == 0) {
                Intent disable = new Intent();
                disable.setAction("com.cyanogenmod.dockaudio.UNDOCK");
                context.sendBroadcast(disable);
            }

        }else if(intent.getAction().equals("com.cyanogenmod.dockaudio.UNDOCK")){
            Log.i(LOG_TAG, "Disabled audio on dock!");
            am.setParameters("DockState=0");

        }else if(intent.getAction().equals("com.cyanogenmod.dockaudio.ENABLE_SPEAKER_AUDIO")){
            Log.i(LOG_TAG, "Enabled speaker audio!");
            AudioSystem.setDeviceConnectionState(AUDIO_OUT_SPDIF, 0, "");
            AudioSystem.setDeviceConnectionState(AUDIO_OUT_ANALOG, 0, "");
            am.setParameters("routing=2;DockState=" + getDockState(context));
            AudioSystem.setForceUse(FOR_DOCK, 0);

        }else if(intent.getAction().equals("com.cyanogenmod.dockaudio.ENABLE_ANALOG_AUDIO")){
            Log.i(LOG_TAG, "Enabled analog audio on dock!");
            am.setParameters("routing=2048;DockState=" + getDockState(context));
            AudioSystem.setForceUse(FOR_DOCK, AUDIO_OUT_ANALOG);
            AudioSystem.setDeviceConnectionState(AUDIO_OUT_ANALOG, 1, "");

        }else if(intent.getAction().equals("com.cyanogenmod.dockaudio.ENABLE_DIGITAL_AUDIO")){
            Log.i(LOG_TAG, "Enabled digital audio on dock!");
            am.setParameters("routing=1024;DockState=" + getDockState(context));
            AudioSystem.setForceUse(FOR_DOCK, AUDIO_OUT_SPDIF);
            AudioSystem.setDeviceConnectionState(AUDIO_OUT_SPDIF, 1, "");
        }
    }
}
