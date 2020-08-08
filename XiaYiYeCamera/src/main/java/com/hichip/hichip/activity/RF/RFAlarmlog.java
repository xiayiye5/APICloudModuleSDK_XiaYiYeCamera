package com.hichip.hichip.activity.RF;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import com.hichip.customview.dialog.NiftyDialogBuilder;
import com.hichip.customview.mtrefreshlistview.MeiTuanListView;
import com.hichip.customview.mtrefreshlistview.MeiTuanListView.OnMeiTuanRefreshListener;
import com.hichip.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.content.HiChipDefines.HI_P2P_FILE_INFO;
import com.hichip.control.HiCamera;
import com.hichip.thecamhi.activity.PlaybackOnlineActivity;
import com.hichip.thecamhi.activity.VideoOnlineActivity;
import com.hichip.thecamhi.base.DatabaseManager;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.bean.RFAlarmEvtent;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.utils.SharePreUtils;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class RFAlarmlog extends HiActivity implements ICameraIOSessionCallback, OnClickListener {

	private MeiTuanListView mLvAlarmlog;
	private MyCamera mMyCamera;
	private ArrayList<RFAlarmEvtent> evts = new ArrayList<>();
	private RfListAdapter mAdapter;
	private TitleView mTitleView;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");// HH是24小时制 hh是12小时制
	private TextView tv_rf_log_type;
	private String mTableName; // 数据库中表的名字
	// 记录当前页面显示的日期
	private Date mDate = HiTools.getStartTimeOfDay();
	private DatabaseManager mDbManager;
	private TextView mTvBeforeDay, mAfterDay, mTvTime;
	private SimpleDateFormat sdfMd = new SimpleDateFormat("yyyy-MM-dd");
	private String mCurenntDate = sdfMd.format(new Date());
	private String[] mAlarmList;
	private String[] puprfList;
	private String mClearDate;
	private int mTypeNum = 10; // 0-移动侦测 1-外置报警 2-声音报警 3-串口报警 6-RF报警 10-全部类型
	private int mFlag = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rf_alarmlog);
		getIntentData();
		initView();
		setListerners();
		getData();
	}

	private void setListerners() {
		tv_rf_log_type.setOnClickListener(this);
		mTvBeforeDay.setOnClickListener(this);
		mAfterDay.setOnClickListener(this);
		mLvAlarmlog.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mMyCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_ALARM_LOG_NAME)) {
					byte[] AlarmTime = evts.get(position).getTimezone().getBytes();
					mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_ALARM_LOG_NAME, HiChipDefines.HI_P2P_ALARM_TIME.parseContent(AlarmTime));
				}
			}
		});
		mLvAlarmlog.setOnMeiTuanRefreshListener(new OnMeiTuanRefreshListener() {

			@Override
			public void onRefresh() {
				getData();
			}
		});
	}

	private int mLocal = 0;
	private int mOnline = 0;

	private void getData() {
		evts.clear();
		String str = SharePreUtils.getString("clearData", RFAlarmlog.this, mMyCamera.getUid());
		mClearDate = TextUtils.isEmpty(str) ? null : str;
		if (mClearDate == null) {
			ArrayList<RFAlarmEvtent> list = mDbManager.getAllAlarmLogOneday(mTableName, sdf.format(getTimeMothDay(mDate, 0)), sdf.format(getTimeMothDay(mDate, 1)));
			if (list != null && list.size() > 0) {
				evts.addAll(list);
				mLocal = evts.size();
			}
		} else {
			mTvBeforeDay.setEnabled(false);
		}
		mAdapter.notifyDataSetChanged();
		showjuHuaDialog();
		mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_ALARM_LOG_EXT, null);
		// 昨天凌晨到今天凌晨 整整的昨天
		ArrayList<RFAlarmEvtent> listBefore = mDbManager.getAllAlarmLogOneday(mTableName, sdf.format(getTimeMothDay(mDate, -1)), sdf.format(getTimeMothDay(mDate, 0)));
		if (listBefore == null || listBefore.size() < 1) {
			mTvBeforeDay.setTextColor(getResources().getColor(R.color.color_gray));
			mTvBeforeDay.setEnabled(false);
		}
	}

	private void getIntentData() {
		String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
		for (MyCamera camera : HiDataValue.CameraList) {
			if (uid.equals(camera.getUid())) {
				this.mMyCamera = camera;
				break;
			}
		}
		mDbManager = new DatabaseManager(RFAlarmlog.this);
		mTableName = "RF_" + mMyCamera.getUid().replace("-", "");
		mAlarmList = getResources().getStringArray(R.array.tips_alarm_list_array);
		puprfList = new String[]{getString(R.string.all_types), mAlarmList[0], mAlarmList[1], mAlarmList[2], mAlarmList[3], getString(R.string.rf_alarm)};
	}

	private void initView() {
		mLvAlarmlog = (MeiTuanListView) findViewById(R.id.lv_alarm_log);
		mTitleView = (TitleView) findViewById(R.id.rf_alarmlog_top);
		mTitleView.setTitle(getString(R.string.title_alarm_list));
		mTitleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		mTitleView.setButton(TitleView.NAVIGATION_BUTTON_RIGHT);
		mTitleView.setRightBtnTextBackround(R.drawable.ic_delete);
		mTitleView.setNavigationBarButtonListener(new NavigationBarButtonListener() {

			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
					case TitleView.NAVIGATION_BUTTON_LEFT:
						RFAlarmlog.this.finish();
						break;
					case TitleView.NAVIGATION_BUTTON_RIGHT:
						final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(RFAlarmlog.this);
						dialog.withMessage(getString(R.string.clear_all_log)).withButton1Text(getString(R.string.btn_no))
								.withButton2Text(getString(R.string.btn_yes)).setButton1Click(new OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						}).setButton2Click(new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
								boolean rowNum = mDbManager.deleteTableData(mTableName);
								mClearDate = sdf.format(new Date());
								// 记录下清楚的时间
								SharePreUtils.putString("clearData", RFAlarmlog.this, mMyCamera.getUid(), mClearDate);
								evts.clear();
								mAdapter.notifyDataSetChanged();
							}
						}).show();
						break;
				}

			}
		});

		mAdapter = new RfListAdapter();
		mLvAlarmlog.setAdapter(mAdapter);
		mLvAlarmlog.addFooterView(new ViewStub(this));
		tv_rf_log_type = (TextView) findViewById(R.id.tv_rf_log_type);
		mTvBeforeDay = (TextView) findViewById(R.id.tv_before_day);
		mAfterDay = (TextView) findViewById(R.id.tv_after_day);
		mTvTime = (TextView) findViewById(R.id.tv_time);
		mTvTime.setText(getString(R.string.today));

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mMyCamera != null) {
			mMyCamera.registerIOSessionListener(this);
		}
		// getData();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mFlag = 0;
		if (mMyCamera != null) {
			mMyCamera.unregisterIOSessionListener(this);
		}
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

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
					if (msg.arg2 == 0) {// success
						byte[] data = msg.getData().getByteArray(HiDataValue.EXTRAS_KEY_DATA);
						switch (msg.arg1) {
							case HiChipDefines.HI_P2P_GET_ALARM_LOG_NAME:
								HiChipDefines.HI_P2P_FILE_INFO file_info = new HI_P2P_FILE_INFO(data);

								Bundle extras = new Bundle();
								extras.putString(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
								byte[] b_startTime = file_info.sStartTime.parseContent();
								extras.putByteArray("st", b_startTime);

								long startTimeLong = file_info.sStartTime.getTimeInMillis();
								long endTimeLong = file_info.sEndTime.getTimeInMillis();

								long pbtime = startTimeLong - endTimeLong;
								extras.putLong("pb_time", pbtime);
								extras.putLong(VideoOnlineActivity.VIDEO_PLAYBACK_START_TIME, startTimeLong);
								extras.putLong(VideoOnlineActivity.VIDEO_PLAYBACK_END_TIME, endTimeLong);

								Intent intent = new Intent();
								intent.putExtras(extras);
								intent.setClass(RFAlarmlog.this, PlaybackOnlineActivity.class);
								startActivity(intent);

								break;
							case HiChipDefines.HI_P2P_GET_ALARM_LOG_EXT:
								// 解析和处理所有的日志
								handlerAllAlarmLog(data);
								break;
						}
					} else {

					}

					break;

			}

		}
	};
	// protected boolean isHave;

	private void handlerAllAlarmLog(byte[] data) {
		mFlag++;
		dismissjuHuaDialog();
		String string = new String(data).trim();
		if (TextUtils.isEmpty(string)) {
			if (mFlag == 2) {
				HiToast.showToast(RFAlarmlog.this, "未发现新数据！");
				mLvAlarmlog.setOnRefreshComplete();
			}
			return;
		}
		final String[] strings = string.split("\\r\\n");
		boolean isTableExist = HiTools.sqlTableIsExist(RFAlarmlog.this, mTableName);
		if (!isTableExist) {
			// 创建RF LOG表
			mDbManager.createRFLogTable(mTableName);
		}
		new Thread() {
			public void run() {
				handData(strings);
				evts.clear();
				if (mClearDate == null) {
					evts.addAll(mDbManager.getAllAlarmLogOneday(mTableName, sdf.format(getTimeMothDay(mDate, 0)), sdf.format(getTimeMothDay(mDate, 1))));
				} else {
					evts.addAll(mDbManager.getAllAlarmLogOneday(mTableName, mClearDate, sdf.format(new Date())));
				}
				mOnline = evts.size();
				handler.obtainMessage(0X000110).sendToTarget();
			}
		}.start();
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0X000110) {
				dismissjuHuaDialog();
				mLvAlarmlog.setOnRefreshComplete();
				if (mLocal == mOnline) {
					HiToast.showToast(RFAlarmlog.this, "未发现新数据");
				}
				if (evts.size() < 1) {
					HiToast.showToast(RFAlarmlog.this, getString(R.string.tips_no_data));
				}
				mAdapter.notifyDataSetChanged();
			}
		}

		;
	};

	private void handData(final String[] strings) {
		for (String str : strings) {// 遍历解析后的所有的RF AlarmEvent
			if (!TextUtils.isEmpty(str)) {
				String[] strs = str.split(":");
				if (!TextUtils.isEmpty(strs[1]) && !TextUtils.isEmpty(strs[2])) {// 没有码值的就不是RF报警
					int typeNum = Integer.parseInt(strs[1]);
					String timezone = strs[2];
					String code = strs[3];
					String type = strs[4];
					String name = strs[5];
					String string2 = str.substring(str.lastIndexOf(":") + 1);
					int isHaveRecord = 0;
					try {
						isHaveRecord = Integer.parseInt(string2);
					} catch (Exception e) {
						isHaveRecord = 0;
					}
					RFAlarmEvtent rfEve = new RFAlarmEvtent(typeNum, timezone, code, type, name, isHaveRecord);
					// isHave = mDbManager.queryRfLogByTimezone(strs[1], mTableName);
					// if (!isHave) {
					if (mClearDate != null && Long.parseLong(timezone) > Long.parseLong(mClearDate)) {
						long resultCode = mDbManager.addRfLogToDb(rfEve, mTableName);
					}
					if (mClearDate == null) {
						long resultCode = mDbManager.addRfLogToDb(rfEve, mTableName);
						// }
					}
				}
			}
		}
	}

	;

	private class RfListAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return evts.size();
		}

		@Override
		public Object getItem(int position) {
			return evts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final RFAlarmEvtent evtent = (RFAlarmEvtent) getItem(position);
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(RFAlarmlog.this, R.layout.item_rf_alarmlog, null);
				holder = new ViewHolder();
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
				holder.tvTime = (TextView) convertView.findViewById(R.id.tv_alarm_time);
				holder.tvCode = (TextView) convertView.findViewById(R.id.item_tv_code);
				holder.ivIsHaveVideo = (ImageView) convertView.findViewById(R.id.iv_hava_video);
				convertView.setTag(holder);
			}
			holder = (ViewHolder) convertView.getTag();
			Date data = null;
			try {
				data = sdf.parse(evtent.getTimezone());
				holder.tvTime.setText(HiDataValue.sdf.format(data));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (evtent.getIsHaveRecord() == 0) {
				holder.ivIsHaveVideo.setVisibility(View.GONE);
			} else {
				holder.ivIsHaveVideo.setVisibility(View.VISIBLE);
			}
			if ("door".equals(evtent.type)) {
				holder.ivIcon.setImageResource(R.drawable.door);
			} else if ("infra".equals(evtent.type)) {
				holder.ivIcon.setImageResource(R.drawable.infrared);
			} else if ("fire".equals(evtent.type)) {
				holder.ivIcon.setImageResource(R.drawable.fire);
			} else if ("gas".equals(evtent.type)) {
				holder.ivIcon.setImageResource(R.drawable.gas);
			} else if ("beep".equals(evtent.type)) {
				holder.ivIcon.setImageResource(R.drawable.rf_other);
			} else if ("key0".equals(evtent.type) || "key1".equals(evtent.type) || "key2".equals(evtent.type) || "key3".equals(evtent.type)) {
				holder.ivIcon.setImageResource(R.drawable.remote_control);
			} else {
				holder.ivIcon.setImageResource(R.drawable.move);
			}
			switch (evtent.getTypeNum()) {
				case 0:
					holder.tvCode.setText(mAlarmList[0]);
					break;
				case 1:
					holder.tvCode.setText(mAlarmList[1]);
					break;
				case 2:
					holder.tvCode.setText(mAlarmList[2]);
					break;
				case 3:
					holder.tvCode.setText(mAlarmList[3]);
					break;
				case 4:
					holder.tvCode.setText("温度报警");
					break;
				case 5:
					holder.tvCode.setText("湿度报警");
					break;
				case 6:
					holder.tvCode.setText(evtent.getName());
					break;
			}
			return convertView;

		}

		public class ViewHolder {
			private ImageView ivIcon;
			private TextView tvTime;
			private TextView tvCode;
			private ImageView ivIsHaveVideo;

		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tv_rf_log_type) {
			showTypePup();
		} else if (id == R.id.tv_before_day) {
			mDate = getTimeMothDay(mDate, -1);
			ArrayList<RFAlarmEvtent> list = new ArrayList<>();
			if (mTypeNum == 10) {
				ArrayList<RFAlarmEvtent> listBefore = mDbManager.getAllAlarmLogOneday(mTableName, sdf.format(getTimeMothDay(mDate, -1)), sdf.format(getTimeMothDay(mDate, 0)));
				if (listBefore == null || listBefore.size() < 1) {
					mTvBeforeDay.setEnabled(false);
				}
				mTvTime.setText(sdfMd.format(mDate).equals(mCurenntDate) ? getString(R.string.today) : sdfMd.format(mDate));
				list = mDbManager.getAllAlarmLogOneday(mTableName, sdf.format(getTimeMothDay(mDate, 0)), sdf.format(getTimeMothDay(mDate, 1)));
			} else {
				ArrayList<RFAlarmEvtent> listBefore = mDbManager.getAllAlarmLogOnedayAndType(mTableName, sdf.format(getTimeMothDay(mDate, -1)), sdf.format(getTimeMothDay(mDate, 0)), mTypeNum);
				if (listBefore == null || listBefore.size() < 1) {
					mTvBeforeDay.setEnabled(false);
				}
				mTvTime.setText(sdfMd.format(mDate).equals(mCurenntDate) ? getString(R.string.today) : sdfMd.format(mDate));
				list = mDbManager.getAllAlarmLogOnedayAndType(mTableName, sdf.format(getTimeMothDay(mDate, 0)), sdf.format(getTimeMothDay(mDate, 1)), mTypeNum);
			}
			evts.clear();
			evts.addAll(list);
			mAdapter.notifyDataSetChanged();
			mAfterDay.setEnabled(true);
		} else if (id == R.id.tv_after_day) {
			ArrayList<RFAlarmEvtent> list;
			mDate = getTimeMothDay(mDate, 1);
			mTvTime.setText(sdfMd.format(mDate).equals(mCurenntDate) ? getString(R.string.today) : sdfMd.format(mDate));
			if (mTypeNum == 10) {
				list = mDbManager.getAllAlarmLogOneday(mTableName, sdf.format(getTimeMothDay(mDate, 0)), sdf.format(getTimeMothDay(mDate, 1)));
			} else {
				list = mDbManager.getAllAlarmLogOnedayAndType(mTableName, sdf.format(getTimeMothDay(mDate, 0)), sdf.format(getTimeMothDay(mDate, 1)), mTypeNum);
			}
			evts.clear();
			evts.addAll(list);
			mAdapter.notifyDataSetChanged();
			mTvBeforeDay.setEnabled(true);
			if (mDate.getTime() - HiTools.getStartTimeOfDay().getTime() >= 0) {
				mAfterDay.setEnabled(false);
			}
		}
	}

	/**
	 * @param date  当天日期
	 * @param value 0 当天 1后一天 -1前一天
	 */
	public Date getTimeMothDay(Date date, int value) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, value);
		date = calendar.getTime();
		return date;
	}

	private int mSelectType = 0;

	private void showTypePup() {
		View custonView = View.inflate(RFAlarmlog.this, R.layout.pup_rf_all_type, null);
		final PopupWindow popupWindow = new PopupWindow(custonView);
		ColorDrawable cd = new ColorDrawable(-0);
		popupWindow.setBackgroundDrawable(cd);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setFocusable(true);
		popupWindow.setWidth(LayoutParams.MATCH_PARENT);
		popupWindow.setHeight(LayoutParams.MATCH_PARENT);
		popupWindow.showAtLocation(custonView, Gravity.CENTER, 0, 0);
		ListView lv_pup_rf = (ListView) custonView.findViewById(R.id.lv_pup_rf);
		final PupRfListAdapter adapter = new PupRfListAdapter();
		adapter.selectItem = mSelectType;
		lv_pup_rf.setAdapter(adapter);
		lv_pup_rf.setSelection(adapter.selectItem);
		lv_pup_rf.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				adapter.setSelectItem(position);

				switch (position) {
					case 0:// 全部类型
						mTypeNum = 10;
						tv_rf_log_type.setText(getString(R.string.all_types));
						evts.clear();
						ArrayList<RFAlarmEvtent> list = mDbManager.getAllAlarmLogOneday(mTableName, sdf.format(getTimeMothDay(mDate, 0)), sdf.format(getTimeMothDay(mDate, 1)));
						if (list != null && list.size() > 0) {
							evts.addAll(list);
						} else {
							HiToast.showToast(RFAlarmlog.this, getString(R.string.tips_no_data));
						}
						ArrayList<RFAlarmEvtent> listBefore = mDbManager.getAllAlarmLogOneday(mTableName, sdf.format(getTimeMothDay(mDate, -1)), sdf.format(getTimeMothDay(mDate, 0)));
						if (listBefore == null || listBefore.size() < 1) {
							mTvBeforeDay.setEnabled(false);
						} else {
							mTvBeforeDay.setEnabled(true);
						}
						mAdapter.notifyDataSetChanged();
						mSelectType = 0;
						break;
					case 1:// 移动侦测
						mTypeNum = 0;
						tv_rf_log_type.setText(mAlarmList[0]);
						evts.clear();
						list = mDbManager.getAlarmLogByTypeNum(mTableName, 0, sdf.format(getTimeMothDay(mDate, 0)), sdf.format(getTimeMothDay(mDate, 1)));
						if (list != null && list.size() > 0) {
							evts.addAll(list);
						} else {
							HiToast.showToast(RFAlarmlog.this, getString(R.string.tips_no_data));
						}

						listBefore = mDbManager.getAllAlarmLogOnedayAndType(mTableName, sdf.format(getTimeMothDay(mDate, -1)), sdf.format(getTimeMothDay(mDate, 0)), mTypeNum);
						if (listBefore == null || listBefore.size() < 1) {
							mTvBeforeDay.setEnabled(false);
						} else {
							mTvBeforeDay.setEnabled(true);
						}
						mAdapter.notifyDataSetChanged();
						mSelectType = 1;
						break;
					case 2:
						mTypeNum = 1;
						tv_rf_log_type.setText(mAlarmList[1]);
						evts.clear();
						list = mDbManager.getAlarmLogByTypeNum(mTableName, 1, sdf.format(getTimeMothDay(mDate, 0)), sdf.format(getTimeMothDay(mDate, 1)));
						if (list != null && list.size() > 0) {
							evts.addAll(list);
						} else {
							HiToast.showToast(RFAlarmlog.this, getString(R.string.tips_no_data));
						}

						listBefore = mDbManager.getAllAlarmLogOnedayAndType(mTableName, sdf.format(getTimeMothDay(mDate, -1)), sdf.format(getTimeMothDay(mDate, 0)), mTypeNum);
						if (listBefore == null || listBefore.size() < 1) {
							mTvBeforeDay.setEnabled(false);
						} else {
							mTvBeforeDay.setEnabled(true);
						}

						mAdapter.notifyDataSetChanged();
						mSelectType = 2;
						break;
					case 3:
						mTypeNum = 2;
						tv_rf_log_type.setText(mAlarmList[2]);
						evts.clear();
						list = mDbManager.getAlarmLogByTypeNum(mTableName, 2, sdf.format(getTimeMothDay(mDate, 0)), sdf.format(getTimeMothDay(mDate, 1)));
						if (list != null && list.size() > 0) {
							evts.addAll(list);

						} else {
							HiToast.showToast(RFAlarmlog.this, getString(R.string.tips_no_data));
						}

						listBefore = mDbManager.getAllAlarmLogOnedayAndType(mTableName, sdf.format(getTimeMothDay(mDate, -1)), sdf.format(getTimeMothDay(mDate, 0)), mTypeNum);
						if (listBefore == null || listBefore.size() < 1) {
							mTvBeforeDay.setEnabled(false);
						} else {
							mTvBeforeDay.setEnabled(true);
						}

						mAdapter.notifyDataSetChanged();
						mSelectType = 3;
						break;
					case 4:
						mTypeNum = 3;
						tv_rf_log_type.setText(mAlarmList[3]);
						evts.clear();
						list = mDbManager.getAlarmLogByTypeNum(mTableName, 3, sdf.format(getTimeMothDay(mDate, 0)), sdf.format(getTimeMothDay(mDate, 1)));
						if (list != null && list.size() > 0) {
							evts.addAll(list);
						} else {
							HiToast.showToast(RFAlarmlog.this, getString(R.string.tips_no_data));
						}

						listBefore = mDbManager.getAllAlarmLogOnedayAndType(mTableName, sdf.format(getTimeMothDay(mDate, -1)), sdf.format(getTimeMothDay(mDate, 0)), mTypeNum);
						if (listBefore == null || listBefore.size() < 1) {
							mTvBeforeDay.setEnabled(false);
						} else {
							mTvBeforeDay.setEnabled(true);
						}

						mAdapter.notifyDataSetChanged();
						mSelectType = 4;
						break;
					case 5:// RF报警
						mTypeNum = 6;
						tv_rf_log_type.setText(getString(R.string.rf_alarm));
						evts.clear();
						list = mDbManager.getAlarmLogByTypeNum(mTableName, 6, sdf.format(getTimeMothDay(mDate, 0)), sdf.format(getTimeMothDay(mDate, 1)));
						if (list != null && list.size() > 0) {
							evts.addAll(list);

						} else {
							HiToast.showToast(RFAlarmlog.this, getString(R.string.tips_no_data));
						}

						listBefore = mDbManager.getAllAlarmLogOnedayAndType(mTableName, sdf.format(getTimeMothDay(mDate, -1)), sdf.format(getTimeMothDay(mDate, 0)), mTypeNum);
						if (listBefore == null || listBefore.size() < 1) {
							mTvBeforeDay.setEnabled(false);
						} else {
							mTvBeforeDay.setEnabled(true);
						}

						mAdapter.notifyDataSetChanged();
						mSelectType = 5;
						break;
				}
				popupWindow.dismiss();
			}
		});
		custonView.findViewById(R.id.tv_cancel).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popupWindow.dismiss();
			}
		});
	}

	private class PupRfListAdapter extends BaseAdapter {
		private int selectItem = 0;

		@Override
		public int getCount() {
			return puprfList.length;
		}

		@Override
		public Object getItem(int position) {
			return puprfList[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(RFAlarmlog.this, R.layout.item_rf_alarm_tyoe, null);
				holder = new ViewHolder();
				holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
				holder.ivSel = (ImageView) convertView.findViewById(R.id.iv_sel);
				convertView.setTag(holder);
			}
			holder = (ViewHolder) convertView.getTag();
			holder.tvName.setText(puprfList[position]);
			if (position == selectItem) {
				convertView.setBackgroundResource(R.drawable.shape_rf_log);
				holder.ivSel.setVisibility(View.VISIBLE);
			} else {
				holder.ivSel.setVisibility(View.GONE);
				convertView.setBackgroundResource(0);
			}
			return convertView;
		}

		public void setSelectItem(int position) {
			this.selectItem = position;
			notifyDataSetChanged();
		}

		public class ViewHolder {
			TextView tvName;
			ImageView ivSel;
		}

	}
}