package com.lalalic.lbs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmartLocationTrigger extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		ctx.startService(new Intent(ctx.getApplicationContext(),SmartLocationService.class));
	}

}
