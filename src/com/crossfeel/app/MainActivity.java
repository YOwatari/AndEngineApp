package com.crossfeel.app;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;

public class MainActivity extends MultiSceneActivity {
	
	// 画面サイズ
	private static final int CAMERA_WIDTH = 480;
	private static final int CAMERA_HEIGHT = 800;
	
	public EngineOptions onCreateEngineOptions() {
		// 描画範囲インスタンス化
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		// ゲームエンジン初期化
		EngineOptions eo = new EngineOptions(
				true,
				ScreenOrientation.PORTRAIT_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
				camera
				);
		
		return eo;
	}
	
	// Scene作成
	@Override
	protected Scene onCreateScene() {
		//MainScene mainScene = new MainScene(this);
		//return mainScene;
		InitialScene initialScene = new InitialScene(this);
		return initialScene;
	}
	
	@Override
	protected int getLayoutID() {
		return R.layout.activity_main;
	}
	
	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.renderview;
	}
	
	@Override
	public void appendScene(KeyListenScene scene) {
		
	}
	
	@Override
	public void backToInitial() {
		
	}
	
	@Override
	public void refreshRunningScene(KeyListenScene scene) {
		
	}

}
