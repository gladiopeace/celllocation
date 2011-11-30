package com.lalalic.lbs;

import android.content.Context;
import android.content.SharedPreferences;

class Configuration {
	private static Configuration conf;
	private SharedPreferences properties;

	public static Configuration getInstance(Context ctx) {
		if (conf != null)
			return conf;
		conf = new Configuration(ctx);
		return conf;
	}

	private Configuration(Context ctx) {
		properties = ctx.getSharedPreferences(ctx.getString(R.string.app_name),
				Context.MODE_PRIVATE);
	}

	public void set(String name, String value) {
		if (value.equals(properties.getString(name, null)))
			return;

		SharedPreferences.Editor editor = properties.edit();
		editor.putString(name, value);
		editor.commit();
	}

	public String get(String name) {
		return properties.getString(name, null);
	}
}