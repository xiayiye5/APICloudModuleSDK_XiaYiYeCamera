package com.hichip.thecamhi.activity.setting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.hichip.R;
import com.hichip.hichip.activity.TimeZoneListActivity;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.content.HiChipDefines.HI_P2P_S_TIME_PARAM;
import com.hichip.content.HiChipDefines.HI_P2P_S_TIME_ZONE;
import com.hichip.content.HiChipDefines.HI_P2P_S_TIME_ZONE_EXT;
import com.hichip.control.HiCamera;
import com.hichip.sdk.HiChipP2P;
import com.hichip.system.HiDefaultData;
import com.hichip.hichip.widget.SwitchButton;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;

public class TimeSettingActivity extends HiActivity implements ICameraIOSessionCallback, OnClickListener {
	private MyCamera mCamera;
	private TextView tvDeviceTime;
	private RelativeLayout mtlTimeZone;
	private TextView mTvTimeZone;
	private String[] strings;
	protected int index = 0;
	private int deviceTimezonIndex = -1;
	private int mTz = -1;
	private int mDesmode = 0;
	protected HI_P2P_S_TIME_ZONE timezone;
	protected HI_P2P_S_TIME_ZONE_EXT time_ZONE_EXT;
	public static final String REQUESTCODE_INDEX = "INDEX";
	public static final int REQUSTCIDE_119 = 0X119;
	private boolean mIsSupportZoneExt;

	private RelativeLayout mRlXls;
	private SwitchButton mSwBtnXls;
	private boolean      mISSwiBtn=false;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_equipment_time_setting);

		String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);

		for (MyCamera camera : HiDataValue.CameraList) {
			if (uid.equals(camera.getUid())) {
				mCamera = camera;
				mIsSupportZoneExt = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT);
				if (mIsSupportZoneExt) {// ֧����ʱ��
					mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT, new byte[0]);
					strings = getResources().getStringArray(R.array.device_timezone_new);
				} else {
					mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE, new byte[0]);
					strings = getResources().getStringArray(R.array.device_timezone_old);
				}
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_PARAM, new byte[0]);
				break;
			}
		}

		showLoadingProgress();
		initView();
		setListenersAndGetData();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.cancel();
					mTvTimeZone.setText(getString(R.string.tip_get_devi_tizone_fail));
					HiToast.showToast(TimeSettingActivity.this, getString(R.string.tip_get_devi_tizone_fail));
				}

			}
		}, 8000);
	}

	private void setListenersAndGetData() {
		mtlTimeZone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TimeSettingActivity.this, TimeZoneListActivity.class);
				intent.putExtra(REQUESTCODE_INDEX, index);
				if (timezone != null) {
					intent.putExtra("u32DstMode", timezone.u32DstMode);
				}
				intent.putExtra("stringarray", strings);
				intent.putExtra("boolean", mIsSupportZoneExt);
				startActivityForResult(intent, REQUSTCIDE_119);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUSTCIDE_119 && resultCode == RESULT_OK) {
			mTz = data.getIntExtra(TimeZoneListActivity.KEY_TZ, 0);
			// mDesmode = data.getIntExtra(TimeZoneListActivity.KEY_DESMODE, 0);
			index = mTz;
			// mTvTimeZone.setText(strings[mTz]);
			if (mIsSupportZoneExt) {
				mTvTimeZone.setText(HiDefaultData.TimeZoneField1[index][1] + " " + strings[mTz]);
				mRlXls.setVisibility("1".equals(HiDefaultData.TimeZoneField1[index][2]) ? View.VISIBLE : View.GONE);
				mSwBtnXls.setChecked(false);
			} else {
				mTvTimeZone.setText(strings[mTz]);
				mRlXls.setVisibility(HiDefaultData.TimeZoneField[index][1] == 1 ? View.VISIBLE : View.GONE);
				mSwBtnXls.setChecked(false);
			}

		}
	}

	private void initView() {
		TitleView title = (TitleView) findViewById(R.id.title_top);
		title.setTitle(getResources().getString(R.string.title_equipment_setting));
		title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		title.setNavigationBarButtonListener(new NavigationBarButtonListener() {

			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					TimeSettingActivity.this.finish();
					break;
				}

			}
		});
		tvDeviceTime = (TextView) findViewById(R.id.tv_device_time);
		mtlTimeZone = (RelativeLayout) findViewById(R.id.rl_time_zone);
		mTvTimeZone = (TextView) findViewById(R.id.tv_time_zone);
		mRlXls = (RelativeLayout) findViewById(R.id.ll_xls);
		mSwBtnXls = (SwitchButton) findViewById(R.id.time_zone_xls);

		TextView phone_time_zone_et = (TextView) findViewById(R.id.phone_time_zone_et);

		TimeZone tz = TimeZone.getDefault();
		float tim = (float) tz.getRawOffset() / (3600000.0f);
		String gmt = null;
		
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		Date datad = cal.getTime();
		boolean daylightT = tz.inDaylightTime(datad);
		if(daylightT) tim += 1;
		
		gmt = "GMT" + tim;
		if (tim > 0) {
			gmt = "GMT+" + tim;
		}
		phone_time_zone_et.setText(gmt + "  " + tz.getDisplayName());

		Button setting_time_zone_btn = (Button) findViewById(R.id.setting_time_zone_btn);
		setting_time_zone_btn.setOnClickListener(this);
		Button synchronization_time_btn = (Button) findViewById(R.id.synchronization_time_btn);
		synchronization_time_btn.setOnClickListener(this);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.arg2 != 0 && msg.what != HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL)
				return;
			switch (msg.what) {
			case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
				if (msg.arg1 == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
					dismissjuHuaDialog();
					syncDeviceTime();
				}
				break;
			case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
				if (msg.arg2 == 0) {
					handlerSuccess(msg);
				} else {
					switch (msg.arg1) {
					case HiChipDefines.HI_P2P_SET_TIME_ZONE:
					case HiChipDefines.HI_P2P_SET_TIME_ZONE_EXT:
						dismissjuHuaDialog();
						HiToast.showToast(TimeSettingActivity.this, getString(R.string.tips_setzonefail));
						break;

					}
				}
				break;
			}
		}

		private void handlerSuccess(Message msg) {
			Bundle bundle = msg.getData();
			byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
			switch (msg.arg1) {
			case HiChipDefines.HI_P2P_GET_TIME_PARAM:
				HI_P2P_S_TIME_PARAM timeParam = new HI_P2P_S_TIME_PARAM(data);
				StringBuffer sb = new StringBuffer();

				sb.append(timeParam.u32Year + "-" + timeParam.u32Month + "-" + timeParam.u32Day + " " + timeParam.u32Hour + ":" + timeParam.u32Minute + ":" + timeParam.u32Second);
				Date date1 = new Date();
				try {
					date1 = sdf.parse(sb.toString());
				} catch (ParseException e) {
				}
				tvDeviceTime.setText(sdf.format(date1));
				break;
			case HiChipDefines.HI_P2P_GET_TIME_ZONE:
				dismissLoadingProgress();
				timezone = new HI_P2P_S_TIME_ZONE(data);
				index = -1;
				for (int i = 0; i < HiDefaultData.TimeZoneField.length; i++) {
					if (HiDefaultData.TimeZoneField[i][0] == timezone.s32TimeZone) {
						index = i;
						mTz = index;
						deviceTimezonIndex = i;
						break;
					}
				}
				if (index == -1) {
					mTvTimeZone.setText(getString(R.string.tip_get_devi_tizone_fail));
					HiToast.showToast(TimeSettingActivity.this, getString(R.string.tip_get_devi_tizone_fail));
				} else {
					mTvTimeZone.setText(strings[index]);
					mRlXls.setVisibility(HiDefaultData.TimeZoneField[index][1] == 1 ? View.VISIBLE : View.GONE);
					mSwBtnXls.setChecked(timezone.u32DstMode == 1 ? true : false);
					mISSwiBtn=timezone.u32DstMode == 1 ? true : false;
				}

				break;

			case HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT:// ��ʱ��
				dismissLoadingProgress();
				if (data != null && data.length >= 36) {
					time_ZONE_EXT = new HI_P2P_S_TIME_ZONE_EXT(data);
					index = -1;
					for (int i = 0; i < HiDefaultData.TimeZoneField1.length; i++) {
						if (isEqual(time_ZONE_EXT.sTimeZone, HiDefaultData.TimeZoneField1[i][0])) {
							index = i;
							mTz = index;
							deviceTimezonIndex = i;
							break;
						}
					}
					if (index == -1) {
						mTvTimeZone.setText(getString(R.string.tip_get_devi_tizone_fail));
						HiToast.showToast(TimeSettingActivity.this, getString(R.string.tip_get_devi_tizone_fail));
					} else {
						mTvTimeZone.setText(HiDefaultData.TimeZoneField1[index][1] + " " + strings[index]);
						mRlXls.setVisibility("1".equals(HiDefaultData.TimeZoneField1[index][2]) ? View.VISIBLE : View.GONE);
						mSwBtnXls.setChecked(time_ZONE_EXT.u32DstMode == 1 ? true : false);
						mISSwiBtn=time_ZONE_EXT.u32DstMode == 1 ? true : false;
					}
				}

				break;
			case HiChipDefines.HI_P2P_SET_TIME_PARAM:
				dismissjuHuaDialog();
				// HiToast.showToast(TimeSettingActivity.this,
				// getString(R.string.tips_device_time_setting_synchroned_time));
				break;
			case HiChipDefines.HI_P2P_SET_TIME_ZONE:
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_REBOOT, new byte[0]);
				HiToast.showToast(TimeSettingActivity.this, getString(R.string.tips_device_time_setting_timezone));
				// mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE, new
				// byte[0]);

				break;
			case HiChipDefines.HI_P2P_SET_TIME_ZONE_EXT:
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_REBOOT, new byte[0]);
				HiToast.showToast(TimeSettingActivity.this, getString(R.string.tips_device_time_setting_timezone));
				// mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT,
				// new byte[0]);
				break;
			}
		}
	};

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
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.synchronization_time_btn) {
			showjuHuaDialog();
			syncDeviceTime();
		} else if (id == R.id.setting_time_zone_btn) {
			if (mTvTimeZone.getText().toString().equals(getString(R.string.tip_get_devi_tizone_fail))) {
				return;
			}
			if ((deviceTimezonIndex == mTz || mTz == -1) && mRlXls.getVisibility() == View.GONE) {// �������õ�ǰ�豸��ʱ��
				HiToast.showToast(TimeSettingActivity.this, getString(R.string.tip_not_settimezone));
				return;
			}
			if (mRlXls.getVisibility() == View.VISIBLE && mSwBtnXls.isChecked() == mISSwiBtn && (deviceTimezonIndex == mTz || mTz == -1)) {
				HiToast.showToast(TimeSettingActivity.this, getString(R.string.tip_not_settimezone));
				return;
			}
			if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_QUERY_START_NODST)) {
				// sendTimeZone();
				showAlertDialog();
			} else {
				showAlertDialog();
			}
		}

	}

	private void syncDeviceTime() {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		cal.setTimeInMillis(System.currentTimeMillis());

		byte[] time = HI_P2P_S_TIME_PARAM.parseContent(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));

		mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_TIME_PARAM, time);
		mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_PARAM, new byte[0]);
		if (mIsSupportZoneExt) {// ֧����ʱ��
			mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT, new byte[0]);
		} else {
			mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE, new byte[0]);
		}
	}

	public void sendTimeZone() {
		mDesmode = mSwBtnXls.isChecked() ? 1 : 0;
		showjuHuaDialog();
		if (mIsSupportZoneExt) {
			if (mTz >= 0) {
				byte[] byte_time = HiDefaultData.TimeZoneField1[mTz][0].getBytes();
				if (byte_time.length <= 32) {
					mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_TIME_ZONE_EXT, HI_P2P_S_TIME_ZONE_EXT.parseContent(byte_time, mDesmode));
				}
			}

		} else {
			int tz = HiDefaultData.TimeZoneField[mTz][0];
			mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_TIME_ZONE, HI_P2P_S_TIME_ZONE.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, tz, mDesmode));
		}
	}

	public void showAlertDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(TimeSettingActivity.this);

		builder.setTitle(getString(R.string.tips_warning));
		builder.setMessage(getResources().getString(R.string.tips_device_time_setting_reboot_camera));
		builder.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				sendTimeZone();
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), null);
		builder.show();
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

	@Override
	public void receiveSessionState(HiCamera arg0, int arg1) {
		if (arg0 == null && arg0 != mCamera) {
			return;
		}
		Message message = Message.obtain();
		message.what = HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
		message.obj = arg0;
		message.arg1 = arg1;
		handler.sendMessage(message);

	}

	private boolean isEqual(byte[] bys, String str) {
		String string = new String(bys);
		String temp = string.substring(0, str.length());
		if (temp.equalsIgnoreCase(str)) {
			return true;
		}
		return false;
	}

}
