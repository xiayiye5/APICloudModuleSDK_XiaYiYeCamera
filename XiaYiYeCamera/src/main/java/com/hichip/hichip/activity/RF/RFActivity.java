package com.hichip.hichip.activity.RF;

import java.util.ArrayList;
import com.hichip.customview.dialog.NiftyDialogBuilder;
import com.hichip.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.hichip.widget.SwitchButton;
//import com.hichip.thecamhi.activity.ScanQRCodeActivity;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.bean.RFDevice;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.main.MainActivity;
import com.hichip.thecamhi.widget.swipe.SwipeMenu;
import com.hichip.thecamhi.widget.swipe.SwipeMenuCreator;
import com.hichip.thecamhi.widget.swipe.SwipeMenuItem;
import com.hichip.thecamhi.widget.swipe.SwipeMenuListView;
import com.hichip.thecamhi.widget.swipe.SwipeMenuListView.OnMenuItemClickListener;

import android.app.AlertDialog;
import android.app.ActionBar.LayoutParams;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

public class RFActivity extends HiActivity implements ICameraIOSessionCallback, OnCheckedChangeListener {
	private MyCamera mMyCamera;
	private ArrayList<RFDevice> list_rf_info = new ArrayList<>();
	private ArrayList<RFDevice> list_rf_device_key = new ArrayList<>();
	private SwipeMenuListView mLvListRf;
	private RfListAdapter mAdapter;
	private boolean mIsHaveKey = false;// 有没有遥控器设备 true 有 false 没有
	protected View mHeadView;
	private SwitchButton switch_bf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rf);
		getIntentData();
		initView();
		setListeners();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mMyCamera != null) {
			mMyCamera.registerIOSessionListener(this);
			list_rf_info.clear();
			list_rf_device_key.clear();
			mIsHaveKey = false;
			initData();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mMyCamera != null) {
			mMyCamera.unregisterIOSessionListener(this);
		}
	}

	private void initData() {
		mAdapter = new RfListAdapter();
		mLvListRf.setAdapter(mAdapter);
		showjuHuaDialog();
		mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_ALARM_GET, null);
		mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET, null);
	}

	private void getIntentData() {
		String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
		if (TextUtils.isEmpty(uid)) {
			finish();
			return;
		}
		for (MyCamera camera : HiDataValue.CameraList) {
			if (uid.equals(camera.getUid())) {
				this.mMyCamera = camera;
				break;
			}
		}
	}

	private void setListeners() {
		mLvListRf.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public void onMenuItemClick(int position, SwipeMenu menu, int index) {
				switch (index) {
					case 0:
						showDeleteDialog(position);
						break;
				}
			}
		});
		mLvListRf.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (!switch_bf.isChecked()) {
					HiToast.showToast(RFActivity.this, "请先开启RF布防!");
					return;
				}
				if (mIsHaveKey) {
					if (position == 0) {
						Intent intent = new Intent(RFActivity.this, RemoteControlKeyActivity.class);
						intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
						startActivity(intent);
						return;
					}
				}
				RFDevice device = list_rf_info.get(position);
				Intent intent = new Intent(RFActivity.this, SetUpAndAddRFActivity.class);
				intent.putExtra("device", device);
				intent.putExtra("edit", true);
				intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
				startActivity(intent);
			}
		});
		switch_bf.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (switch_bf.isChecked()) {
						switch_bf.setEnabled(false);
						showClosePup();
						return true;
					}
				}
				return false;
			}
		});
	}

	private void showClosePup() {
		final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(RFActivity.this);
		dialog.withTitle("确认关闭?").withMessage("关闭之后,关于设备的所有报警将会失效!").withButton1Text("确认").withButton2Text("不关闭").setButton1Click(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				switch_bf.setEnabled(true);
				switch_bf.setChecked(false);
			}
		}).setButton2Click(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				switch_bf.setEnabled(true);
			}
		}).isCancelable(false).show();
	}

	private void initView() {
		TitleView titleView = (TitleView) findViewById(R.id.rf_title);
		titleView.setTitleSingline(false);
		titleView.setTitle("管理传感器\n" + mMyCamera.getUid());
		titleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		titleView.setButton(TitleView.NAVIGATION_BUTTON_RIGHT);
		titleView.setRightBtnTextBackround(R.drawable.add_sense);
		int left = HiTools.dip2px(this, 2);
		int top = HiTools.dip2px(this, 2);
		;
		int bottom = HiTools.dip2px(this, 2);
		titleView.setRightBackroundPadding(left, top, 0, bottom);
		titleView.setNavigationBarButtonListener(new NavigationBarButtonListener() {

			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
					case TitleView.NAVIGATION_BUTTON_LEFT:
						RFActivity.this.finish();
						break;
					case TitleView.NAVIGATION_BUTTON_RIGHT:
						if (switch_bf.isChecked()) {
							showPupAdd();
						} else {
							HiToast.showToast(RFActivity.this, "请先开起RF布防");
						}
						break;

				}

			}
		});
		mLvListRf = (SwipeMenuListView) findViewById(R.id.lv_list_rf);
		SwipeMenuCreator creator = new SwipeMenuCreator() {
			@Override
			public void create(SwipeMenu menu) {

				SwipeMenuItem deleteItem = new SwipeMenuItem(RFActivity.this);
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
				deleteItem.setWidth(HiTools.dip2px(RFActivity.this, 80));
				deleteItem.setHeight(HiTools.dip2px(RFActivity.this, 200));
				menu.addMenuItem(deleteItem);
			}
		};
		mLvListRf.setMenuCreator(creator);
		switch_bf = (SwitchButton) findViewById(R.id.switch_bf);
	}

	protected void showPupAdd() {
		View customView = View.inflate(this, R.layout.pup_rf_add, null);
		final PopupWindow pup = new PopupWindow(customView);
		// 产生背景变暗效果
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = 0.4f;
		getWindow().setAttributes(lp);
		ColorDrawable cd = new ColorDrawable();
		pup.setBackgroundDrawable(cd);
		pup.setOutsideTouchable(true);
		pup.setHeight(LayoutParams.WRAP_CONTENT);
		pup.setWidth(LayoutParams.WRAP_CONTENT);
		pup.showAtLocation(switch_bf, Gravity.CENTER, 0, 0);
		pup.setOnDismissListener(new OnDismissListener() {
			// 在dismiss中恢复透明度
			@Override
			public void onDismiss() {
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = 1f;
				getWindow().setAttributes(lp);
			}
		});
		TextView tv_scan_code_add=(TextView) customView.findViewById(R.id.tv_scan_code_add);
		tv_scan_code_add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*Intent intent = new Intent(RFActivity.this, ScanQRCodeActivity.class);
				intent.putExtra("category", 1);
				intent.putExtra("list_rf_info", list_rf_info);
				intent.putExtra("list_rf_device_key", list_rf_device_key);
				intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
				startActivity(intent);
				pup.dismiss();*/
			}
		});
		TextView check_code_add=(TextView) customView.findViewById(R.id.check_code_add);
		check_code_add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RFActivity.this, AddRFActivity.class);
				intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
				startActivity(intent);
				pup.dismiss();
			}
		});
	}

	@Override
	public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
		if (arg0 != mMyCamera)
			return;
		Message msg = Message.obtain();
		msg.what = HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL;
		msg.arg1 = arg1;
		msg.arg2 = arg3;
		Bundle bundle = new Bundle();
		bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
		msg.setData(bundle);
		mHandler.sendMessage(msg);

	}

	@Override
	public void receiveSessionState(HiCamera arg0, int arg1) {
		if (arg0 != mMyCamera)
			return;
		Message msg = Message.obtain();
		msg.what = HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
		msg.obj = arg0;
		msg.arg1 = arg1;
		mHandler.sendMessage(msg);
	}

	private class RfListAdapter extends BaseAdapter {
		private boolean enable = true;

		@Override
		public int getCount() {
			return list_rf_info.size();
		}

		@Override
		public Object getItem(int position) {
			return list_rf_info.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = new ViewHolder();
			if (convertView == null) {
				convertView = View.inflate(RFActivity.this, R.layout.item_list_rf, null);
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.item_iv);
				holder.tvName = (TextView) convertView.findViewById(R.id.item_name);
				holder.tvTypeAndCode = (TextView) convertView.findViewById(R.id.item_tv_code);
				holder.ivArrow = (ImageView) convertView.findViewById(R.id.item_arrow);
				convertView.setTag(holder);
			}
			RFDevice info = (RFDevice) getItem(position);
			holder = (ViewHolder) convertView.getTag();
			if (mIsHaveKey && position == 0) {
				holder.tvName.setText("遥控传感器");
				holder.tvTypeAndCode.setVisibility(View.GONE);
			} else {
				holder.tvName.setText(new String(info.getName()));
				holder.tvTypeAndCode.setVisibility(View.VISIBLE);
				holder.tvTypeAndCode.setText(new String(info.getCode()));
			}

			if ("door".equals(info.type)) {
				holder.ivIcon.setBackgroundResource(R.drawable.door);
			} else if ("infra".equals(info.type)) {
				holder.ivIcon.setBackgroundResource(R.drawable.infrared);
			} else if ("fire".equals(info.type)) {
				holder.ivIcon.setBackgroundResource(R.drawable.fire);
			} else if ("gas".equals(info.type)) {
				holder.ivIcon.setBackgroundResource(R.drawable.gas);
			} else if ("beep".equals(info.type)) {
				holder.ivIcon.setBackgroundResource(R.drawable.rf_other);
			} else if ("key".equals(info.type)) {
				holder.ivIcon.setBackgroundResource(R.drawable.remote_control);
			}
			if (!enable) {
				holder.tvName.setTextColor((getResources().getColor(R.color.color_eeeeee)));
				holder.tvTypeAndCode.setTextColor((getResources().getColor(R.color.color_eeeeee)));
			} else {
				holder.tvName.setTextColor((getResources().getColor(R.color.color_666666)));
				holder.tvTypeAndCode.setTextColor((getResources().getColor(R.color.color_666666)));
			}

			return convertView;

		}

		public class ViewHolder {
			public ImageView ivIcon;
			public TextView tvName;
			public TextView tvTypeAndCode;
			public ImageView ivArrow;
		}

		public void setEnable(boolean enable) {
			this.enable = enable;
			notifyDataSetChanged();
		}
	}

	protected void showDeleteDialog(final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(RFActivity.this);
		builder.setTitle("提示");
		builder.setMessage("确定要删除设备吗?");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showjuHuaDialog();
				if (mIsHaveKey && position == 0) {
					for (RFDevice device : list_rf_device_key) {
						mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_SET, HiChipDefines.HI_P2P_IPCRF_INFO.parseContent(device.u32Index, 0, "0", device.getType(), "0", (byte) 0, (byte) 0));
					}
					list_rf_device_key.clear();
					list_rf_info.remove(0);
					mIsHaveKey = false;
				} else {
					String type = new String(list_rf_info.get(position).getType()).trim();
					mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_SET, HiChipDefines.HI_P2P_IPCRF_INFO.parseContent(list_rf_info.get(position).u32Index, 0, "0", type, "0", (byte) 0, (byte) 0));
					list_rf_info.remove(position);
				}
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		// 参数都设置完成了，创建并显示出来
		builder.create().show();

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
					switch (msg.arg1) {
						case HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED:
							Intent intent = new Intent(RFActivity.this, MainActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							break;
					}
				case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
					if (msg.arg2 == 0) {// success
						byte[] data = msg.getData().getByteArray(HiDataValue.EXTRAS_KEY_DATA);
						switch (msg.arg1) {
							case HiChipDefines.HI_P2P_IPCRF_ALARM_GET:
								HiChipDefines.HI_P2P_IPCRF_ENABLE enable = new HiChipDefines.HI_P2P_IPCRF_ENABLE(data);
								switch_bf.setChecked(enable.u32Enable == 1 ? true : false);
								switch_bf.setOnCheckedChangeListener(RFActivity.this);
								if (enable.u32Enable == 1) {
									mAdapter.setEnable(true);
								} else {
									mAdapter.setEnable(false);
								}

								break;
							case HiChipDefines.HI_P2P_IPCRF_ALARM_SET:
								dismissjuHuaDialog();
								break;
							case HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_SET:
								dismissjuHuaDialog();
								mAdapter.notifyDataSetChanged();
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
											mIsHaveKey = true;
											RFDevice device = new RFDevice(new String(info.sName).trim(), new String(info.sType).trim(), new String(info.sRfCode).trim(), info.u32Index, info.u32Enable);
											list_rf_device_key.add(device);
										} else {
											RFDevice device = new RFDevice(new String(info.sName).trim(), new String(info.sType).trim(), new String(info.sRfCode).trim(), info.u32Index, info.u32Enable);
											list_rf_info.add(device);
										}

									}
								}
								if (allRfInfo.u32Flag == 1) {// 数据收结束了
									dismissjuHuaDialog();
									if (mIsHaveKey) {
										list_rf_info.add(0, new RFDevice(null, "key", null, 0, 0));
									}
									mAdapter.notifyDataSetChanged();
								}
								break;
						}
					} else {// fail
						switch (msg.arg1) {
							case HiChipDefines.HI_P2P_IPCRF_ALARM_SET:
								break;
						}
					}

					break;
			}
		};
	};

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		showjuHuaDialog();
		if (isChecked) {
			mAdapter.setEnable(true);
		} else {
			mAdapter.setEnable(false);
		}
		mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_ALARM_SET, HiChipDefines.HI_P2P_IPCRF_ENABLE.parseContent(isChecked ? 1 : 0));
	}
}
