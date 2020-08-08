package com.hichip.hichip.activity.WallMounted;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.hichip.R;
import com.hichip.base.HiLog;
import com.hichip.callback.PlayLocalFileCallback;
import com.hichip.control.HiGLMonitor;
import com.hichip.sdk.PlayLocal;
import com.hichip.thecamhi.activity.VideoLocalActivity;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.MyLiveViewGLMonitor;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.utils.SharePreUtils;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class WallMountedPlayLocalActivity extends HiActivity implements PlayLocalFileCallback, OnClickListener, OnTouchListener {
	private String filePath;
	private MyLiveViewGLMonitor mMonitor;
	private PlayLocal playLocal;
	private ImageView mIvPausePlay, mIvFastforward, btn_return;
	private TextView mTvSpeed;
	private int mSpeed = 0; // 0——没有倍数 2——二倍 4——四倍 8——八倍;
	private boolean mIsEnd = false;
	private LinearLayout mLlCurrPro;
	private TextView mTvCurrPro, mTvDuraTime;
	public LinearLayout mLlPlay, ll_top;
	private MyCamera mMyCamera;
	private int screen_width;
	private int screen_height;
	private String title;
	//private LinearLayout rl_wall_mounted_guide;
	private String strat_time;
	private TextView tv_timezone;
	private SimpleDateFormat sdf_time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 设置无标题
		setContentView(R.layout.activity_wall_mounted_playlocal);
		getIntentData();
		initViewAndData();
		setListeners();
	}

	private void setListeners() {
		mMonitor.setOnTouchListener(this);
		mIvPausePlay.setOnClickListener(this);
		mIvFastforward.setOnClickListener(this);
		btn_return.setOnClickListener(this);
		//rl_wall_mounted_guide.setOnClickListener(this);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int count = seekBar.getProgress();
				final int pre = count / 1000;
				if (playLocal != null) {
					playLocal.PlayLocal_Speed(0, 0);
					// 只传时间,不传百分比了
					if (mIsEnd) {
						startVideoPath();
						playLocal.setLiveShowMonitor(mMonitor);
					}else {
						mIsPalying = true;
						mIvPausePlay.setSelected(false);
						playLocal.PlayLocal_Resume();
					}
					playLocal.PlayLocal_Seek(pre, false);
				}
				mIsDrag = false;
				mTvSpeed.setText(" ");
				mSpeed = 0;
				mLlCurrPro.setVisibility(View.GONE);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				mIsDrag = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (mIsDrag) {
					if (mProgressTime > 0) {
						int count = seekBar.getProgress();
						int pre = count / 1000;
						if (playLocal != null) {
							playLocal.PlayLocal_Speed(0, 0);
							// 只传时间,不传百分比了
							playLocal.PlayLocal_Seek(pre, true);
						}
					}
					mLlCurrPro.setVisibility(View.VISIBLE);
					mTvCurrPro.setText(sdf.format(new Date(progress)));
					mTvDuraTime.setText(sdf.format(new Date(seekBar.getMax())));
				} else {
					mLlCurrPro.setVisibility(View.GONE);
				}
			}
		});

	}

	private void initViewAndData() {
		mMonitor = (MyLiveViewGLMonitor) findViewById(R.id.monitor_playback_local);
		mMonitor.setCamera(mMyCamera);
		float cirx=SharePreUtils.getFloat("chche", this, mMyCamera.getUid() + "xcircle");
		float ciry=SharePreUtils.getFloat("chche", this, mMyCamera.getUid() + "ycircle");
		float cirr=SharePreUtils.getFloat("chche", this, mMyCamera.getUid() + "rcircle");
		mMonitor.SetCirInfo(cirx, ciry, cirr);
//		mMonitor.SetViewType_EXT(mMonitor.TOP_ALL_VIEW, 2);
		mMonitor.SetViewType_EXT(mMonitor.TOP_ALL_VIEW, mMyCamera.getFishModType());
		mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_SIDE, 1);
		mMonitor.SetScreenSize(getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight());
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screen_width = dm.widthPixels;
		screen_height = dm.heightPixels;
		mTvTotalTime = (TextView) findViewById(R.id.tv_total);
		mTvCurrent = (TextView) findViewById(R.id.tv_current);
		mSeekBar = (SeekBar) findViewById(R.id.sb_playing);
		mIvPausePlay = (ImageView) findViewById(R.id.iv_pause_play);
		mIvFastforward = (ImageView) findViewById(R.id.iv_fastforward);
		mTvSpeed = (TextView) findViewById(R.id.tv_speed);
		mLlCurrPro = (LinearLayout) findViewById(R.id.ll_play_local_pro);
		mTvCurrPro = (TextView) findViewById(R.id.tv_play_loca_current_pro);
		mTvDuraTime = (TextView) findViewById(R.id.tv_play_loca_druation_tim);
		mLlPlay = (LinearLayout) findViewById(R.id.ll_playlocal);
		ll_top = (LinearLayout) findViewById(R.id.ll_top);
		btn_return = (ImageView) findViewById(R.id.btn_return);
		//rl_wall_mounted_guide = (LinearLayout) findViewById(R.id.rl_wall_mounted_guide);
		tv_timezone=(TextView) findViewById(R.id.tv_timezone);


		// 是否显示引导界面
		boolean isFirst = SharePreUtils.getBoolean("cache", this, mMyCamera.getUid() + "pb");
		//rl_wall_mounted_guide.setVisibility(isFirst ? View.GONE : View.VISIBLE);

		playLocal = new PlayLocal();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (playLocal != null) {
			playLocal.registerPlayLocalStateListener(this);
			// 设置显示窗口
			playLocal.setLiveShowMonitor(mMonitor);
			if (!filePath.isEmpty()) {
				startVideoPath();
			}

		}
	}

	private void startVideoPath() {
		// 1 表示打开视频失败, 0表示打开视频成功
		if (HiTools.isSDCardExist()) {
			int playstate = playLocal.StartPlayLocal(filePath);
			if (playstate != 0) {
				HiToast.showToast(this, getString(R.string.tips_open_video_fail));
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
				
						WallMountedPlayLocalActivity.this.finish();
					}
				}, 1000);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (playLocal != null) {
			playLocal.unregisterPlayLocalStateListener(this);
			playLocal.StopPlayLocal();
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		finish();
		
	}
	
	@Override
	public void onBackPressed() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		finish();
		
	}

	private void getIntentData() {
		Bundle bundle = getIntent().getExtras();

		String uid = bundle.getString(HiDataValue.EXTRAS_KEY_UID);
		strat_time=bundle.getString("strat_time");
		if (TextUtils.isEmpty(uid))		
			finish();
		for (MyCamera camera : HiDataValue.CameraList) {
			if (camera.getUid().equalsIgnoreCase(uid)) {
				mMyCamera = camera;
				break;
			}
		}

		filePath = bundle.getString(VideoLocalActivity.FILE_PATH);
		title = filePath.substring(filePath.lastIndexOf("/") + 1);
	}

	/**
	 * 视频宽，高，文件时长 ，文件播放进度，音频类型，播放状态
	 */
	@Override
	public void callbackplaylocal(int width, int height, int filetime, long cursec, int audiotype, int state) {
		Message msg = Message.obtain();
		if (cursec != 0) {
			if (mFirstTime == 0) {
				mFirstTime = cursec; // 记录第一次播放进度的时间
			}
			long sub = cursec - mFirstTime;// 播放时长
			if (sub > 1000) {
				msg.what = HANDLE_MESSAGE_SEEKBAR_RUN;
				msg.arg1 = (int) sub;
				mHandler.sendMessage(msg);
			}
		}
		switch (state) {
		case PlayLocalFileCallback.PLAYLOCAL_STATE_OPEN:
			msg.what = HANDLE_MESSAGE_SEEKBAR_START;
			msg.arg1 = filetime; // 单位是秒,时间转换时要乘以1000
			mHandler.sendMessage(msg);
			break;
		case PlayLocalFileCallback.PLAY_STATE_END:
			mHandler.sendEmptyMessage(HANDLE_MESSAGE_SEEKBAR_END);
			break;
		case PlayLocalFileCallback.PLAYLOCAL_STATE_ING:
			break;
		case PlayLocalFileCallback.PLAYLOCAL_STATE_ERROR:
			HiToast.showToast(WallMountedPlayLocalActivity.this, getString(R.string.data_parsing_error));
			break;
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_MESSAGE_SEEKBAR_START:
				tv_timezone.setText(strat_time);
				resetViewMonitor();
				mIsEnd = false;
				mIsPalying = true;
				mIvPausePlay.setSelected(false);
				mTvTotalTime.setText(sdf.format(new Date(msg.arg1 * 1000)));
				mSeekBar.setMax(msg.arg1 * 1000);
				mProgressTime = msg.arg1 * 1000;
				break;
			case HANDLE_MESSAGE_SEEKBAR_RUN:
				if (!mIsDrag) {
					mSeekBar.setProgress(msg.arg1);
				}
				mTvCurrent.setText(sdf.format(new Date(msg.arg1)));
				
				Date date;
				try {
					date = sdf_time.parse(strat_time);
					long time=date.getTime()+msg.arg1;
					tv_timezone.setText(sdf_time.format(new Date(time)));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				break;
			case HANDLE_MESSAGE_SEEKBAR_END:
				mIsEnd = true;
				mIsPalying = false;
				mSeekBar.setProgress(mProgressTime);
				mTvCurrent.setText(sdf.format(new Date(mProgressTime)));
				mIvPausePlay.setSelected(true);
				playLocal.StopPlayLocal();// ***
				mTvSpeed.setText(" ");
				mSpeed = 0;
				HiToast.showToast(WallMountedPlayLocalActivity.this, getString(R.string.tips_stop_video));
				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.pb_local_exit) {
			finish();
		} else if (id == R.id.iv_pause_play) {
			if (mIsEnd) {
				playLocal.setLiveShowMonitor(mMonitor);
				// 1 表示打开视频失败, 0表示打开视频成功
				int playstate = playLocal.StartPlayLocal(filePath);
				if (playstate != 0) {
					HiToast.showToast(WallMountedPlayLocalActivity.this, getString(R.string.tips_open_video_fail));

					finish();
				} else {
					handSpeed();
				}
			} else {
				if (mIsPalying) {
					playLocal.PlayLocal_pause();
				} else {
					playLocal.PlayLocal_Resume();
				}
				mIvPausePlay.setSelected(mIsPalying);
				mIsPalying = !mIsPalying;
			}
		} else if (id == R.id.iv_fastforward) {
			handleFast();
		} else if (id == R.id.btn_return) {
			WallMountedPlayLocalActivity.this.finish();
		} else if (id == R.id.rl_wall_mounted_guide) {//rl_wall_mounted_guide.setVisibility(View.GONE);
			SharePreUtils.putBoolean("cache", this, mMyCamera.getUid() + "pb", true);
		}

	}

	protected void resetViewMonitor() {
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(screen_width, screen_height);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		mMonitor.setLayoutParams(lp);

	}

	private void handSpeed() {
		switch (mSpeed) {
		case 2:
			playLocal.PlayLocal_Speed(3, 30);
			break;
		case 4:
			playLocal.PlayLocal_Speed(4, 15);
			break;
		case 8:
			playLocal.PlayLocal_Speed(12, 15);
			break;
		case 16:
			playLocal.PlayLocal_Speed(15, 15);
			break;
		case 0:
			playLocal.PlayLocal_Speed(0, 0);
			break;
		}
	}

	private void handleFast() {
		switch (mSpeed) {
		case 0:
			mSpeed = 2;
			mTvSpeed.setText("X 2");
			playLocal.PlayLocal_Speed(3, 30);
			break;
		case 2:
			mSpeed = 4;
			mTvSpeed.setText("X 4");
			playLocal.PlayLocal_Speed(4, 15);
			break;
		case 4:
			mSpeed = 8;
			mTvSpeed.setText("X 8");
			playLocal.PlayLocal_Speed(12, 15);
			break;
		case 8:
			mSpeed = 16;
			mTvSpeed.setText("X 16");
			playLocal.PlayLocal_Speed(15, 15);
			break;
		case 16:
			mSpeed = 0;
			mTvSpeed.setText(" ");
			playLocal.PlayLocal_Speed(0, 0);
			break;
		}
	}

	private long mFirstTime;
	private final static int HANDLE_MESSAGE_SEEKBAR_START = 0x90000002;
	private final static int HANDLE_MESSAGE_SEEKBAR_RUN = 0x90000003;
	private final static int HANDLE_MESSAGE_SEEKBAR_END = 0x90000004;
	private boolean mIsPalying = false;
	private TextView mTvTotalTime, mTvCurrent;
	private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
	private boolean mIsDrag = false;
	private SeekBar mSeekBar;
	private int mProgressTime;

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
	int moveX;
	int moveY;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.monitor_playback_local) {
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
				// mMonitor.setState(3);
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
					break;
				case MotionEvent.ACTION_MOVE:
					if (mMonitor.getTouchMove() != 0)
						break;
					if (Math.abs(move_x - action_down_x) > 40 || Math.abs(move_y - action_down_y) > 40) {
						mMonitor.setTouchMove(1);
					}
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

		Log.i("tedu", "--mMonitor.width--:"+mMonitor.width+"--mMonitor.height--:"+mMonitor.height);
		mMonitor.setMatrix(mMonitor.left, mMonitor.bottom, mMonitor.width, mMonitor.height);
	}
	
	private void initMatrix(int screen_width, int screen_height) {
		mMonitor.left = 0;
		mMonitor.bottom = 0;
		mMonitor.width = screen_width;
		mMonitor.height = screen_height;
	}

}
