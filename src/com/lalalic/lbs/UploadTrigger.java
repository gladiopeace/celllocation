package com.lalalic.lbs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class UploadTrigger extends BroadcastReceiver{
	@Override
	public void onReceive(Context ctx, Intent intent) {
		NetworkInfo info = intent
				.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		switch (info.getState()) {
		case CONNECTED:
			if(SmartLocationService.service!=null)
				SmartLocationService.service.startUploadService();
			break;
		case CONNECTING:
			break;
		default:
			if(SmartLocationService.service!=null)
				SmartLocationService.service.stopUploadService();
		}
	}

}
