package com.tokeninc.sardis.application_template

import android.app.Application
import com.tokeninc.deviceinfo.DeviceInfo
import dagger.hilt.android.HiltAndroidApp

//Hilt needs to know application, Application should be annotated with @HiltAndroidApp
@HiltAndroidApp
class AppTemp: Application() {
    private var currentDeviceMode = DeviceInfo.PosModeEnum.VUK507.name
    private var currentFiscalID: String? = null
    private var currentCardRedirection = DeviceInfo.CardRedirect.NOT_ASSIGNED.name

    fun getCurrentDeviceMode(): String {
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

    fun getCurrentCardRedirection(): String {
        return currentCardRedirection
    }

    fun setCurrentCardRedirection(currentCardRedirection: String) {
        this.currentCardRedirection = currentCardRedirection
    }
}
