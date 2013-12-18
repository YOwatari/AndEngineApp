package com.crossfeel.app;

import android.app.Application;

/*
 * アプリ全体からアクセスできるオブジェクト
 * たぶん、getterとかsetterとか作ったほうがいい
 */

public class GlobalsData extends Application {
	// 照明
	public int light;
	public int lightControll;
	public int lightLevel;
	
	// シーン
	public int sceneMode;
	
	// 主音量
	public int volume;
	
	// 環境音
	public int SSClassification;
	public int SSPlayState;
	public int SSVolume;
	
	public boolean fadeinLight;
	public boolean fadeoutLight;
	
	public boolean thread;
	
	public GlobalsData() {
		// 全て初期値に設定
		
		light = 0;
		lightControll = 0;
		lightLevel = 1;
		
		sceneMode = 1;
		
		volume = 1;
		
		SSClassification = 1;
		SSPlayState = 0;
		SSVolume = 1;
		
		fadeinLight = false;
		fadeoutLight = false;
		
		thread = true;
	}
	

	@Override
	public void onCreate() {
		super.onCreate();
	}
}
