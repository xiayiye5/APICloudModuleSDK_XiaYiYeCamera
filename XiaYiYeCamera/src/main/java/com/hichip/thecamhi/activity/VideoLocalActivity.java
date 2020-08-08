package com.hichip.thecamhi.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.hichip.customview.dialog.NiftyDialogBuilder;
import com.hichip.R;
import com.hichip.hichip.activity.PlaybackLocalActivity;
import com.hichip.hichip.activity.FishEye.FishEyePlaybackLocalActivity;
import com.hichip.hichip.activity.WallMounted.WallMountedPlayLocalActivity;
import com.hichip.callback.PlayLocalFileCallback;
import com.hichip.sdk.PlayLocal;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.utils.SharePreUtils;
import com.hichip.thecamhi.widget.swipe.SwipeMenu;
import com.hichip.thecamhi.widget.swipe.SwipeMenuCreator;
import com.hichip.thecamhi.widget.swipe.SwipeMenuItem;
import com.hichip.thecamhi.widget.swipe.SwipeMenuListView;
import com.hichip.thecamhi.widget.swipe.SwipeMenuListView.OnMenuItemClickListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 本地录像窗口 录像——下载
 *
 * @author lt
 */
public class VideoLocalActivity extends HiActivity implements OnItemClickListener, PlayLocalFileCallback {

	private List<VideoInfo> video_list = new ArrayList<VideoInfo>();

	private SwipeMenuListView listViewVideo;
	private VideoInfoAdapter adapter;

	private String absolutePath;
	private MyCamera mCamera;

	private boolean delModel = false;
	private RadioGroup mRGLocAndDow;
	private RadioButton mRbtnDown, radio_local;
	private TitleView mTitle;

	private String uid;
	private boolean isRecord = true;
	private boolean isGoto;
	public static final String FILE_PATH = "file_path";
	private PlayLocal mPlayLocal;

	protected File mConverFile;

	private boolean isfirstIn = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_local);

		Bundle extras = this.getIntent().getExtras();
		uid = extras.getString(HiDataValue.EXTRAS_KEY_UID);
		isGoto = extras.getBoolean("goto");

		for (MyCamera camera : HiDataValue.CameraList) {
			if (camera.getUid().equals(uid)) {
				this.mCamera = camera;
			}
		}
		mPlayLocal = new PlayLocal();

		adapter = new VideoInfoAdapter(this);
		initView();

		listViewVideo = (SwipeMenuListView) findViewById(R.id.list_video_local);
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {

				SwipeMenuItem deleteItem = new SwipeMenuItem(VideoLocalActivity.this);
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
				deleteItem.setWidth(HiTools.dip2px(VideoLocalActivity.this, 80));
				deleteItem.setHeight(HiTools.dip2px(VideoLocalActivity.this, 200));
				menu.addMenuItem(deleteItem);
			}
		};
		listViewVideo.setMenuCreator(creator);
		listViewVideo.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public void onMenuItemClick(int position, SwipeMenu menu, int index) {
				switch (index) {
				case 0:
					deleteRecording(position);
					break;
				}
			}
		});

		listViewVideo.setOnItemClickListener(this);

		listViewVideo.setAdapter(adapter);
		adapter.notifyDataSetChanged();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mPlayLocal != null) {
			mPlayLocal.registerPlayLocalStateListener(this);
		}
		// 用户可能误删本地视频，回来时刷新下视频列表
	
			if (!isfirstIn&&!isGoto&&isRecord) {
				localVideo();
				adapter.notifyDataSetChanged();
				
			} 
			
		isfirstIn = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mPlayLocal != null) {
			mPlayLocal.unregisterPlayLocalStateListener(this);
			mPlayLocal.Stop2Mp4();
		}
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}
	}

	private void initView() {
		mTitle = (TitleView) findViewById(R.id.video_local_title_top);
		mTitle.setTitle(uid);
		mTitle.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		mTitle.setButton(TitleView.NAVIGATION_BUTTON_RIGHT);
		mTitle.setRightBtnTextBackround(R.drawable.edit);
		mTitle.setNavigationBarButtonListener(new NavigationBarButtonListener() {

			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					VideoLocalActivity.this.finish();
					break;
				case TitleView.NAVIGATION_BUTTON_RIGHT:
					delModel = !delModel;
					if (delModel) {
						mTitle.setRightBtnTextBackround(R.drawable.finish);
					} else {
						mTitle.setRightBtnTextBackround(R.drawable.edit);
					}

					if (adapter != null) {
						adapter.notifyDataSetChanged();
					}

					break;
				}

			}
		});
		localVideo();

		mRGLocAndDow = (RadioGroup) findViewById(R.id.rg_loaandonli);
		radio_local = (RadioButton) findViewById(R.id.radio_local);
		mRbtnDown = (RadioButton) findViewById(R.id.radio_online);
		if (isGoto) {
			mRbtnDown.setChecked(true);
			downloadVideo();
			isRecord = false;
			adapter.notifyDataSetChanged();

		}
		mRGLocAndDow.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radio_local) {
					localVideo();
					isRecord = true;
					adapter.notifyDataSetChanged();
				} else if (checkedId == R.id.radio_online) {
					downloadVideo();
					isRecord = false;
					adapter.notifyDataSetChanged();
				}

			}
		});

	}

	public final synchronized void setImagesPath(String path) {
		video_list.clear();
		File folder = new File(path);
		String[] imageFiles = folder.list();

		if (imageFiles != null && imageFiles.length > 0) {
			Arrays.sort(imageFiles);
			for (String imageFile : imageFiles) {
				String abpath = path + "/" + imageFile;
				int fileLen = 0;
				File dF = new File(abpath);
				FileInputStream fis;
				try {
					fis = new FileInputStream(dF);
					fileLen = fis.available();
				} catch (IOException e) {
					e.printStackTrace();
				}
				long times = dF.lastModified();

				VideoInfo vi = new VideoInfo();

				vi.filename = imageFile;
				vi.start_time = coverStr(imageFile);
				vi.fileLen = fileLen;
				vi.setTime(times);
				video_list.add(vi);
			}
			Collections.reverse(video_list);
		}
	}

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private String coverStr(String str) {
		String string = str.split("\\.")[0];
		str = string.replace("_", "");
		try {
			Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(str);
			return sdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
	}

	private class VideoInfo {
		public String filename;
		public int fileLen;
		// public long time;
		private String time;
		private String start_time;

		public String getStart_time() {
			return start_time;
		}

		public void setStart_time(String start_time) {
			this.start_time = start_time;
		}

		public String getTime() {
			return time;
		}

		public void setTime(long t) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			time = df.format(t);
		}
	}

	public class VideoInfoAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public VideoInfoAdapter(Context c) {
			this.mInflater = LayoutInflater.from(c);
		}

		public int getCount() {
			return video_list.size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		@SuppressLint("InflateParams")
		public View getView(final int position, View convertView, ViewGroup parent) {
			final VideoInfo vi = video_list.get(position);
			if (vi == null) {
				return null;
			}
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_video_local, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.txt_time);
				holder.ivLocal = (ImageView) convertView.findViewById(R.id.iv_local);
				holder.size = (TextView) convertView.findViewById(R.id.txt_size);
				holder.delete_icon_local_video = (ImageView) convertView.findViewById(R.id.delete_icon_local_video);
				holder.ivTranscord = (ImageView) convertView.findViewById(R.id.iv_transcoding);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (delModel) {
				holder.delete_icon_local_video.setVisibility(View.VISIBLE);
			} else {
				holder.delete_icon_local_video.setVisibility(View.GONE);
			}
			holder.name.setText(vi.start_time);
			holder.size.setText(HiTools.formetFileSize(vi.fileLen));
			if (isRecord) {
				holder.ivLocal.setImageResource(R.drawable.local);
			} else {
				holder.ivLocal.setImageResource(R.drawable.download);
			}
			final String[] strings = vi.filename.split("\\.");

			holder.ivTranscord.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mFirstTime = 0;
					if (strings.length > 1) {
						if (!"mp4".equalsIgnoreCase(strings[1])) {
							transDialogshow(vi);
						} else {
							DumpDialogshow(vi);
						}
					}
				}
			});
			return convertView;
		}

		private final class ViewHolder {
			public TextView name;
			public ImageView ivLocal;
			public TextView size;
			public ImageView delete_icon_local_video;
			public ImageView ivTranscord;
		}

	}

	protected void DumpDialogshow(final VideoInfo vi) {
		final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(VideoLocalActivity.this);
		dialog.isCancelable(false).withTitle(getString(R.string.to_album))
				.withMessage(getString(R.string.tint_to_album)).withButton1Text(getString(R.string.cancel))
				.withButton2Text(getString(R.string.sure)).setButton1Click(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				}).setButton2Click(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						final File file = new File(HiDataValue.LOCAL_CONVERT_PATH + uid + "/", vi.filename);
						if (HiTools.isSDCardExist() && file.exists()) {
							HiToast.showToast(VideoLocalActivity.this, getString(R.string.file_alearly_convert));
							return;
						}
						// 发通知图库更新文件显示
						showjuHuaDialog();
						if (!file.getParentFile().exists()) {
							file.getParentFile().mkdirs();
						}
						// 文件拷贝到convert文件夹
						if (file.isFile() && file.exists()) {
							file.delete();
						}
						new Thread() {
							public void run() {
								copyFile(absolutePath + "/" + vi.filename, file.getAbsolutePath());
								Message msg = Message.obtain();
								msg.obj = file;
								msg.what = 0X888;
								mHandler.sendMessage(msg);
							};
						}.start();

					}
				});
		dialog.show();

	}

	/**
	 * 复制单个文件
	 *
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) {  // 文件存在时
				InputStream inStream = new FileInputStream(oldPath); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * 
	 */
	protected void transDialogshow(final VideoInfo vi) {
		View customView = View.inflate(VideoLocalActivity.this, R.layout.dialog_transcord, null);
		final PopupWindow popupWindow = new PopupWindow(customView);
		ColorDrawable cd = new ColorDrawable(-000);
		popupWindow.setBackgroundDrawable(cd);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setFocusable(true);
		popupWindow.setWidth(LayoutParams.MATCH_PARENT);
		popupWindow.setHeight(LayoutParams.MATCH_PARENT);
		popupWindow.showAtLocation(customView, Gravity.CENTER, 0, 0);
		customView.findViewById(R.id.tv_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popupWindow.dismiss();
			}
		});
		customView.findViewById(R.id.tv_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popupWindow.dismiss();
				if (HiTools.isSDCardExist()) {
					String[] strings = vi.filename.split("\\.");
					File folderFiel = new File(HiDataValue.LOCAL_CONVERT_PATH + uid + "/");
					if (!folderFiel.exists()) {
						folderFiel.mkdirs();
					}
					mConverFile = new File(folderFiel.getAbsolutePath() + "/" + strings[0] + ".mp4");
					if (!mConverFile.exists()) {
						try {
							mConverFile.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						HiToast.showToast(VideoLocalActivity.this, getString(R.string.file_alearly_convert));
						return;
					}
					int b = mPlayLocal.Start2Mp4(absolutePath + "/" + vi.filename, mConverFile.getAbsolutePath());
					if (b != 0) {
						Toast.makeText(VideoLocalActivity.this, "转换失败", Toast.LENGTH_LONG);
					}

				}
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

		if (delModel) {
			deleteRecording(position);
		} else {
			VideoInfo videoInfo = video_list.get(position);
			Bundle bundle = new Bundle();
			bundle.putString(FILE_PATH, absolutePath + "/" + videoInfo.filename);
			bundle.putString(HiDataValue.EXTRAS_KEY_UID, uid);
			bundle.putString("strat_time", videoInfo.start_time);
			Intent intent = new Intent();
			if(mCamera==null){
				Log.e("VideoLocalActivity","camera is null");//bugly by #176350
				return;
			}
			if (mCamera.isWallMounted) {
				intent.setClass(VideoLocalActivity.this, WallMountedPlayLocalActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				return;
			}

			if (mCamera.isFishEye()) {
				// 初始化鱼眼顶装和壁装的模式
				int num = SharePreUtils.getInt("mInstallMode", VideoLocalActivity.this, uid);
				mCamera.mInstallMode = num == -1 ? 0 : num;
				intent.setClass(VideoLocalActivity.this, FishEyePlaybackLocalActivity.class);
			} else {
				intent.setClass(VideoLocalActivity.this, PlaybackLocalActivity.class);
			}
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	private void deleteRecording(final int position) {
		final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(VideoLocalActivity.this);
		dialog.withButton1Text(getString(R.string.btn_no)).withButton2Text(getString(R.string.btn_yes))
				.withMessage(getString(R.string.tips_delete_video_local)).setButton1Click(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();

					}
				}).setButton2Click(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
						File file = new File(absolutePath + "/" + video_list.get(position).filename);
						boolean deleted = file.delete();
						video_list.remove(position);
						adapter.notifyDataSetChanged();

					}
				}).show();

	}

	private void downloadVideo() {

		File folder = new File(HiDataValue.ONLINE_VIDEO_PATH + uid);

		absolutePath = folder.getAbsolutePath();

		setImagesPath(absolutePath);

	}

	private void localVideo() {
		if (mCamera != null) {
			File folder = new File(HiDataValue.LOCAL_VIDEO_PATH + uid);

			absolutePath = folder.getAbsolutePath();

			setImagesPath(absolutePath);
		}

	}

// 文件转码的回调
	/**
	 * 视频宽，高，文件时长 ，文件播放进度，音频类型，播放状态
	 */
	@Override
	public void callbackplaylocal(int width, int height, int filetime, long cursec, int audiotype, int state) {
		Message message = Message.obtain();
		switch (state) {
		case PlayLocalFileCallback.LOCAL2MP4_STATE_OPEN:
			message.what = PlayLocalFileCallback.LOCAL2MP4_STATE_OPEN;
			message.arg1 = filetime;
			mHandler.sendMessage(message);
			// showTransPupwindow();
			break;
		case PlayLocalFileCallback.LOCAL2MP4_STATE_ING:
			// if (cursec > 0) {
			if (mFirstTime == 0) {
				mFirstTime = cursec;
			}
			int sub = (int) (cursec - mFirstTime);
			// if (sub > 0) {
			message.what = PlayLocalFileCallback.LOCAL2MP4_STATE_ING;
			message.arg1 = sub;
			message.arg2 = filetime;
			mHandler.sendMessage(message);
			// }
			// }
			break;
		case PlayLocalFileCallback.LOCAL2MP4_STATE_END:
			mPlayLocal.Stop2Mp4();
			mHandler.sendEmptyMessage(PlayLocalFileCallback.LOCAL2MP4_STATE_END);
			// 发通知图库更新文件显示
			Uri localUri = Uri.fromFile(mConverFile);
			Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
			sendBroadcast(localIntent);
			break;
		case PlayLocalFileCallback.LOCAL2MP4_STATE_ERROR:
			HiToast.showToast(VideoLocalActivity.this, getString(R.string.data_parsing_error));
			break;
		}

	}

	private PopupWindow mPopupWindow;
	private TextView mTvCancel, mTvTransRote;
	private long mFirstTime = 0;
	private SeekBar mSeekBarTrans;

	private void showTransPupwindow(int filetime) {
		View customView = View.inflate(VideoLocalActivity.this, R.layout.dialog_trans_pro, null);
		mPopupWindow = new PopupWindow(customView);
		ColorDrawable cd = new ColorDrawable(-000);
		mPopupWindow.setBackgroundDrawable(cd);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);
		mPopupWindow.setWidth(LayoutParams.MATCH_PARENT);
		mPopupWindow.setHeight(LayoutParams.MATCH_PARENT);
		mPopupWindow.showAtLocation(customView, Gravity.CENTER, 0, 0);
		// 取消
		mTvCancel = (TextView) customView.findViewById(R.id.tv_cancel);
		mTvCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPopupWindow.dismiss();
			}
		});

		mPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				if (getString(R.string.cancel).equals(mTvCancel.getText().toString())) {
					mPlayLocal.Stop2Mp4();
					// 删除文件
					if (mConverFile.exists()) {
						mConverFile.delete();
					}
				}

			}
		});
		mTvTransRote = (TextView) customView.findViewById(R.id.rate_loading_trances);
		mSeekBarTrans = (SeekBar) customView.findViewById(R.id.sb_transd_video);
		mSeekBarTrans.setMax(filetime * 1000);
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0X888:
				File file = (File) msg.obj;
				if (file != null && file.exists() && file.isFile()) {
					Uri localUri = Uri.fromFile(file);
					Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
					sendBroadcast(localIntent);
					dismissjuHuaDialog();
					HiToast.showToast(VideoLocalActivity.this, getString(R.string.success_to_album));
				}
				break;
			case PlayLocalFileCallback.LOCAL2MP4_STATE_OPEN:
				showTransPupwindow(msg.arg1);
				break;
			case PlayLocalFileCallback.LOCAL2MP4_STATE_ING:
				int sub = msg.arg1;
				int fileTime = msg.arg2;
				int rate = (int) Math.round((double) sub / 1000 / fileTime * 100);
				if (rate < 100) {
					mTvTransRote.setText(rate + "%");
				}
				mSeekBarTrans.setProgress(sub);
				break;
			case PlayLocalFileCallback.LOCAL2MP4_STATE_END:
				mTvTransRote.setText("100%");
				mTvCancel.setText(getString(R.string.finish));
				break;
			case PlayLocalFileCallback.LOCAL2MP4_STATE_ERROR:
				HiToast.showToast(VideoLocalActivity.this, getString(R.string.data_parsing_error));
				if (mConverFile != null && mConverFile.exists()) {
					mConverFile.delete();
				}
				break;

			}

		};
	};

}
