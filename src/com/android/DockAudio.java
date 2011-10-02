package com.android.dockaudio;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class DockAudio extends BroadcastReceiver {
    private static final String LOG_TAG = "DockAudio";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "Got intent from dock");
        if(intent.getExtras().containsKey("android.intent.extra.DOCK_STATE")){
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int state = intent.getExtras().getInt("android.intent.extra.DOCK_STATE",1);
            if(state == 0){
                Log.i(LOG_TAG, "Removed from dock!");
                am.setParameters("DockState=0");
            } else {
                Log.i(LOG_TAG, "Docked!");
                am.setParameters("DockState=1");
            }
        }
    }
}
