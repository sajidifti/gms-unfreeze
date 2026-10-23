package com.sajidifti.gmsunfreeze;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;

/**
 * Re-installs the bundled "android.cts" stub. HyperOS 3's greezer sees the package
 * added broadcast, assumes a CTS run and switches its screen-off freezer off until reboot.
 */
final class Trigger {

    static final String STUB = "com.sajidifti.android.cts.trigger";
    static final String CHANNEL = "status";
    static final int NOTIFY_ID = 1;

    private Trigger() {}

    /** Starts the stub install; the result arrives in InstallResultReceiver. */
    static void fire(Context context) {
        PackageInstaller installer = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params =
                new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(STUB);
        // No prompt once this app is the stub's installer of record.
        params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
        try {
            int id = installer.createSession(params);
            try (PackageInstaller.Session session = installer.openSession(id)) {
                try (InputStream in = context.getAssets().open("trigger.apk");
                     OutputStream out = session.openWrite("trigger.apk", 0, -1)) {
                    in.transferTo(out);
                    session.fsync(out);
                }
                PendingIntent result = PendingIntent.getBroadcast(context, id,
                        new Intent(context, InstallResultReceiver.class),
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                session.commit(result.getIntentSender());
            }
        } catch (Exception e) {
            record(context, "Failed: " + e);
        }
    }

    static void record(Context context, String status) {
        prefs(context).edit()
                .putString("last", DateFormat.getDateTimeInstance().format(new Date()) + "\n" + status)
                .apply();
    }

    static String lastResult(Context context) {
        return prefs(context).getString("last", "Never triggered");
    }

    static NotificationManager notifications(Context context) {
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        nm.createNotificationChannel(
                new NotificationChannel(CHANNEL, "Status", NotificationManager.IMPORTANCE_DEFAULT));
        return nm;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("state", Context.MODE_PRIVATE);
    }
}
