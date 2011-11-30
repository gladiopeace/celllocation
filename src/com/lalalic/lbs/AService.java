package com.lalalic.lbs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

public abstract class AService extends Service {

	public AService() {
		super();
	}
	protected void notify(String message) {
		notify(message, false);
	}
	protected void notify(String message, boolean stop) {
		String title=this.getString(R.string.notification_title);
		
		int icon = R.drawable.icon;
		String tickerText = this.getString(R.string.app_name);
		Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());
	
		Intent notificationIntent = stop ? new Intent() : new Intent(Intent.ACTION_PICK_ACTIVITY,null,this.getApplicationContext(),MainUI.class);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0,
				notificationIntent, 0);
		
		notification.setLatestEventInfo(this, title, message, contentIntent);
	
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(ns);
		mNotificationManager.notify(R.drawable.icon, notification);
	}

}