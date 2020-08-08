package com.hichip.hichip.activity.WallMounted;

import java.io.File;
import com.hichip.customview.dialog.NiftyDialogBuilder;
import com.hichip.R;
import com.hichip.hichip.activity.FishEye.FishEyePhotoActivity;
import com.hichip.base.HiLog;
import com.hichip.control.HiGLMonitor;
import com.hichip.hichip.pictureviewer.ImagePagerActivity;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.MyLiveViewGLMonitor;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.utils.SharePreUtils;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WallMountedPhotoActivity extends HiActivity implements OnTouchListener, OnClickListener {
	
	private MyCamera mMyCamera;
	private int  mListPosition;
	private String pathPhoto;
	private MyLiveViewGLMonitor mMonitor;
	private TextView tv_tit;
	private ImageView btn_return;
	private ImageView iv_more;
	public  RelativeLayout ll_top;
	private boolean isFirstIn = true;
    private boolean pupIsShow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_wall_mounted_photo);
		getIntentData();
		initVeiw();
		setListeners();
	}
	
	private void setListeners() {
		mMonitor.setOnTouchListener(this);
		btn_return.setOnClickListener(this);
		iv_more.setOnClickListener(this);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (mMonitor.mVisible&&!isFirstIn ) {
			final int y = HiTools.dip2px(this,45f);
			tv_tit.postDelayed(new Runnable() {
				
				@Override
				public void run() {
                    if (!pupIsShow){
                        ll_top.animate().translationY(-y).start();
                        mMonitor.mVisible = !mMonitor.mVisible;
                    }

				}
			}, 1000);

		}
		
		isFirstIn   = false;
		
		new Thread() {
			public void run() {
				int result = mMyCamera.ShowPic(pathPhoto);
				if (result == -1) {
					mHandler.obtainMessage(110).sendToTarget();
				}
			};
		}.start();
	}


	@Override
	protected void onStop() {
		if (!mMonitor.mVisible) {
			ll_top.animate().translationY(1.0f).start();
		}
		mMonitor.mVisible = !mMonitor.mVisible;
		super.onStop();
	}
	
	private void initVeiw() {
		mMonitor = (MyLiveViewGLMonitor) findViewById(R.id.monitor_photo);
		float cirx=SharePreUtils.getFloat("chche", this, mMyCamera.getUid() + "xcircle");
		float ciry=SharePreUtils.getFloat("chche", this, mMyCamera.getUid() + "ycircle");
		float cirr=SharePreUtils.getFloat("chche", this, mMyCamera.getUid() + "rcircle");
		mMonitor.SetCirInfo(cirx, ciry, cirr);
//		mMonitor.SetViewType_EXT(mMonitor.TOP_ALL_VIEW,2);
		mMonitor.SetViewType_EXT(mMonitor.TOP_ALL_VIEW, mMyCamera.getFishModType());
		mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_SIDE, 1);
		mMonitor.SetScreenSize(getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight());
		mMyCamera.setLiveShowMonitor(mMonitor);
		mMonitor.setCamera(mMyCamera);
		tv_tit=(TextView) findViewById(R.id.tv_tit);
		String[] strs=pathPhoto.split("/");
		String name=strs[strs.length-1];
		tv_tit.setText(name.substring(4, 8)+"-"+name.substring(8,10)+"-"+name.substring(10, 12)+"  "+name.substring(13,15)+":"+name.substring(15, 17)+":"+
		name.substring(17,19));
		btn_return=(ImageView) findViewById(R.id.btn_return);
		iv_more=(ImageView) findViewById(R.id.iv_more);
		ll_top=(RelativeLayout) findViewById(R.id.ll_top);
	}

	private void getIntentData() {
		String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
		for (MyCamera camera : HiDataValue.CameraList) {
			if (uid.equalsIgnoreCase(camera.getUid())) {
				this.mMyCamera = camera;
				break;
			}
		}
		pathPhoto = getIntent().getStringExtra("photo_path");
		mListPosition=getIntent().getIntExtra("position", -1);
		
	}
	

	@SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 110:
				HiToast.showToast(WallMountedPhotoActivity.this, "error!");
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				WallMountedPhotoActivity.this.finish();
				break;

			}

		};
	};
	
	private float action_down_x;
	private float action_down_y;
	float lastX;
	float lastY;
	int xlenOld;
	int ylenOld;
	float move_x;
	float move_y;
	public float left;
	public float width;
	public float height;
	public float bottom;
	double nLenStart = 0;

	int moveX;
	int moveY;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.monitor_photo) {
			int nCnt = event.getPointerCount();
			if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN && 2 == nCnt) {
				mMonitor.setTouchMove(2);
				for (int i = 0; i < nCnt; i++) {
					float x = event.getX(i);
					float y = event.getY(i);
					Point pt = new Point((int) x, (int) y);
				}
				xlenOld = Math.abs((int) event.getX(0) - (int) event.getX(1));
				ylenOld = Math.abs((int) event.getY(0) - (int) event.getY(1));
				nLenStart = Math.sqrt((double) xlenOld * xlenOld + (double) ylenOld * ylenOld);

			} else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE && 2 == nCnt) {
				mMonitor.setTouchMove(2);
				for (int i = 0; i < nCnt; i++) {
					float x = event.getX(i);
					float y = event.getY(i);
					Point pt = new Point((int) x, (int) y);
				}
				int xlen = Math.abs((int) event.getX(0) - (int) event.getX(1));
				int ylen = Math.abs((int) event.getY(0) - (int) event.getY(1));

				int moveX = Math.abs(xlen - xlenOld);
				int moveY = Math.abs(ylen - ylenOld);

				double nLenEnd = Math.sqrt((double) xlen * xlen + (double) ylen * ylen);
				if (moveX < 20 && moveY < 20) {
					return false;
				}
				if (nLenEnd > nLenStart) {
					resetMonitorSize(true, nLenEnd);
				} else {
					resetMonitorSize(false, nLenEnd);
				}

				xlenOld = xlen;
				ylenOld = ylen;
				nLenStart = nLenEnd;
				return true;
			}  else if (event.getAction()== MotionEvent.ACTION_UP ) {
				mMonitor.setTouchMove(0);
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_return) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			WallMountedPhotoActivity.this.finish();
		} else if (id == R.id.iv_more) {
			View customView = View.inflate(this, R.layout.pup_photo_dele_share, null);
			final PopupWindow pup = new PopupWindow(customView);
			ColorDrawable cd = new ColorDrawable(-0000);
			pup.setBackgroundDrawable(cd);
			pup.setOutsideTouchable(true);
			pup.setFocusable(true);
			pup.setWidth(HiTools.dip2px(this, 90));
			pup.setHeight(LayoutParams.WRAP_CONTENT);
			int disX = HiTools.dip2px(this, 47);
			pup.showAsDropDown(iv_more, -disX, 0);
			pupIsShow = true;
			LinearLayout ll_share = (LinearLayout) customView.findViewById(R.id.ll_share);
			ll_share.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					pup.dismiss();
					pupIsShow = false;
					File file = new File(pathPhoto);
					if (file.isFile() && file.exists()) {
						Intent shareIntent = new Intent();
						Uri imageUri;
						if (HiDataValue.ANDROID_VERSION >= 24) {
							imageUri = FileProvider.getUriForFile(WallMountedPhotoActivity.this, HiDataValue.FILEPROVIDER, file);
							shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						} else {
							imageUri = Uri.fromFile(file);
						}
						shareIntent.setAction(Intent.ACTION_SEND);
						shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
						shareIntent.setType("image/*");
						startActivity(Intent.createChooser(shareIntent, "分享到"));
					}

				}
			});
			LinearLayout ll_delete = (LinearLayout) customView.findViewById(R.id.ll_delete);
			ll_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					pup.dismiss();
					pupIsShow = false;
					showDeleteDialog();
				}
			});
		}
		
	}
	protected void showDeleteDialog() {
		final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(this);
		dialog.withMessage(getString(R.string.tips_msg_delete_snapshot)).withButton1Text(getString(R.string.btn_no)).withButton2Text(getString(R.string.btn_ok)).setButton1Click(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		}).setButton2Click(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				File file=new File(pathPhoto);
				if(file.exists()&&file.isFile()){
					file.delete();
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					WallMountedPhotoActivity.this.finish();
					Intent intent=new Intent();
					intent.setAction(ImagePagerActivity.BROAD_ACTION);
					intent.putExtra(ImagePagerActivity.INDEX, mListPosition);
					sendBroadcast(intent);
				}
			}
		}).show();
	}
	
	
	private void resetMonitorSize(boolean large, double move) {
		if (mMonitor.height == 0 && mMonitor.width == 0) {
			initMatrix((int) mMonitor.screen_width, (int) mMonitor.screen_height);
		}
		moveX = (int) (move / 2);
		moveY = (int) ((move * mMonitor.screen_height / mMonitor.screen_width) / 2);
		if (large) {
			HiLog.e(" larger and larger ");
			if (mMonitor.width <= 4 * mMonitor.screen_width && mMonitor.height <= 4 * mMonitor.screen_height) {
				mMonitor.left -= (moveX / 2);
				mMonitor.bottom -= (moveY / 2);
				mMonitor.width += (moveX);
				mMonitor.height += (moveY);
			}
		} else {
			HiLog.e(" smaller and smaller ");
			mMonitor.left += (moveX / 2);
			mMonitor.bottom += (moveY / 2);
			mMonitor.width -= (moveX);
			mMonitor.height -= (moveY);
		}
		if (mMonitor.left > 0 || mMonitor.width < (int) mMonitor.screen_width || mMonitor.height < (int) mMonitor.screen_height || mMonitor.bottom > 0) {
			initMatrix((int) mMonitor.screen_width, (int) mMonitor.screen_height);
		}

		if (mMonitor.width > (int) mMonitor.screen_width) {
			mMonitor.setState(1);
		} else {
			mMonitor.setState(0);
		}

		mMonitor.setMatrix(mMonitor.left, mMonitor.bottom, mMonitor.width, mMonitor.height);
	}
	
	private void initMatrix(int screen_width, int screen_height) {
		mMonitor.left = 0;
		mMonitor.bottom = 0;
		mMonitor.width = screen_width;
		mMonitor.height = screen_height;
	}
	

	@Override
	public void onBackPressed() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		finish();
	
	}
}
