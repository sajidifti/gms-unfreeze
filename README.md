# GMS Unfreeze

Stops HyperOS 3 from freezing Google Play services (and with it FCM push, e.g. WhatsApp calls and messages) while the screen is off. No root, no Shizuku, no PC after setup.

## How it works

HyperOS's freezer (`com.miui.server.greeze.power.PowerStrategyMode`) only runs while its internal `mEnabled` flags equal `0xc`. When a package whose name contains `android.cts` is added or updated, `EventReceiver.dealPackageAddRemove` assumes a CTS test run, sets flag `0x2` and turns the freezer off until the next reboot.

This app bundles an empty stub, `com.sajidifti.android.cts.trigger`, and re-installs it after every boot.

Logcat confirms it:

```
GreezeManager: pkg:com.sajidifti.android.cts.trigger package added.
PowerStrategyMode: setEnable newValue=false ... type=2 enable=true
```

## Use

1. Install the app, open it, tap **Disable freezer now**.
2. Allow **Install unknown apps** for GMS Unfreeze, tap again and confirm the install.
3. After each reboot the app posts a notification. Tap it and confirm. HyperOS always asks, even for updates.

## Limits

- The whole screen-off freezer is off, not just for GMS, so standby battery use may go up.
- HyperOS shows a "frequent installs" risk check if the trigger is installed many times in one day.
- Tested on Redmi Note 14 (beryl), HyperOS OS3.0.306.0.WOQCNXM.

## Download

Get the APK from [Releases](https://github.com/sajidifti/gms-unfreeze/releases).

## Build

```bash
./gradlew :app:assembleRelease
```

The stub is built and bundled into `app` as `assets/trigger.apk` automatically. Output: `app/build/outputs/apk/release/app-release.apk`. Without `SIGNING_KEYSTORE`/`SIGNING_PASSWORD` set, it is signed with the debug key.

## Releasing

Push a `vX.Y.Z` tag. The `Release` workflow builds with the release key from the `SIGNING_KEYSTORE_B64` and `SIGNING_PASSWORD` secrets and publishes the APK. App and stub must always be signed with the same key.
