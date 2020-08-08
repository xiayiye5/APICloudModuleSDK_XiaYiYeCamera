package com.hichip.thecamhi.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.hichip.R;
import com.hichip.tools.HiSearchSDK;
import com.hichip.tools.HiSearchSDK.HiSearchResult;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
/**
 * 摄像机列表(点击 局域网搜索UID 跳转页面)
 * */
public class SearchCameraActivity extends HiActivity {
	private ProgressBar prsbLoading;
	private LinearLayout layFailSearch;
	private ListView listSearchResult;
	private SearchResultListAdapter adapter;

	private HiSearchSDK searchSDK;
	private List<HiSearchResult> list = new ArrayList<HiSearchResult>();
	private static final int isCheckData = 0 * 9995;
	Message msg2;
	private long oldClickTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_search_camera);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		layFailSearch = (LinearLayout) findViewById(R.id.lay_fail_lan_search);
		prsbLoading = (ProgressBar) findViewById(R.id.progressBar2);
		listSearchResult = (ListView) findViewById(R.id.list_search_result);
		adapter = new SearchResultListAdapter(this);

		listSearchResult.setAdapter(adapter);

		listSearchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				HiSearchResult r = list.get(position);
				for (MyCamera camera : HiDataValue.CameraList) {
					if (r.uid.equalsIgnoreCase(camera.getUid())) {
						HiToast.showToast(SearchCameraActivity.this, getString(R.string.tip_device_add));
						return;
					}
				}

				Bundle extras = new Bundle();
				extras.putString(HiDataValue.EXTRAS_KEY_UID, r.uid);

				Intent intent = new Intent();
				intent.putExtras(extras);

				intent.setClass(SearchCameraActivity.this, AddCameraActivity.class);

				SearchCameraActivity.this.setResult(RESULT_OK, intent);

				finish();

			}
		});

		TitleView nb = (TitleView) findViewById(R.id.title_top);

		nb.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		nb.setButton(TitleView.NAVIGATION_BUTTON_RIGHT);

		nb.setTitle(getString(R.string.camera_list));
		nb.setRightBtnTextBackround(R.drawable.refresh);
		int left = HiTools.dip2px(this, 5);
		int top = HiTools.dip2px(this, 6);
		int right = 0;
		int bottom = HiTools.dip2px(this, 6);
		nb.setRightBackroundPadding(left, top, right, bottom);
		nb.setNavigationBarButtonListener(new TitleView.NavigationBarButtonListener() {

			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					finish();
					break;
				case TitleView.NAVIGATION_BUTTON_RIGHT:

					if (System.currentTimeMillis() - oldClickTime >= 2000) {
						startSearch();
						oldClickTime = System.currentTimeMillis();
					}

					break;
				}
			}
		});

		// Button title_btn_back = (Button) findViewById(R.id.title_btn_back);
		// title_btn_back.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// AddCameraActivity.addCameraStep--;
		// finish();
		// overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);
		// }
		// });
		//
		// Button title_btn_right = (Button) findViewById(R.id.title_btn_right);
		// title_btn_right.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Intent intent = new Intent();
		// intent.setClass(AddCameraStep3Activity.this, MainActivity.class);
		// startActivity(intent);
		// finish();
		// overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);
		//
		// }
		// });
		//
		/*
		 * TextView txtStepNum = (TextView)findViewById(R.id.text_step_num); txtStepNum.setText(String.valueOf(addCameraStep));
		 */
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	//
	// switch (keyCode) {
	// case KeyEvent.KEYCODE_BACK:
	// AddCameraActivity.addCameraStep--;
	// finish();
	// overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);
	// break;
	// }
	//
	// return super.onKeyDown(keyCode, event);
	// }

	private CountDownTimer timer;

	private long oldRefreshTime;

	private void startSearch() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		oldRefreshTime = System.currentTimeMillis();
		int timeLong = 20000;

		if (adapter != null) {
			list.clear();
			listSearchResult.requestLayout();
			adapter.notifyDataSetChanged();
		}

		searchSDK = new HiSearchSDK(new HiSearchSDK.ISearchResult() {
			@Override
			public void onReceiveSearchResult(HiSearchResult result) {
				String temp = result.uid.substring(0, 4);
				if (!TextUtils.isEmpty(temp)) {
					Message msg = handler.obtainMessage();
					msg.obj = result;
					msg.what = HiDataValue.HANDLE_MESSAGE_SCAN_RESULT;
					handler.sendMessage(msg);
				}
			}
		});
		searchSDK.search2();
		timer = new CountDownTimer(timeLong, 1000) {
			@Override
			public void onFinish() {
				if (list == null || list.size() == 0) {
					searchSDK.stop();
					layFailSearch.setVisibility(View.VISIBLE);
					prsbLoading.setVisibility(View.GONE);
				}
			}

			@Override
			public void onTick(long arg0) {

			}
		}.start();
		prsbLoading.setVisibility(View.VISIBLE);
		layFailSearch.setVisibility(View.GONE);
		list.clear();
	}

	@Override
	protected void onPause() {
		super.onPause();
		searchSDK.stop();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

	}

	// private void startSearch() {
	//
	// prsbLoading.setVisibility(View.VISIBLE);
	// layFailSearch.setVisibility(View.GONE);
	// btnRefresh.setEnabled(false);
	//
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// list.clear();
	//
	// byte[] out = new byte[4096];
	//
	// SearchLib.SearchDevices(out,3);
	//
	// Log.v("result", "result:"+Packet.getHex(out, 4096));
	//
	// SearchDefines.HI_LAN_SEARCH_RESULT result = new
	// SearchDefines.HI_LAN_SEARCH_RESULT(out);
	// Log.v("result", "result:"+result);
	//
	// if(result != null && result.search_info.length > 0) {
	// for(SearchDefines.HI_LAN_SEARCH_INFO info : result.search_info) {
	// list.add(new SearchResult(new String(info.uid).trim(), new
	// String(info.ip).trim(), (int) info.port));
	// }
	// }
	//
	// out = null;
	//
	// Message msg = handler.obtainMessage();
	// msg.what = HANDLE_MSG_LAN_SEARCH_END;
	//// msg.setData(bundle);
	// handler.sendMessage(msg);
	//
	//
	// }
	// }).start();
	// }

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	protected void onResume() {
		super.onResume();

		startSearch();
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// Bundle bundle = msg.getData();
			switch (msg.what) {
			case HiDataValue.HANDLE_MESSAGE_SCAN_RESULT:

				HiSearchResult searchResult = (HiSearchResult) msg.obj;

				if (adapter != null) {
					boolean isHave=false;
					for(int i=0;i<list.size();i++){
						HiSearchSDK.HiSearchResult result=list.get(i);
						if(result.uid.equalsIgnoreCase(searchResult.uid)){
							isHave=true;
							break;
						}
					}
					if(!isHave){
						list.add(searchResult);
						listSearchResult.requestLayout();
						adapter.notifyDataSetChanged();
					}
				}
				prsbLoading.setVisibility(View.GONE);

				if (list != null && list.size() > 0 && layFailSearch.getVisibility() == View.VISIBLE) {
					layFailSearch.setVisibility(View.GONE);
				}

				// btnRefresh.setEnabled(true);

				break;
			case HiDataValue.HANDLE_MESSAGE_SCAN_CHECK:
				if (msg.arg1 == isCheckData) {

					if (list == null || list.size() == 0) {
						searchSDK.stop();
						layFailSearch.setVisibility(View.VISIBLE);
						prsbLoading.setVisibility(View.GONE);
					}

				}
				break;
			}

		}

	};


	private class SearchResultListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public SearchResultListAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public int getCount() {

			return list.size();
		}

		public Object getItem(int position) {

			return list.get(position);
		}

		public long getItemId(int position) {

			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final HiSearchResult result = (HiSearchResult) getItem(position);
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_scan_result, null);
				holder = new ViewHolder();
				holder.uid = (TextView) convertView.findViewById(R.id.txt_camera_uid);
				holder.ip = (TextView) convertView.findViewById(R.id.txt_camera_ip);
				holder.state = (TextView) convertView.findViewById(R.id.txt_camera_state);
				convertView.setTag(holder);
			}else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.uid.setText(result.uid);
			holder.ip.setText(result.ip);
			holder.uid.setTextColor(Color.rgb(0x00, 0x00, 0x00));
			holder.state.setText(" ");
			for (MyCamera camera : HiDataValue.CameraList) {
				if (camera.getUid().equalsIgnoreCase((result.uid))) {
					holder.uid.setTextColor(Color.rgb(0x99, 0x99, 0x99));
					holder.state.setText(getString(R.string.aleary_add_device));
				}
			}
			return convertView;
		}

		public final class ViewHolder {
			public TextView uid;
			public TextView ip;
			public TextView state;
		}
	}
}
