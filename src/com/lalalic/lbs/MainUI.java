package com.lalalic.lbs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;

public class MainUI extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);
		this.bindService(new Intent(this.getApplicationContext(), SmartLocationService.class),
				new LocalServiceConnection(), BIND_AUTO_CREATE);
		final Button button = (Button) findViewById(R.id.btnRefresh);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SmartLocationService service = SmartLocationService.service;
				if (service == null)
					return;
				service.flush();

				if (!service.hasUploadService())
					service.upload();
			}
		});

		final EditText userName = (EditText) findViewById(R.id.user);
		final EditText password = (EditText) findViewById(R.id.password);
		OnFocusChangeListener listener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean focused) {
				EditText editText=(EditText)view;
				if (focused) {
					editText.selectAll();
					return;
				}
				switch (view.getId()) {
				case R.id.user:
					Configuration.getInstance(MainUI.this).set("user",
							editText.getEditableText().toString());
					break;
				case R.id.password:
					Configuration.getInstance(MainUI.this).set("password",
							editText.getEditableText().toString());
					break;
				}
			}
		};
		userName.setOnFocusChangeListener(listener);
		password.setOnFocusChangeListener(listener);
		
		Configuration conf=Configuration.getInstance(this);
		String value=conf.get("user");
		if(value!=null)
			userName.setText(value);
		userName.selectAll();
		
		value=conf.get("password");
		if(value!=null)
			password.setText(value);
	}

}
