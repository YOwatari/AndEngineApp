package com.crossfeel.app;

import neclighting.btconnect.CommCommand;
import neclighting.btconnect.SPPAbstractActivity;
import neclighting.btconnect.SPPController;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

/*
 * 所感
 * Viewを切り捨てたい
 */

public class CFActivity extends SPPAbstractActivity implements Handler.Callback {

	// 共通データ(アプリケーション)
	private GlobalsData gData;

	// コントローラー
	private SPPController mControll;

	private Context mCtx;

	// view
	private TextView tView;

	// ディスプレイサイズ取得用
	private Display disp;
	private int dWidth;
	private int dHeight;
	
	// スレッド関連
	private ExRunnable runnable;
	private Thread thread;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ディスプレイサイズ取得(古いやり方：非推奨)
		disp = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		dWidth = disp.getWidth();
		dHeight = disp.getHeight();

		gData = (GlobalsData) this.getApplication();

		mCtx = this;
		mControll = new SPPController();

		// 表示
		tView = new TextView(this);
		tView.setText("CF Activity start");
		setContentView(tView);
		
		// CF使用有無ダイアログ
		// CF無しならスレッドの起動は不要

		// BluetoothをONにする
		showBluetoothCheckDialog();

		AlertDialog dialog = new AlertDialog.Builder(mCtx)
				.setMessage(
						mCtx.getResources().getString(
								R.string.error_BtDisconnected))
				.setPositiveButton(
						mCtx.getResources().getString(R.string.dialog_YES),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent i = new Intent(
										android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
								mCtx.startActivity(i);
							}
						})
				.setNegativeButton(
						mCtx.getResources().getString(R.string.dialog_NO), null)
				.setInverseBackgroundForced(true).setCancelable(false).create();
		
		// 接続準備
		mControll.connect(mCtx, super.mSPPStatus, dialog);

		// スレッド起動
		handler = new Handler(this);
		runnable = new ExRunnable(handler, gData);
		thread = new Thread(runnable);
		thread.start();
	}

	@Override
	protected void onStart() {
		super.onStart();

		// 接続確認
		if (mControll.isConnect()) {
			mControll.notifyStart();

			// 照明・スピーカーOn
			mControll.powerOn();

			// 初期設定
			mControll.lightOn();
			mControll.soundScapeOff();

			// 色々OFF
			mControll.toneOff();
			mControll.sleepTimerOff();
			mControll.wakeupTimerOff();
		}

		// 初回設定
		if (mControll.isFirstSettingFinish()) {
			// 自動接続OFF・BTスタンバイON
			mControll.firstSetting(false, true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		// 照明・スピーカーオフ
		//mControll.powerOff();
		
		// スレッドが停止するようにしとく
		gData.thread = false;
		
		// 切断処理を行う
		mControll.notifyEnd();
		mControll.disConnect(mCtx);
		super.onDestroy();
	}

	/*
	 * Bluetoothが有効か判定
	 */
	private void showBluetoothCheckDialog() {
		// BluetoothON/OFFチェック
		BluetoothAdapter BtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!BtAdapter.isEnabled()) {

			new AlertDialog.Builder(this)
					.setMessage(
							getResources().getString(
									R.string.comment_BluetoothOFF))
					.setPositiveButton(
							getResources().getString(R.string.dialog_YES),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									bluetoothOn();
								}

							})
					.setNegativeButton(
							getResources().getString(R.string.dialog_END),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									setResult(99);
									finish();
								}
							}).setCancelable(false)
					.setInverseBackgroundForced(true).show();
		}

	}

	/*
	 * Bluetooothを有効にする
	 */
	private void bluetoothOn() {

		// Bluetoothオン
		BluetoothAdapter BtAdapter = BluetoothAdapter.getDefaultAdapter();
		BtAdapter.enable();
		if (BtAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			enableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
					0);
			startActivity(enableIntent);
		}
	}

	// 以下、返信

	/**
	 * BT接続 通知
	 */
	@Override
	protected void receiveBTAccept() {
		mControll.notifyStart();
	}

	/**
	 * BT切断 通知
	 */
	@Override
	protected void receiveBTDisconnect() {
		//tView.setText("Bluetooth切断");
	}

	/**
	 * アプリ起動 返信 notifyStart() 完了後
	 */
	@Override
	public void receiveResStart() {
		// 機器の状態(SPPStatus)を画面に反映させる

		// 照明の状態
		gData.light = mSPPStatus.getLightingState();
		gData.lightControll = mSPPStatus.getLightControl();
		gData.lightLevel = mSPPStatus.getLightLevel();

		// シーンモード
		gData.sceneMode = mSPPStatus.getSceneMode();

		// 主音量
		gData.volume = mSPPStatus.getVolume();

		// 環境音
		gData.SSClassification = mSPPStatus.getSSClassification();
		gData.SSPlayState = mSPPStatus.getSSPlayState();
		gData.SSVolume = mSPPStatus.getSSVolume();

		tViewUpdate();
	}

	/**
	 * アプリ終了 返信 notifyEnd() 完了後
	 */
	@Override
	public void receiveResEnd() {
		if (super.mSPPStatus.getRecieveResult() != 0) {
			tView.setText("終了コマンドが失敗しました。コード："
					+ super.mSPPStatus.getRecieveResult());
		} else {
			tView.setText("終了コマンドが成功しました");
		}
	}

	/**
	 * 初回設定完了 返信
	 * 
	 */
	@Override
	public void receiveResSetup() {
		if (super.mSPPStatus.getRecieveResult() != 0) {
			tView.setText("初回設定コマンドが失敗しました。コード："
					+ super.mSPPStatus.getRecieveResult());
		} else {
			tView.setText("初回設定コマンドが成功しました");
		}
	}

	/**
	 * 主音量 返信
	 */
	@Override
	public void receiveResVolume() {
		tView.setText("CF Volume: " + String.valueOf(mSPPStatus.getVolume()));
	}

	/**
	 * 環境音量 返信
	 */
	@Override
	public void receiveResSSVolume() {
		tView.setText("receiveResSSVolume()");
	}

	/**
	 * シーンモード 返信
	 */
	@Override
	public void receiveResScenemode() {
		tView.setText("receiveResScenemode()");
	}

	/**
	 * 環境音 返信
	 */
	@Override
	public void receiveResSoundscape() {
		tView.setText("receiveResSoundscape()");
	}

	/**
	 * 音色 返信
	 */
	@Override
	public void receiveResTone() {
		tView.setText("receiveResTone()");
	}

	/**
	 * 電源OFF 返信
	 */
	@Override
	public void receiveResPoweroff() {
		tView.setText("receiveResPoweroff()");
	}

	/**
	 * 電源ON 返信
	 */
	@Override
	public void receiveResPoweron() {
		tView.setText("receiveResPoweron()");
	}

	/**
	 * 照明 返信
	 */
	@Override
	public void receiveResLightControl() {
		tView.setText("receiveResLightControl");
	}

	/**
	 * 調光レベル 返信
	 */
	@Override
	public void receiveResDimmer() {
		
		// 最速フェードイン処理
		if (gData.fadeinLight) {
			// level=10の時は音が鳴るから使わない
			if (mSPPStatus.getLightLevel() < 9) {
				mControll.changeLightLevel(mSPPStatus.getLightLevel() + 1);
			} else {
				gData.fadeinLight = false;
			}
		}
		
		// 最速フェードアウト処理
		if (gData.fadeoutLight) {
			// level=1の時は音が鳴るから使わない
			if (mSPPStatus.getLightLevel() > 1) {
				mControll.changeLightLevel(mSPPStatus.getLightLevel() - 1);
			} else {
				gData.fadeoutLight = false;
			}
		}
		
	}

	/**
	 * BTスタンバイ 返信 bluetoothStandbyOn() bluetoothStandbyOff()
	 */
	@Override
	public void receiveResBTStandby() {
		if (super.mSPPStatus.getRecieveResult() != 0) {
			tView.setText("BTスタンバイコマンドが失敗しました。コード："
					+ super.mSPPStatus.getRecieveResult());
		} else {
			tView.setText("BTスタンバイコマンドが成功しました");
		}
	}

	/**
	 * 自動接続 返信 autoConnectOn() autoConnectOff()
	 */
	@Override
	public void receiveResAutoConnect() {
		if (super.mSPPStatus.getRecieveResult() != 0) {
			tView.setText("自動接続コマンドが失敗しました。コード："
					+ super.mSPPStatus.getRecieveResult());
		} else {
			tView.setText("自動接続コマンドが成功しました");
		}
	}

	/**
	 * おはようタイマー 返信
	 */
	@Override
	public void receiveResWakeupTimer() {
		tView.setText("receiveResWakeupTimer()");
	}

	/**
	 * おやすみタイマー 返信 sleepTimerOn() 完了後 sleepTimerOff() 完了後
	 */
	@Override
	public void receiveResSleepTimer() {
		if (super.mSPPStatus.getRecieveResult() != 0) {
			tView.setText("おやすみタイマーコマンドが失敗しました。コード："
					+ super.mSPPStatus.getRecieveResult());
		} else {
			tView.setText("おやすみタイマーコマンドが成功しました");
		}

		// おやすみタイマーがONになったらライト設定を行う
		if (mSPPStatus.getSleepTimer() != CommCommand.PCDT_SLEEP_TIMER_OFF) {
			// Spinner spinner11 = (Spinner) findViewById(R.id.spinner11);
			// mControll.changeSleepTimerLight(Integer.parseInt((String)spinner11.getSelectedItem()));
		}
	}

	/**
	 * おやすみタイマー 返信
	 */
	@Override
	public void receiveResSleepLight() {
		tView.setText("receiveResSleepLight()");
	}

	/**
	 * 未定義 返信
	 */
	@Override
	public void receiveResUnknown() {
		tView.setText("receiveResUnknown()");
	}

	// ---------------------------------------
	// 機器からの連絡

	/**
	 * 主音量変更 通知（機器からの連絡）
	 */
	@Override
	protected void receiveNtVolumeChange() {
		tView.setText("receiveNtVolumeChange()");
	}

	/**
	 * 環境音量変更 通知（機器からの連絡）
	 */
	@Override
	protected void receiveNtSSVolumeChange() {
		tView.setText("receiveNtSSVolumeChange()");
	}

	/**
	 * 環境音変更 通知（機器からの連絡）
	 */
	@Override
	protected void receiveNtSoundscapeChange() {
		tView.setText("receiveNtSoundscapeChange()");
	}

	/**
	 * 音色変更 通知（機器からの連絡）
	 */
	@Override
	protected void receiveNtToneChange() {
		tView.setText("receiveNtToneChange()");
	}

	/**
	 * 照明SPパワー状態 通知（機器からの連絡）
	 */
	@Override
	protected void receiveNtPowerChange() {
		int powerState = mSPPStatus.getPowerState();
		if (powerState == CommCommand.PCDT_POWER_ON) {
			mControll.notifyStart();
		}
		tView.setText("receiveNtPowerChange()");
	}

	/**
	 * 照明点灯状態通知（機器からの連絡）
	 */
	@Override
	protected void receiveNtLightStatusChange() {
		tView.setText("receiveNtLightStatusChange()");
	}

	/**
	 * おはようタイマー再生/停止要求　通知（機器からの連絡）
	 */
	@Override
	protected void receiveNtWakeupStatusChange() {
		tView.setText("receiveNtWakeupStatusChange()");
	}

	/**
	 * おやすみタイマー残量変化 通知（機器からの連絡）
	 */
	@Override
	protected void receiveNtSleepRemainChange() {
		tView.setText("receiveNtSleepRemainChange()");
	}

	/**
	 * おはようタイマー再生/停止要求　通知（機器からの連絡）
	 */
	@Override
	protected void receiveNtWakeupControl() {
		tView.setText("receiveNtWakeupControl()");
	}

	/**
	 * おやすみタイマー再生/停止要求(再生は使用予定なし) 通知（機器からの連絡）
	 */
	@Override
	protected void receiveNtSleepControl() {
		tView.setText("receiveNtSleepControl()");
	}

	/**
	 * 本体エラー情報 通知（機器からの連絡）
	 */
	@Override
	protected void receiveNtErrorStatus() {
		tView.setText("receiveNtErrorStatus()");
	}

	/**
	 * Contoller起動中確認 通知（機器からの連絡）
	 */
	@Override
	protected void receiveNtControllerResponse() {
		tView.setText("receiveNtControllerResponse()");
	}

	/**
	 * エラー時の処理を行う（resultが0以外)
	 */
	@Override
	protected void receiveError() {
		tView.setText("receiveError()");
	}

	// タッチイベント
	// できれば、ダイアログ出してゲーム画面に移るようにしたい
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_UP:
			if ((event.getX() > 0 && event.getX() < dWidth)
					&& (event.getY() > 0 && event.getY() < dHeight / 2.0f)) {
				if (mControll.isConnect()) {
					mControll.notifyStart();
					mControll.powerOn();
				}
			} else if ((event.getX() > 0 && event.getX() < dWidth)
					&& (event.getY() > dHeight / 2.0f && event.getY() < dHeight)) {
				Intent intent = new Intent(this, AndEngineActivity.class);
				startActivity(intent);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_CANCEL:
			break;
		}

		return super.onTouchEvent(event);
	}

	// テキスト更新
	private void tViewUpdate() {
		String tmpString = "CFStatus" + "\nConnected: "
				+ String.valueOf(mControll.isConnect()) + "\nFirstSetting: "
				+ String.valueOf(mControll.isFirstSettingFinish())
				+ "\nLight: " + String.valueOf(gData.light)
				+ "\nLightControll: " + String.valueOf(gData.lightControll)
				+ "\nLightLevel: " + String.valueOf(gData.lightLevel)
				+ "\nSceneMode: " + String.valueOf(gData.sceneMode)
				+ "\nVolume: " + String.valueOf(gData.volume)
				+ "\nSSClassification: "
				+ String.valueOf(gData.SSClassification) + "\nSSPlayState: "
				+ String.valueOf(gData.SSPlayState) + "\nSSVolume: "
				+ String.valueOf(gData.SSVolume);
		tView.setText(tmpString);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case ExRunnable.LightOnOFF:
			if (gData.light == 1) {
				mControll.lightOn();
			} else {
				mControll.lightOff();
			}
			return true;
		case ExRunnable.LightLevel:
			mControll.changeLightLevel(gData.lightLevel);
			return true;
		case ExRunnable.Volume:
			mControll.changeMainVolume(gData.volume);
			return true;
		case ExRunnable.SSOnOff:
			if (gData.SSPlayState == 1) {
				mControll.soundScapeOn(gData.SSVolume);
			} else {
				mControll.soundScapeOff();
			}
			return true;
		case ExRunnable.SSMode:
			mControll.changeSoundScape(gData.SSClassification);
			return true;
		case ExRunnable.SSVolume:
			mControll.changeSoundScapeVolume(gData.SSVolume);
			return true;

		default:
			thread = null;
			return false;
		}
	}
}