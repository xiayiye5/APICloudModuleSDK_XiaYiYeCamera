package com.hichip.hichip.activity;


import com.hichip.R;
import com.hichip.system.HiDefaultData;
import com.hichip.thecamhi.activity.setting.TimeSettingActivity;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.main.HiActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TimeZoneListActivity extends HiActivity {
	private ListView mLvTimeZone;
	private String[] strings;
	private TimeZoneAdapter mAdapter;
	private int index;
	private int mPosition;
	public static final String KEY_TZ="KEY_TZ";
	private int u32DstMode;
	private boolean mIsSupportZoneExt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_time_zone_list);
		initView();
		setListenrs();
		initData();
	}

	private void setListenrs() {
		mLvTimeZone.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(mIsSupportZoneExt){//是新时区
					if(position<HiDefaultData.TimeZoneField1.length){
						//mRlXls.setVisibility("1".equals(HiDefaultData.TimeZoneField1[position][2])?View.VISIBLE:View.GONE);
						mAdapter.setSelectItem(position);
						//mSwBtnXls.setChecked(false);
						mPosition=position;
						mAdapter.notifyDataSetChanged();
					}
				}else {
					if(position<HiDefaultData.TimeZoneField.length){
						//mRlXls.setVisibility(HiDefaultData.TimeZoneField[position][1]==1?View.VISIBLE:View.GONE);
						mAdapter.setSelectItem(position);
						//mSwBtnXls.setChecked(false);
						mPosition=position;
						mAdapter.notifyDataSetChanged();
					}
				}
			}
		});
		
	}
	private void initData() {
		index=getIntent().getIntExtra(TimeSettingActivity.REQUESTCODE_INDEX, 0);
		u32DstMode=getIntent().getIntExtra("u32DstMode", 0);
		strings=getIntent().getStringArrayExtra("stringarray");
		mIsSupportZoneExt=getIntent().getBooleanExtra("boolean", false);
		mPosition=index;
		mAdapter=new TimeZoneAdapter(TimeZoneListActivity.this);
		mAdapter.setSelectItem(index);
		mLvTimeZone.setAdapter(mAdapter);
		mLvTimeZone.setSelection(index);
		//mSwBtnXls.setChecked(u32DstMode==1?true:false);
		//mRlXls.setVisibility("1".equals(HiDefaultData.TimeZoneField1[index][2])?View.VISIBLE:View.GONE);
		
		
	}

	private void initView() {
		TitleView titleView=(TitleView) findViewById(R.id.zone_title_top);
		titleView.setTitle(getString(R.string.time_zone));
		titleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		titleView.setButton(TitleView.NAVIGATION_BUTTON_RIGHT);
		titleView.setNavigationBarButtonListener(new TitleView.NavigationBarButtonListener() {

			@Override
			public void OnNavigationButtonClick(int which) {
				switch(which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					finish();
					break;
				case TitleView.NAVIGATION_BUTTON_RIGHT:
					complete();
					break;
				}
			}
		});
		//strings = getResources().getStringArray(R.array.device_timezone);
		mLvTimeZone = (ListView) findViewById(R.id.lv_time_zone);
		//mRlXls=(RelativeLayout) findViewById(R.id.ll_xls);
		//mSwBtnXls=(SwitchButton) findViewById(R.id.time_zone_xls);

	}

	protected void complete() {
		//int desMode=mSwBtnXls.isChecked()?1:0;
		Intent intent=new Intent();
		intent.putExtra(KEY_TZ, mPosition);
		//intent.putExtra(KEY_DESMODE, desMode);
		
		setResult(RESULT_OK, intent);
		finish();
		
	}

	private class TimeZoneAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private int selectItem;
		

		public TimeZoneAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return strings.length;
		}

		@Override
		public String getItem(int position) {
			return strings[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_time_zone, null);
				holder = new ViewHolder();
				holder.tvTimeZoneName=(TextView) convertView.findViewById(R.id.tv_time_zone_name);
                holder.ivTag=(ImageView) convertView.findViewById(R.id.imageView1);
                convertView.setTag(holder);
			}else {
				holder=(ViewHolder) convertView.getTag();
			}
			if(mIsSupportZoneExt){
				holder.tvTimeZoneName.setText(HiDefaultData.TimeZoneField1[position][1]+" "+getItem(position));
			}else {
				holder.tvTimeZoneName.setText(getItem(position));
				
			}
			if(position==selectItem){
				 holder.ivTag.setVisibility(View.VISIBLE);
			}else {
				holder.ivTag.setVisibility(View.GONE);
			}
			return convertView;
		}
		
		public void setSelectItem(int position){
			this.selectItem=position;
		}

		private class ViewHolder {
			public TextView tvTimeZoneName;
			public ImageView ivTag;
		}
	}
}
