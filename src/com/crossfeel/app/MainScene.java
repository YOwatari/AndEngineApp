package com.crossfeel.app;

import android.view.KeyEvent;

public class MainScene extends KeyListenScene {

	public MainScene(MultiSceneActivity baseActivity) {
		super(baseActivity);
		init();
	}

	// 初期化
	public void init() {
	}

	@Override
	public void prepareSoundAndMusic() {

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		return false;
	}

}
