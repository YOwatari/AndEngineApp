package com.crossfeel.app;

import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;

public class AndEngineActivity extends MultiSceneActivity {
	
	// �ｽ�ｽﾊサ�ｽC�ｽY
	private int CAMERA_WIDTH = 480;
	private int CAMERA_HEIGHT = 800;
	
	public EngineOptions onCreateEngineOptions() {
		// �ｽ`�ｽ�ｽﾍ囲イ�ｽ�ｽ�ｽX�ｽ^�ｽ�ｽ�ｽX�ｽ�ｽ
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		// �ｽQ�ｽ[�ｽ�ｽ�ｽG�ｽ�ｽ�ｽW�ｽ�ｽ�ｽ�ｽ�ｽ�ｽ
		EngineOptions eo = new EngineOptions(
				true,
				ScreenOrientation.PORTRAIT_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
				camera
				);
		
		// �ｽ�ｽ�ｽ�ｽ�ｽ�ｽ�ｽ�ｽ
		eo.getAudioOptions().setNeedsSound(true);
		
		return eo;
	}
	
	// Scene�ｽ�ｬ
	@Override
	protected Scene onCreateScene() {
		SoundFactory.setAssetBasePath("mfx/");
		return new InitialScene(this);
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
