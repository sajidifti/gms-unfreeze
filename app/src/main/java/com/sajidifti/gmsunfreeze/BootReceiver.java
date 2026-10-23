package com.sajidifti.gmsunfreeze;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult pending = goAsync();
        new Thread(() -> {
            Trigger.fire(context);
            pending.finish();
        }).start();
    }
}
