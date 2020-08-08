package com.hichip.hichip.activity.WallMounted;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.hichip.R;
import com.hichip.base.HiLog;
import com.hichip.base.HiThread;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.callback.ICameraPlayStateCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.control.HiGLMonitor;
import com.hichip.tools.Packet;
import com.hichip.thecamhi.activity.VideoOnlineActivity;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.MyLiveViewGLMonitor;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.utils.SharePreUtils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class WallMountedOnlineActivity extends HiActivity implements ICameraIOSessionCallback, ICameraPlayStateCallback, OnTouchListener, OnClickListener {
	private final static int HANDLE_MESSAGE_PROGRESSBAR_RUN = 0x90000002;
	private final static int HANDLE_MESSAGE_SEEKBAR_RUN = 0x90000003;
	public final static short HI_P2P_PB_PLAY = 1;
	public final static short HI_P2P_PB_STOP = 2;
	public final static short HI_P2P_PB_PAUSE = 3;
	public final static short HI_P2P_PB_SETPOS = 4;
	public final static short HI_P2P_PB_GETPOS = 5;
	private int video_width;
	private int video_height;
	private ProgressThread pthread = null;
	private ProgressBar prs_loading;
	private ImageView img_shade;
	private byte[] startTime;
	private byte[] oldStartTime;
	private MyLiveViewGLMonitor mMonitor;
	private MyCamera mCamera;
	private SeekBar prs_playing;
	private boolean mVisible = true;
	private long playback_time;
	private long startTimeLong;
	private long endTimeLong;
	private int progressTime;
	private short model;// PLAY=1,STOP=2,PAUSE=3,SETPOS=4,GETPOS=5
	private RelativeLayout playback_view_screen;
	private boolean visible = true;
	private ImageView play_btn_playback_online, btn_return;
	private ConnectionChangeReceiver myReceiver;
	private boolean isPlaying = false;
	public LinearLayout mllPlay, ll_top;
	private TextView mTvStartTime, mTvEndTime;
	private final static SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
	private SimpleDateFormat sdf_time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private boolean mIsDrag = false;
	private LinearLayout mLlCurrPro;
	private TextView mTvCurrPro, mTvPrecent;
	private boolean mIsEnd = false;
	private String str;
	public RadioButton rbtn_circle;
	//private LinearLayout rl_wall_mounted_guide;
	private TextView tv_timezone;
	
	private boolean isCanDragStart = true;
	private boolean isAfterEndDrag;
	private int pre;

	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		myReceiver = new ConnectionChangeReceiver();
		this.registerReceiver(myReceiver, filter);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_wallmounted_online);

	
		
		Bundle bundle = this.getIntent().getExtras();
		str = bundle.getString("title");
		String uid = bundle.getString(HiDataValue.EXTRAS_KEY_UID);
		byte[] b_startTime = bundle.getByteArray("st");
		oldStartTime = new byte[8];
		System.arraycopy(b_startTime, 0, oldStartTime, 0, 8);
		playback_time = bundle.getLong("pb_time");

		startTimeLong = bundle.getLong(VideoOnlineActivity.VIDEO_PLAYBACK_START_TIME);
		endTimeLong = bundle.getLong(VideoOnlineActivity.VIDEO_PLAYBACK_END_TIME);

		for (MyCamera camera : HiDataValue.CameraList) {
			if (camera.getUid().equals(uid)) {
				mCamera = camera;
				break;
			}
		}
		initView();
		setListerners();
		showLoadingShade();
		mCamera.registerIOSessionListener(this);
		mCamera.registerPlayStateListener(WallMountedOnlineActivity.this);

		if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_QUERY_START_NODST)) {
			startTime = oldStartTime;
		} else {
			if (mCamera.getSummerTimer()) {
				HiChipDefines.STimeDay newTime = new HiChipDefines.STimeDay(oldStartTime, 0);
				newTime.resetData(-1);
				startTime = newTime.parseContent();
			} else {
				startTime = oldStartTime;
			}
		}
		startPlayBack();
		model = HI_P2P_PB_PLAY;
	}

	private void setListerners() {
		mMonitor.setOnTouchListener(this);
		ll_top.setOnClickListener(this);
		btn_return.setOnClickListener(this);
		//rl_wall_mounted_guide.setOnClickListener(this);
	}

	private void startPlayBack() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				mCamera.startPlayback(new HiChipDefines.STimeDay(startTime, 0), mMonitor);
			}
		}.start();
	}

	private void initView() {

		mMonitor = (MyLiveViewGLMonitor) findViewById(R.id.monitor_playback_view);
		mMonitor.setCamera(mCamera);
		float cirx=SharePreUtils.getFloat("chche", this, mCamera.getUid() + "xcircle");
		float ciry=SharePreUtils.getFloat("chche", this, mCamera.getUid() + "ycircle");
		float cirr=SharePreUtils.getFloat("chche", this, mCamera.getUid() + "rcircle");
		mMonitor.SetCirInfo(cirx, ciry, cirr);
//		mMonitor.SetViewType_EXT(mMonitor.TOP_ALL_VIEW, 2);
		mMonitor.SetViewType_EXT(mMonitor.TOP_ALL_VIEW, mCamera.getFishModType());
		mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_SIDE, 1);
		mMonitor.SetScreenSize(getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight());
		rbtn_circle = (RadioButton) findViewById(R.id.rbtn_circle);
		mCamera.setLiveShowMonitor(mMonitor);
		prs_loading = (ProgressBar) findViewById(R.id.prs_loading);
		img_shade = (ImageView) findViewById(R.id.img_shade);
		mllPlay = (LinearLayout) findViewById(R.id.rl_play);
		mTvStartTime = (TextView) findViewById(R.id.tv_start_time);
		mTvEndTime = (TextView) findViewById(R.id.tv_end_time);
		mTvEndTime.setText(sdf.format(new Date(endTimeLong - startTimeLong)));
		progressTime = (int) ((endTimeLong - startTimeLong) / 1000);
		prs_playing = (SeekBar) findViewById(R.id.prs_playing);
		prs_playing.setMax(progressTime);
		prs_playing.setProgress(0);
		mLlCurrPro = (LinearLayout) findViewById(R.id.ll_cureent_progress);
		mTvCurrPro = (TextView) findViewById(R.id.tv_current_pro);
		mTvPrecent = (TextView) findViewById(R.id.tv_precent);
		ll_top = (LinearLayout) findViewById(R.id.ll_top);
		btn_return = (ImageView) findViewById(R.id.btn_return);
		//rl_wall_mounted_guide = (LinearLayout) findViewById(R.id.rl_wall_mounted_guide);
		tv_timezone=(TextView) findViewById(R.id.tv_timezone);


		// 是否显示引导界面
		boolean isFirst = SharePreUtils.getBoolean("cache", this, mCamera.getUid() + "pb");
		//rl_wall_mounted_guide.setVisibility(isFirst ? View.GONE : View.VISIBLE);
		prs_playing.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

				int count = seekBar.getProgress();
				 pre = count * 100 / progressTime;
				
				if (mIsEnd) {
					if (!play_btn_playback_online.isEnabled()||!isCanDragStart) {
						mIsDrag = false;
						mLlCurrPro.setVisibility(View.GONE);
						return;
					}
					isCanDragStart =false;
					play_btn_playback_online.setEnabled(false);
					startPlayBack();
					isAfterEndDrag = true;
				} else {
					play_btn_playback_online.setEnabled(false);
				if (pre < 1) {  // 1.文件的时长的1% （0-6） 秒重头开始播放 下面要的是整数
					if (!mIsEnd) {
						mCamera.stopPlayback();

						startPlayBack();
					}
				} else {
					//mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_POS_SET, HiChipDefines.HI_P2P_PB_SETPOS_REQ.parseContent(0, pre, startTime));
				
					if(mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_POS_SET_NEW))
						mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_POS_SET_NEW, HiChipDefines.HI_P2P_PB_SETPOS_REQ.parseContent(0, pre, startTime));
					else
					mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_POS_SET, HiChipDefines.HI_P2P_PB_SETPOS_REQ.parseContent(0, pre, startTime));
				}
				}
				model = HI_P2P_PB_PAUSE;
				mIsDrag = false;
				mLlCurrPro.setVisibility(View.GONE);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				mIsDrag = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (mIsDrag) {
					mLlCurrPro.setVisibility(View.VISIBLE);
					double dou = ((double) progress / progressTime) * 100;
					int rate = (int) Math.round(dou);
					mTvPrecent.setText(rate + "%");
				} else {
					mLlCurrPro.setVisibility(View.GONE);
				}

			}
		});

		playback_view_screen = (RelativeLayout) findViewById(R.id.playback_view_screen);
		playback_view_screen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (model == 0) {
					return;
				}
				visible = !visible;
				mllPlay.setVisibility(visible ? View.VISIBLE : View.GONE);
			}
		});

		play_btn_playback_online = (ImageView) findViewById(R.id.play_btn_playback_online);
		play_btn_playback_online.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (mCamera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
//					play_btn_playback_online.setClickable(false);
					if (mIsEnd) {
						play_btn_playback_online.setEnabled(false);
						startPlayBack();
					} else {
						if (isPlaying) {
							play_btn_playback_online.setSelected(true);
						} else {
							play_btn_playback_online.setSelected(false);
						}
						isPlaying = !isPlaying;
						//mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_PLAY_CONTROL, HiChipDefines.HI_P2P_S_PB_PLAY_REQ.parseContent(0, HI_P2P_PB_PAUSE, startTime));
						if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_PLAY_CONTROL_NEW)) {
							mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_PLAY_CONTROL_NEW,
									HiChipDefines.HI_P2P_S_PB_PLAY_REQ.parseContent(0, HI_P2P_PB_PAUSE, startTime));
						} else if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_PLAY_CONTROL_EXT)) {
							mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_PLAY_CONTROL_EXT,
									HiChipDefines.HI_P2P_S_PB_PLAY_REQ.parseContent(0, HI_P2P_PB_PAUSE, startTime));
						} else {
							mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_PLAY_CONTROL,
									HiChipDefines.HI_P2P_S_PB_PLAY_REQ.parseContent(0, HI_P2P_PB_PAUSE, startTime));
						}
					}
				}
			}
		});
	}

	private void setImageVisible(boolean b) {
		if (b) {
			prs_playing.setVisibility(View.VISIBLE);
			play_btn_playback_online.setVisibility(View.VISIBLE);

		} else {

			play_btn_playback_online.setVisibility(View.GONE);
			prs_playing.setVisibility(View.GONE);
		}
	}

	private void showLoadingShade() {

		prs_loading.setMax(100);
		prs_loading.setProgress(10);
		pthread = new ProgressThread();
		pthread.startThread();
	}

	private void displayLoadingShade() {
		if (pthread != null)
			pthread.stopThread();
		pthread = null;
		prs_loading.setVisibility(View.GONE);
		img_shade.setVisibility(View.GONE);

		visible = true;
		setImageVisible(visible);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mCamera != null) {
			mCamera.registerIOSessionListener(this);
			mCamera.registerPlayStateListener(this);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mCamera != null) {

			if (model != 0) {
				model = 0;
				oldTime = 0;
			}

			mCamera.stopPlayback();
			mCamera.unregisterIOSessionListener(this);
			mCamera.unregisterPlayStateListener(this);
			HiLog.e("unregister");

		} else {
			HiLog.e("camera == null");
		}

		if (pthread != null) {

			pthread.stopThread();
			pthread = null;

		}

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		finish();
		
	}
	
	@Override
	public void onBackPressed() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		finish();
	
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myReceiver != null) {
			unregisterReceiver(myReceiver);
		}
	}

	private class ProgressThread extends HiThread {
		public void run() {
			while (isRunning) {
				sleep(100);
				Message msg = handler.obtainMessage();
				msg.what = HANDLE_MESSAGE_PROGRESSBAR_RUN;
				handler.sendMessage(msg);
			}
		}
	}

	@Override
	public void receiveIOCtrlData(HiCamera camera, int arg1, byte[] arg2, int arg3) {

		if (mCamera != camera)
			return;

		Bundle bundle = new Bundle();
		bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
		Message msg = handler.obtainMessage();
		msg.what = arg1;
		msg.arg2 = arg3;
		msg.setData(bundle);
		handler.sendMessage(msg);

	}

	@Override
	public void receiveSessionState(HiCamera arg0, int arg1) {
		if (arg0 != mCamera || mCamera == null) {
			return;
		}
		Message message = Message.obtain();
		message.what = HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
		message.arg1 = arg1;
		message.obj = arg0;
		handler.sendMessage(message);

	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
				MyCamera camera = (MyCamera) msg.obj;
				switch (msg.arg1) {
				case HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED:
				case HiCamera.CAMERA_CHANNEL_STREAM_ERROR:
					play_btn_playback_online.setEnabled(true);
					isCanDragStart = true;
					if (camera != null) {
						camera.stopPlayback();
					}
					HiToast.showToast(WallMountedOnlineActivity.this, getString(R.string.tips_network_disconnect));
					NetworkError();
					break;
				}
				break;

			case ICameraPlayStateCallback.PLAY_STATE_START:

				
				tv_timezone.setText(sdf_time.format(new Date(startTimeLong)));
				isPlaying = true;
				play_btn_playback_online.setEnabled(true);
				isCanDragStart = true;
				model = HI_P2P_PB_PLAY;
				mIsEnd = false;
				video_width = msg.arg1;
				video_height = msg.arg2;
				play_btn_playback_online.setSelected(false);
				resetMonitorSize();
				break;
			case ICameraPlayStateCallback.PLAY_STATE_EDN:
				isPlaying = false;
				mIsEnd = true;
				model = HI_P2P_PB_STOP;
				play_btn_playback_online.setSelected(true);
				play_btn_playback_online.setEnabled(true);
				isCanDragStart = true;
				mTvStartTime.setText(sdf.format(new Date(endTimeLong - startTimeLong)));
				tv_timezone.setText(sdf_time.format(new Date(endTimeLong)));
				prs_playing.setProgress(progressTime);
				mCamera.stopPlayback();
				HiToast.showToast(WallMountedOnlineActivity.this, getString(R.string.tips_stop_video));
				break;
			case ICameraPlayStateCallback.PLAY_STATE_POS:
				break;
			case HANDLE_MESSAGE_PROGRESSBAR_RUN:
				int cur = prs_loading.getProgress();
				if (cur >= 100) {
					prs_loading.setProgress(10);
				} else {
					prs_loading.setProgress(cur + 8);
				}
				model = HI_P2P_PB_PLAY;
				break;
			case HANDLE_MESSAGE_SEEKBAR_RUN:
				if(!play_btn_playback_online.isEnabled())
                    play_btn_playback_online.setEnabled(true);
				isCanDragStart = true;
				if (!mIsDrag) {
					prs_playing.setProgress(msg.arg1);
				}
				mTvStartTime.setText(sdf.format(new Date(msg.arg1 * 1000)));
				long time=startTimeLong+msg.arg1 * 1000;
				tv_timezone.setText(sdf_time.format(new Date(time)));
				break;
			case HiChipDefines.HI_P2P_PB_POS_SET:
			case HiChipDefines.HI_P2P_PB_POS_SET_NEW:
				model = HI_P2P_PB_PLAY;
				if (!isPlaying && !mIsEnd) {
					// pos_set 有可能先回调,STATE_START也有可能先回调,两种可能都判断了
				//	mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_PLAY_CONTROL, HiChipDefines.HI_P2P_S_PB_PLAY_REQ.parseContent(0, HI_P2P_PB_PAUSE, startTime));
					
					if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_PLAY_CONTROL_NEW)) {
						mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_PLAY_CONTROL_NEW,
								HiChipDefines.HI_P2P_S_PB_PLAY_REQ.parseContent(0, HI_P2P_PB_PAUSE, startTime));
					} else if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_PLAY_CONTROL_EXT)) {
						mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_PLAY_CONTROL_EXT,
								HiChipDefines.HI_P2P_S_PB_PLAY_REQ.parseContent(0, HI_P2P_PB_PAUSE, startTime));
					} else {
						mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_PLAY_CONTROL,
								HiChipDefines.HI_P2P_S_PB_PLAY_REQ.parseContent(0, HI_P2P_PB_PAUSE, startTime));
					}
					
					play_btn_playback_online.setSelected(false);
					isPlaying = !isPlaying;
				}
				break;
			case HiChipDefines.HI_P2P_PB_PLAY_CONTROL:// stopPlayback
			case HiChipDefines.HI_P2P_PB_PLAY_CONTROL_NEW:// stopPlayback
			case HiChipDefines.HI_P2P_PB_PLAY_CONTROL_EXT:// stopPlayback
														// startPlayback
														// 都会回调这个CONTROL
														// *****注意******
				if (msg.arg2 == 0) {
					if (isAfterEndDrag) {
						isAfterEndDrag = false;
						if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_POS_SET_NEW))
							mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_POS_SET_NEW,
									HiChipDefines.HI_P2P_PB_SETPOS_REQ.parseContent(0, pre, startTime));
						else
							mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_POS_SET,
									HiChipDefines.HI_P2P_PB_SETPOS_REQ.parseContent(0, pre, startTime));
					}
					byte[] data = msg.getData().getByteArray(HiDataValue.EXTRAS_KEY_DATA);
					int width = Packet.byteArrayToInt_Little(data, 0);
					int height = Packet.byteArrayToInt_Little(data, 4);
					int commd = Packet.byteArrayToInt_Little(data, 12);
					if (commd == 1) {
						if (width > 0 && height > 0 && height < 5000 && width < 5000) {
						//	play_btn_playback_online.setClickable(true);
						} else {
							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
							WallMountedOnlineActivity.this.finish();
							HiToast.showToast(WallMountedOnlineActivity.this, getString(R.string.tips_open_video_fail));
						}
					} 
//					else
//						play_btn_playback_online.setClickable(true);
				} else {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					WallMountedOnlineActivity.this.finish();
					HiToast.showToast(WallMountedOnlineActivity.this, getString(R.string.tips_open_video_fail));
				}

				break;

			}
		}

	};

	private void resetMonitorSize() {

		if (video_width == 0 || video_height == 0) {
			return;
		}
		displayLoadingShade();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screen_width = dm.widthPixels;
		int screen_height = dm.heightPixels;
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(screen_width, screen_height);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		mMonitor.setLayoutParams(lp);
	}

	long oldTime;

	@Override
	public void callbackPlayUTC(HiCamera camera, int timeInteger) {
		// +++
		if (mCamera != camera || model == HI_P2P_PB_PAUSE || model == 0)
			return;

		if (oldTime == 0) {
			oldTime = (long) timeInteger;
		}
		long sub = (long) timeInteger - oldTime;
		int step = (int) (sub / 1000);
		Message msg = handler.obtainMessage();
		msg.what = HANDLE_MESSAGE_SEEKBAR_RUN;
		msg.arg1 = step;
		handler.sendMessage(msg);
	}

	@Override
	public void callbackState(HiCamera camera, int arg1, int arg2, int arg3) {
		if (mCamera != camera)
			return;

		if (arg1 == ICameraPlayStateCallback.PLAY_STATE_START) {
			HiLog.e("state=PLAY_STATE_START");
		}

		Message msg = handler.obtainMessage();
		msg.what = arg1;
		msg.arg1 = arg2;
		msg.arg2 = arg3;
		handler.sendMessage(msg);

	}

	public void NetworkError() {
		showAlertnew(null, null, getString(R.string.disconnect), getString(R.string.finish), null, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					WallMountedOnlineActivity.this.finish();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					WallMountedOnlineActivity.this.finish();
					break;
				}

			}
		});
	}

	public class ConnectionChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mobNetInfo != null && !mobNetInfo.isConnected() && wifiNetInfo != null && !wifiNetInfo.isConnected()) {
				HiToast.showToast(context, getString(R.string.tips_wifi_connect_failed));
				if (mCamera != null) {
					mCamera.stopPlayback();
				}
				NetworkError();
				return;
			}
		}
	}

	private float action_down_x;
	private float action_down_y;

	float lastX;
	float lastY;

	int xlenOld;
	int ylenOld;

	float move_x;
	float move_y;

	public float left;
	public float width;
	public float height;
	public float bottom;

	double nLenStart = 0;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.monitor_playback_view) {
			int nCnt = event.getPointerCount();
			if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN && 2 == nCnt) {
				mMonitor.setTouchMove(2);
				for (int i = 0; i < nCnt; i++) {
					float x = event.getX(i);
					float y = event.getY(i);
					Point pt = new Point((int) x, (int) y);
				}
				xlenOld = Math.abs((int) event.getX(0) - (int) event.getX(1));
				ylenOld = Math.abs((int) event.getY(0) - (int) event.getY(1));
				nLenStart = Math.sqrt((double) xlenOld * xlenOld + (double) ylenOld * ylenOld);

			} else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE && 2 == nCnt) {
				mMonitor.setTouchMove(2);
				for (int i = 0; i < nCnt; i++) {
					float x = event.getX(i);
					float y = event.getY(i);
					Point pt = new Point((int) x, (int) y);
				}
				int xlen = Math.abs((int) event.getX(0) - (int) event.getX(1));
				int ylen = Math.abs((int) event.getY(0) - (int) event.getY(1));

				int moveX = Math.abs(xlen - xlenOld);
				int moveY = Math.abs(ylen - ylenOld);

				double nLenEnd = Math.sqrt((double) xlen * xlen + (double) ylen * ylen);
				if (moveX < 20 && moveY < 20) {
					return false;
				}
				if (nLenEnd > nLenStart) {
					resetMonitorSize(true, nLenEnd);
				} else {
					resetMonitorSize(false, nLenEnd);
				}

				xlenOld = xlen;
				ylenOld = ylen;
				nLenStart = nLenEnd;
				return true;
			} else if (nCnt == 1) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mMonitor.setTouchMove(0);
					if (model == 0) {
						return false;
					}
					break;
				case MotionEvent.ACTION_MOVE:
					if (mMonitor.getTouchMove() != 0)
						break;
					if (Math.abs(move_x - action_down_x) > 40 || Math.abs(move_y - action_down_y) > 40) {
						mMonitor.setTouchMove(1);
					}
					break;
				case MotionEvent.ACTION_UP:
					break;
				}
			}
		}
		return false;
	}

	int moveX;
	int moveY;

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_return) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			finish();
		} else if (id == R.id.rl_wall_mounted_guide) {//rl_wall_mounted_guide.setVisibility(View.GONE);
			SharePreUtils.putBoolean("cache", this, mCamera.getUid() + "pb", true);
		}
	}
	
	
	
	private void resetMonitorSize(boolean large, double move) {
		if (mMonitor.height == 0 && mMonitor.width == 0) {
			initMatrix((int) mMonitor.screen_width, (int) mMonitor.screen_height);
		}
		moveX = (int) (move / 2);
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
		if (mMonitor.left > 0 || mMonitor.width < (int) mMonitor.screen_width || mMonitor.height < (int) mMonitor.screen_height || mMonitor.bottom > 0) {
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
	
}
