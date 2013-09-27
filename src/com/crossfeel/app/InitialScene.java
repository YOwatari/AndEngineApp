package com.crossfeel.app;

import java.io.IOException;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.Texture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

import android.graphics.Typeface;
import android.view.KeyEvent;

/*
 * 最初のゲーム画面
 * Sceneで共有する部分は、KeyListenSceneに書いたほうが良い
 */

public class InitialScene extends KeyListenScene implements
		IOnSceneTouchListener {
	private Rectangle[] boxSprite = new Rectangle[6];
	private Text titleText;

	// 12色のパステルカラー
	private static String[] colorHex = { "#ff7f7f", "#ff7fbf", "#bf7fff",
			"#7f7fff", "#7fbfff", "#7fffff", "#7fffbf", "#7fffbf", "#7fff7f",
			"#bfff7f", "#ffff7f", "#ffbf7f" };

	private int lightningBox = 0;

	private GlobalsData gData = (GlobalsData) getBaseActivity()
			.getApplication();

	private Sound titleCall;

	private boolean start = true;

	public InitialScene(MultiSceneActivity context) {
		super(context);
		init();
	}

	@Override
	public void init() {
		// フェードアウトしとく
		gData.fadeoutLight = true;
		gData.light = 9;
		
		gData.volume = 5;
		
		Texture texture = new BitmapTextureAtlas(getBaseActivity()
				.getTextureManager(), 512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Font font = new Font(getBaseActivity().getFontManager(), texture,
				Typeface.DEFAULT_BOLD, 48, true, Color.WHITE);
		getBaseActivity().getTextureManager().loadTexture(texture);
		getBaseActivity().getFontManager().loadFont(font);

		titleText = new Text(0, 0, font, "CrossFeel✕JinRoh", 30,
				new TextOptions(HorizontalAlign.CENTER), getBaseActivity()
						.getVertexBufferObjectManager());
		titleText.setPosition(getBaseActivity().getEngine().getCamera()
				.getWidth()
				/ 2.0f - titleText.getWidth() / 2.0f, getBaseActivity()
				.getEngine().getCamera().getHeight()
				/ 2.0f - titleText.getHeight() / 2.0f);
		attachChild(titleText);

		setBoxies();

		setOnSceneTouchListener(this);

		// 画面範囲を見やすくする(デバッグ)
		// float[] gray = HexToFloat("#666666");
		// getBackground().setColor(gray[0], gray[1], gray[2]);

		registerUpdateHandler(pUpdateHandler);
	}

	/**
	 * 箱を配置する
	 */
	public void setBoxies() {
		for (int i = 0; i < boxSprite.length; i++) {
			float size = getBaseActivity().getEngine().getCamera().getHeight() / 10.0f;
			float margin = size / 2.0f;
			float initX = (getBaseActivity().getEngine().getCamera().getWidth() - (size * 3.0f + margin * 2.0f)) / 2.0f;
			float x = initX + size * (i) + margin * (i);
			float y = getBaseActivity().getEngine().getCamera().getHeight()
					/ 8.0f * 3.0f - size / 2.0f;
			if (i > 2) {
				x = initX + size * (i - 3) + margin * (i - 3);
				y += getBaseActivity().getEngine().getCamera().getHeight() / 4.0f;
			}
			boxSprite[i] = new Rectangle(x, y, size, size, getBaseActivity()
					.getVertexBufferObjectManager());

			boxSprite[i].setBlendFunction(GL10.GL_SRC_ALPHA,
					GL10.GL_ONE_MINUS_SRC_ALPHA);
			float color[] = HexToFloat(colorHex[i]);
			boxSprite[i].setColor(color[0], color[1], color[2], 0.3f);
			attachChild(boxSprite[i]);
		}
	}

	/**
	 * Hex貰ってRGBのfloat配列を返す
	 */
	public float[] HexToFloat(String hexString) {
		float[] color = new float[3];

		if (hexString.length() > 6) {
			hexString = hexString.substring(1, 7);
		}

		color[0] = Integer.parseInt(hexString.substring(0, 2), 16) / 255.0f;
		color[1] = Integer.parseInt(hexString.substring(2, 4), 16) / 255.0f;
		color[2] = Integer.parseInt(hexString.substring(4, 6), 16) / 255.0f;

		return color;
	}

	@Override
	public void prepareSoundAndMusic() {
		try {
			titleCall = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "TitleCall.wav");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		return false;
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		switch (pSceneTouchEvent.getAction()) {
		case TouchEvent.ACTION_DOWN:
			break;
		case TouchEvent.ACTION_UP:
		case TouchEvent.ACTION_CANCEL:
			ResourceUtil.getInstance(getBaseActivity()).resetAllTexture();
			MainGameScene scene = new MainGameScene(getBaseActivity());
			getBaseActivity().getEngine().setScene(scene);
			getBaseActivity().appendScene(scene);
			break;

		default:
			break;
		}

		return false;
	}

	/**
	 * タイマーハンドラ 40Fごとの処理
	 */
	private TimerHandler pUpdateHandler = new TimerHandler(40f/60f, true,
			new ITimerCallback() {

				@Override
				public void onTimePassed(TimerHandler pTimerHandler) {
					if (start) {
						titleCall.play();
						start = false;
					}

					boxSprite[lightningBox].setAlpha(20.0f / 60.0f);

					Random rand = new Random(System.currentTimeMillis());
					int tmp = rand.nextInt(boxSprite.length);
					while (lightningBox == tmp) {
						tmp = rand.nextInt(boxSprite.length);
					}
					lightningBox = tmp;
					boxSprite[lightningBox].setAlpha(1.0f);

				}
			});

}
