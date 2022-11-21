package com.tokeninc.sardis.application_template.helpers.PrintHelpers;
/*
import android.os.IBinder;
import android.os.RemoteException;

import com.example.printertest.IPrinterService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PrintServiceBinding {
    private IPrinterService printerService;
    private Runnable runnable;

    public PrintServiceBinding() {

        Method method = null;
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, "PrinterService");
            if (binder != null) {
                printerService = IPrinterService.Stub.asInterface(binder);
                executeRunnable();
            }
        }
        catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {}

    }

    private synchronized void executeRunnable() {
        if (printerService != null && runnable != null) {
            synchronized (runnable) {
                runnable.run();
                runnable = null;
            }
        }
    }

    public void print(String text) {
        runnable = () -> {
            try {
                printerService.printText(text);
            }
            catch (RemoteException e) {}
        };
        executeRunnable();
    }

    public void printBitmap(String name, int verticalMargin) {
        runnable = () -> {
            try {
                printerService.printBitmap(name, verticalMargin);
            }
            catch (RemoteException e) {}
        };
        executeRunnable();
    }
}

 */
