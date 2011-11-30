package com.lalalic.lbs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class SmartLocationService extends AService {
	public static SmartLocationService service;
	private static final int BUFFER_SIZE = 1024;

	public class LocalBinder extends Binder {
		SmartLocationService getService() {
			return SmartLocationService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();
	private FileChannel out;
	private ByteBuffer buffer;
	private int counter = 0;
	private String lastLacCID;
	private boolean uploadService = false;

	public boolean hasUploadService() {
		return uploadService;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		notify("Started");
		TelephonyManager tel = (TelephonyManager) this
				.getSystemService(TELEPHONY_SERVICE);

		handleLocation(tel.getCellLocation());

		tel.listen(new PhoneStateListener() {
			@Override
			public void onCellLocationChanged(CellLocation location) {
				handleLocation(location);
			}
		}, PhoneStateListener.LISTEN_CELL_LOCATION);
		service = this;
	}

	@Override
	public void onDestroy() {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			try {
				flush();
				if (out != null && out.isOpen())
					out.close();
			} catch (IOException e) {
				notify(e.getMessage());
			}
		}

		notify("Stopped", true);
		super.onDestroy();
	}

	private void handleLocation(CellLocation location) {
		if (location == null)
			return;
		if (location.toString().equalsIgnoreCase(lastLacCID))
			return;

		lastLacCID = location.toString();

		if (lastLacCID.equalsIgnoreCase("[-1,-1]"))
			return;

		notify(counter + ":" + lastLacCID);
		save(lastLacCID);
	}

	private void save(String location) {
		try {
			if (buffer == null)
				buffer = ByteBuffer.allocate(BUFFER_SIZE);

			buffer.put((",[" + System.currentTimeMillis() + "," + location + "]")
					.getBytes());

			counter++;
		} catch (BufferOverflowException bofEx) {
			try {
				flush();
				save(location);
			} catch (Exception e) {
				notify(e.getMessage());
			}
		}
	}

	void flush() {
		try {
			if (!Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())) {
				notify("External storage not ready");
				return;
			}

			if (out == null || !out.isOpen()) {
				File f = new File(Environment.getExternalStorageDirectory(),
						this.getString(R.string.DB));
				if (!f.exists()) {
					if (!f.getParentFile().exists())
						f.getParentFile().mkdirs();
					f.createNewFile();
				}
				out = new FileOutputStream(f, true).getChannel();
			}
			buffer.flip();
			out.write(buffer);
			buffer.clear();
			out.force(true);
			out.close();
			out = null;

			if (uploadService)
				upload();
		} catch (Exception e) {
			notify(e.getMessage());
		}
	}

	public void startUploadService() {
		this.uploadService = true;
	}

	public void stopUploadService() {
		this.uploadService = false;
	}

	public void upload() {
		new Thread() {
			@Override
			public void run() {
				doUpload();
			}

		}.start();
	}

	private boolean doUpload() {
		File dbFile = new File(Environment.getExternalStorageDirectory(),
				this.getString(R.string.DB));
		if (!dbFile.exists() || dbFile.length() == 0) {
			notify("upload: no content");
			return false;
		}
		final HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 60 * 1000);
		HttpConnectionParams.setSoTimeout(httpParams, 10 * 60 * 1000);
		HttpClient httpclient = new DefaultHttpClient(httpParams);
		httpclient.getParams().setParameter(
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

		try {
			HttpPost httppost = new HttpPost(
					this.getString(R.string.Service_URL));

			MultipartEntity mpEntity = new MultipartEntity();
			ContentBody cbFile = new FileBody(dbFile,
					"text/plain; charset=\"UTF-8\"");
			mpEntity.addPart("trackupload", cbFile);
			httppost.setEntity(mpEntity);
			httppost.addHeader(
					"user",
					Configuration.getInstance(SmartLocationService.this).get(
							"user"));
			httppost.addHeader(
					"password",
					Configuration.getInstance(SmartLocationService.this).get(
							"password"));

			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			boolean uploaded = false;
			if (entity != null)
				uploaded = EntityUtils.toString(entity)
						.equalsIgnoreCase("true");

			if (uploaded) {
				dbFile.delete();
				notify("uploaded");
			}
			return uploaded;

		} catch (Exception e) {
			notify("error:" + e.getMessage());
			return false;
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
	}
}
