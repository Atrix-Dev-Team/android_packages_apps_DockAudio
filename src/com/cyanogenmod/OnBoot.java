package com.cyanogenmod.dockaudio;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class OnBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(), ListenSwitch.class.getName());
        ComponentName service = context.startService(new Intent().setComponent(comp));
    }
}
