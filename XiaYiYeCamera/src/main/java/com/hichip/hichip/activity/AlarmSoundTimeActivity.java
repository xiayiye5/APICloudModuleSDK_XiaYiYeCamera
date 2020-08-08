package com.hichip.hichip.activity;

import com.hichip.R;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.main.HiActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class AlarmSoundTimeActivity extends HiActivity implements OnClickListener {

	private TitleView title;
	private int mTime;
	private ImageView iv_five, iv_ten, iv_twenty, iv_thirty;
	private RelativeLayout rl_five, rl_ten, rl_twenty, rl_thirty;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_sound_time);
		getIntentData();
		initView();
		setOnListeners();
	}

	private void setOnListeners() {
		rl_five.setOnClickListener(this);
		rl_ten.setOnClickListener(this);
		rl_twenty.setOnClickListener(this);
		rl_thirty.setOnClickListener(this);
	}

	private void getIntentData() {
		mTime = getIntent().getIntExtra("mTime", 5);
	}

	private void initView() {
		title = (TitleView) findViewById(R.id.title);
		title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		title.setTitle(getString(R.string.alarm_time_));
		title.setNavigationBarButtonListener(new NavigationBarButtonListener() {
			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					finish();
					break;
				}
			}
		});
		iv_five = (ImageView) findViewById(R.id.iv_five);
		iv_ten = (ImageView) findViewById(R.id.iv_ten);
		iv_twenty = (ImageView) findViewById(R.id.iv_twenty);
		iv_thirty = (ImageView) findViewById(R.id.iv_thirty);

		rl_five = (RelativeLayout) findViewById(R.id.rl_five);
		rl_ten = (RelativeLayout) findViewById(R.id.rl_ten);
		rl_twenty = (RelativeLayout) findViewById(R.id.rl_twenty);
		rl_thirty = (RelativeLayout) findViewById(R.id.rl_thirty);

		if (mTime == 5) {
			iv_five.setVisibility(View.VISIBLE);
			iv_ten.setVisibility(View.GONE);
			iv_twenty.setVisibility(View.GONE);
			iv_thirty.setVisibility(View.GONE);
		} else if (mTime == 10) {
			iv_five.setVisibility(View.GONE);
			iv_ten.setVisibility(View.VISIBLE);
			iv_twenty.setVisibility(View.GONE);
			iv_thirty.setVisibility(View.GONE);
		} else if (mTime == 20) {
			iv_five.setVisibility(View.GONE);
			iv_ten.setVisibility(View.GONE);
			iv_twenty.setVisibility(View.VISIBLE);
			iv_thirty.setVisibility(View.GONE);
		} else if (mTime == 30) {
			iv_five.setVisibility(View.GONE);
			iv_ten.setVisibility(View.GONE);
			iv_twenty.setVisibility(View.GONE);
			iv_thirty.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.rl_five) {
			iv_five.setVisibility(View.VISIBLE);
			iv_ten.setVisibility(View.GONE);
			iv_twenty.setVisibility(View.GONE);
			iv_thirty.setVisibility(View.GONE);
			Intent intent = new Intent();
			intent.putExtra("mTime", 5);
			setResult(RESULT_OK, intent);
			finish();
		} else if (id == R.id.rl_ten) {
			Intent intent;
			iv_five.setVisibility(View.GONE);
			iv_ten.setVisibility(View.VISIBLE);
			iv_twenty.setVisibility(View.GONE);
			iv_thirty.setVisibility(View.GONE);
			intent = new Intent();
			intent.putExtra("mTime", 10);
			setResult(RESULT_OK, intent);
			finish();
		} else if (id == R.id.rl_twenty) {
			Intent intent;
			iv_five.setVisibility(View.GONE);
			iv_ten.setVisibility(View.GONE);
			iv_twenty.setVisibility(View.VISIBLE);
			iv_thirty.setVisibility(View.GONE);
			intent = new Intent();
			intent.putExtra("mTime", 20);
			setResult(RESULT_OK, intent);
			finish();
		} else if (id == R.id.rl_thirty) {
			Intent intent;
			iv_five.setVisibility(View.GONE);
			iv_ten.setVisibility(View.GONE);
			iv_twenty.setVisibility(View.GONE);
			iv_thirty.setVisibility(View.VISIBLE);
			intent = new Intent();
			intent.putExtra("mTime", 30);
			setResult(RESULT_OK, intent);
			finish();
		}

	}

}
