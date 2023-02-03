package com.tokeninc.sardis.application_template.helpers.printHelpers

import android.os.IBinder
import android.os.RemoteException
import com.token.printerlib.IPrinterService
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * This class is for printing slips.
 */
class PrintServiceBinding() {
    private var printerService: IPrinterService? = null
    private var runnable: Runnable? = null

    init {
        var method: Method? = null
        try {
            method = Class.forName("android.os.ServiceManager")
                .getMethod("getService", String::class.java)
            val binder = method.invoke(null, "PrinterService") as IBinder
            printerService = IPrinterService.Stub.asInterface(binder)
            executeRunnable()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun executeRunnable() {
        if (printerService != null && runnable != null) {
            synchronized(runnable!!) {
                runnable!!.run()
                runnable = null
            }
        }
    }

    fun print(text: String?) {
        runnable = Runnable {
            try {
                printerService!!.printText(text)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
        executeRunnable()
    }

    fun printBitmap(name: String?, verticalMargin: Int) {
        runnable = Runnable {
            try {
                printerService!!.printBitmap(name, verticalMargin)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
        executeRunnable()
    }
}