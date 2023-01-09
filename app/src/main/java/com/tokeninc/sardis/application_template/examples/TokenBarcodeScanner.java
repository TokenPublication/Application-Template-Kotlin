package com.tokeninc.sardis.application_template.examples;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class TokenBarcodeScanner{

    private WeakReference<? extends AppCompatActivity> mActivity;
    private WeakReference<TokenBarcodeListener> mListener;
    private BroadcastReceiver receiver;
    private boolean isDisplayConfirmDialog = true;

    public TokenBarcodeScanner(WeakReference<? extends AppCompatActivity> activity,TokenBarcodeListener mListener){
        this.mActivity = activity;
        this.mListener = new WeakReference<>(mListener);
        startBarcodeScanner();
    }

    public TokenBarcodeScanner(WeakReference<? extends AppCompatActivity> activity,
                               boolean isDisplayConfirmDialog,TokenBarcodeListener mListener){
        this.mActivity = activity;
        this.mListener = new WeakReference<>(mListener);
        this.isDisplayConfirmDialog = isDisplayConfirmDialog;
        startBarcodeScanner();
    }

    private void startBarcodeScanner(){
        String activityIntent = "com.tokeninc.barcodescanner.REQUEST_BARCODE";
        Intent intent = new Intent(activityIntent);
        Bundle bundle = new Bundle();
        bundle.putBoolean("confirmDialog",isDisplayConfirmDialog);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.hasExtra("barcode") && mListener.get() != null){
                    mListener.get().onBarcodeDataReceived(intent.getStringExtra("barcode"));
                    if(mActivity.get() != null){
                        mActivity.get().unregisterReceiver(receiver);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(activityIntent);
        mActivity.get().registerReceiver(receiver, filter);
        if(mActivity.get() != null){
            mActivity.get().startActivity(intent);
        }
    }
}

