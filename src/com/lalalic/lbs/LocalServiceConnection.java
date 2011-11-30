package com.lalalic.lbs;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public final class LocalServiceConnection implements ServiceConnection {

	@Override
	public void onServiceConnected(ComponentName cn, IBinder binder) {
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		
	}
}