package com.sajidifti.gmsunfreeze;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;

public class InstallResultReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
        if (status == PackageInstaller.STATUS_SUCCESS) {
            Trigger.record(context, "Freezer disabled (trigger installed)");
            Trigger.notifications(context).cancel(Trigger.NOTIFY_ID);
            return;
        }
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            // First install, or the system wants confirmation: ask the user with one tap.
            Intent confirm = intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent.class);
            Trigger.record(context, "Waiting for install confirmation");
            if (confirm == null) return;
            confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Works when our activity is on screen; otherwise the notification below is the way in.
            try {
                context.startActivity(confirm);
            } catch (RuntimeException ignored) {
            }
            PendingIntent tap = PendingIntent.getActivity(context, 0, confirm,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            Notification n = new Notification.Builder(context, Trigger.CHANNEL)
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setContentTitle("GMS fix not active")
                    .setContentText("Tap to confirm, so HyperOS stops freezing notifications")
                    .setContentIntent(tap)
                    .setAutoCancel(true)
                    .build();
            Trigger.notifications(context).notify(Trigger.NOTIFY_ID, n);
            return;
        }
        Trigger.record(context, "Failed: " + intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
    }
}
