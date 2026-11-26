package com.simats.focusflow.service

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast

class FlowDeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, FlowDeviceAdminReceiver::class.java)
        }

        fun isDeviceAdminActive(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
            return dpm.isAdminActive(getComponentName(context))
        }

        fun requestDeviceAdmin(
            context: Context,
            promptMessage: String = "Activate Device Admin to lock uninstall during Strict Focus"
        ) {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return
            val component = getComponentName(context)
            if (!dpm.isAdminActive(component)) {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component)
                    putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, promptMessage)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
        }

        fun requireDeviceAdmin(
            context: Context,
            promptMessage: String = "Activate Device Admin to lock uninstall during Strict Focus"
        ): Boolean {
            if (isDeviceAdminActive(context)) {
                return true
            }
            requestDeviceAdmin(context, promptMessage)
            try {
                Toast.makeText(context.applicationContext, promptMessage, Toast.LENGTH_LONG).show()
            } catch (_: Exception) {}
            return false
        }

        fun removeDeviceAdmin(context: Context) {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return
            val component = getComponentName(context)
            if (dpm.isAdminActive(component)) {
                try {
                    dpm.removeActiveAdmin(component)
                } catch (_: Exception) {}
            }
        }
    }
}
