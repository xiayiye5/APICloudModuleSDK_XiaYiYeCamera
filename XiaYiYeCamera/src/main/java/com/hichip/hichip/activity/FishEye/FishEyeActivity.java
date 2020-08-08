package com.hichip.hichip.activity.FishEye;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import com.hichip.customview.CircularView;
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
import com.hichip.thecamhi.utils.BitmapUtils;
import com.hichip.thecamhi.utils.SharePreUtils;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FishEyeActivity extends HiActivity implements OnClickListener, OnTouchListener, ICameraIOSessionCallback,
		ICameraPlayStateCallback, OnCheckedChangeListener {
	private MyCamera mMyCamera;
	public MyLiveViewGLMonitor mMonitor;
	private Button btn_return;
	public RelativeLayout rl_guide, rl_live_view_model;
	private RelativeLayout rl_voice, rl_talk, rl_snapshot, rl_record_video, rl_cruise;
	private ImageView iv_voice, iv_recording, iv_talk, iv_loading2;
	public ImageView iv_live_cruise;
	public boolean mIsCruise = false;
	public static int mFrameMode = 1; // 1.圆 2.圆柱 3.二画面 4.四画面 5.碗
	private ImageView iv_record_video;
	public ImageView iv_white_light, mIvFullScreen;
	public int mWallMode = 0;// 0-壁装全景 1-壁装放大局部画面
	private RelativeLayout rl_top;
	private LinearLayout ll_buttom;
	private TextView txt_recording, tv_install_mode_, tv_know, play_view_model, tv_install;
	private int mCameraVideoQuality;
	private int RECORDING_STATUS_NONE = 0;
	private int RECORDING_STATUS_LOADING = 1;
	private int RECORDING_STATUS_ING = 2;
	private int mRecordingState = RECORDING_STATUS_NONE;
	private Timer timer;
	private TimerTask timerTask;
	private String recordFile;
	private boolean isListening = false;
	private boolean isTalking = false;
	public static int misFullScreen = 1; // 1是竖屏 2是横屏
	private RadioGroup rg_live_fisheye_view_model, rg_view_model;
	public RadioButton rbtn_land_circle;
	private RadioButton rbtn_land_cylinder, rbtn_land_bowl, rbtn_land_two, rbtn_land_four, rbtn_land_wall_partview;
	private CircularView circular;
	private RelativeLayout rl_view_model;
	public ImageView rbtn_circle;
	public ImageView rbtn_cylinder;
	public ImageView rbtn_bowl;
	public ImageView rbtn_two;
	public ImageView rbtn_four;
	public ImageView rbtn_wall_partview;

	public int lightModel = 0;// 0 non,1 HI_P2P_WHITE_LIGHT_GET_EXT,2 HI_P2P_WHITE_LIGHT_GET 3 纯白光灯
	private HI_P2P_WHITE_LIGHT_INFO light_info;
	private HI_P2P_WHITE_LIGHT_INFO_EXT light_info_ext;
	private ABSOLUTE_LIGHT_TYPE abs_light;
	private boolean mWhiteLightSele = false;
	public RelativeLayout ll_land_top;
	private ImageView btn_land_return;
	public ImageView iv_land_white_light;
	private TextView tv_timezone;
	private float cirx;
	private float ciry;
	private float cirr;
	private Handler monitorHandler = new Handler();
	private boolean needDelay;

	private List<Toast> toastList1 = new ArrayList<>();
	private List<Toast> toastList2 = new ArrayList<>();
	private List<Toast> toastList3 = new ArrayList<>();
	//点击对讲时监听的状态
	private boolean preVoiceStatus;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_fish_eye);
		getIntentData();

		mFrameMode = 1;// 有可能调用finish之后并不会马上调用ondestory,所以在这里回置初始值
		Log.i("tedu", "--555    mFrameMode我变成1了--");
		misFullScreen = 1;
		// 白光灯
		getLightModel();
		initView();
		initFishView();
		// 是否显示引导界面
		rl_guide.setVisibility(mMyCamera.isFirst ? View.GONE : View.VISIBLE);
		// 显示
		setListeners();
	}

	@Override
	protected void onResume() {
		super.onResume();
		showLoadingView();
		if (mMyCamera != null) {
			new Thread() {
				public void run() {
					mMyCamera.startLiveShow(mMyCamera.getVideoQuality(), mMonitor);
				};
			}.start();
			mMyCamera.registerIOSessionListener(this);
			mMyCamera.registerPlayStateListener(this);
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

		if (mMyCamera != null) {
			mMyCamera.stopLiveShow();
			mMyCamera.unregisterIOSessionListener(this);
			mMyCamera.unregisterPlayStateListener(this);
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		finish();

	}

	private void cancelToast(List<Toast> list) {
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) != null) {
					list.get(i).cancel();
				}
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}
		if (mRecordHandler != null) {
			mRecordHandler.removeCallbacksAndMessages(null);
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

	private void getLightModel() {
		boolean b = mMyCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_ABSOLUTE_LIGHT_TYPE);
		if (b) {
			lightModel = 3;
			mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_ABSOLUTE_LIGHT_TYPE, null);
			return;
		}
		b = mMyCamera.getCommandFunction(HiChipDefines.HI_P2P_WHITE_LIGHT_GET_EXT);
		if (b) {
			lightModel = 1;
			mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_GET_EXT, null);
			return;
		}
		b = mMyCamera.getCommandFunction(HiChipDefines.HI_P2P_WHITE_LIGHT_GET);
		if (b) {
			lightModel = 2;
			mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_GET, null);
		}
	}

	private void initFishView() {
		// 找圆心功能,把返回来的 X Y 和R传下去
		cirx = SharePreUtils.getFloat("chche", this, mMyCamera.getUid() + "xcircle");
		ciry = SharePreUtils.getFloat("chche", this, mMyCamera.getUid() + "ycircle");
		cirr = SharePreUtils.getFloat("chche", this, mMyCamera.getUid() + "rcircle");
		mMyCamera.mInstallMode = SharePreUtils.getInt("mInstallMode", this, mMyCamera.getUid());
		mMonitor.SetCirInfo(cirx, ciry, cirr);
		mMonitor.SetViewType(mMonitor.TOP_ALL_VIEW);/* 设置为鱼眼顶装 */
		// mMonitor.SetViewType_EXT(mMonitor.TOP_ALL_VIEW,
		// mMyCamera.getFishModType());/* 设置为鱼眼顶装 */
		// setScreenSize 竖屏的时候用宽宽 横屏的时候用宽高
		Log.e("mMyCamera.mInstallMode", mMyCamera.mInstallMode + "");
		mMonitor.SetScreenSize(getWindowManager().getDefaultDisplay().getWidth(),
				getWindowManager().getDefaultDisplay().getWidth());
		if (mMyCamera.mInstallMode == 0) {
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CIRCLE, mFrameMode);
			handTopModelView();
		} else {
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_NEWSIDE, 1);
			handWallModelView();
		}
		// 设置第一个选中
		setSelectedMode(0);
		// rbtn_circle.setPressed(true);
		mMonitor.setCamera(mMyCamera);
		mMonitor.SetCruise(mIsCruise); /* 巡航开启 */
		mMyCamera.setVideoQuality(0);
		mMyCamera.setLiveShowMonitor(mMonitor);
	}

	public void setSelectedMode(int i) {
		rbtn_circle.setSelected(i == 0);
		rbtn_cylinder.setSelected(i == 1);
		rbtn_bowl.setSelected(i == 2);
		rbtn_two.setSelected(i == 3);
		rbtn_four.setSelected(i == 4);
		rbtn_wall_partview.setSelected(i == 5);
	}

	private void setListeners() {
		mMonitor.setOnTouchListener(this);
		btn_return.setOnClickListener(this);
		mIvFullScreen.setOnClickListener(this);
		rl_voice.setOnClickListener(this);
		tv_install_mode_.setOnClickListener(this);
		rl_talk.setOnClickListener(this);
		rl_snapshot.setOnClickListener(this);
		rl_record_video.setOnClickListener(this);
		rl_cruise.setOnClickListener(this);
		tv_know.setOnClickListener(this);
		play_view_model.setOnClickListener(this);
		rl_live_view_model.setOnClickListener(this);
		rg_live_fisheye_view_model.setOnCheckedChangeListener(this);
		rg_view_model.setOnCheckedChangeListener(this);
		iv_white_light.setOnClickListener(this);
		btn_land_return.setOnClickListener(this);
		iv_land_white_light.setOnClickListener(this);

		rbtn_circle.setOnClickListener(this);
		rbtn_cylinder.setOnClickListener(this);
		rbtn_bowl.setOnClickListener(this);
		rbtn_two.setOnClickListener(this);
		rbtn_four.setOnClickListener(this);
		rbtn_wall_partview.setOnClickListener(this);
	}

	private void initView() {
		mMonitor = (MyLiveViewGLMonitor) findViewById(R.id.monitor_live_view);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getWidth());
		mMonitor.setLayoutParams(params);
		btn_return = (Button) findViewById(R.id.btn_return);
		rl_voice = (RelativeLayout) findViewById(R.id.rl_voice);
		iv_loading2 = (ImageView) findViewById(R.id.iv_loading2);
		rl_talk = (RelativeLayout) findViewById(R.id.rl_talk);
		iv_talk = (ImageView) findViewById(R.id.iv_talk);
		mIvFullScreen = (ImageView) findViewById(R.id.iv_full_screen);
		rl_top = (RelativeLayout) findViewById(R.id.rl_top);
		ll_buttom = (LinearLayout) findViewById(R.id.ll_buttom);
		txt_recording = (TextView) findViewById(R.id.txt_recording);
		iv_recording = (ImageView) findViewById(R.id.iv_recording);
		iv_voice = (ImageView) findViewById(R.id.iv_voice);
		tv_install_mode_ = (TextView) findViewById(R.id.tv_install_mode_);
		tv_install_mode_
				.setText(mMyCamera.mInstallMode == 0 ? getString(R.string.fish_top) : getString(R.string.fish_wall));
		rl_snapshot = (RelativeLayout) findViewById(R.id.rl_snapshot);
		rl_record_video = (RelativeLayout) findViewById(R.id.rl_record_video);
		iv_record_video = (ImageView) findViewById(R.id.iv_record_video);
		rl_cruise = (RelativeLayout) findViewById(R.id.rl_cruise);
		iv_live_cruise = (ImageView) findViewById(R.id.iv_live_cruise);
		rg_live_fisheye_view_model = (RadioGroup) findViewById(R.id.rg_live_fisheye_view_model);
		rbtn_circle = (ImageView) findViewById(R.id.rbtn_live_circle);
		rbtn_cylinder = (ImageView) findViewById(R.id.rbtn_live_cylinder);
		rbtn_two = (ImageView) findViewById(R.id.rbtn_live_two);
		rbtn_four = (ImageView) findViewById(R.id.rbtn_live_four);
		tv_know = (TextView) findViewById(R.id.tv_know);
		rl_guide = (RelativeLayout) findViewById(R.id.rl_guide);
		circular = (CircularView) findViewById(R.id.circular);
		circular.setMonitor(mMonitor);
		rbtn_bowl = (ImageView) findViewById(R.id.rbtn_live_bowl);
		rl_view_model = (RelativeLayout) findViewById(R.id.rl_view_model);

		play_view_model = (TextView) findViewById(R.id.play_view_model);
		rl_live_view_model = (RelativeLayout) findViewById(R.id.rl_live_view_model);
		rg_view_model = (RadioGroup) findViewById(R.id.rg_view_model);

		rbtn_land_circle = (RadioButton) findViewById(R.id.rbtn_circle);
		rbtn_land_cylinder = (RadioButton) findViewById(R.id.rbtn_cylinder);
		rbtn_land_bowl = (RadioButton) findViewById(R.id.rbtn_bowl);
		rbtn_land_two = (RadioButton) findViewById(R.id.rbtn_two);
		rbtn_land_four = (RadioButton) findViewById(R.id.rbtn_four);
		rbtn_land_wall_partview = (RadioButton) findViewById(R.id.rbtn_wall_partview);
		rbtn_wall_partview = (ImageView) findViewById(R.id.rbtn_live_wall_partview);
		tv_install = (TextView) findViewById(R.id.tv_install);
		iv_white_light = (ImageView) findViewById(R.id.iv_white_light);
		iv_white_light.setVisibility(lightModel == 0 ? View.GONE : View.VISIBLE);
		ll_land_top = (RelativeLayout) findViewById(R.id.ll_land_top);
		btn_land_return = (ImageView) findViewById(R.id.btn_live_land_return);
		iv_land_white_light = (ImageView) findViewById(R.id.iv_land_white_light);
		tv_timezone = (TextView) findViewById(R.id.tv_timezone);
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

	private void getIntentData() {
		String uid = getIntent().getExtras().getString(HiDataValue.EXTRAS_KEY_UID);
		for (MyCamera camera : HiDataValue.CameraList) {
			if (camera.getUid().equals(uid)) {
				this.mMyCamera = camera;
				mCameraVideoQuality = mMyCamera.getVideoQuality();
				break;
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.rl_talk) {
			handTalk();
		} else if (id == R.id.btn_return) {
			finish();
		} else if (id == R.id.iv_full_screen) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else if (id == R.id.rl_voice) {
			if (HiDataValue.ANDROID_VERSION >= 23
					&& (!HiTools.checkPermission(FishEyeActivity.this, Manifest.permission.RECORD_AUDIO) || !HiTools
					.checkPermission(FishEyeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
				showAlertDialog();
				return;
			}
			clickListen(v);
		} else if (id == R.id.rl_snapshot) {
			if (HiDataValue.ANDROID_VERSION >= 23
					&& (!HiTools.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
				showAlertDialog();
				return;
			}
			clickSnapshot();
		} else if (id == R.id.rl_record_video) {
			if (HiDataValue.ANDROID_VERSION >= 23
					&& (!HiTools.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
				showAlertDialog();
				return;
			}

			clickRecording(v);
		} else if (id == R.id.rl_cruise) {
			iv_live_cruise.setSelected(mIsCruise == false ? true : false);
			if (mMonitor.mthreadGesture != null && mMonitor.mthreadGesture.isRunning) {
				mMonitor.mthreadGesture.stopThread();
			}
			if (mMonitor.mthreadGesture_2 != null && mMonitor.mthreadGesture_2.isRunning) {
				mMonitor.mthreadGesture_2.stopThread();
			}
			monitorHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mMonitor.SetCruise(mIsCruise = !mIsCruise);
				}
			}, 30);
			if (mMyCamera.mInstallMode == 1 && mWallMode == 0) {// 壁装全景
				mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_SIDE, 0);// 壁装局部放大
				mWallMode = 1;
				setSelectedMode(5);
				// rbtn_wall_partview.setChecked(true);
			}
		} else if (id == R.id.tv_know) {
			SharePreUtils.putBoolean("cache", FishEyeActivity.this, mMyCamera.getUid(), true);
			mMyCamera.isFirst = true;
			rl_guide.setVisibility(View.GONE);
		} else if (id == R.id.tv_install_mode_) {
			handInstallMode(v);
		} else if (id == R.id.play_view_model) {
			handRadioButtonCheck();
			rl_live_view_model.setVisibility(View.VISIBLE);
			ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f);
			scaleAnimation.setDuration(400);
			rl_live_view_model.startAnimation(scaleAnimation);
		} else if (id == R.id.rl_live_view_model) {
			ScaleAnimation scaleAnimation;
			rl_live_view_model.setVisibility(View.GONE);
			scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f);
			scaleAnimation.setDuration(200);
			rl_live_view_model.startAnimation(scaleAnimation);
		} else if (id == R.id.iv_white_light) {
			if (lightModel == 3) {// 纯白光灯
				handAbsWhiteLight(-HiTools.dip2px(this, 50), iv_white_light);
			} else {
				handWhiteLight(-HiTools.dip2px(this, 50), iv_white_light);
			}
		} else if (id == R.id.btn_live_land_return) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (id == R.id.iv_land_white_light) {
			if (lightModel == 3) {// 纯白光灯
				handAbsWhiteLight(0, iv_land_white_light);
			} else {
				handWhiteLight(0, iv_land_white_light);
			}
		} else if (id == R.id.rbtn_live_circle) {
			if (mMyCamera.mInstallMode == 0) {
				mFrameMode = 1;
				Log.i("tedu", "--444    mFrameMode我变成1了--");
				mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CIRCLE, mFrameMode);
				mMonitor.mIsZoom = false;
			} else {
				mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_NEWSIDE, 1);
				mWallMode = 0;
				if (mIsCruise) {
					mMonitor.SetCruise(mIsCruise = !mIsCruise);
					iv_live_cruise.setSelected(mIsCruise);
				}
			}
			handRadioButtonCheck();
		} else if (id == R.id.rbtn_live_cylinder) {
			mFrameMode = 2;
			mMonitor.mIsZoom = false;
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CARTOON, 1);
			handRadioButtonCheck();
		} else if (id == R.id.rbtn_live_bowl) {
			mFrameMode = 5;
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CARTOON, 0);// 立体碗
			handRadioButtonCheck();
		} else if (id == R.id.rbtn_live_two) {
			mFrameMode = 3;
			mMonitor.mIsZoom = false;
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_COLUMN, 2);
			handRadioButtonCheck();
		} else if (id == R.id.rbtn_live_four) {
			mFrameMode = 4;
			mMonitor.mIsZoom = false;
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CIRCLE, mFrameMode);
			handRadioButtonCheck();
		} else if (id == R.id.rbtn_live_wall_partview) {
			mWallMode = 1;
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_SIDE, 0);
			handRadioButtonCheck();
		}
	}

	// 纯白光灯
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
					mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_ABSOLUTE_LIGHT_TYPE,
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
					mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_ABSOLUTE_LIGHT_TYPE,
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
					mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_ABSOLUTE_LIGHT_TYPE,
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
						mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_SET_EXT,
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
						mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_SET_EXT,
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
						mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_SET_EXT,
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
				mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_SET,
						HI_P2P_WHITE_LIGHT_INFO.parseContent(light_info.u32Chn, light_info.u32State));
				iv_white_light.setSelected(mWhiteLightSele);
			}

		}
	}

	private void handTalk() {

		if (mRecordingState == RECORDING_STATUS_ING) {
			mMyCamera.PausePlayAudio();
		} else {
			mMyCamera.stopListening();
			mVoiceIsTran = false;
		}
		if (isTalking) {
			mMyCamera.stopTalk();
		} else {
			mMyCamera.startTalk();
		}
		if (isListening) {
            preVoiceStatus =true;
			iv_voice.setSelected(false);
			mMyCamera.stopListening();
			mVoiceIsTran = false;
			isListening = !isListening;
		}else {
			if (preVoiceStatus){
				preVoiceStatus =false;
				iv_voice.setSelected(true);
				mMyCamera.startListening();
				mVoiceIsTran = true;
				isListening = !isListening;
			}
		}
		isTalking = !isTalking;
		iv_talk.setSelected(isTalking);
	}

	private void handRadioButtonCheck() {
		tv_install.setText(mMyCamera.mInstallMode == 0 ? getString(R.string.fish_top) : getString(R.string.fish_wall));
		if (mMyCamera.mInstallMode == 1) {
			handWallModelView();
			if (mWallMode == 0) {
				rbtn_land_circle.setChecked(true);
				// rbtn_circle.setChecked(true);
				setSelectedMode(0);
			} else {
				rbtn_land_wall_partview.setChecked(true);
				// rbtn_wall_partview.setChecked(true);
				setSelectedMode(5);
			}
			return;
		}
		handTopModelView();
		switch (mFrameMode) {
		case 1:// 1.圆
				// rbtn_circle.setChecked(true);
			setSelectedMode(0);
			rbtn_land_circle.setChecked(true);
			break;
		case 2:// 2.圆柱
				// rbtn_cylinder.setChecked(true);
			setSelectedMode(1);
			rbtn_land_cylinder.setChecked(true);
			break;
		case 3:// 3.二画面
				// rbtn_two.setChecked(true);
			setSelectedMode(3);
			rbtn_land_two.setChecked(true);
			break;
		case 4:// 4.四画面
				// rbtn_four.setChecked(true);
			setSelectedMode(4);
			rbtn_land_four.setChecked(true);
			break;
		case 5:// 5.碗
				// rbtn_bowl.setChecked(true);
			setSelectedMode(2);
			rbtn_land_bowl.setChecked(true);
			break;
		}
	}

	private void handInstallMode(View v) {
		View customView = View.inflate(FishEyeActivity.this, R.layout.pup_install_mode, null);
		final PopupWindow pWindow = new PopupWindow(customView);
		ColorDrawable cd = new ColorDrawable(-0000);
		pWindow.setBackgroundDrawable(cd);
		pWindow.setOutsideTouchable(true);
		pWindow.setFocusable(true);
		pWindow.setWidth(LayoutParams.WRAP_CONTENT);
		pWindow.setHeight(LayoutParams.WRAP_CONTENT);
		pWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		int offsetY = HiTools.dip2px(FishEyeActivity.this, 45);
		pWindow.showAtLocation(v, 0, location[0], location[1] + offsetY);
		final LinearLayout ll_top = (LinearLayout) customView.findViewById(R.id.ll_top);
		final LinearLayout ll_wall = (LinearLayout) customView.findViewById(R.id.ll_wall);
		if (mMyCamera.mInstallMode == 0) {
			ll_top.setSelected(true);
		} else {
			ll_wall.setSelected(true);
		}
		final Animation anim = AnimationUtils.loadAnimation(FishEyeActivity.this, R.anim.alpha_view_model);
		// 顶装(圆)
		ll_top.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMyCamera.mInstallMode == 0) {
					pWindow.dismiss();
					return;
				}

				mMyCamera.mInstallMode = 0;
				tv_install_mode_.setText(getString(R.string.fish_top));
				mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CIRCLE, 1);
				mMonitor.mIsZoom = false;
				ll_top.setSelected(true);
				ll_wall.setSelected(false);
				pWindow.dismiss();
				SharePreUtils.putInt("mInstallMode", FishEyeActivity.this, mMyCamera.getUid(), mMyCamera.mInstallMode);
				// rbtn_circle.setChecked(true);
				setSelectedMode(0);
				rl_view_model.startAnimation(anim);
				handTopModelView();
				if (mIsCruise) {
					mMonitor.SetCruise(mIsCruise = !mIsCruise);
					iv_live_cruise.setSelected(mIsCruise);
				}
				// 向设备发送顶装壁装切换命令
				if (mMyCamera.getCommandFunction(HiChipDefines.HI_P2P_SET_DEVICE_FISH_PARAM)) {
					showjuHuaDialog();
					mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_DEVICE_FISH_PARAM, HiChipDefines.HI_P2P_DEV_FISH
							.parseContent(1, mMyCamera.mInstallMode, mMyCamera.getFishModType(), cirx, ciry, cirr));
				}

			}

		});
		ll_wall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMyCamera.mInstallMode == 1) {
					pWindow.dismiss();
					return;
				}
				mMyCamera.mInstallMode = 1;
				tv_install_mode_.setText(getString(R.string.fish_wall));
				mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_NEWSIDE, 1);// 1 才是全景
				mMonitor.mIsZoom = false;
				ll_top.setSelected(false);
				ll_wall.setSelected(true);
				// rbtn_circle.setChecked(true);
				setSelectedMode(0);
				mWallMode = 0;
				mFrameMode = 1;
				Log.i("tedu", "--333    mFrameMode我变成1了--");
				pWindow.dismiss();
				SharePreUtils.putInt("mInstallMode", FishEyeActivity.this, mMyCamera.getUid(), mMyCamera.mInstallMode);
				rl_view_model.startAnimation(anim);
				handWallModelView();
				if (mIsCruise) {
					mMonitor.SetCruise(mIsCruise = !mIsCruise);
					iv_live_cruise.setSelected(mIsCruise);
				}

				// 向设备发送顶装壁装切换命令
				if (mMyCamera.getCommandFunction(HiChipDefines.HI_P2P_SET_DEVICE_FISH_PARAM)) {
					showjuHuaDialog();
					mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_DEVICE_FISH_PARAM, HiChipDefines.HI_P2P_DEV_FISH
							.parseContent(1, mMyCamera.mInstallMode, mMyCamera.getFishModType(), cirx, ciry, cirr));
				}
			}

		});
	}

	private void handTopModelView() {
		rbtn_cylinder.setVisibility(View.VISIBLE);
		rbtn_two.setVisibility(View.VISIBLE);
		rbtn_four.setVisibility(View.VISIBLE);
		rbtn_bowl.setVisibility(View.VISIBLE);
		rbtn_wall_partview.setVisibility(View.GONE);

		rbtn_land_cylinder.setVisibility(View.VISIBLE);
		rbtn_land_two.setVisibility(View.VISIBLE);
		rbtn_land_four.setVisibility(View.VISIBLE);
		rbtn_land_bowl.setVisibility(View.VISIBLE);
		rbtn_land_wall_partview.setVisibility(View.GONE);

	}

	private void handWallModelView() {
		rbtn_cylinder.setVisibility(View.GONE);
		rbtn_two.setVisibility(View.GONE);
		rbtn_four.setVisibility(View.GONE);
		rbtn_bowl.setVisibility(View.GONE);
		rbtn_wall_partview.setVisibility(View.VISIBLE);

		rbtn_land_cylinder.setVisibility(View.GONE);
		rbtn_land_two.setVisibility(View.GONE);
		rbtn_land_four.setVisibility(View.GONE);
		rbtn_land_bowl.setVisibility(View.GONE);
		rbtn_land_wall_partview.setVisibility(View.VISIBLE);
	}

	private void clickSnapshot() {
		if (mMyCamera != null) {
			if (HiTools.isSDCardValid()) {
				File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
				File sargetFolder = new File(rootFolder.getAbsolutePath() + "/Snapshot/");
				File yargetFolder = new File(sargetFolder.getAbsolutePath() + "/" + mMyCamera.getUid() + "/");
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

				Bitmap frame = mMyCamera != null ? mMyCamera.getSnapshot() : null;

				if (frame != null && HiTools.saveImage(file, frame)) {
					SaveToPhone(file, filename);
					Toast toast1 = Toast.makeText(this, getText(R.string.tips_snapshot_success), Toast.LENGTH_SHORT);
					toast1.show();
					toastList1.add(toast1);
				} else {

					Toast toast2 = Toast.makeText(this, getText(R.string.tips_snapshot_failed), Toast.LENGTH_SHORT);
					toast2.show();
					toastList2.add(toast2);
				}
			} else {

				Toast toast3 = Toast.makeText(this, getText(R.string.tips_no_sdcard).toString(), Toast.LENGTH_SHORT);
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
	private void clickListen(View iv) {
		if (mRecordingState == RECORDING_STATUS_ING) {// 正在录像中...
			if (mVoiceIsTran) {
				mMyCamera.PausePlayAudio();
			} else {
				mMyCamera.stopRecording();
				mMyCamera.startListening();
				mVoiceIsTran = true;
				// 需要延时一秒
				needDelay = true;
				TimerRecording();
			}

			if (isListening) {
				// mMyCamera.stopListening();
				if (mVoiceIsTran) {
					mMyCamera.PausePlayAudio();
				}

			} else {
				// mMyCamera.startListening();

				if (mVoiceIsTran) {
					mMyCamera.ResumePlayAudio();
				}

			}

		} else {
			if (isListening) {
				preVoiceStatus = false;
				mMyCamera.stopListening();
				mVoiceIsTran = false;
			} else {
				preVoiceStatus = true;
				mMyCamera.startListening();
				mVoiceIsTran = true;
			}
		}

		if (isTalking) {
			iv_talk.setSelected(false);
			mMyCamera.stopTalk();
			isTalking = !isTalking;
		}

		isListening = !isListening;
		iv_voice.setSelected(isListening);

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
		final File cameraFolder = new File(HiDataValue.LOCAL_VIDEO_PATH + "/" + mMyCamera.getUid());
		if (!cameraFolder.exists()) {
			cameraFolder.mkdirs();
		}
		timer = new Timer();
		timerTask = new TimerTask() {

			@Override
			public void run() {
				if (mRecordingState == RECORDING_STATUS_ING) {
					mMyCamera.stopRecording();
				}
				recordFile = cameraFolder.getAbsoluteFile() + "/" + HiTools.getFileNameWithTime(1);
				double available = HiTools.getAvailableSize();
				if (available < 100 && available > 0) {// 设备内存小于100M
					mHandler.sendEmptyMessage(0X999);
					return;
				}
				if (needDelay) {
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							mMyCamera.startRecording(recordFile);
						}
					}, 1000);
				} else {
					mMyCamera.startRecording(recordFile);
					needDelay = true;
				}

			}
		};
		timer.schedule(timerTask, 0, 10 * 60 * 1000);

	}

	private Handler mRecoidHandler = new Handler() {
		public void handleMessage(Message msg) {
		};
	};

	private void clickRecording(View v) {
		if (mRecordingState == RECORDING_STATUS_NONE) {
			mRecordingState = RECORDING_STATUS_LOADING;
			needDelay = false;
			TimerRecording();

			Log.i("tedu", "--鱼眼直播界面  本地录像开始 开始--");
			mRecordingState = RECORDING_STATUS_ING;
			txt_recording.setVisibility(View.VISIBLE);
			iv_recording.setVisibility(View.VISIBLE);
			mRecordHandler.sendEmptyMessage(110);
			iv_record_video.setSelected(true);

		} else if (mRecordingState == RECORDING_STATUS_ING) {
			// mRecordingState = RECORDING_STATUS_LOADING;
			if (mVoiceIsTran && !iv_voice.isSelected()) {
				mMyCamera.stopListening();
				mVoiceIsTran = false;
			}
			mMyCamera.stopRecording();
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
			if (timerTask != null) {
				timerTask.cancel();
				timerTask = null;
			}
			iv_record_video.setSelected(false);

			Log.i("tedu", "--鱼眼直播界面     本地录像结束 结束--");
			mRecordingState = RECORDING_STATUS_NONE;
			txt_recording.setVisibility(View.INVISIBLE);
			iv_recording.setVisibility(View.INVISIBLE);
			mRecordHandler.removeCallbacksAndMessages(null);
			if (!TextUtils.isEmpty(recordFile)) {
				File file = new File(recordFile);
				if (file.length() <= 1024 && file.isFile() && file.exists()) {
					file.delete();
				}
			}
		}
	}

	private float action_down_X;
	private float action_down_Y;

	private float move_X;
	private float move_Y;
	private int xlenOld;
	private int ylenOld;
	private double nLenStart;

	// Moniter手势处理
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.monitor_live_view) {
			int nCnt = event.getPointerCount();
			if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN && 2 == nCnt) {// 两个手指
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
					mMonitor.SetZoom(true);
					mMonitor.SetZoom(true);
					mMonitor.SetZoom(true);
					mMonitor.SetZoom(true);
				} else {// 缩小
					mMonitor.SetZoom(false);
					mMonitor.SetZoom(false);
					mMonitor.SetZoom(false);
					mMonitor.SetZoom(false);

				}
				xlenOld = xlen;
				ylenOld = ylen;
				nLenStart = nLenEnd;
				return true;
			} else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP && 2 == nCnt) {
				mMonitor.setTouchMove(0);
			} else if (nCnt == 1) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					float x = event.getRawX();
					float y = event.getRawY();

					action_down_X = event.getRawX();
					action_down_Y = event.getRawY();
					mMonitor.SetCruise(false);
					monitorHandler.removeCallbacksAndMessages(null);
					mMonitor.setTouchMove(0);
					break;
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
						}, 4000);
					}
					break;
				}

			}

		}
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		RelativeLayout.LayoutParams params = null;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			misFullScreen = 1;
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			rl_top.setVisibility(View.VISIBLE);
			ll_buttom.setVisibility(View.VISIBLE);
			mIvFullScreen.setImageResource(R.drawable.full_screen);
			tv_timezone.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			iv_recording.animate().translationY(0f).start();
			txt_recording.animate().translationY(0f).start();

			if (lightModel != 0) {
				iv_land_white_light.setVisibility(View.GONE);
				iv_white_light.setVisibility(View.VISIBLE);
			}
			play_view_model.setVisibility(View.GONE);
			ll_land_top.setVisibility(View.GONE);
			mIvFullScreen.setVisibility(View.VISIBLE);
			params = new RelativeLayout.LayoutParams(getWindowManager().getDefaultDisplay().getWidth(),
					getWindowManager().getDefaultDisplay().getWidth());
			mMonitor.SetScreenSize(getWindowManager().getDefaultDisplay().getWidth(),
					getWindowManager().getDefaultDisplay().getWidth());
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			misFullScreen = 2;
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			rl_top.setVisibility(View.GONE);
			ll_buttom.setVisibility(View.GONE);
			iv_white_light.setVisibility(View.GONE);
			tv_timezone.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			int dis = HiTools.dip2px(FishEyeActivity.this, 50);
			iv_recording.animate().translationY(dis).start();
			txt_recording.animate().translationY(dis).start();

			if (lightModel != 0) {
				iv_land_white_light.setVisibility(View.VISIBLE);
			}
			mIvFullScreen.setVisibility(View.GONE);
			// ll_land_top.setVisibility(View.VISIBLE);
			play_view_model.setVisibility(View.VISIBLE);
			params = new RelativeLayout.LayoutParams(getWindowManager().getDefaultDisplay().getWidth(),
					getWindowManager().getDefaultDisplay().getHeight());
			mMonitor.SetScreenSize(getWindowManager().getDefaultDisplay().getWidth(),
					getWindowManager().getDefaultDisplay().getHeight());
		}
		if (params != null) {
			mMonitor.setLayoutParams(params);
		}
	}

	private void showAlertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(FishEyeActivity.this);
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

	@Override
	public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
		if (arg0 != mMyCamera)
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
		if (arg0 != mMyCamera)
			return;
		Message message = Message.obtain();
		message.what = HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
		message.arg1 = arg1;
		message.obj = arg0;
		mHandler.sendMessage(message);

	}

	@Override
	public void callbackPlayUTC(HiCamera arg0, int arg1) {
		if (arg0 != mMyCamera) {
			return;
		}
	}

	@Override
	public void callbackState(HiCamera arg0, int arg1, int arg2, int arg3) {
		if (mMyCamera != arg0)
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

	private Handler mRecordHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 110:
				if (iv_recording.getVisibility() == View.INVISIBLE) {
					iv_recording.setVisibility(View.VISIBLE);
				} else {
					iv_recording.setVisibility(View.INVISIBLE);
				}
				mRecordHandler.sendEmptyMessageDelayed(110, 1000);
				break;
			}
		};
	};

	private boolean isSaveSnapshot;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0X999:
				HiToast.showToast(FishEyeActivity.this, getString(R.string.failed_recording));
				break;
			case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
				handSessionState(msg);
				break;
			case HiDataValue.HANDLE_MESSAGE_PLAY_STATE:
				handPalyState(msg);
				break;
			case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
				if (msg.arg2 == 0) {// 成功
					handReceiveIoCtrlSuccess(msg);
				} else {
					switch (msg.arg1) {
					case HiChipDefines.HI_P2P_GET_ABSOLUTE_LIGHT_TYPE:
						break;
					case HiChipDefines.HI_P2P_SET_DEVICE_FISH_PARAM:
						Log.e("=======", "++++++++++fail");
						dismissjuHuaDialog();
						break;
					}

				}
				break;
			}
		}

		protected void handReceiveIoCtrlSuccess(Message msg) {
			byte[] data = msg.getData().getByteArray(HiDataValue.EXTRAS_KEY_DATA);
			switch (msg.arg1) {
			case HiChipDefines.HI_P2P_START_LIVE:
				mMonitor.setFlensType(mMyCamera.getFishModType());
				int width = Packet.byteArrayToInt_Little(data, 4);
				int heigth = Packet.byteArrayToInt_Little(data, 8);
				if (width <= 0 || heigth <= 0 || width > 5000 || heigth > 5000) {
					FishEyeActivity.this.finish();
					HiToast.showToast(FishEyeActivity.this, getString(R.string.tips_open_video_fail));
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
			case HiChipDefines.HI_P2P_SET_DEVICE_FISH_PARAM:
				dismissjuHuaDialog();
				Log.e("=======", "++++++++++success");
				break;
			}
		}

		private void handPalyState(Message msg) {
			Bundle bundle = msg.getData();
			int command = bundle.getInt("command");
			switch (command) {
			case -1:
			case -2:
				Log.i("tedu", "--setYUV  OOM啦--");
				finish();
				break;
			case ICameraPlayStateCallback.PLAY_STATE_START:
				setTime();
				dismissLoadingView();
				Bitmap frame = null;

				if (!isSaveSnapshot){
					try {
						frame = mMyCamera != null ? mMyCamera.getSnapshot_EXT(1, 640, 352) : null;
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (frame != null) {
						Bitmap bitmap = BitmapUtils.ImageCrop(frame);
						saveSnapshot(bitmap);
						isSaveSnapshot = true;
					}
				}


				break;
			// 本地录像开始
			case ICameraPlayStateCallback.PLAY_STATE_RECORDING_START:
				// Log.i("tedu", "--鱼眼直播界面 本地录像开始 开始--");
				// mRecordingState = RECORDING_STATUS_ING;
				// txt_recording.setVisibility(View.VISIBLE);
				// iv_recording.setVisibility(View.VISIBLE);
				// mRecordHandler.sendEmptyMessage(110);
				// mMonitor.setEnabled(true);
				break;
			// 本地录像结束
			case ICameraPlayStateCallback.PLAY_STATE_RECORDING_END:
				// Log.i("tedu", "--鱼眼直播界面 本地录像结束 结束--");
				// mRecordingState = RECORDING_STATUS_NONE;
				// txt_recording.setVisibility(View.INVISIBLE);
				// iv_recording.setVisibility(View.INVISIBLE);
				// mRecordHandler.removeCallbacksAndMessages(null);
				// if (!TextUtils.isEmpty(recordFile)) {
				// File file = new File(recordFile);
				// if (file.length() <= 1024 && file.isFile() && file.exists()) {
				// file.delete();
				// }
				// }
				break;
			case ICameraPlayStateCallback.PLAY_STATE_RECORD_ERROR:
				Log.i("tedu", "--鱼眼直播界面     本地录像ERROR ERROR--");
				mRecordingState = RECORDING_STATUS_NONE;
				txt_recording.setVisibility(View.INVISIBLE);
				iv_recording.setVisibility(View.INVISIBLE);
				mRecordHandler.removeCallbacksAndMessages(null);
				if (!TextUtils.isEmpty(recordFile)) {
					File file = new File(recordFile);
					if (file.length() <= 1024 && file.isFile() && file.exists()) {
						file.delete();
					}
				}
				break;
			}
		}

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
						HiTools.saveBitmap(frame, sargetFolder.getAbsolutePath() + "/" + mMyCamera.getUid());
						HiLog.v(sargetFolder.getAbsolutePath() + "/" + mMyCamera.getUid());
						mMyCamera.snapshot = frame;
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

		private void handSessionState(Message msg) {
			switch (msg.arg1) {
			case HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED:
				finish();
				showLoadingView();
				if (mMyCamera != null) {
					mMyCamera.stopLiveShow();
					if (isListening) {
						isListening = false;
					}
				}
				break;
			case HiCamera.CAMERA_CONNECTION_STATE_LOGIN:
				if (mCameraVideoQuality != mMyCamera.getVideoQuality()) {
					mMyCamera.stopRecording();
					if (timer != null) {
						timer.cancel();
						timer = null;
					}
					if (timerTask != null) {
						timerTask.cancel();
						timerTask = null;
					}
					mCameraVideoQuality = mMyCamera.getVideoQuality();
				}
				new Thread() {
					public void run() {
						mMyCamera.startLiveShow(mMyCamera.getVideoQuality(), mMonitor);
					}
				}.start();
				break;
			case HiCamera.CAMERA_CONNECTION_STATE_WRONG_PASSWORD:
				break;
			case HiCamera.CAMERA_CONNECTION_STATE_CONNECTING:
				break;
			case HiCamera.CAMERA_CHANNEL_STREAM_ERROR:
				break;

			}
		};
	};
	private int oldInstallMode;

	private void showLoadingView() {
		Animation rotateAnim = AnimationUtils.loadAnimation(FishEyeActivity.this, R.anim.rotate);
		iv_loading2.setVisibility(View.VISIBLE);
		iv_loading2.startAnimation(rotateAnim);
	}

	private void dismissLoadingView() {
		iv_loading2.clearAnimation();
		iv_loading2.setVisibility(View.GONE);
	}

	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {

		if (rl_live_view_model.getVisibility() == View.VISIBLE) {
			rl_live_view_model.setVisibility(View.GONE);
			ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f);
			scaleAnimation.setDuration(200);
			rl_live_view_model.startAnimation(scaleAnimation);
		}

		if (checkedId == R.id.rbtn_live_circle || checkedId == R.id.rbtn_circle) {
			if (mMyCamera.mInstallMode == 0) {
				mFrameMode = 1;
				Log.i("tedu", "--444    mFrameMode我变成1了--");
				mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CIRCLE, mFrameMode);
				mMonitor.mIsZoom = false;
			} else {
				mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_NEWSIDE, 1);
				mWallMode = 0;
				if (mIsCruise) {
					mMonitor.SetCruise(mIsCruise = !mIsCruise);
					iv_live_cruise.setSelected(mIsCruise);
				}
			}
			handRadioButtonCheck();
		} else if (checkedId == R.id.rbtn_cylinder || checkedId == R.id.rbtn_live_cylinder) {
			mFrameMode = 2;
			mMonitor.mIsZoom = false;
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CARTOON, 1);
			handRadioButtonCheck();
		} else if (checkedId == R.id.rbtn_live_bowl || checkedId == R.id.rbtn_bowl) {
			mFrameMode = 5;
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CARTOON, 0);// 立体碗
			handRadioButtonCheck();
		} else if (checkedId == R.id.rbtn_live_two || checkedId == R.id.rbtn_two) {
			mFrameMode = 3;
			mMonitor.mIsZoom = false;
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_COLUMN, 2);
			handRadioButtonCheck();
		} else if (checkedId == R.id.rbtn_live_four || checkedId == R.id.rbtn_four) {
			mFrameMode = 4;
			mMonitor.mIsZoom = false;
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CIRCLE, mFrameMode);
			handRadioButtonCheck();
		} else if (checkedId == R.id.rbtn_live_wall_partview || checkedId == R.id.rbtn_wall_partview) {
			mWallMode = 1;
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_SIDE, 0);
			handRadioButtonCheck();
		}
	}
}
