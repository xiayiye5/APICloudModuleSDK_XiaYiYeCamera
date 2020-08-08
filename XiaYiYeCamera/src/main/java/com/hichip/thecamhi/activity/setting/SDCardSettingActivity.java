package com.hichip.thecamhi.activity.setting;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.logging.LogConfigurationException;

import com.hichip.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.content.HiChipDefines.HI_P2P_S_SD_INFO;
import com.hichip.content.HiChipDefines.SD_STATE;
import com.hichip.control.HiCamera;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;

public class SDCardSettingActivity extends HiActivity implements ICameraIOSessionCallback {
	private MyCamera mCamera;
	private TextView available_space_size, storage_space_size;
	private HI_P2P_S_SD_INFO sd_info;
	private SD_STATE sd_info_4178;
	private Button format_sd_card;
	private boolean isFirstIn = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sd_card_setting);

		String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
		for (MyCamera camera : HiDataValue.CameraList) {
			if (uid.equals(camera.getUid())) {
				mCamera = camera;
				// mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_SD_INFO, new byte[0]);
				break;

			}
		}

		initView();

	}

	private void initView() {
		TitleView title = (TitleView) findViewById(R.id.title_top);
		title.setTitle(getResources().getString(R.string.title_sdcard_setting));
		title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		title.setNavigationBarButtonListener(new NavigationBarButtonListener() {

			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					SDCardSettingActivity.this.finish();
					break;
				}
			}
		});

		available_space_size = (TextView) findViewById(R.id.available_space_size);
		storage_space_size = (TextView) findViewById(R.id.storage_space_size);

		format_sd_card = (Button) findViewById(R.id.format_sd_card);
		format_sd_card.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_SDSTATE)) {
					
					if (sd_info_4178 != null && sd_info_4178.u32Status == 1) {
						//���sd����ǰ�Ƿ��ڸ�ʽ����...	
						isClickFormatButton = true;
						mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_SDSTATE, new byte[0]);
					}
				}else {
					if (sd_info != null && sd_info.u32Status == 1) {
						formatSDCard();
					}
				}
			

			}
		});
	}

	private void formatSDCard() {

		showYesNoDialog(R.string.tips_format_sd_card, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked
					showLoadingProgress(false,getResources().getString(R.string.sd_card_formating));
					progrssIsShowing = true;
					mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_FORMAT_SD, null);
					// ���ڸ�ʽ��״̬�У���ť���ɵ�
					format_sd_card.setEnabled(false);
					format_sd_card.setTextColor(getResources().getColor(R.color.color_gray_));

					break;
				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked

					break;
				}
			}
		});
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
				if (msg.arg2 == 0) {
					// MyCamera camera = (MyCamera)msg.obj;
					Bundle bundle = msg.getData();
					byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
					switch (msg.arg1) {
					case HiChipDefines.HI_P2P_GET_SD_INFO:
						sd_info = new HI_P2P_S_SD_INFO(data);
						int used = sd_info.u32Space - sd_info.u32LeftSpace;
						// sd_info.u32Status 1��ʾ��SD�� 0��ʾû��
						if (sd_info.u32Status == 0) {
							format_sd_card.setEnabled(false);
							format_sd_card.setTextColor(getResources().getColor(R.color.color_gray_));
							available_space_size.setText("0 MB");
							storage_space_size.setText("0 MB");
						} else {
							format_sd_card.setTextColor(Color.RED);
							if (sd_info.u32Space == 0) {
								available_space_size.setText(getString(R.string.tip_count));
								storage_space_size.setText(R.string.tip_count);
							} else {
								available_space_size.setText(String.valueOf(sd_info.u32LeftSpace / 1024) + " MB");
								storage_space_size.setText(String.valueOf(sd_info.u32Space / 1024) + " MB");
							}
						}

						break;
					case HiChipDefines.HI_P2P_SET_FORMAT_SD:
						// dismissLoadingProgress();
						// HiToast.showToast(SDCardSettingActivity.this,
						// getString(R.string.tips_format_sd_card_success));
						// finish();

						if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_SDSTATE)) {
							// ��4187,��ȡһ��SD����ʽ�����ȣ����ǻ�ȡsd����Ϣ						
							mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_SDSTATE, new byte[0]);
						} else {
							// ��4187
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									dismissLoadingProgress();
									HiToast.showToast(SDCardSettingActivity.this,
											getString(R.string.tips_format_sd_card_success));
									finish();
								}
							}, 10000);
						}

						break;

					// ͨ��4178��ȡSD����Ϣ
					case HiChipDefines.HI_P2P_GET_SDSTATE:
						dismissjuHuaDialog();
						sd_info_4178 = new SD_STATE(data);
						LogSdCardInfo(sd_info_4178);
						// sd_info.s32format 1��ʾ��SD�����ڸ�ʽ����0�ɹ���-1ʧ��
						if (sd_info_4178.s32format == 1) {
							// ���ڸ�ʽ�����Ƴ�3���ٴλ�ȡ��ʽ�������							
							//HiToast.showToast(SDCardSettingActivity.this, getResources().getString(R.string.sd_card_formating));	
							getSdCardStatus();

						} else {
							//�����ʽ����ť����,��⵽SD����ǰû���ڸ�ʽ�������и�ʽ������
							if (isClickFormatButton){
								isClickFormatButton = false;
								formatSDCard();
							}else {
								initSdCardInfo(sd_info_4178.s32format);		
							}
																	
						}
						
						
						isFirstIn = false;
						
						break;

					}
				}
				break;
			}
		}
	};
	private boolean isClickFormatButton;
	private boolean progrssIsShowing = false;

	@Override
	protected void onResume() {
		super.onResume();
		if (mCamera != null) {
			mCamera.registerIOSessionListener(this);
			if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_SDSTATE)) {
				// ��4187����ȡsd����Ϣ��չʾ
				showjuHuaDialog();			
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_SDSTATE, new byte[0]);
			} else {
				// ��4187
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_SD_INFO, new byte[0]);
			}

		}
	}

	protected void LogSdCardInfo(SD_STATE sd_info_41782) {
		Log.e("sd_info_41782.u32Channel", sd_info_41782.u32Channel + "");
		Log.e("sd_info_41782.u32Status", sd_info_41782.u32Status + "");
		Log.e("sd_info_41782.s32format", sd_info_41782.s32format + "");
		Log.e("sd_info_41782.u32Space", sd_info_41782.u32Space + "");
		Log.e("sd_info_41782.u32LeftSpace", sd_info_41782.u32LeftSpace + "");

	}

	protected void getSdCardStatus() {
		Log.e("==", progrssIsShowing+"");
		if (!progrssIsShowing) {
			showLoadingProgress(false,getResources().getString(R.string.sd_card_formating));
			progrssIsShowing =true;
		}
				
		format_sd_card.postDelayed(new Runnable() {

			@Override
			public void run() {				
				//��ȡ�����sd����ʽ�����ȣ����ǲ�ѯsd����Ϣ��			
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_SDSTATE, new byte[0]);
			}
		}, 3000);

	}

	protected void initSdCardInfo(int s32format) {
		dismissLoadingProgress();
		progrssIsShowing =false;
		if (s32format == 0&&!isFirstIn) {				
				HiToast.showToast(SDCardSettingActivity.this, getResources().getString(R.string.sd_card_format_success));				
		}
		if (s32format == -1&&!isFirstIn) {
		
			HiToast.showToast(SDCardSettingActivity.this, getResources().getString(R.string.sd_card_format_fail));		
		}
		// sd_info.u32Status 1��ʾ��SD�� 0��ʾû��
		if (sd_info_4178.u32Status == 0) {
			format_sd_card.setEnabled(false);
			format_sd_card.setTextColor(getResources().getColor(R.color.color_gray_));
			available_space_size.setText("0 MB");
			storage_space_size.setText("0 MB");
		} else {
			format_sd_card.setTextColor(Color.RED);
			if (sd_info_4178.u32Space == 0) {
				available_space_size.setText(getString(R.string.tip_count));
				storage_space_size.setText(R.string.tip_count);
			} else {
				available_space_size.setText(String.valueOf(sd_info_4178.u32LeftSpace / 1024) + " MB");
				storage_space_size.setText(String.valueOf(sd_info_4178.u32Space / 1024) + " MB");
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mCamera != null) {
			mCamera.unregisterIOSessionListener(this);
		}
	}

	@Override
	public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
		if (arg0 != mCamera)
			return;

		Bundle bundle = new Bundle();
		bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
		Message msg = handler.obtainMessage();
		msg.what = HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL;
		msg.obj = arg0;
		msg.arg1 = arg1;
		msg.arg2 = arg3;
		msg.setData(bundle);
		handler.sendMessage(msg);

	}

	@Override
	public void receiveSessionState(HiCamera arg0, int arg1) {
		// TODO Auto-generated method stub

	}
}
