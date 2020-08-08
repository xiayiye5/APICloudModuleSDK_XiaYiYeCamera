package com.hichip.thecamhi.activity.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import com.hichip.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.hichip.widget.SwitchButton;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;

/**
 * 报警移动侦测 Activity
 * 
 * @author lt
 */
public class AlarmSettingActivity extends HiActivity implements ICameraIOSessionCallback, CompoundButton.OnCheckedChangeListener, OnCheckedChangeListener {

	private MyCamera mCamera;
	private RadioGroup mRGSensi;
	private RadioButton rBtnMiddle, rBtnLow, rBtnHigh;
	HiChipDefines.HI_P2P_S_MD_PARAM md_param = null;
	HiChipDefines.HI_P2P_S_MD_PARAM md_param2 = null;
	HiChipDefines.HI_P2P_S_MD_PARAM md_param3 = null;
	HiChipDefines.HI_P2P_S_MD_PARAM md_param4 = null;
	private SwitchButton togbtn_motion_detection;
	private int motion_detection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_setting);
		String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
		for (MyCamera camera : HiDataValue.CameraList) {
			if (uid.equals(camera.getUid())) {
				mCamera = camera;
				HiChipDefines.HI_P2P_S_MD_PARAM mdparam = new HiChipDefines.HI_P2P_S_MD_PARAM(0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_1, 0, 0, 0, 0, 0, 0));
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam.parseContent());



				HiChipDefines.HI_P2P_S_MD_PARAM mdparam2 = new HiChipDefines.HI_P2P_S_MD_PARAM(0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_2, 0, 0, 0, 0, 0, 0));
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam2.parseContent());

				HiChipDefines.HI_P2P_S_MD_PARAM mdparam3 = new HiChipDefines.HI_P2P_S_MD_PARAM(0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_3, 0, 0, 0, 0, 0, 0));
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam3.parseContent());

				HiChipDefines.HI_P2P_S_MD_PARAM mdparam4 = new HiChipDefines.HI_P2P_S_MD_PARAM(0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_4, 0, 0, 0, 0, 0, 0));
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam4.parseContent());
				break;

			}
		}

		initView();

	}

	private void initView() {
		TitleView title = (TitleView) findViewById(R.id.title_top);
		title.setTitle(getResources().getString(R.string.title_alarm_motion_detection));
		title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		title.setNavigationBarButtonListener(new NavigationBarButtonListener() {

			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					AlarmSettingActivity.this.finish();
					break;
				case TitleView.NAVIGATION_BUTTON_RIGHT:

					break;

				}

			}
		});

		initRGSensiView();
		togbtn_motion_detection = (SwitchButton) findViewById(R.id.togbtn_motion_detection);
	}

	private void initRGSensiView() {
		mRGSensi = (RadioGroup) findViewById(R.id.radioGroup_alarm_settion);
		String[] menuNameArrays = this.getResources().getStringArray(R.array.motion_detection_sensitivity);
		rBtnLow = (RadioButton) findViewById(R.id.radio_low);
		rBtnMiddle = (RadioButton) findViewById(R.id.radio_middle);
		rBtnHigh = (RadioButton) findViewById(R.id.radio_high);
		rBtnHigh.setText(menuNameArrays[0]);
		rBtnMiddle.setText(menuNameArrays[1]);
		rBtnLow.setText(menuNameArrays[2]);
	}

	@Override
	public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
		if (arg0 != mCamera)
			return;

		Bundle bundle = new Bundle();
		bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
		Message msg = handler.obtainMessage();
		msg.what = HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL;
		msg.obj = arg0;
		msg.arg1 = arg1;
		msg.arg2 = arg3;
		msg.setData(bundle);
		handler.sendMessage(msg);

	}

	protected void sendMotionDetection() {
		if (md_param == null && md_param2 == null && md_param3 == null && md_param4 == null) {
			return;
		}

		int guard_switch = togbtn_motion_detection.isChecked() ? 1 : 0;
		md_param.struArea.u32Enable = guard_switch;
		int md = 0;

		switch (motion_detection) {
		case 0:
			md = 25;
			break;
		case 1:
			md = 50;
			break;
		case 2:
			md = 75;
			break;
		}

		md_param.struArea.u32Sensi = md;
		showjuHuaDialog();
		mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_MD_PARAM, md_param.parseContent());
		if (!togbtn_motion_detection.isChecked() && md_param2 != null) {
			md_param2.struArea.u32Enable = 0;
			mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_MD_PARAM, md_param2.parseContent());
		}
		if (!togbtn_motion_detection.isChecked() && md_param3 != null) {
			md_param3.struArea.u32Enable = 0;
			mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_MD_PARAM, md_param3.parseContent());
		}
		if (!togbtn_motion_detection.isChecked() && md_param4 != null) {
			md_param4.struArea.u32Enable = 0;
			mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_MD_PARAM, md_param4.parseContent());
		}

	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL: {
				if (msg.arg2 == 0) {
					// MyCamera camera = (MyCamera)msg.obj;
					Bundle bundle = msg.getData();
					byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
					switch (msg.arg1) {
					case HiChipDefines.HI_P2P_GET_MD_PARAM:
						HiChipDefines.HI_P2P_S_MD_PARAM md_param_temp = new HiChipDefines.HI_P2P_S_MD_PARAM(data);
						if (md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_1) {
							md_param = md_param_temp;
							togbtn_motion_detection.setChecked(md_param.struArea.u32Enable == 1 ? true : false);
							togbtn_motion_detection.setOnCheckedChangeListener(AlarmSettingActivity.this);
							int sensitivity = md_param.struArea.u32Sensi;
							Log.e("==AlarmSetting==", sensitivity+"");
							if (sensitivity >= 0 && sensitivity <= 25) {
								motion_detection=0;
								rBtnLow.setChecked(true);
							} else if (sensitivity > 25 && sensitivity <= 50) {
								motion_detection=1;
								rBtnMiddle.setChecked(true);
							} else if (sensitivity > 50) {
								motion_detection=2;
								rBtnHigh.setChecked(true);
							}
						} else if (md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_2) {
							md_param2 = md_param_temp;
						} else if (md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_3) {
							md_param3 = md_param_temp;
						} else if (md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_4) {
							md_param4 = md_param_temp;
						}
						mRGSensi.setOnCheckedChangeListener(AlarmSettingActivity.this);

						break;
					case HiChipDefines.HI_P2P_SET_MD_PARAM:
						dismissjuHuaDialog();
						break;

					}
				} else {
					switch (msg.arg1) {
					case HiChipDefines.HI_P2P_SET_MD_PARAM:
						HiToast.showToast(AlarmSettingActivity.this, getString(R.string.tips_alarm_setting_failed));
						break;

					default:
						break;
					}
				}
			}
			}
		}
	};

	@Override
	public void receiveSessionState(HiCamera arg0, int arg1) {

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mCamera != null) {
			mCamera.registerIOSessionListener(this);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mCamera != null) {
			mCamera.unregisterIOSessionListener(this);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		sendMotionDetection();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (checkedId == R.id.radio_low) {
			motion_detection = 0;
			sendMotionDetection();
		} else if (checkedId == R.id.radio_middle) {
			motion_detection = 1;
			sendMotionDetection();
		} else if (checkedId == R.id.radio_high) {
			motion_detection = 2;
			sendMotionDetection();
		}
	}
}
