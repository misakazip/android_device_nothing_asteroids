/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glyph.Services;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;

import org.lineageos.glyph.Constants.Constants;
import org.lineageos.glyph.Manager.StatusManager;
import org.lineageos.glyph.Utils.FileUtils;

public class TorchService extends Service {

    private boolean flashlightEnabled;

    private final ContentObserver flashlightObserver =
            new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            flashlightEnabled = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.FLASHLIGHT_ENABLED, 0) != 0;
            updateGlyph();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.FLASHLIGHT_ENABLED), false,
                flashlightObserver);
        flashlightObserver.onChange(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(flashlightObserver);
        flashlightEnabled = false;
        updateGlyph();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateGlyph() {
        FileUtils.writeAllLed(flashlightEnabled || StatusManager.isAllLedActive()
                ? Constants.getMaxBrightness() : 0);
    }
}
