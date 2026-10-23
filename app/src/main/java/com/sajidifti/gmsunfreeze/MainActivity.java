package com.sajidifti.gmsunfreeze;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        // Edge-to-edge is forced on targetSdk 35+, so keep content clear of the system bars.
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            android.graphics.Insets bars = insets.getInsets(android.view.WindowInsets.Type.systemBars());
            v.setPadding(pad, bars.top + pad, pad, bars.bottom + pad);
            return insets;
        });

        status = new TextView(this);
        status.setTextSize(15);
        status.setTextIsSelectable(true);

        // Plain TextView: the themed Button renders invisibly on HyperOS.
        TextView apply = new TextView(this);
        apply.setText("Disable freezer now");
        apply.setTextSize(16);
        apply.setTextColor(0xFFFFFFFF);
        apply.setBackgroundColor(0xFF1A73E8);
        apply.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = pad;
        apply.setLayoutParams(lp);
        apply.setOnClickListener(v -> {
            if (!getPackageManager().canRequestPackageInstalls()) {
                startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + getPackageName())));
                return;
            }
            status.setText("Installing trigger…");
            new Thread(() -> Trigger.fire(this)).start();
        });

        root.addView(apply);
        root.addView(status);
        setContentView(root);

        requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status.setText("Last run:\n" + Trigger.lastResult(this)
                + "\n\nRuns automatically after every boot. The first time, allow"
                + " \"Install unknown apps\" and confirm the install once.");
    }
}
