package com.crossfeel.app;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class SocketIOService extends Service implements IOCallback {

	private static SocketIO socket;

	class ServiceBinder extends Binder {
		SocketIOService getService() {
			return SocketIOService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// ソケットが無かったら作る
		if (socket == null) {
			try {
				socket = new SocketIO(SocketIOConfig.URL);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		// 接続してなかったら接続する
		if (!socket.isConnected()){
			try {
				socket.connect(this);
			} catch (Exception e) {
				Log.e(SocketIOConfig.TAG, e.getMessage(), e);
			}
		}
		
		return START_STICKY_COMPATIBILITY;
	}
	
	@Override
	public void onDestroy() {
		socket.disconnect();
		super.onDestroy();
	}
	
	@Override
	public void on(String event, IOAcknowledge ack, Object... args) {
		if (SocketIOConfig.DEBUG) {
			Log.d(SocketIOConfig.TAG, "SocketIOServerService.on(" + event + ", " + (String)args[0] + ")");
		}
		Intent intent = new Intent();
		intent.putExtra("event", event);
		intent.putExtra("message",(String)args[0]);
		intent.setAction("socket messaging message intent");
		sendBroadcast(intent);
	}

	@Override
	public void onConnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnect() {
		if (SocketIOConfig.DEBUG) {
			Log.d(SocketIOConfig.TAG, "SocketIOServerService.onDisconnect()");
		}
	}

	@Override
	public void onError(SocketIOException error) {
		if (SocketIOConfig.DEBUG) {
			Log.d(SocketIOConfig.TAG, error.getMessage(), error);
		}
		Intent intent = new Intent();
		intent.setAction("socket messaging error intent");
		sendBroadcast(intent);
	}

	@Override
	public void onMessage(String data, IOAcknowledge ack) {
		if (SocketIOConfig.DEBUG) {
			Log.d(SocketIOConfig.TAG, "SocketIOServerService.onMessage(" + data + ")");
		}

	}

	@Override
	public void onMessage(JSONObject json, IOAcknowledge ack) {
		if (SocketIOConfig.DEBUG) {
			try {
				Log.d(SocketIOConfig.TAG, "SocketIOServerService.onMessage(" + json.toString(2) + ")");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ServiceBinder();
	}
	
	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
	
	protected void send(String event, String message) {
		socket.emit(event, message);
	}
	
	protected void send(String event, JSONObject json) {
		socket.emit(event, json.toString());
	}
}
