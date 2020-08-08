package com.hichip.hichip.activity.Share;

import java.util.ArrayList;
import com.hichip.R;
import com.hichip.control.HiCamera;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.utils.BitmapUtils;
import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SeleShaCameraListActivity extends HiActivity implements OnClickListener, OnItemClickListener {
	private TitleView titleView;
	private ListView lv_share_camera;
	private ArrayList<MyCamera> mShareCameraList = new ArrayList<>();
	private ShareCameraListAdapter mAdapter;
	private String[] str_state;
	private ArrayList<MyCamera> mSeleShareCamerList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selesha_cameralist);
		initView();
		setListeners();
		initData();
	}

	private void setListeners() {
		lv_share_camera.setOnItemClickListener(this);

	}

	private void initData() {
		for (MyCamera camera : HiDataValue.CameraList) {
			camera.isChecked = false;
			mShareCameraList.add(camera);
		}
		str_state = getResources().getStringArray(R.array.connect_state);

		mAdapter = new ShareCameraListAdapter();
		lv_share_camera.setAdapter(mAdapter);

	}

	private void initView() {
		titleView = (TitleView) findViewById(R.id.title_share_camera);
		titleView.setButton(TitleView.NAVIGATION_BUTTON_RIGHT);
		titleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		titleView.setTitle(getString(R.string.tips_sele_share_camera));
		titleView.setNavigationBarButtonListener(new NavigationBarButtonListener() {
			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					finish();
					break;
				case TitleView.NAVIGATION_BUTTON_RIGHT:
					if (HiDataValue.ANDROID_VERSION > 23 && !HiTools.checkPermission(SeleShaCameraListActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
						HiToast.showToast(SeleShaCameraListActivity.this, getString(R.string.tips_no_permission));
						return;
					}
					if (mSeleShareCamerList.size() < 1) {
						HiToast.showToast(SeleShaCameraListActivity.this, getString(R.string.toast_sele_device));
						return;
					}
					StringBuffer sb = new StringBuffer();
					// sb.append(getString(R.string.app_name)+"_AC[");
					sb.append("[");
					int random = (int) (Math.random() * 90) + 10;
					for (int i = 0; i < mSeleShareCamerList.size(); i++) {
						MyCamera camera = mSeleShareCamerList.get(i);
						if (i < mSeleShareCamerList.size() - 1) {
							sb.append("{\"U\":" + "\"" + camera.getUid() + random + "\"," + "\"A\":" + "\"" + camera.getUsername() + random + "\",\"P\":\"" + camera.getPassword() + random + "\"},");
						} else {
							sb.append("{\"U\":" + "\"" + camera.getUid() + random + "\"," + "\"A\":" + "\"" + camera.getUsername() + random + "\",\"P\":\"" + camera.getPassword() + random + "\"}");
						}
					}
					sb.append("]");
					Intent intent = new Intent(SeleShaCameraListActivity.this, ShareQRCodeActivity.class);
					intent.putExtra("sharelist", sb.toString());
					intent.putExtra("camer_num", mSeleShareCamerList.size());
					startActivity(intent);
					break;
				}
			}
		});
		lv_share_camera = (ListView) findViewById(R.id.lv_share_camera);

	}

	private class ShareCameraListAdapter extends BaseAdapter {
		private String strState = " ";

		@Override
		public int getCount() {
			return mShareCameraList.size();
		}

		@Override
		public MyCamera getItem(int position) {
			return mShareCameraList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(SeleShaCameraListActivity.this, R.layout.item_selesha_cameralist, null);
				holder = new ViewHolder();
				holder.cb_camera = (CheckBox) convertView.findViewById(R.id.cb_share_camera);
				holder.snapshot_camera_item = (ImageView) convertView.findViewById(R.id.snapshot_camera_item);
				holder.nickname_camera_item = (TextView) convertView.findViewById(R.id.nickname_camera_item);
				holder.state_camera_item = (TextView) convertView.findViewById(R.id.state_camera_item);
				holder.uid_camera_item = (TextView) convertView.findViewById(R.id.uid_camera_item);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			MyCamera camera = mShareCameraList.get(position);
			if (camera.snapshot == null) {
				holder.snapshot_camera_item.setImageResource(R.drawable.videoclip);
			} else {
				Bitmap bitmap = BitmapUtils.setRoundedCorner(camera.snapshot, 50);
				holder.snapshot_camera_item.setImageBitmap(bitmap);
			}

			holder.uid_camera_item.setText(camera.getUid());
			holder.nickname_camera_item.setText(camera.getNikeName());

			int state = camera.getConnectState();
			switch (state) {
			case 0:// DISCONNECTED
				holder.state_camera_item.setTextColor(getResources().getColor(R.color.color_disconnected));
				break;
			case -8:
			case 1:// CONNECTING
				holder.state_camera_item.setTextColor(getResources().getColor(R.color.color_connecting));
				break;
			case 2:// CONNECTED
				holder.state_camera_item.setTextColor(getResources().getColor(R.color.color_connected));
				break;
			case 3:// WRONG_PASSWORD
				holder.state_camera_item.setTextColor(getResources().getColor(R.color.color_pass_word));
				break;
			case 4:// STATE_LOGIN
				holder.state_camera_item.setTextColor(getResources().getColor(R.color.color_login));
				break;
			}
			if (state >= 0 && state <= 4) {
				strState = str_state[state];
				holder.state_camera_item.setText(strState);
			}
			if (state == -8) {// 也要设置为连接中...
				holder.state_camera_item.setText(str_state[2]);
			}
			if (camera.isSystemState == 1 && camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
				holder.state_camera_item.setText(getString(R.string.tips_restart));
			}
			if (camera.isSystemState == 2 && camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
				holder.state_camera_item.setText(getString(R.string.tips_recovery));
			}
			if (camera.isSystemState == 3 && camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
				holder.state_camera_item.setText(getString(R.string.tips_update));
			}
			if (camera.isChecked) {
				holder.cb_camera.setChecked(true);
				convertView.setBackgroundColor(getResources().getColor(R.color.color_gray));
			} else {
				holder.cb_camera.setChecked(false);
				convertView.setBackgroundColor(getResources().getColor(R.color.white));
			}

			return convertView;

		}

		private class ViewHolder {
			CheckBox cb_camera;
			ImageView snapshot_camera_item;
			TextView nickname_camera_item;
			TextView state_camera_item;
			TextView uid_camera_item;
		}
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			MyCamera camera = mShareCameraList.get(position);
			if (camera.isChecked) {
				camera.isChecked = false;
				mSeleShareCamerList.remove(camera);
			} else {
				if(mSeleShareCamerList.size()>=10){
					HiToast.showToast(SeleShaCameraListActivity.this, getString(R.string.totast_more_ten));
					return;
				}
				camera.isChecked = true;
				mSeleShareCamerList.add(camera);
			}
			mAdapter.notifyDataSetChanged();
	}

}
