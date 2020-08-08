package com.hichip.hichip.activity.WallMounted;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import com.hichip.R;
import com.hichip.base.HiLog;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.callback.ICameraPlayStateCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.content.HiChipDefines.ABSOLUTE_LIGHT_TYPE;
import com.hichip.content.HiChipDefines.HI_P2P_WHITE_LIGHT_INFO;
import com.hichip.content.HiChipDefines.HI_P2P_WHITE_LIGHT_INFO_EXT;
import com.hichip.control.HiCamera;
import com.hichip.control.HiGLMonitor;
import com.hichip.tools.Packet;
import com.hichip.thecamhi.activity.LiveViewActivity;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.MyLiveViewGLMonitor;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.utils.SharePreUtils;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupWindow.OnDismissListener;

public class WallMountedActivity extends HiActivity
		implements ICameraPlayStateCallback, ICameraIOSessionCallback, OnTouchListener, OnClickListener {

	private String uid;
	private MyCamera mCamera;
	private MyLiveViewGLMonitor mMonitor;
	private ImageView iv_loading2;
	public boolean mIsCruise = false;
	public LinearLayout ll_top, ll_bottom;
	private ImageView btn_return;
	private ImageView btn_live_listen, btn_microphone, btn_live_snapshot, btn_live_record;
	private final static int RECORDING_STATUS_NONE = 0;
	private final static int RECORDING_STATUS_LOADING = 1;
	private final static int RECORDING_STATUS_ING = 2;
	private int mRecordingState = RECORDING_STATUS_NONE;
	private Timer timer;
	private TimerTask timerTask;
	protected String recordFile;
	private boolean isListening = false;
	private long oldClickTime;
	private boolean isTalking = false;
	private TextView txt_recording;
	private ImageView mIvRecording, iv_cruise;
	private int video_width;
	private int video_height;
	// private LinearLayout rl_wall_mounted_guide;
	private TextView tv_timezone;
	public ImageView iv_white_light;
	public int lightModel;
	private LinearLayout ll_white_light;

	private Handler monitorHandler = new Handler();
	private List<Toast> toastList1 =new ArrayList<>();
	private List<Toast> toastList2 =new ArrayList<>();
	private List<Toast> toastList3 =new ArrayList<>();
	private boolean isSaveSnapshot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wall_mounted);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getIntentData();
		// 白光灯
		getLightModel();
		HiTools.cameraWhetherNull(this, mCamera);
		initView();
		setListeners();

	}

	private void getLightModel() {
		boolean b = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_ABSOLUTE_LIGHT_TYPE);
		if (b) {
			lightModel = 3;
			mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_ABSOLUTE_LIGHT_TYPE, null);
			return;
		}
		b = mCamera.getCommandFunction(HiChipDefines.HI_P2P_WHITE_LIGHT_GET_EXT);
		if (b) {
			lightModel = 1;
			mCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_GET_EXT, null);
			return;
		}
		b = mCamera.getCommandFunction(HiChipDefines.HI_P2P_WHITE_LIGHT_GET);
		if (b) {
			lightModel = 2;
			mCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_GET, null);
		}
	}

	private void setListeners() {
		mMonitor.setOnTouchListener(this);
		btn_return.setOnClickListener(this);
		btn_live_listen.setOnClickListener(this);
		btn_live_snapshot.setOnClickListener(this);
		btn_live_record.setOnClickListener(this);
		iv_cruise.setOnClickListener(this);
		iv_white_light.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		showLoadingView();
		mCamera.registerIOSessionListener(this);
		mCamera.registerPlayStateListener(this);
		if (mCamera != null) {
			new Thread() {
				public void run() {
					mCamera.startLiveShow(mCamera.getVideoQuality(), mMonitor);
				};
			}.start();

		}
	}

	@Override
	protected void onPause() {
		cancelToast(toastList1);
		cancelToast(toastList2);
		cancelToast(toastList3);
		super.onPause();
		mVoiceIsTran = false;
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
		if (mCamera != null) {
			mCamera.stopLiveShow();
			mCamera.unregisterIOSessionListener(this);
			mCamera.unregisterPlayStateListener(this);
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		finish();
	}
	
	private void cancelToast(List<Toast> list) {
		if (list.size()>0) {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i)!=null) {
					list.get(i).cancel();
				}
			}
		}	
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}
		if (handler != null) {
			handler.removeCallbacksAndMessages(null);
		}
		if (monitorHandler != null) {
			monitorHandler.removeCallbacksAndMessages(null);
		}
	}

	@Override
	public void onBackPressed() {
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		finish();

	}

	private void initView() {
		mMonitor = (MyLiveViewGLMonitor) findViewById(R.id.monitor_wall_mounted);
		mCamera.setVideoQuality(0);
		float cirx = SharePreUtils.getFloat("chche", this, mCamera.getUid() + "xcircle");
		float ciry = SharePreUtils.getFloat("chche", this, mCamera.getUid() + "ycircle");
		float cirr = SharePreUtils.getFloat("chche", this, mCamera.getUid() + "rcircle");
		mMonitor.SetCirInfo(cirx, ciry, cirr);

		// mMonitor.SetViewType_EXT(mMonitor.TOP_ALL_VIEW, 2);
		mMonitor.SetViewType_EXT(mMonitor.TOP_ALL_VIEW, mCamera.getFishModType());
		mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_SIDE, 1);
		// setScreenSize 竖屏的时候用宽宽 横屏的时候用宽高
		mMonitor.SetScreenSize(getWindowManager().getDefaultDisplay().getWidth(),
				getWindowManager().getDefaultDisplay().getHeight());
		mCamera.setLiveShowMonitor(mMonitor);
		mMonitor.setCamera(mCamera);

		iv_white_light = (ImageView) findViewById(R.id.iv_white_light);
		ll_white_light = (LinearLayout) findViewById(R.id.ll_white_light);
		ll_white_light.setVisibility(lightModel == 0 ? View.GONE : View.VISIBLE);
		iv_loading2 = (ImageView) findViewById(R.id.iv_loading2);
		ll_top = (LinearLayout) findViewById(R.id.ll_top);
		ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);
		btn_return = (ImageView) findViewById(R.id.btn_return);
		btn_live_listen = (ImageView) findViewById(R.id.btn_live_listen);

		btn_microphone = (ImageView) findViewById(R.id.btn_microphone);
		btn_microphone.setOnTouchListener(this);
		btn_microphone.setVisibility(View.GONE);

		btn_live_snapshot = (ImageView) findViewById(R.id.btn_live_snapshot);
		btn_live_record = (ImageView) findViewById(R.id.btn_live_record);
		txt_recording = (TextView) findViewById(R.id.txt_recording);
		mIvRecording = (ImageView) findViewById(R.id.iv_recording);
		iv_cruise = (ImageView) findViewById(R.id.iv_cruise);
		// rl_wall_mounted_guide = (LinearLayout)
		// findViewById(R.id.rl_wall_mounted_guide);
		tv_timezone = (TextView) findViewById(R.id.tv_timezone);
		// 是否显示引导界面
		boolean isFirst = SharePreUtils.getBoolean("cache", this, mCamera.getUid() + "pb");
		// rl_wall_mounted_guide.setVisibility(isFirst ? View.GONE : View.VISIBLE);

	}

	// 设置时间 给tv_timezone
	private void setTime() {
		String str_data = sdf.format(new Date());
		tv_timezone.setText(str_data);
		timeHandler.sendEmptyMessage(0X119);
	}

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Handler timeHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0X119:
				setTime();
				break;
			}
		};
	};
	protected HI_P2P_WHITE_LIGHT_INFO light_info;
	protected boolean mWhiteLightSele;
	protected HI_P2P_WHITE_LIGHT_INFO_EXT light_info_ext;
	protected ABSOLUTE_LIGHT_TYPE abs_light;

	private void getIntentData() {
		uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
		for (MyCamera camera : HiDataValue.CameraList) {
			if (camera.getUid().equalsIgnoreCase(uid)) {
				this.mCamera = camera;
				break;
			}
		}
	}

	private void showLoadingView() {
		Animation rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
		iv_loading2.setVisibility(View.VISIBLE);
		iv_loading2.startAnimation(rotateAnim);
	}

	private void dismissLoadingView() {
		iv_loading2.clearAnimation();
		iv_loading2.setVisibility(View.GONE);
	}

	@Override
	public void callbackPlayUTC(HiCamera arg0, int arg1) {

	}

	@Override
	public void callbackState(HiCamera camera, int arg1, int arg2, int arg3) {
		if (mCamera != camera)
			return;
		Bundle bundle = new Bundle();
		bundle.putInt("command", arg1);
		bundle.putInt("width", arg2);
		bundle.putInt("height", arg3);
		Message msg = mHandler.obtainMessage();
		msg.what = HiDataValue.HANDLE_MESSAGE_PLAY_STATE;
		msg.setData(bundle);
		mHandler.sendMessage(msg);

	}

	@Override
	public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
		if (arg0 != mCamera)
			return;
		Bundle bundle = new Bundle();
		bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
		Message msg = mHandler.obtainMessage();
		msg.what = HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL;
		msg.obj = arg0;
		msg.arg1 = arg1;
		msg.arg2 = arg3;
		msg.setData(bundle);
		mHandler.sendMessage(msg);

	}

	@Override
	public void receiveSessionState(HiCamera arg0, int arg1) {
		if (mCamera != arg0)
			return;
		Message msg = mHandler.obtainMessage();
		msg.what = HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
		msg.obj = arg0;
		msg.arg1 = arg1;
		mHandler.sendMessage(msg);
	}

	@SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
				if (msg.arg2 == 0) {
					byte[] data = msg.getData().getByteArray(HiDataValue.EXTRAS_KEY_DATA);
					switch (msg.arg1) {
					case HiChipDefines.HI_P2P_START_LIVE:
						int width = Packet.byteArrayToInt_Little(data, 4);
						int heigth = Packet.byteArrayToInt_Little(data, 8);
						if (width <= 0 || heigth <= 0 || width > 5000 || heigth > 5000) {
							WallMountedActivity.this.finish();
							HiToast.showToast(WallMountedActivity.this, getString(R.string.tips_open_video_fail));
						}
						break;
					case HiChipDefines.HI_P2P_WHITE_LIGHT_GET:
						if (lightModel != 2) {
							return;
						}
						light_info = new HI_P2P_WHITE_LIGHT_INFO(data);
						if (light_info != null && light_info.u32State < 2) {
							if (light_info.u32State == 0) {
								iv_white_light.setSelected(true);
								mWhiteLightSele = true;
							} else {
								iv_white_light.setSelected(false);
								mWhiteLightSele = false;
							}
						}
						break;
					case HiChipDefines.HI_P2P_WHITE_LIGHT_GET_EXT:
						if (lightModel != 1) {
							return;
						}
						light_info_ext = new HI_P2P_WHITE_LIGHT_INFO_EXT(data);
						break;
					case HiChipDefines.HI_P2P_GET_ABSOLUTE_LIGHT_TYPE:
						if (lightModel != 3)
							return;
						abs_light = new ABSOLUTE_LIGHT_TYPE(data);
						break;

					}

				}
				break;
			case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
				handCameraSessionState(msg.arg1);
				break;
			case HiDataValue.HANDLE_MESSAGE_PLAY_STATE:
				Bundle bundle = msg.getData();
				handCameraPlayState(bundle);
				break;
			}

		};
	};

	protected void handCameraPlayState(Bundle bundle) {
		int command = bundle.getInt("command");
		switch (command) {
		case ICameraPlayStateCallback.PLAY_STATE_START:// 开始播放
			setTime();
			dismissLoadingView();
			video_width = bundle.getInt("width");
			video_height = bundle.getInt("height");
			Bitmap frame = null;

			if (!isSaveSnapshot){
				try {
					frame = mCamera != null ? mCamera.getSnapshot_EXT(1, 640, 352) : null;
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (frame != null) {
					saveSnapshot(frame);
					isSaveSnapshot = true;
				}
			}



			break;

			// 本地录像开始
		case ICameraPlayStateCallback.PLAY_STATE_RECORDING_START:
//			mRecordingState = RECORDING_STATUS_ING;
//			txt_recording.setV isibility(View.VISIBLE);
//			mIvRecording.setVisibility(View.VISIBLE);
//			handler.sendEmptyMessage(110);
			break;
			// 本地录像结束
		case ICameraPlayStateCallback.PLAY_STATE_RECORDING_END:
		case ICameraPlayStateCallback.PLAY_STATE_RECORD_ERROR:
//			mRecordingState = RECORDING_STATUS_NONE;
//			txt_recording.setVisibility(View.GONE);
//			mIvRecording.setVisibility(View.GONE);
//			handler.removeCallbacksAndMessages(null);
//			if (!TextUtils.isEmpty(recordFile)) {
//				File file = new File(recordFile);
//				if (file.length() <= 1024 && file.isFile() && file.exists()) {
//					file.delete();
//				}
//			}

		}

	}

	protected void handCameraSessionState(int arg1) {
		switch (arg1) {
		case HiCamera.CAMERA_CONNECTION_STATE_LOGIN:

			break;
		case HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED:
			finish();
			setViewWhetherClick(false);
			showLoadingView();
			if (mCamera != null) {
				mCamera.stopLiveShow();
				if (isListening) {
					isListening = false;
					btn_live_listen.setImageResource(R.drawable.camhi_live_normal_speaker);
					btn_microphone.setVisibility(View.GONE);
				}
			}

			break;
		case HiCamera.CAMERA_CHANNEL_STREAM_ERROR:// 通道出错处理，断线重连
			if (mCamera != null) {
				mCamera.stopLiveShow();
				if (isListening) {
					isListening = false;
					btn_live_listen.setImageResource(R.drawable.camhi_live_normal_speaker);
					btn_microphone.setVisibility(View.GONE);
				}
			}
			break;

		}

	}

	private float action_down_X;
	private float action_down_Y;

	private float move_X;
	private float move_Y;
	private int xlenOld;
	private int ylenOld;
	private double nLenStart;

	int moveX;
	int moveY;

	// Moniter手势处理
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.monitor_wall_mounted) {
			int nCnt = event.getPointerCount();
			if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN && 2 == nCnt) {// ������ָ
				mMonitor.setTouchMove(2);
				for (int i = 0; i < nCnt; i++) {
					float x = event.getX(i);
					float y = event.getY(i);
				}
				xlenOld = Math.abs((int) event.getX(0) - (int) event.getX(1));
				ylenOld = Math.abs((int) event.getY(0) - (int) event.getY(1));
				nLenStart = Math.sqrt((double) xlenOld * xlenOld + (double) ylenOld * ylenOld);
			} else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE && 2 == nCnt) {
				mMonitor.setTouchMove(2);
				for (int i = 0; i < nCnt; i++) {
					float x = event.getX(i);
					float y = event.getY(i);
				}
				int xlen = Math.abs((int) event.getX(0) - (int) event.getX(1));
				int ylen = Math.abs((int) event.getY(0) - (int) event.getY(1));
				int moveX = Math.abs(xlen - xlenOld);
				int moveY = Math.abs(ylen - ylenOld);
				double nLenEnd = Math.sqrt((double) xlen * xlen + (double) ylen * ylen);
				if (moveX < 10 && moveY < 10) {
					return true;
				}
				if (nLenEnd > nLenStart) {// 放大
					resetMonitorSize(true, nLenEnd);
				} else {// 缩小
					resetMonitorSize(false, nLenEnd);
				}
				xlenOld = xlen;
				ylenOld = ylen;
				nLenStart = nLenEnd;
				return true;
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				mMonitor.setTouchMove(0);
			} else if (nCnt == 1) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:				
					float x = event.getRawX();
					float y = event.getRawY();
					action_down_X = event.getRawX();
					action_down_Y = event.getRawY();
					// mMonitor.SetCruise(false);
					monitorHandler.removeCallbacksAndMessages(null);
					mMonitor.setTouchMove(0);
				case MotionEvent.ACTION_MOVE:				
					if (mMonitor.getTouchMove() != 0) {
						break;
					}
					break;
				case MotionEvent.ACTION_UP:
					if (mIsCruise) {
						monitorHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								mMonitor.SetCruise(mIsCruise);
							}
						}, 2000);
					}
					break;
				}
			}
		} else if (v.getId() == R.id.btn_microphone) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				btn_live_listen.setClickable(false);
				btn_live_listen.setImageResource(R.drawable.camhi_live_normal_speaker);
				if (System.currentTimeMillis() - oldClickTime < 1000) {
					break;
				}
				oldClickTime = System.currentTimeMillis();
				if (mRecordingState == RECORDING_STATUS_ING) {
					mCamera.PausePlayAudio();
				} else {
					mCamera.stopListening();
					mVoiceIsTran = false;
				}
				mCamera.startTalk();
				btn_microphone.setImageResource(R.drawable.camhi_live_select_micphone);
				isTalking = true;
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				break;
			}
			case MotionEvent.ACTION_UP: {
				btn_live_listen.setClickable(true);
				btn_live_listen.setImageResource(R.drawable.camhi_live_select_speaker);
				mCamera.stopTalk();
				if (mRecordingState == RECORDING_STATUS_ING) {
					mCamera.ResumePlayAudio();
				} else {
					mCamera.startListening();
					mVoiceIsTran = true;
				}
				btn_microphone.setImageResource(R.drawable.camhi_live_normal_micphone);
				isTalking = false;
				break;
			}
			}
		}
		return true;
	}

	private void resetMonitorSize(boolean large, double move) {
		if (mMonitor.height == 0 && mMonitor.width == 0) {
			initMatrix((int) mMonitor.screen_width, (int) mMonitor.screen_height);
		}
		moveX = (int) ((move * mMonitor.screen_width/mMonitor.screen_height) / 2);
		moveY = (int) ((move * mMonitor.screen_height / mMonitor.screen_width) / 2);
		if (large) {
			HiLog.e(" larger and larger ");
			if (mMonitor.width <= 4 * mMonitor.screen_width && mMonitor.height <= 4 * mMonitor.screen_height) {
				mMonitor.left -= (moveX / 2);
				mMonitor.bottom -= (moveY / 2);
				mMonitor.width += (moveX);
				mMonitor.height += (moveY);
			}
		} else {
			HiLog.e(" smaller and smaller ");
			mMonitor.left += (moveX / 2);
			mMonitor.bottom += (moveY / 2);
			mMonitor.width -= (moveX);
			mMonitor.height -= (moveY);
		}
		if (mMonitor.left > 0 || mMonitor.width < (int) mMonitor.screen_width
				|| mMonitor.height < (int) mMonitor.screen_height || mMonitor.bottom > 0) {
			initMatrix((int) mMonitor.screen_width, (int) mMonitor.screen_height);
		}

		if (mMonitor.width > (int) mMonitor.screen_width) {
			mMonitor.setState(1);
		} else {
			mMonitor.setState(0);
		}

		mMonitor.setMatrix(mMonitor.left, mMonitor.bottom, mMonitor.width, mMonitor.height);
	}

	private void initMatrix(int screen_width, int screen_height) {
		mMonitor.left = 0;
		mMonitor.bottom = 0;
		mMonitor.width = screen_width;
		mMonitor.height = screen_height;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_return) {
			WallMountedActivity.this.finish();
		} else if (id == R.id.btn_live_listen) {
			if (HiDataValue.ANDROID_VERSION >= 23 && (!HiTools.checkPermission(this, Manifest.permission.RECORD_AUDIO)
					|| !HiTools.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
				showAlertDialog();
				return;
			}
			clickListen((ImageView) v);
		} else if (id == R.id.btn_live_snapshot) {
			if (HiDataValue.ANDROID_VERSION >= 23 && (!HiTools.checkPermission(this, Manifest.permission.CAMERA)
					|| !HiTools.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
				showAlertDialog();
				return;
			}
			clickSnapshot();
		} else if (id == R.id.btn_live_record) {
			if (HiDataValue.ANDROID_VERSION >= 23
					&& (!HiTools.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
				showAlertDialog();
				return;
			}
			clickRecording((ImageView) v);
		} else if (id == R.id.iv_cruise) {
			setCruise();
		} else if (id == R.id.rl_wall_mounted_guide) {// rl_wall_mounted_guide.setVisibility(View.GONE);
			SharePreUtils.putBoolean("cache", this, mCamera.getUid() + "pb", true);
		} else if (id == R.id.iv_white_light) {
			if (lightModel == 3) {// 纯白光灯
				handAbsWhiteLight(0, iv_white_light);
			} else {
				handWhiteLight(0, iv_white_light);
			}
		}

	}

	public void setCruise() {
		iv_cruise.setSelected(mIsCruise == false ? true : false);
		if (mMonitor.GetFishLager() == (float) 0.0) {
			mMonitor.SetPosition(true, 8);
		}
		initMatrix((int) mMonitor.screen_width, (int) mMonitor.screen_height);
		mMonitor.setMatrix(mMonitor.left, mMonitor.bottom, mMonitor.width, mMonitor.height);
		mMonitor.SetCruise(mIsCruise = !mIsCruise);
		
	}

	// ���׹��
	private void handAbsWhiteLight(int dis, final ImageView iv) {
		iv.setSelected(mWhiteLightSele = !mWhiteLightSele);
		View customView = View.inflate(this, R.layout.pup_abs_white_light, null);
		PopupWindow pup = new PopupWindow(customView);
		ColorDrawable cd = new ColorDrawable(-000);
		pup.setBackgroundDrawable(cd);
		pup.setOutsideTouchable(true);
		pup.setFocusable(true);
		pup.setWidth(LayoutParams.WRAP_CONTENT);
		pup.setHeight(LayoutParams.WRAP_CONTENT);
		pup.showAtLocation(iv_white_light, Gravity.CENTER, 0, dis);
		final ImageView iv_auto = (ImageView) customView.findViewById(R.id.iv_auto);
		final ImageView iv_open = (ImageView) customView.findViewById(R.id.iv_open);
		final ImageView iv_close = (ImageView) customView.findViewById(R.id.iv_close);
		if (abs_light != null && abs_light.s32State < 3) {
			if (abs_light.s32State == 0) {
				iv_auto.setImageResource(R.drawable.finish);
				iv_open.setImageResource(0);
				iv_close.setImageResource(0);
			} else if (abs_light.s32State == 1) {
				iv_auto.setImageResource(0);
				iv_open.setImageResource(R.drawable.finish);
				iv_close.setImageResource(0);
			} else if (abs_light.s32State == 2) {
				iv_auto.setImageResource(0);
				iv_open.setImageResource(0);
				iv_close.setImageResource(R.drawable.finish);
			}
		}
		LinearLayout ll_auto = (LinearLayout) customView.findViewById(R.id.ll_auto);
		ll_auto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (abs_light != null) {
					abs_light.s32State = 0;
					iv_auto.setImageResource(R.drawable.finish);
					iv_open.setImageResource(0);
					iv_close.setImageResource(0);
					mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_ABSOLUTE_LIGHT_TYPE,
							ABSOLUTE_LIGHT_TYPE.parseContent(abs_light.s32State));
				}

			}
		});
		LinearLayout ll_open = (LinearLayout) customView.findViewById(R.id.ll_open);
		ll_open.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (abs_light != null) {
					abs_light.s32State = 1;
					iv_auto.setImageResource(0);
					iv_open.setImageResource(R.drawable.finish);
					iv_close.setImageResource(0);
					mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_ABSOLUTE_LIGHT_TYPE,
							ABSOLUTE_LIGHT_TYPE.parseContent(abs_light.s32State));
				}

			}
		});
		LinearLayout ll_close = (LinearLayout) customView.findViewById(R.id.ll_close);
		ll_close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (abs_light != null) {
					abs_light.s32State = 2;
					iv_auto.setImageResource(0);
					iv_open.setImageResource(0);
					iv_close.setImageResource(R.drawable.finish);
					mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_ABSOLUTE_LIGHT_TYPE,
							ABSOLUTE_LIGHT_TYPE.parseContent(abs_light.s32State));
				}

			}
		});
		pup.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				iv.setSelected(false);
			}
		});
		iv.setSelected(true);
	}

	private void handWhiteLight(int dis, final ImageView iv) {
		if (lightModel == 1) {// 支持扩展
			iv.setSelected(mWhiteLightSele = !mWhiteLightSele);
			View customView = View.inflate(this, R.layout.pup_white_light, null);
			PopupWindow pup = new PopupWindow(customView);
			ColorDrawable cd = new ColorDrawable(-000);
			pup.setBackgroundDrawable(cd);
			pup.setOutsideTouchable(true);
			pup.setFocusable(true);
			pup.setWidth(LayoutParams.WRAP_CONTENT);
			pup.setHeight(LayoutParams.WRAP_CONTENT);
			pup.showAtLocation(iv_white_light, Gravity.CENTER, 0, dis);
			final ImageView iv_infra = (ImageView) customView.findViewById(R.id.iv_infra);
			final ImageView iv_full_color = (ImageView) customView.findViewById(R.id.iv_full_color);
			final ImageView iv_intell = (ImageView) customView.findViewById(R.id.iv_intell);
			if (light_info_ext != null && light_info_ext.u32State < 3) {
				if (light_info_ext.u32State == 0) {
					iv_infra.setImageResource(R.drawable.finish);
					iv_full_color.setImageResource(0);
					iv_intell.setImageResource(0);
				} else if (light_info_ext.u32State == 1) {
					iv_infra.setImageResource(0);
					iv_full_color.setImageResource(R.drawable.finish);
					iv_intell.setImageResource(0);
				} else if (light_info_ext.u32State == 2) {
					iv_infra.setImageResource(0);
					iv_full_color.setImageResource(0);
					iv_intell.setImageResource(R.drawable.finish);
				}

			}
			LinearLayout ll_infraed = (LinearLayout) customView.findViewById(R.id.ll_infrared_mode);
			ll_infraed.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (light_info_ext != null) {
						light_info_ext.u32State = 0;
						iv_infra.setImageResource(R.drawable.finish);
						iv_full_color.setImageResource(0);
						iv_intell.setImageResource(0);
						mCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_SET_EXT,
								HI_P2P_WHITE_LIGHT_INFO_EXT.parseContent(light_info_ext.u32Chn,
										light_info_ext.u32State));
					}

				}
			});
			LinearLayout ll_full_color = (LinearLayout) customView.findViewById(R.id.ll_full_color_mode);
			ll_full_color.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (light_info_ext != null) {
						light_info_ext.u32State = 1;
						iv_infra.setImageResource(0);
						iv_full_color.setImageResource(R.drawable.finish);
						iv_intell.setImageResource(0);
						mCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_SET_EXT,
								HI_P2P_WHITE_LIGHT_INFO_EXT.parseContent(light_info_ext.u32Chn,
										light_info_ext.u32State));
					}
				}
			});

			LinearLayout ll_intell = (LinearLayout) customView.findViewById(R.id.ll_intelligence_mode);
			ll_intell.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (light_info_ext != null) {
						light_info_ext.u32State = 2;
						iv_infra.setImageResource(0);
						iv_full_color.setImageResource(0);
						iv_intell.setImageResource(R.drawable.finish);
						mCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_SET_EXT,
								HI_P2P_WHITE_LIGHT_INFO_EXT.parseContent(light_info_ext.u32Chn,
										light_info_ext.u32State));
					}
				}
			});
			pup.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					iv.setSelected(false);
				}
			});
			iv.setSelected(true);

		} else if (lightModel == 2) {// 只有开关
			if (light_info != null) {
				if (mWhiteLightSele) {// 开的状态,去关
					light_info.u32State = 1;
					mWhiteLightSele = false;
				} else {
					light_info.u32State = 0;
					mWhiteLightSele = true;
				}
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_SET,
						HI_P2P_WHITE_LIGHT_INFO.parseContent(light_info.u32Chn, light_info.u32State));
				iv_white_light.setSelected(mWhiteLightSele);
			}

		}
	}

	// 点击录像按钮，保存录像文件
	private void clickRecording(ImageView v) {
		if (mRecordingState == RECORDING_STATUS_NONE) {
//			mRecordingState = RECORDING_STATUS_LOADING;
			TimerRecording();
			btn_live_record.setImageResource(R.drawable.camhi_live_select_recording);
			
			mRecordingState = RECORDING_STATUS_ING;
			txt_recording.setVisibility(View.VISIBLE);
			mIvRecording.setVisibility(View.VISIBLE);
			handler.sendEmptyMessage(110);

		} else if (mRecordingState == RECORDING_STATUS_ING) {
			mRecordingState = RECORDING_STATUS_LOADING;
			if (mVoiceIsTran && btn_microphone.getVisibility() == View.GONE) {
				mCamera.stopListening();
				mVoiceIsTran = false;
			}
			mCamera.stopRecording();
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
			if (timerTask != null) {
				timerTask.cancel();
				timerTask = null;
			}
			btn_live_record.setImageResource(R.drawable.camhi_live_normal_recording);
					
			mRecordingState = RECORDING_STATUS_NONE;
			txt_recording.setVisibility(View.GONE);
			mIvRecording.setVisibility(View.GONE);
			handler.removeCallbacksAndMessages(null);
			if (!TextUtils.isEmpty(recordFile)) {
				File file = new File(recordFile);
				if (file.length() <= 1024 && file.isFile() && file.exists()) {
					file.delete();
				}
			}	
		}
	}

	private void TimerRecording() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
		final File cameraFolder = new File(HiDataValue.LOCAL_VIDEO_PATH + "/" + mCamera.getUid());
		if (!cameraFolder.exists()) {
			cameraFolder.mkdirs();
		}
		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				if (mRecordingState == RECORDING_STATUS_ING) {
					mCamera.stopRecording();
				}
				recordFile = cameraFolder.getAbsoluteFile() + "/" + HiTools.getFileNameWithTime(1);
				long available = HiTools.getAvailableSize();
				if (available < 100 && available > 0) {// 设备内存小于100M
					handler.sendEmptyMessage(0X120);
					return;
				}
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mCamera.startRecording(recordFile);
					}
				}, 1000);
			}
		};
		timer.schedule(timerTask, 0, 10 * 60 * 1000);
	}

	// 拍照，保存到本地文件夹 :/storage/sdcard1/CamHigh/Snapshot/Camera的UID/IMG_+时间+.jpg
	private void clickSnapshot() {
		if (mCamera != null) {
			if (HiTools.isSDCardValid()) {
				File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
				File sargetFolder = new File(rootFolder.getAbsolutePath() + "/Snapshot/");
				File yargetFolder = new File(sargetFolder.getAbsolutePath() + "/" + mCamera.getUid() + "/");
				// File targetFolder=new
				// File(yargetFolder.getAbsolutePath()+"/"+getTimeForNow()+"/");
				if (!rootFolder.exists()) {
					rootFolder.mkdirs();
				}
				if (!sargetFolder.exists()) {
					sargetFolder.mkdirs();
				}
				if (!yargetFolder.exists()) {
					yargetFolder.mkdirs();
				}
				String filename = HiTools.getFileNameWithTime(0);
				final String file = yargetFolder.getAbsoluteFile() + "/" + filename;
				HiLog.v("btn_live_snapshot:" + file);

				Bitmap frame = mCamera != null ? mCamera.getSnapshot() : null;
				if (frame != null && HiTools.saveImage(file, frame)) {
					SaveToPhone(file, filename);
					Toast toast1 = Toast.makeText(this, getText(R.string.tips_snapshot_success),Toast.LENGTH_SHORT);				
					toast1.show();
					toastList1.add(toast1);
				} else {
					
					Toast toast2 = Toast.makeText(this, getText(R.string.tips_snapshot_failed),Toast.LENGTH_SHORT);					
					toast2.show();
					toastList2.add(toast2);
				}
			} else {
			
				    Toast toast3 = Toast.makeText(this, getText(R.string.tips_no_sdcard).toString(),Toast.LENGTH_SHORT);
				    toast3.show();
				    toastList3.add(toast3);
			}
		}

	}

	private void SaveToPhone(final String path, final String fileName) {
		
	new Thread(new Runnable() {
			
			@Override
			public void run() {

				// 其次把文件插入到系统图库
				try {
					MediaStore.Images.Media.insertImage(getContentResolver(), path, fileName, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 最后通知图库更新
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));

			}
		}).start();
	
	}

	private boolean mVoiceIsTran = false; // 当前声音是否是传输状态

	// 点击声音按钮开始监听语音，按住喇叭说话，松开接收
	private void clickListen(ImageView iv) {
		if (mRecordingState == RECORDING_STATUS_ING) {// 正在录像中...
			if (mVoiceIsTran) {
				mCamera.PausePlayAudio();
			} else {
				mCamera.stopRecording();
				mCamera.startListening();
				mVoiceIsTran = true;
				TimerRecording();
			}
			if (isListening) {
				iv.setImageResource(R.drawable.camhi_live_normal_speaker);
				btn_microphone.setVisibility(View.GONE);
			//	mCamera.stopListening();
				if(mVoiceIsTran)
				{
					mCamera.PausePlayAudio();
				}
			
			} else {
				iv.setImageResource(R.drawable.camhi_live_select_speaker);
				btn_microphone.setVisibility(View.VISIBLE);
			//	mCamera.startListening();
				if(mVoiceIsTran)
				{
					mCamera.ResumePlayAudio();
				}
				
				
			}
		} else {
			if (isListening) {
				iv.setImageResource(R.drawable.camhi_live_normal_speaker);
				mCamera.stopListening();
				mVoiceIsTran = false;
				btn_microphone.setVisibility(View.GONE);
			} else {
				iv.setImageResource(R.drawable.camhi_live_select_speaker);
				btn_microphone.setVisibility(View.VISIBLE);
				mCamera.startListening();
				mVoiceIsTran = true;
			}
		}
		isListening = !isListening;
	}

	private void showAlertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.tips_no_permission));
		builder.setPositiveButton(getString(R.string.setting), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent();
				intent.setAction("android.intent.action.MAIN");
				intent.setClassName("com.android.settings", "com.android.settings.ManageApplications");
				startActivity(intent);

			}
		});
		builder.setNegativeButton(getString(R.string.cancel), null);
		builder.show();

	}

	private void setViewWhetherClick(boolean whetherClick) {
		btn_live_listen.setClickable(whetherClick);
		btn_live_record.setClickable(whetherClick);
	}

	// 保存国科的快照
	@SuppressLint("StaticFieldLeak")
	private void saveSnapshot(final Bitmap frame) {

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

					File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
					File sargetFolder = new File(rootFolder.getAbsolutePath() + "/android/data/"
							+ getResources().getString(R.string.app_name));

					if (!rootFolder.exists()) {
						rootFolder.mkdirs();
					}
					if (!sargetFolder.exists()) {
						sargetFolder.mkdirs();
					}

					HiTools.saveBitmap(frame, sargetFolder.getAbsolutePath() + "/" + mCamera.getUid());
					HiLog.v(sargetFolder.getAbsolutePath() + "/" + mCamera.getUid());
					mCamera.snapshot = frame;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				Intent intent = new Intent();
				intent.setAction(HiDataValue.ACTION_CAMERA_INIT_END);
				sendBroadcast(intent);
				super.onPostExecute(result);
			}
		}.execute();

	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 110:
				if (mIvRecording.getVisibility() == View.GONE) {
					mIvRecording.setVisibility(View.VISIBLE);
				} else {
					mIvRecording.setVisibility(View.GONE);
				}
				handler.sendEmptyMessageDelayed(110, 1000);
				break;
			case 0X120:
				HiToast.showToast(WallMountedActivity.this, getString(R.string.failed_recording));
				break;
			}
		};
	};

}
