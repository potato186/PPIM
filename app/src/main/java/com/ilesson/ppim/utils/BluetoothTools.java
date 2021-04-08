package com.ilesson.ppim.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class BluetoothTools {
	@SuppressLint("NewApi")
	public static final int bluetoothState(Context cx) {
		BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
		if (null == ba) {
			return -2;// 错误
		} else if (!ba.isEnabled()) {
			return -1;// 未打开
		} else {
			if (BluetoothProfile.STATE_CONNECTED == ba
					.getProfileConnectionState(BluetoothProfile.A2DP)) {
				return 1;// 仅音箱
			}
			if (BluetoothProfile.STATE_CONNECTED == ba
					.getProfileConnectionState(BluetoothProfile.HEADSET)) {
				return 2;// 音箱+语音
			}
			ConnectivityManager cm = (ConnectivityManager) cx
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm
					.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
			if (netInfo == null) {
				return 0; // 没连接
			}
			return -3;// 其它设备
		}
	}

	public static final boolean isBluetoothConnected(Context cx) {
		return bluetoothState(cx) > 0;
	}
}
