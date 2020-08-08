package com.hichip.hichip.activity.RF;

import com.hichip.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.main.MainActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

public class CheckRFCodeActivity extends HiActivity implements OnClickListener, ICameraIOSessionCallback {
	private TitleView mTitleView;
	private MyCamera mMyCamera;
	private Button mButCheckCode;
	private ImageView iv_radar_11,iv_radar_22;
	private String       mRFType;
	private String       mKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_rf_code);
		getIntentData();
		initView();
		setListerners();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mMyCamera != null) {
			mMyCamera.registerIOSessionListener(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mMyCamera != null) {
			mMyCamera.unregisterIOSessionListener(this);
			mHandler.removeCallbacksAndMessages(null);
			mButCheckCode.setEnabled(true);
			//mButCheckCode.setText("再次对码");
			//iv_radar_11.clearAnimation();
			iv_radar_22.clearAnimation();
		}
	}
	@Override
	protected void onStop() {
		super.onStop();
		mButCheckCode.setText("再次配对");
	}

	private void setListerners() {
		mButCheckCode.setOnClickListener(this);

	}

	private void getIntentData() {
		String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
		mRFType=getIntent().getStringExtra(HiDataValue.EXTRAS_RF_TYPE);
		mKey=getIntent().getStringExtra(RemoteControlActivity.KEY);
		if (TextUtils.isEmpty(uid)) {
			finish();
			return;
		}
		for (MyCamera camera : HiDataValue.CameraList) {
			if (uid.equals(camera.getUid())) {
				this.mMyCamera = camera;
			}
		}
	}

	private void initView() {
		mTitleView = (TitleView) findViewById(R.id.rf_code_top);
		mTitleView.setTitle("传感器配对");
		mTitleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		mTitleView.setNavigationBarButtonListener(new NavigationBarButtonListener() {
			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
					case TitleView.NAVIGATION_BUTTON_LEFT:
						CheckRFCodeActivity.this.finish();
						break;
				}
			}
		});
		mButCheckCode = (Button) findViewById(R.id.btn_check_code);
		//iv_radar_11=(ImageView) findViewById(R.id.iv_radar_11);
		iv_radar_22=(ImageView) findViewById(R.id.iv_radar_22);
	}

	private int countdownnum;


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_check_code) {
			countdownnum = 60;
			mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_CAPTURE, null);
			mButCheckCode.setEnabled(false);

			Animation animation = AnimationUtils.loadAnimation(CheckRFCodeActivity.this, R.anim.rotate_radar);
			//iv_radar_11.startAnimation(animation);
			iv_radar_22.startAnimation(animation);

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (countdownnum > 0) {
						countdownnum--;
						mButCheckCode.setText(countdownnum + "秒");
						mHandler.postDelayed(this, 1000);
					}
					if (countdownnum == 0) {
						mButCheckCode.setEnabled(true);
						mButCheckCode.setText("再次对码");
						//iv_radar_11.clearAnimation();
						iv_radar_22.clearAnimation();
					}

				}
			});
		}

	}

	@Override
	public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
		if (arg0 != mMyCamera) {
			return;
		}
		Message msg = Message.obtain();
		Bundle bundle = new Bundle();
		bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
		msg.what = HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL;
		msg.arg1 = arg1;
		msg.arg2 = arg3;
		msg.setData(bundle);
		mHandler.sendMessage(msg);

	}

	@Override
	public void receiveSessionState(HiCamera arg0, int arg1) {
		if(arg0!=mMyCamera) return;
		Message msg=Message.obtain();
		msg.what=HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
		msg.obj=arg0;
		msg.arg1=arg1;
		mHandler.sendMessage(msg);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
					if(msg.arg1==HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED){
						Intent intent=new Intent(CheckRFCodeActivity.this,MainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					break;
				case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
					byte[] data=msg.getData().getByteArray(HiDataValue.EXTRAS_KEY_DATA);
					if (msg.arg2 == 0) {// success
						handleSuccess(msg, data);
					} else { // fail
					}

					break;

			}
		}

		private void handleSuccess(Message msg, byte[] data) {
			switch (msg.arg1) {
				case HiChipDefines.HI_P2P_IPCRF_CAPTURE:
					if(data!=null&&!TextUtils.isEmpty(new String(data).trim())){
						HiChipDefines.HI_P2P_IPCRF_Code md_param_temp = new HiChipDefines.HI_P2P_IPCRF_Code(data);
						byte[] code=md_param_temp.sRfCode;
						Intent intent = new Intent(CheckRFCodeActivity.this, SetUpAndAddRFActivity.class);
						intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
						intent.putExtra(HiDataValue.EXTRAS_RF_TYPE, mRFType);
						intent.putExtra(HiDataValue.EXTRAS_KEY_DATA, code);
						intent.putExtra(RemoteControlActivity.KEY, mKey);
						startActivity(intent);
					}
					break;
			}
		};
	};

}
