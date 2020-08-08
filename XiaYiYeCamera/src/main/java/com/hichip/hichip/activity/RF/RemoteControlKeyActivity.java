package com.hichip.hichip.activity.RF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.hichip.customview.dialog.NiftyDialogBuilder;
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
import com.hichip.thecamhi.bean.RFDevice;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.main.MainActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 按键控制的Activity
 * @author lt
 */
public class RemoteControlKeyActivity extends HiActivity implements ICameraIOSessionCallback, OnCheckedChangeListener {
	private List<RFDevice> list_rf_device_key = new ArrayList<>();
	private TitleView mTitleView;
	private MyCamera mMyCamera;
	private RelativeLayout rl_sos, rl_ring, rl_key_1, rl_key_0, rl_open_close;
	private SwitchButton btn_swi_open_close, btn_swi_sos, btn_swi_ring;
	private TextView tv_sos_code, tv_ring_code, tv_key_1, tv_key_1_code, tv_key_0, tv_key_0_code;
	private HashMap<String, RFDevice> map_key0_key1=new HashMap<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote_control_key);
		getIntentData();
		initView();
		getData();
		setListeners();
	}

	private void setListeners() {
		btn_swi_open_close.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					if (btn_swi_open_close.isChecked()) {
						btn_swi_open_close.setEnabled(false);
						showClosePup();
						return true;
					}
				}
				return false;
			}
		});
	}


	private void showClosePup() {
		final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(RemoteControlKeyActivity.this);
		dialog.withTitle("确认关闭?").withMessage("关闭之后,已添加的报警开关按键操作将会失效!").withButton1Text("确认").withButton2Text("不关闭").setButton1Click(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				btn_swi_open_close.setEnabled(true);
				btn_swi_open_close.setChecked(false);
			}
		}).setButton2Click(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				btn_swi_open_close.setEnabled(true);
			}
		}).isCancelable(false).show();
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
		}
	}

	private void getData() {
		mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET, null);
	}

	private void getIntentData() {
		String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
		for (MyCamera camera : HiDataValue.CameraList) {
			if (uid.equals(camera.getUid())) {
				this.mMyCamera = camera;
				break;
			}
		}
	}

	private void initView() {
		mTitleView = (TitleView) findViewById(R.id.key_top);
		mTitleView.setTitle("按键控制");
		mTitleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		mTitleView.setNavigationBarButtonListener(new NavigationBarButtonListener() {
			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
					case TitleView.NAVIGATION_BUTTON_LEFT:
						RemoteControlKeyActivity.this.finish();
						break;
				}
			}
		});
		rl_sos = (RelativeLayout) findViewById(R.id.rl_sos);
		rl_ring = (RelativeLayout) findViewById(R.id.rl_ring);

		tv_sos_code = (TextView) findViewById(R.id.tv_sos_code);
		tv_ring_code = (TextView) findViewById(R.id.tv_ring_code);

		btn_swi_open_close = (SwitchButton) findViewById(R.id.btn_swi_open_close);
		btn_swi_sos = (SwitchButton) findViewById(R.id.btn_swi_sos);
		btn_swi_ring = (SwitchButton) findViewById(R.id.btn_swi_ring);
		rl_key_1 = (RelativeLayout) findViewById(R.id.rl_key_1);
		tv_key_1 = (TextView) findViewById(R.id.tv_key_1);
		tv_key_1_code = (TextView) findViewById(R.id.tv_key_1_code);
		rl_key_0 = (RelativeLayout) findViewById(R.id.rl_key_0);
		tv_key_0 = (TextView) findViewById(R.id.tv_key_0);
		tv_key_0_code = (TextView) findViewById(R.id.tv_key_0_code);
		rl_open_close = (RelativeLayout) findViewById(R.id.rl_open_close);
	}

	@Override
	public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
		if (arg0 != mMyCamera) {
			return;
		}
		Message msg = Message.obtain();
		msg.arg1 = arg1;
		msg.arg2 = arg3;
		msg.what = HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL;
		Bundle bundle = new Bundle();
		bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
		msg.setData(bundle);
		mHandler.sendMessage(msg);

	}

	@Override
	public void receiveSessionState(HiCamera arg0, int arg1) {
		if(arg0!=mMyCamera) return;
		Message msg=Message.obtain();
		msg.what=HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
		msg.arg1=arg1;
		mHandler.sendMessage(msg);

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
					if(msg.arg1==HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED){
						Intent intent=new Intent(RemoteControlKeyActivity.this,MainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					break;

				case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
					if (msg.arg2 == 0) {
						byte[] data = msg.getData().getByteArray(HiDataValue.EXTRAS_KEY_DATA);
						switch (msg.arg1) {
							case HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_SET:
								dismissjuHuaDialog();
								HiToast.showToast(RemoteControlKeyActivity.this, "修改成功");
								break;
							case HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET:
								HiChipDefines.HI_P2P_IPCRF_ALL_INFO allRfInfo = new HiChipDefines.HI_P2P_IPCRF_ALL_INFO(data);
								for (int i = 0; i < allRfInfo.sRfInfo.length; i++) {
									HiChipDefines.HI_P2P_IPCRF_INFO info = allRfInfo.sRfInfo[i];
									String code = new String(info.sRfCode).trim();
									if (!TextUtils.isEmpty(code) && code.length() > 10) {
										String str = new String(info.sType).trim();
										str = str.substring(0, 3);
										if ("key".equals(str)) {
											RFDevice device = new RFDevice(new String(info.sName).trim(), new String(info.sType).trim(), new String(info.sRfCode).trim(), info.u32Index, info.u32Enable);
											list_rf_device_key.add(device);
										}
									}
								}
								handlerView(list_rf_device_key);
								btn_swi_open_close.setOnCheckedChangeListener(RemoteControlKeyActivity.this);
								break;
						}
					} else {
					}
					break;
			}

		};
	};


	protected void handlerView(List<RFDevice> list_rf_device_key2) {
		for (final RFDevice device : list_rf_device_key2) {
			if (device.getType().equals(RemoteControlActivity.KEY_0)) {
				tv_key_0_code.setText(device.getCode());
				rl_open_close.setVisibility(View.VISIBLE);
				rl_key_0.setVisibility(View.VISIBLE);
				if (device.getEnable() == 0) {
					btn_swi_open_close.setChecked(false);
					handOpenCloseView(false);
				}
				map_key0_key1.put(device.getType(), device);
			} else if (device.getType().equals(RemoteControlActivity.KEY_1)) {
				tv_key_1_code.setText(device.getCode());
				rl_open_close.setVisibility(View.VISIBLE);
				rl_key_1.setVisibility(View.VISIBLE);
				if (device.getEnable() == 0) {
					btn_swi_open_close.setChecked(false);
					handOpenCloseView(false);
				}
				map_key0_key1.put(device.getType(), device);
			} else if (device.getType().equals(RemoteControlActivity.KEY_2)) {
				rl_sos.setVisibility(View.VISIBLE);
				tv_sos_code.setText(device.getCode());
				btn_swi_sos.setChecked(device.getEnable() == 1 ? true : false);
				handSwibut(device, btn_swi_sos);
			} else if (device.getType().equals(RemoteControlActivity.KEY_3)) {
				rl_ring.setVisibility(View.VISIBLE);
				tv_ring_code.setText(device.getCode());
				btn_swi_ring.setChecked(device.getEnable() == 1 ? true : false);
				handSwibut(device, btn_swi_ring);
			}

		}

	}


	private void handSwibut(final RFDevice device, final SwitchButton butSwi) {
		butSwi.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int index = device.getU32Index();
				int enable = isChecked ? 1 : 0;
				String code = device.getCode();
				String type = device.getType();
				String name = device.getName();
				byte voiceLink = (byte) 0;
				byte ptzLink = (byte) 0;
				showjuHuaDialog();
				mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_SET, HiChipDefines.HI_P2P_IPCRF_INFO.parseContent(index, enable, code, type, name, voiceLink, ptzLink));
			}
		});
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(buttonView.getId()==R.id.btn_swi_open_close){
			for(Map.Entry<String, RFDevice> entry:map_key0_key1.entrySet()){
				RFDevice device=entry.getValue();
				int index = device.getU32Index();
				int enable = isChecked?1:0;
				String code = device.getCode();
				String type = device.getType();
				String name = device.getName();
				byte voiceLink = (byte) 0;
				byte ptzLink = (byte) 0;
				showjuHuaDialog();
				handOpenCloseView(isChecked);
				mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_SET, HiChipDefines.HI_P2P_IPCRF_INFO.parseContent(index, enable, code, type, name, voiceLink, ptzLink));
			}
		}
	}

	private void handOpenCloseView(boolean isChecked) {
		tv_key_0.setTextColor(isChecked?getResources().getColor(R.color.color_666666):getResources().getColor(R.color.color_888888));
		tv_key_0_code.setTextColor(isChecked?getResources().getColor(R.color.color_666666):getResources().getColor(R.color.color_888888));
		tv_key_1.setTextColor(isChecked?getResources().getColor(R.color.color_666666):getResources().getColor(R.color.color_888888));
		tv_key_1_code.setTextColor(isChecked?getResources().getColor(R.color.color_666666):getResources().getColor(R.color.color_888888));

	}
}






