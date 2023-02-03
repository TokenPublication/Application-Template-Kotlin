package com.tokeninc.sardis.application_template

import android.app.Application
import com.tokeninc.deviceinfo.DeviceInfo

class AppTemp: Application() {
    private var currentDeviceMode = DeviceInfo.PosModeEnum.VUK507.name
    private var currentFiscalID: String? = null
    private var currentCardRedirection = DeviceInfo.CardRedirect.NOT_ASSIGNED.name

    override fun onCreate() {
        super.onCreate()
        startDeviceInfo()
    }

    fun getCurrentDeviceMode(): String? {
        return currentDeviceMode
    }

    fun setCurrentDeviceMode(currentDeviceMode: String) {
        this.currentDeviceMode = currentDeviceMode
    }

    fun getCurrentFiscalID(): String? {
        return currentFiscalID
    }

    fun setCurrentFiscalID(currentFiscalID: String?) {
        this.currentFiscalID = currentFiscalID
    }

    fun getCurrentCardRedirection(): String? {
        return currentCardRedirection
    }

    fun setCurrentCardRedirection(currentCardRedirection: String) {
        this.currentCardRedirection = currentCardRedirection
    }

    private fun startDeviceInfo() {
        val deviceInfo = DeviceInfo(this)
        deviceInfo.getFields(
            { fields: Array<String>? ->
                if (fields == null) return@getFields
                // fields is the string array that contains info in the requested order
                setCurrentFiscalID(fields[0])
                setCurrentDeviceMode(fields[1])
                setCurrentCardRedirection(fields[2])
                deviceInfo.unbind()
            },
            DeviceInfo.Field.FISCAL_ID,
            DeviceInfo.Field.OPERATION_MODE,
            DeviceInfo.Field.CARD_REDIRECTION
        )
    }
}