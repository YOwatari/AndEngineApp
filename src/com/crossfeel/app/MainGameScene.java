package com.crossfeel.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
import android.util.Log;
import android.view.KeyEvent;

/*
 * メインゲーム部分
 * 変数多すぎ。なんとかするべき
 * 動作がよくわかんなくなってきてるので、綺麗にしたい
 */

public class MainGameScene extends KeyListenScene implements
		IOnSceneTouchListener {

	private GlobalsData gData;

	private Text infoText;
	private Rectangle btn;

	private Text chronoMeterText;

	private List<Text> roleText = new ArrayList<Text>();

	private Rectangle[] boxSprite = new Rectangle[6];
	private String[] role = { "占い師", "怪盗", "人狼", "人狼", "村人", "村人", };

	// 12色のパステルカラー
	private String[] colorHex = { "#ff7f7f", "#ff7fbf", "#bf7fff", "#7f7fff",
			"#7fbfff", "#7fffff", "#7fffbf", "#7fffbf", "#7fff7f", "#bfff7f",
			"#ffff7f", "#ffbf7f" };

	private static final int PLAYERS_JOIN = 0;
	private static final int PLAYERS_TURN = 1;
	private static final int TALKING = 2;
	private static final int VOTING = 3;
	private static final int RESULT = 4;
	private int phase = PLAYERS_JOIN;

	private int turnPlayer = 0;
	private boolean VillagersTurn = false;
	private boolean FortuneTellerTurn = false;
	private boolean PhantomThiefTurn = false;

	private int stealPlayerNumber = 7;
	private int PhantomThiefNumber = 7;

	private int chronoCount = 0;
	private boolean waitFlag = false;

	private boolean votingFlag = false;

	private static final int playerturnTime = 5;
	private static final int takingTime = 5 * 60;

	private List<Integer> players = new ArrayList<Integer>();

	private Sound gStart, pLength, ptStart1, ptStart2, nPlayer, resultSE,
			theEnd, talkEnd, talkStart, voteStart1, voteStart2;
	private boolean start = true;
	private boolean pl = false;
	private boolean pt2 = false;
	private boolean vs1 = false;
	private boolean vs2 = false;

	// コンストラクタ
	public MainGameScene(MultiSceneActivity context) {
		super(context);
		gData = (GlobalsData) context.getApplication();
		gData.volume = 5;
		init();
	}

	@Override
	public void init() {
		setRecangleButton();

		// 夜演出：虫の声
		gData.SSClassification = 5;
		gData.SSVolume = 3;
		gData.SSPlayState = 1;

		// テキストを書く準備
		Texture texture = new BitmapTextureAtlas(getBaseActivity()
				.getTextureManager(), 512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Font font = new Font(getBaseActivity().getFontManager(), texture,
				Typeface.DEFAULT_BOLD, 26, true, Color.WHITE);
		getBaseActivity().getTextureManager().loadTexture(texture);
		getBaseActivity().getFontManager().loadFont(font);

		// テキスト表示
		infoText = new Text(0, 0, font, "プレイヤー人数を決定してください", 28,
				new TextOptions(HorizontalAlign.CENTER), getBaseActivity()
						.getVertexBufferObjectManager());
		infoText.setPosition(getBaseActivity().getEngine().getCamera()
				.getWidth()
				/ 2.0f - infoText.getWidth() / 2.0f, getBaseActivity()
				.getEngine().getCamera().getHeight()
				/ 2.0f - infoText.getHeight() / 2.0f);
		attachChild(infoText);

		setBoxies();

		// タッチ受け取るフラグ
		setOnSceneTouchListener(this);

		// アップデートハンドラ
		registerUpdateHandler(updateHandler);
	}

	/**
	 * 箱を配置する
	 */
	public void setBoxies() {
		RoleShuffle(role);
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

			// boxの情報を持つオブジェクトをSpriteに持たせる
			boxSprite[i].setUserData(new BoxInfo(i, role[i]));
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

	/**
	 * 配列の中身入れ替え関数 Roleのシャッフルに使う
	 * 
	 * @param array
	 */
	public <T> void RoleShuffle(T[] array) {
		for (int i = 0; i < array.length; i++) {
			int j = (int) Math.floor(Math.random() * (i + 1));

			T tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}

	public void setRecangleButton() {
		btn = new Rectangle(0, getBaseActivity().getEngine().getCamera()
				.getHeight()
				/ 2.0f
				- getBaseActivity().getEngine().getCamera().getHeight()
				/ 30f, getBaseActivity().getEngine().getCamera().getWidth(),
				getBaseActivity().getEngine().getCamera().getHeight() / 15f,
				getBaseActivity().getVertexBufferObjectManager());
		btn.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		btn.setColor(Color.WHITE);
		btn.setAlpha(0.15f);
		attachChild(btn);
	}

	@Override
	public void prepareSoundAndMusic() {
		try {
			gStart = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "GameStart.wav");
			pLength = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "PlayerLength.wav");
			nPlayer = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "NextPlayer.wav");
			ptStart1 = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(),
					"PlayerTurnStart1.wav");
			ptStart2 = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(),
					"PlayerTurnStart2.wav");
			resultSE = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "Result.wav");
			talkEnd = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "TalkEnd.wav");
			talkStart = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "TalkStart.wav");
			theEnd = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "TheEnd.wav");
			voteStart1 = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "VoteStart1.wav");
			voteStart2 = SoundFactory.createSoundFromAsset(getBaseActivity()
					.getSoundManager(), getBaseActivity(), "VoteStart2.wav");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		return false;
	}

	private Text changeText(Text t, String str) {
		t.setText(str);
		t.setPosition(getBaseActivity().getEngine().getCamera().getWidth()
				/ 2.0f - t.getWidth() / 2.0f, t.getY());
		return t;
	}

	/**
	 * タイマー表示
	 * 
	 * @param count
	 */
	private void SetChronometer(int count) {
		chronoCount = count * 100;
		// テキストを書く準備
		Texture texture = new BitmapTextureAtlas(getBaseActivity()
				.getTextureManager(), 512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Font font = new Font(getBaseActivity().getFontManager(), texture,
				Typeface.DEFAULT_BOLD, 80, true, Color.WHITE);
		getBaseActivity().getTextureManager().loadTexture(texture);
		getBaseActivity().getFontManager().loadFont(font);

		// テキスト表示
		String minutes = "00";
		if (chronoCount / 100 / 60 > 0) {
			minutes = String.format(Locale.getDefault(), "%02d",
					chronoCount / 100 / 60);
		}
		String secounds = String.format(Locale.getDefault(), "%02d",
				(chronoCount - (chronoCount / 100 / 60) * 100 * 60) / 100);
		String centiSec = String.format(Locale.getDefault(), "%02d",
				(chronoCount - (chronoCount / 100 / 60) * 100 * 60) % 100);
		chronoMeterText = new Text(0, 0, font, minutes + ":" + secounds + "'"
				+ centiSec, 80, new TextOptions(HorizontalAlign.CENTER),
				getBaseActivity().getVertexBufferObjectManager());
		chronoMeterText.setPosition(getBaseActivity().getEngine().getCamera()
				.getWidth()
				/ 2.0f - chronoMeterText.getWidth() / 2.0f, getBaseActivity()
				.getEngine().getCamera().getHeight()
				/ 2.0f / 3.0f - chronoMeterText.getHeight() / 2.0f);
		attachChild(chronoMeterText);

		TimerHandler chronometerHandler = new TimerHandler(1.0f / 60.0f, true,
				new ITimerCallback() {

					@Override
					public void onTimePassed(TimerHandler pTimerHandler) {
						chronoCount--;
						String minutes = "00";
						if (chronoCount / 100 / 60 > 0) {
							minutes = String.format(Locale.getDefault(),
									"%02d", chronoCount / 100 / 60);
							if (phase == TALKING) {
								gData.lightLevel = chronoCount / 100 / 60 + 2;
							}
						}
						String secounds = String.format(
								Locale.getDefault(),
								"%02d",
								(chronoCount - (chronoCount / 100 / 60) * 100 * 60) / 100);
						String centiSec = String.format(
								Locale.getDefault(),
								"%02d",
								(chronoCount - (chronoCount / 100 / 60) * 100 * 60) % 100);
						chronoMeterText.setText(minutes + ":" + secounds + "'"
								+ centiSec);

						if (chronoCount == 0) {
							unregisterUpdateHandler(pTimerHandler);
							detachChild(chronoMeterText);
							if (!roleText.isEmpty()) {
								for (Iterator<Text> iterator = roleText
										.iterator(); iterator.hasNext();) {
									Text rt = (Text) iterator.next();
									detachChild(rt);
								}
								roleText.clear();
							}

							switch (phase) {
							case PLAYERS_TURN:
								FortuneTellerTurn = false;
								PhantomThiefTurn = false;
								if (turnPlayer < players.size()) {
									nPlayer.play();
									infoText = changeText(infoText,
											"この色のプレイヤーに渡してください");
									btn.setColor(boxSprite[players
											.get(turnPlayer)].getColor());
									checkNextPlayer();
								} else {
									// 全プレイヤー終了
									// 議論フェーズ初期画面
									registerUpdateHandler(AsaHandler);

									phase = TALKING;
									infoText = changeText(infoText,
											"議論をスタートしますか？");
									btn.setColor(Color.WHITE);
									btn.setAlpha(0.15f);

									turnPlayer = 0;
									for (Iterator<Integer> iterator = players
											.iterator(); iterator.hasNext();) {
										Integer i = (Integer) iterator.next();
										BoxInfo bi = (BoxInfo) boxSprite[i]
												.getUserData();
										bi.playerChecked = false;
									}
								}
								break;
							case TALKING:
								talkEnd.play();
								vs1 = true;
								registerUpdateHandler(halfWaitHandler);
								// TODO: 照明：夕方演出
								// シーン切り替え

								phase = VOTING;
								// 投票ターンの初期画面
								// 照明：ターンを経る毎に暗く
								waitFlag = true;
								infoText = changeText(infoText,
										"この色のプレイヤーへ渡してください");
								btn.setColor(boxSprite[players.get(turnPlayer)]
										.getColor());

								TimerHandler playerToCheckMessage = new TimerHandler(
										2.0f, new ITimerCallback() {

											@Override
											public void onTimePassed(
													TimerHandler pTimerHandler) {
												infoText = changeText(infoText,
														"あなたはこの色のプレイヤーですか？");
												btn.setColor(boxSprite[players
														.get(turnPlayer)]
														.getColor());
												waitFlag = false;
											}
										});
								registerUpdateHandler(playerToCheckMessage);
								break;
							case VOTING:
								break;
							case RESULT:
								break;
							default:
								break;
							}

						}
						pTimerHandler.reset();

					}
				});

		registerUpdateHandler(chronometerHandler);
	}

	private void handToNext() {
		waitFlag = true;
		SetChronometer(playerturnTime);

		if (VillagersTurn) {
			infoText = changeText(infoText, "しばらくお待ちください");
			VillagersTurn = false;
		}
	}

	private void checkNextPlayer() {
		TimerHandler playerToCheckMessage = new TimerHandler(3.0f,
				new ITimerCallback() {
					@Override
					public void onTimePassed(TimerHandler pTimerHandler) {
						infoText = changeText(infoText, "あなたはこの色のプレイヤーですか？");
						waitFlag = false;
					}
				});
		registerUpdateHandler(playerToCheckMessage);
	}

	private void setRoleText(Rectangle bs) {
		BoxInfo bi = (BoxInfo) bs.getUserData();

		// テキストを書く準備
		Texture texture = new BitmapTextureAtlas(getBaseActivity()
				.getTextureManager(), 512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Font font = new Font(getBaseActivity().getFontManager(), texture,
				Typeface.DEFAULT_BOLD, 26, true, Color.WHITE);
		getBaseActivity().getTextureManager().loadTexture(texture);
		getBaseActivity().getFontManager().loadFont(font);

		// テキスト表示
		Text roleText = new Text(0, 0, font, bi.role, 30, new TextOptions(
				HorizontalAlign.CENTER), getBaseActivity()
				.getVertexBufferObjectManager());

		float x = (bs.getX() + bs.getWidth() / 2.0f) - roleText.getWidth()
				/ 2.0f;
		float y = (bs.getY() + bs.getHeight() / 2.0f) - roleText.getHeight()
				/ 2.0f;
		roleText.setPosition(x, y);

		attachChild(roleText);
		this.roleText.add(roleText);
	}

	private void setVoteText(Rectangle bs) {
		BoxInfo bi = (BoxInfo) bs.getUserData();

		// テキストを書く準備
		Texture texture = new BitmapTextureAtlas(getBaseActivity()
				.getTextureManager(), 512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Font font = new Font(getBaseActivity().getFontManager(), texture,
				Typeface.DEFAULT_BOLD, 26, true, Color.WHITE);
		getBaseActivity().getTextureManager().loadTexture(texture);
		getBaseActivity().getFontManager().loadFont(font);

		// テキスト表示
		Text roleText = new Text(0, 0, font, String.valueOf(bi.vote), 30,
				new TextOptions(HorizontalAlign.CENTER), getBaseActivity()
						.getVertexBufferObjectManager());

		float x = (bs.getX() + bs.getWidth() / 2.0f) - roleText.getWidth()
				/ 2.0f;
		float y = bs.getY() - roleText.getHeight();
		if (bi.number > 2) {
			y = bs.getY() + bs.getHeight();
		}
		roleText.setPosition(x, y);

		attachChild(roleText);
		this.roleText.add(roleText);
	}

	// タッチイベントに対する処理
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		float x = pSceneTouchEvent.getX();
		float y = pSceneTouchEvent.getY();

		BoxInfo bi = null;
		boolean touchedInfo = false;

		switch (pSceneTouchEvent.getAction()) {
		// タッチ位置検出
		case TouchEvent.ACTION_DOWN:
			// 何がタッチされたか
			for (int i = 0; i < boxSprite.length; i++) {
				if ((x > boxSprite[i].getX() && x < boxSprite[i].getX()
						+ boxSprite[i].getWidth())
						&& (y > boxSprite[i].getY() && y < boxSprite[i].getY()
								+ boxSprite[i].getHeight())) {
					bi = (BoxInfo) boxSprite[i].getUserData();
					touchedInfo = false;
				} else if ((x > btn.getX() && x < btn.getX() + btn.getWidth())
						&& (y > btn.getY() && y < btn.getY() + btn.getHeight())) {
					bi = null;
					touchedInfo = true;
				}
			}

			switch (phase) {
			case PLAYERS_JOIN:
				if (bi != null) {
					// プレイヤーの実非をスイッチ。合わせて光らせる
					bi.player = !bi.player;
					boxSprite[bi.number].setAlpha(bi.player ? 1.0f : 0.3f);

					// プレイヤーになったら追加。非プレイヤーになったら削除。
					if (bi.player) {
						players.add(bi.number);
					} else {
						if (!players.isEmpty()) {
							players.remove(players.indexOf(bi.number));
						}
					}
				} else if (touchedInfo) {

					if (!players.isEmpty() && players.size() > 3) {
						// playersをソートして次のフェーズへ
						Collections.sort(players);
						phase = PLAYERS_TURN;

						// プレイヤーターンの初期画面
						ptStart1.play();
						pt2 = true;
						registerUpdateHandler(waitHandler);
						waitFlag = true;
						infoText = changeText(infoText, "この色のプレイヤーへ渡してください");
						btn.setColor(boxSprite[players.get(turnPlayer)]
								.getColor());

						TimerHandler playerToCheckMessage = new TimerHandler(
								2.0f, new ITimerCallback() {

									@Override
									public void onTimePassed(
											TimerHandler pTimerHandler) {
										infoText = changeText(infoText,
												"あなたはこの色のプレイヤーですか？");
										btn.setColor(boxSprite[players
												.get(turnPlayer)].getColor());
										waitFlag = false;
									}
								});
						registerUpdateHandler(playerToCheckMessage);
					}
				}
				break;
			case PLAYERS_TURN:
				if (bi != null) {
					if (bi.turn && !bi.playerChecked) {
						infoText = changeText(infoText, "あなたは「" + bi.role
								+ "」です");
						bi.playerChecked = true;
					}
					if (FortuneTellerTurn && bi.role != "占い師") {
						if (bi.player) {
							setRoleText(boxSprite[bi.number]);
						} else {
							for (int i = 0; i < boxSprite.length; i++) {
								BoxInfo tmpbi = (BoxInfo) boxSprite[i]
										.getUserData();
								if (!tmpbi.player) {
									setRoleText(boxSprite[tmpbi.number]);
								}
							}
						}
						FortuneTellerTurn = false;
						infoText = changeText(infoText, "しばらくお待ちください");
					}
					if (PhantomThiefTurn && bi.role != "怪盗" && bi.player) {
						setRoleText(boxSprite[bi.number]);
						stealPlayerNumber = bi.number;
						PhantomThiefTurn = false;
						infoText = changeText(infoText, "しばらくお待ちください");
					}
				} else if (touchedInfo) {
					if (!waitFlag) {
						if (turnPlayer < players.size()) {
							// ターンを経るごとに明るく
							gData.lightLevel = turnPlayer + 2;

							bi = (BoxInfo) boxSprite[players.get(turnPlayer)]
									.getUserData();
							if (bi.playerChecked) {
								if (bi.role == "占い師" && !FortuneTellerTurn) {
									infoText = changeText(infoText,
											"占い先を選んでください");
									FortuneTellerTurn = true;
								} else if (bi.role == "人狼") {
									infoText = changeText(infoText,
											"仲間の人狼を確認してください");
									for (Iterator<Integer> iterator = players
											.iterator(); iterator.hasNext();) {
										Integer p = (Integer) iterator.next();
										BoxInfo tmpbi = (BoxInfo) boxSprite[p]
												.getUserData();
										if (tmpbi.role == "人狼") {
											setRoleText(boxSprite[p]);
										}
									}
								} else if (bi.role == "怪盗") {
									PhantomThiefNumber = bi.number;
									infoText = changeText(infoText,
											"怪盗先を選んでください");
									PhantomThiefTurn = true;
								} else {
									setRoleText(boxSprite[bi.number]);
									VillagersTurn = true;
								}

								// 次のユーザへ渡す
								turnPlayer++;
								handToNext();
							} else {
								if (!bi.turn) {
									infoText = changeText(infoText,
											"あなたの役割を確認してください");
									bi.turn = true;
								}
							}
						}
					}
				}
				break;
			case TALKING:
				talkStart.play();
				infoText = changeText(infoText, "議論中");

				if (stealPlayerNumber < boxSprite.length
						&& PhantomThiefNumber < boxSprite.length) {
					BoxInfo ptbi = (BoxInfo) boxSprite[PhantomThiefNumber]
							.getUserData();
					BoxInfo stealbi = (BoxInfo) boxSprite[stealPlayerNumber]
							.getUserData();
					ptbi.role = stealbi.role;
					stealbi.role = "怪盗";
				}
				SetChronometer(takingTime);

				waitFlag = true;
				// 議論中は操作不可
				// 箱を動かしたりしたら面白そうではある

				// role丸見え(デバッグ)
				// for (int i = 0; i < boxSprite.length; i++) {
				// setRoleText(boxSprite[i]);
				// }
				break;
			case VOTING:
				// ここバグある
				if (bi != null) {
					if (votingFlag && bi.player && !waitFlag
							&& bi.number != players.get(turnPlayer)) {
						bi.vote++;
						votingFlag = false;
						bi.playerChecked = true;

						// 本当は投票確認が欲しい

						if (turnPlayer < players.size()) {
							nPlayer.play();
							waitFlag = true;
							infoText = changeText(infoText, "この色のプレイヤーに渡してください");
							btn.setColor(boxSprite[players.get(turnPlayer)]
									.getColor());
							checkNextPlayer();
						} else {
							phase = RESULT;
							resultSE.play();
							// TODO: 暗めの照明
							infoText = changeText(infoText, "結果発表");
							btn.setColor(Color.WHITE);
							btn.setAlpha(0.15f);
						}

					}
				} else if (touchedInfo) {
					if (!waitFlag) {
						if (turnPlayer < players.size()) {
							bi = (BoxInfo) boxSprite[players.get(turnPlayer)]
									.getUserData();
							if (!bi.playerChecked) {
								infoText = changeText(infoText, "投票先を決定してください");
								votingFlag = true;
								turnPlayer++;
							}
						}
						waitFlag = false;
					}
					Log.d("wa", "flag: " + waitFlag + " turnP: " + turnPlayer);
				}
				break;
			case RESULT:
				// 誰が投票したかの表示が欲しい
				// Restartが欲しい
				if (!waitFlag) {
					for (Iterator<Integer> iterator = players.iterator(); iterator
							.hasNext();) {
						Integer p = (Integer) iterator.next();
						setVoteText(boxSprite[p]);
						setRoleText(boxSprite[p]);
					}
					theEnd.play();
					// TODO: 普通の照明
				} else {

				}
				break;
			default:
				break;
			}

			// 離した時
		case TouchEvent.ACTION_UP:
		case TouchEvent.ACTION_CANCEL:
			if (phase == PLAYERS_JOIN) {
				if (!players.isEmpty() && players.size() > 3) {
					infoText = changeText(infoText, players.size()
							+ "人でプレイしますか？");
				} else {
					infoText = changeText(infoText, "プレイヤー人数を決定してください");
				}
			}

			break;

		default:
			break;
		}

		return false;
	}

	private int c = 2;
	private TimerHandler AsaHandler = new TimerHandler(0.5f, true,
			new ITimerCallback() {
				@Override
				public void onTimePassed(TimerHandler pTimerHandler) {
					if (c < 10) {
						gData.lightLevel = c;
						c++;
					} else {
						unregisterUpdateHandler(AsaHandler);
					}
				}
			});

	private TimerHandler waitHandler = new TimerHandler(3.0f, true,
			new ITimerCallback() {

				@Override
				public void onTimePassed(TimerHandler pTimerHandler) {
					if (pt2) {
						ptStart2.play();
						pt2 = false;
						unregisterUpdateHandler(waitHandler);
					} else {
						unregisterUpdateHandler(waitHandler);
					}
				}
			});

	private TimerHandler halfWaitHandler = new TimerHandler(1.5f, true,
			new ITimerCallback() {

				@Override
				public void onTimePassed(TimerHandler pTimerHandler) {
					if (pl) {
						pLength.play();
						pl = false;
					} else if (vs1) {
						voteStart1.play();
						vs1 = false;
					} else if (vs2) {
						voteStart2.play();
						vs2 = false;
					} else {
						unregisterUpdateHandler(halfWaitHandler);
					}
				}
			});

	private TimerHandler updateHandler = new TimerHandler(1.0f, true,
			new ITimerCallback() {

				@Override
				public void onTimePassed(TimerHandler pTimerHandler) {
					if (start) {
						gStart.play();
						start = false;
						pl = true;
						registerUpdateHandler(halfWaitHandler);
					} else {
						unregisterUpdateHandler(updateHandler);
					}
				}
			});
}
