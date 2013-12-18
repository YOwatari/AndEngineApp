package com.crossfeel.app;

import android.os.Handler;
import android.os.Message;

/*
 * ほぼコピペ
 * CFActivityから呼び出す
 * 
 * Thread から Activity へイベント通知【Androidアプリ】
 * http://shimoigi.net/archives/768
 */

public class ExRunnable implements Runnable {

	private GlobalsData gData;

	public static final int LightOnOFF = 1;
	public static final int LightLevel = 2;
	public static final int Volume = 3;
	public static final int SSOnOff = 4;
	public static final int SSMode = 5;
	public static final int SSVolume = 6;

	private Handler handler;

	public ExRunnable(Handler handler, GlobalsData gData) {
		super();
		this.handler = handler;
		this.gData = gData;
	}

	@Override
	public void run() {
		int lightOnOff = gData.light;
		int lightCtl = gData.lightControll;
		int lightLv = gData.lightLevel;

		int scene = gData.sceneMode;

		int mainVolume = gData.volume;

		int ssMode = gData.SSClassification;
		int ssOnOff = gData.SSPlayState;
		int ssVolume = gData.SSVolume;

		while (gData.thread) {
			// ちょっと待つ
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}

			// 各値に変化があったらイベント通知する

			if (lightOnOff != gData.light) {
				_lightOnOff();
			}
			
			if (lightLv != gData.lightLevel) {
				_lightLevel();
			}

			if (mainVolume != gData.volume) {
				_volume();
			}
			
			if (ssMode != gData.SSClassification) {
				_ssMode();
			}
			
			if (ssOnOff != gData.SSPlayState) {
				_ssOnOff();
			}
			
			if (ssVolume != gData.SSVolume) {
				_ssVolume();
			}

		}
	}

	private void _lightOnOff() {
		Message message = new Message();
		message.what = LightOnOFF;
		handler.sendMessage(message);
	}
	
	private void _lightLevel() {
		Message message = new Message();
		message.what = LightLevel;
		handler.sendMessage(message);
	}

	private void _volume() {
		Message message = new Message();
		message.what = Volume;
		handler.sendMessage(message);
	}
	
	private void _ssMode() {
		Message message = new Message();
		message.what = SSMode;
		handler.sendMessage(message);
	}
	
	private void _ssOnOff() {
		Message message = new Message();
		message.what = SSOnOff;
		handler.sendMessage(message);
	}
	
	private void _ssVolume() {
		Message message = new Message();
		message.what = SSVolume;
		handler.sendMessage(message);
	}
}
