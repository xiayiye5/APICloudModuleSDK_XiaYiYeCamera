package com.hichip.hichip.pictureviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.hichip.customview.dialog.NiftyDialogBuilder;
import com.hichip.R;
import com.hichip.thecamhi.activity.LocalPictureActivity;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.widget.stickygridview.GridItem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.TextView;

public class ImagePagerActivity extends FragmentActivity {
	private int pagerPosition = 0;
	private ArrayList<GridItem> mGirdList;
	private HackyViewPager mPager;
	private TextView indicator;
	private TitleView nb;

	private String time;
	private GridItem mSelectGridItem;
	private ImagePagerAdapter mAdapter;
	public static final String FILENAME = "filename";
	public static final String INDEX = "index";
	private List<Fragment> fragments = new ArrayList<Fragment>();
	public static final String BROAD_ACTION = "action_delete";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_detail_pager);
		getIntentData();
		initView();
		setListeners();
		initData();

	}

	private void setListeners() {
		// 更新下标
		mPager.addOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				CharSequence text = getString(R.string.viewpager_indicator, arg0 + 1, fragments.size());
				indicator.setText(text);

				time = mGirdList.get(arg0).getTime();
				mSelectGridItem = mGirdList.get(arg0);
				pagerPosition = arg0;
				nb.setTitle(time);

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}

	private void getIntentData() {
		pagerPosition = getIntent().getIntExtra(LocalPictureActivity.EXTRA_POSITION, 0);
		mGirdList = getIntent().getParcelableArrayListExtra(LocalPictureActivity.EXTRA_GIRDLST);
		mSelectGridItem = mGirdList.get(pagerPosition);
		for (GridItem gridItem : mGirdList) {
			fragments.add(ImageDetailFragment.newInstance(gridItem.getPath()));
		}

	}

	private void initData() {
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mAdapter);
		mPager.setCurrentItem(pagerPosition);
	}

	private void initView() {
		nb = (TitleView) findViewById(R.id.image_preview_title_top);
		nb.setTitle(mGirdList.get(pagerPosition).getTime());
		nb.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		nb.setButton(TitleView.NAVIGATION_BUTTON_RIGHT);
		nb.setRightBtnTextBackround(R.drawable.ic_delete);
		nb.setNavigationBarButtonListener(new NavigationBarButtonListener() {
			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					Intent intent = new Intent();
					setResult(RESULT_CANCELED, intent);
					finish();
					overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);
					break;
				case TitleView.NAVIGATION_BUTTON_RIGHT:
					showDeleteDialog();
					break;
				}
			}
		});
		mPager = (HackyViewPager) findViewById(R.id.pager);
		indicator = (TextView) findViewById(R.id.indicator);
		CharSequence text = getString(R.string.viewpager_indicator, pagerPosition + 1, fragments.size());
		indicator.setText(text);

	}

	protected void showDeleteDialog() {
		final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(this);
		dialog.withMessage(getString(R.string.tips_msg_delete_snapshot))//getString(R.string.tips_msg_delete_snapshot)
				.withButton1Text(getString(R.string.btn_no))
				.withButton2Text(getString(R.string.btn_ok))
				.setButton1Click(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		})
				.setButton2Click(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				File file = new File(mGirdList.get(pagerPosition).getPath());
				file.delete();
				Intent intent = new Intent();
				intent.putExtra(INDEX, pagerPosition);
				intent.setAction(BROAD_ACTION);
				sendBroadcast(intent);
				fragments.remove(pagerPosition);
				mGirdList.remove(pagerPosition);
				mAdapter.notifyDataSetChanged();
				if (fragments.size() == 0) {
					ImagePagerActivity.this.finish();
					LocalPictureActivity.mActivity.finish();
					overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);
				} else {
					CharSequence text = getString(R.string.viewpager_indicator, pagerPosition + 1, fragments.size());
					indicator.setText(text);
					time = mGirdList.get(pagerPosition).getTime();
					nb.setTitle(time);
				}
			}
		}).show();
	}

	private class ImagePagerAdapter extends FragmentStatePagerAdapter {

		public ImagePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getItemPosition(Object object) {
			return mAdapter.POSITION_NONE;
		}
	}

}
