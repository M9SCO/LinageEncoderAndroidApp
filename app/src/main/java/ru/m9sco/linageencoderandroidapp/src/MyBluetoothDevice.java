package ru.m9sco.linageencoderandroidapp.src;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

public class MyBluetoothDevice{
    public BluetoothDevice mDevice;
    public MyBluetoothDevice(BluetoothDevice device) {
        mDevice = device;
    }

    @SuppressLint("MissingPermission")
    public String toString() {
        if (mDevice != null) {
            return mDevice.getName();
        }
        // fallback name
        return "";
    }
}
