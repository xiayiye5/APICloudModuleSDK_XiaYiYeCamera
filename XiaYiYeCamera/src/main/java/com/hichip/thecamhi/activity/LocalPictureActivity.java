package com.hichip.thecamhi.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.hichip.customview.dialog.NiftyDialogBuilder;
import com.hichip.R;
import com.hichip.hichip.activity.FishEye.FishEyePhotoActivity;
import com.hichip.hichip.activity.WallMounted.WallMountedPhotoActivity;
import com.hichip.hichip.pictureviewer.ImagePagerActivity;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.utils.SharePreUtils;
import com.hichip.thecamhi.widget.stickygridview.GridItem;
import com.hichip.thecamhi.widget.stickygridview.StickyGridAdapter;
import com.hichip.thecamhi.widget.stickygridview.YMComparator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class LocalPictureActivity extends HiActivity implements OnClickListener {
	private String uid;
	private static final int DEFAULT_LIST_SIZE = 20;
	final List<String> IMAGE_FILES = new ArrayList<String>(DEFAULT_LIST_SIZE);
	private ArrayList<GridItem> mGirdList = new ArrayList<GridItem>();
	public static List<GridItem> mDeleteList = new ArrayList<GridItem>();
	private GridView gridview;
	private Map<String, Integer> sectionMap = new HashMap<String, Integer>();
	private static int section = 1;
	private StickyGridAdapter adapter;
	private static final int ACTIVITY_RESULT_PHOTO_VIEW = 10;
	private boolean isDeleteModel = false;
	private LinearLayout mll_anim;
	public static final String EXTRA_GIRDLST = "girdlst";
	public static final String EXTRA_POSITION = "position";
	public static Activity mActivity;
	private MyCamera mMyCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_picture);

		Bundle bundle = this.getIntent().getExtras();
		uid = bundle.getString(HiDataValue.EXTRAS_KEY_UID);
		for(MyCamera camera:HiDataValue.CameraList){
			if(uid.equalsIgnoreCase(camera.getUid())){
			    this.mMyCamera=camera;
			    break;
			}
		}
		
		initView();
		registerBrodCast();
		mActivity = this;

	}

	private void initView() {
		mGirdList.clear();
		initTopView();

		File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapshot/" + uid + "/");
		String imagesPath = folder.getAbsolutePath();
		setImagesPath(imagesPath);
		removeCorruptImage();
		gridview = (GridView) findViewById(R.id.asset_grid);
		Collections.sort(mGirdList, new YMComparator());
		for (ListIterator<GridItem> it = mGirdList.listIterator(); it.hasNext();) {
			GridItem mGridItem = it.next();
			String ym = mGridItem.getTime();
			if (!sectionMap.containsKey(ym)) {
				mGridItem.setSection(section);
				sectionMap.put(ym, section);
				section++;
			} else {
				mGridItem.setSection(sectionMap.get(ym));
			}
		}
		adapter = new StickyGridAdapter(LocalPictureActivity.this, mGirdList, gridview);
		gridview.setAdapter(adapter);

		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position < 0) {
					return;
				}
				if (isDeleteModel) {
					// +++
					// 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
					// ViewHolder holder = (ViewHolder) view.getTag();
					CheckBox checkBox = (CheckBox) view.findViewById(R.id.grid_cb);
					checkBox.setChecked(checkBox.isChecked() ? false : true);
					if (checkBox.isChecked()) {
						mDeleteList.add(mGirdList.get(position));
					} else {
						mDeleteList.remove(mGirdList.get(position));
					}
					adapter.checks[position] = checkBox.isChecked();

				} else {
					
					if (mMyCamera==null) 
						return;
					
					Intent intent = new Intent();	
					if(mMyCamera.isWallMounted){
						intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
						intent.putExtra("photo_path", mGirdList.get(position).getPath());
						intent.putExtra("position", position);
						intent.setClass(LocalPictureActivity.this, WallMountedPhotoActivity.class);
						startActivity(intent);
						return;
					}
					if(mMyCamera.isFishEye()){
						// 初始化鱼眼顶装和壁装的模式
						int num = SharePreUtils.getInt("mInstallMode", LocalPictureActivity.this, mMyCamera.getUid());
						mMyCamera.mInstallMode = num == -1 ? 0 : num;
						intent.setClass(LocalPictureActivity.this, FishEyePhotoActivity.class);
						intent.putExtra("photo_path", mGirdList.get(position).getPath());
						intent.putExtra("position", position);
					}else {
						intent.setClass(LocalPictureActivity.this, ImagePagerActivity.class);
					}
					intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
					intent.putParcelableArrayListExtra(EXTRA_GIRDLST, mGirdList);
					intent.putExtra(EXTRA_POSITION, position);
					startActivityForResult(intent, ACTIVITY_RESULT_PHOTO_VIEW);
				}
			}
		});
	}
  
	private void initTopView() {
		mll_anim = (LinearLayout) findViewById(R.id.ll_anim);
		mll_anim.setOnClickListener(this);
		final TitleView titleView = (TitleView) findViewById(R.id.title_top_lp);
		titleView.setTitle(getString(R.string.title_local_picture));
		titleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		titleView.setButton(TitleView.NAVIGATION_BUTTON_RIGHT);
		titleView.setRightBtnTextBackround(R.drawable.edit);

		titleView.setNavigationBarButtonListener(new NavigationBarButtonListener() {

			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					LocalPictureActivity.this.finish();
					break;
				case TitleView.NAVIGATION_BUTTON_RIGHT:
					mDeleteList.clear();
					if (isDeleteModel) {
						titleView.setRightBtnTextBackround(R.drawable.edit);
						adapter.setDelMode(0);
						loadAnimation(HiTools.dip2px(LocalPictureActivity.this, 50));
						isDeleteModel = false;
					} else {
						titleView.setRightBtnTextBackround(R.drawable.finish);
						loadAnimation(HiTools.dip2px(LocalPictureActivity.this, -50));
						adapter.setDelMode(1);
						isDeleteModel = true;
					}
					break;

				}
			}
		});
	}

	public final synchronized void setImagesPath(String path) {
		IMAGE_FILES.clear();
		File folder = new File(path);
		String[] imageFiles = folder.list();

		if (imageFiles != null && imageFiles.length > 0) {
			Arrays.sort(imageFiles);
			for (String imageFile : imageFiles) {
				File f = new File(path + "/" + imageFile);
				long times = f.lastModified() / 1000;
				GridItem mGridItem = new GridItem(path + "/" + imageFile, paserTimeToYM(times));
				mGirdList.add(mGridItem);

			}
			Collections.reverse(IMAGE_FILES);

		}
	}

	public final void removeCorruptImage() {
		Iterator<String> it = IMAGE_FILES.iterator();
		while (it.hasNext()) {
			String path = it.next();
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			if (bitmap == null) {
				it.remove();
			}
		}
	}

	private String paserTimeToYM(long time) {
		TimeZone tz = TimeZone.getDefault();
		TimeZone.setDefault(tz);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
		return format.format(new Date(time * 1000L));
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == ACTIVITY_RESULT_PHOTO_VIEW) {

			if (resultCode == Activity.RESULT_OK) {
				Bundle bundle = data.getExtras();
				// String bundle.getString("filename");
				int position = bundle.getInt(ImagePagerActivity.INDEX);
				mGirdList.remove(position);
				adapter.notifyDataSetChanged();
			}
		}
	}

	// 页面控件的点击事件
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_return) {
			finish();
		} else if (id == R.id.ll_anim) {
			if (mDeleteList.size() <= 0) {
				HiToast.showToast(LocalPictureActivity.this, getString(R.string.tip_delete_snap));
			} else {
				final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(LocalPictureActivity.this);
				dialog.withMessage(getString(R.string.tips_msg_delete_snapshot)).withButton1Text(getString(R.string.btn_no)).withButton2Text(getString(R.string.btn_ok)).setButton1Click(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				}).setButton2Click(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
						for (GridItem gridItem : mDeleteList) {
							File file = new File(gridItem.getPath());
							file.delete();
						}
						mGirdList.removeAll(mDeleteList);
						for (int i = 0; i < adapter.checks.length - mDeleteList.size(); i++) {
							adapter.checks[i] = false;
						}
						adapter.notifyDataSetChanged();
						mDeleteList.clear();
					}
				}).show();

			}
		}

	}

	/**
	 * 加载动画效果
	 */
	private void loadAnimation(float values) {
		float curTranslationY = mll_anim.getTranslationY();
		ObjectAnimator animator = ObjectAnimator.ofFloat(mll_anim, "translationY", curTranslationY, values);
		animator.setDuration(250);
		animator.start();
	}

	private void registerBrodCast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ImagePagerActivity.BROAD_ACTION);
		registerReceiver(mReceiver, filter);

	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ImagePagerActivity.BROAD_ACTION)) {
				int position = intent.getIntExtra(ImagePagerActivity.INDEX, 0);
				mGirdList.remove(position);
				adapter.notifyDataSetChanged();
			}

		};
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
	}

}
