package com.crossfeel.app;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;

import android.view.KeyEvent;

public abstract class KeyListenScene extends Scene {
	
	private MultiSceneActivity baseActivity;
	
	// コンストラクタ
	public KeyListenScene(MultiSceneActivity baseActivity) {
		setTouchAreaBindingOnActionDownEnabled(true);
		this.baseActivity = baseActivity;
		prepareSoundAndMusic();
	}
	
	public MultiSceneActivity getBaseActivity() {
		return baseActivity;
	}
	
	// 初期化
	public abstract void init();
	// サウンド準備
	public abstract void prepareSoundAndMusic();
	// KeyEventのリスナー
	public abstract boolean dispatchKeyEvent(KeyEvent e);
	
	// Spriteのx座標を画面中央に設定
	public Sprite placeToCenterX(Sprite sp, float y) {
		sp.setPosition(
				baseActivity.getEngine().getCamera().getWidth()/2.0f - sp.getWidth()/2.0f,
				y
				);
		return sp;
	}
	
	// Spriteのy座標を画面中央に設定
	public Sprite placeToCenterY(Sprite sp, float x) {
		sp.setPosition(
				x,
				baseActivity.getEngine().getCamera().getHeight()/2.0f - sp.getHeight()/2.0f
				);
		return sp;
	}
	
	public Sprite placeToCenter(Sprite sp) {
		sp.setPosition(
				baseActivity.getEngine().getCamera().getWidth()/2.0f - sp.getWidth()/2.0f,
				baseActivity.getEngine().getCamera().getHeight()/2.0f - sp.getHeight()/2.0f
				);
		return sp;
	}

}
