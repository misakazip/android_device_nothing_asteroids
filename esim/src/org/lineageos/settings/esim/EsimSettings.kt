/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.esim

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemProperties
import android.provider.Settings
import com.android.settingslib.drawer.CategoryKey
import com.android.settingslib.drawer.EntriesProvider
import com.android.settingslib.drawer.EntryController
import com.android.settingslib.drawer.ProviderSwitch

private const val SETTING_ESIM_SUPPORT = "asteroids_esim_support"
private const val ACTION_SIM_TYPE_UPDATE = "com.android.euicc.service.SIM_TYPE_UPDATE_ACTION"
private const val EXTRA_SLOT_ID = "com.android.euicc.service.extra_slot_id"
private const val EXTRA_SIM_TYPE = "com.android.euicc.service.extra_sim_type"

private fun isJpnSku() = SystemProperties.get("ro.boot.hardware.sku") == "JPN"

private fun isEsimEnabled(context: Context) =
    Settings.Global.getInt(context.contentResolver, SETTING_ESIM_SUPPORT, 0) == 1

private fun setEsimEnabled(context: Context, enabled: Boolean, persist: Boolean) {
    if (!isJpnSku()) return
    if (persist) {
        Settings.Global.putInt(context.contentResolver, SETTING_ESIM_SUPPORT, if (enabled) 1 else 0)
    }
    context.sendBroadcast(
        Intent(ACTION_SIM_TYPE_UPDATE)
            .putExtra(EXTRA_SLOT_ID, 1)
            .putExtra(EXTRA_SIM_TYPE, if (enabled) 1 else 0),
        Manifest.permission.READ_PRIVILEGED_PHONE_STATE,
    )
}

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val component = ComponentName(context, EsimEntriesProvider::class.java)
        val state = if (isJpnSku()) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        context.packageManager.setComponentEnabledSetting(
            component,
            state,
            PackageManager.DONT_KILL_APP,
        )
        if (isJpnSku()) setEsimEnabled(context, isEsimEnabled(context), persist = false)
    }
}

class EsimEntriesProvider : EntriesProvider() {
    override fun createEntryControllers(): List<EntryController> =
        listOf(EsimSwitchController(requireNotNull(context)))
}

private class EsimSwitchController(private val context: Context) :
    EntryController(), ProviderSwitch {

    override fun getKey() = "esim"

    override fun getMetaData() = MetaData(CategoryKey.CATEGORY_NETWORK)
        .setIcon(R.drawable.ic_sim_card)
        .setTitle(R.string.esim_support_title)

    override fun isSwitchChecked() = isEsimEnabled(context)

    override fun onSwitchCheckedChanged(checked: Boolean): Boolean {
        setEsimEnabled(context, checked, persist = true)
        return true
    }

    override fun getSwitchErrorMessage(attemptedChecked: Boolean) = ""
}
