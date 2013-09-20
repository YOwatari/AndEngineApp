package com.crossfeel.app;

import java.util.ArrayList;

import org.andengine.ui.activity.SimpleLayoutGameActivity;

import com.crossfeel.app.KeyListenScene;
import com.crossfeel.app.ResourceUtil;

public abstract class MultiSceneActivity extends SimpleLayoutGameActivity {
	
	private ResourceUtil mResourceUtil;
	private ArrayList<KeyListenScene> mSceneArray;

	@Override
	protected void onCreateResources() {
		mResourceUtil = ResourceUtil.getInstance(this);
		mSceneArray = new ArrayList<KeyListenScene>();
	}

	public ResourceUtil getResourceUtil() {
		return mResourceUtil;
	}
	
	public ArrayList<KeyListenScene> getSceneArray() {
		return mSceneArray;
	}
	
	public abstract void appendScene(KeyListenScene scene);
	
	public abstract void backToInitial();
	
	public abstract void refreshRunningScene(KeyListenScene scene);

}
