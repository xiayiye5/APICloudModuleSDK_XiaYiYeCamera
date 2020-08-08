package com.hichip.thecamhi.activity.setting;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import com.hichip.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.data.HiDeviceInfo;
import com.hichip.sdk.HiChipP2P;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
/**
 * 音频设置 Activity
 * @author lt
 */
public class AudioSettingActivity extends HiActivity implements ICameraIOSessionCallback, OnCheckedChangeListener {

	private MyCamera mCamera = null;
	private SeekBar seekbar_audio_input, seekbar_audio_output;
	private int maxInputValue = 100;
	private int maxOutputValue = 100;
	private TextView txt_audio_output_value, txt_audio_input_value;
	private HiChipDefines.HI_P2P_S_AUDIO_ATTR audio_attr;
	private RadioGroup mRgInput;
	private RadioButton mRbtnLinear, mRbtnmic;
	private int mInputMethod = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_setting);
		String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
		for (MyCamera camera : HiDataValue.CameraList) {
			if (uid.equals(camera.getUid())) {
				mCamera = camera;
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_AUDIO_ATTR, null);
				break;
			}
		}
		initView();
	}

	private void initView() {
		TitleView title = (TitleView) findViewById(R.id.title_top);
		title.setTitle(getResources().getString(R.string.item_audio_setup));
		title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		title.setNavigationBarButtonListener(new NavigationBarButtonListener() {
			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					AudioSettingActivity.this.finish();
					break;
				}
			}
		});
		initRGView();
		txt_audio_output_value = (TextView) findViewById(R.id.txt_audio_output_value);
		txt_audio_input_value = (TextView) findViewById(R.id.txt_audio_input_value);

		seekbar_audio_input = (SeekBar) findViewById(R.id.seekbar_audio_input);

		if (mCamera.getChipVersion() == HiDeviceInfo.CHIP_VERSION_GOKE) {
			maxInputValue = 16;
			maxOutputValue = 13;
		}

		seekbar_audio_input.setMax(maxInputValue - 1);
		seekbar_audio_input.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				txt_audio_input_value.setText(String.valueOf(seekBar.getProgress() + 1));
				sendToDevice();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				txt_audio_input_value.setText((progress + 1)+"");
			}
		});

		seekbar_audio_output = (SeekBar) findViewById(R.id.seekbar_audio_output);
		seekbar_audio_output.setMax(maxOutputValue - 1);
		seekbar_audio_output.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				txt_audio_output_value.setText(String.valueOf(seekBar.getProgress() + 1));
				sendToDevice();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				txt_audio_output_value.setText((progress + 1)+"");
			}
		});
	}

	private void initRGView() {
		String[] menuNameArrays = this.getResources().getStringArray(R.array.audio_input_style);

		mRgInput = (RadioGroup) findViewById(R.id.radiogroup_audio_setting);
		mRbtnLinear = (RadioButton) findViewById(R.id.radio_linear);
		mRbtnmic = (RadioButton) findViewById(R.id.radio_microphone);
		mRbtnLinear.setText(menuNameArrays[0]);
		mRbtnmic.setText(menuNameArrays[1]);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL: {
				if (msg.arg2 == 0) {
					// MyCamera camera = (MyCamera)msg.obj;
					Bundle bundle = msg.getData();
					byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
					switch (msg.arg1) {

					case HiChipDefines.HI_P2P_GET_AUDIO_ATTR:
						audio_attr = new HiChipDefines.HI_P2P_S_AUDIO_ATTR(data);
						seekbar_audio_input.setProgress(audio_attr.u32InVol - 1);
						txt_audio_input_value.setText(String.valueOf(audio_attr.u32InVol));
						seekbar_audio_output.setProgress(audio_attr.u32OutVol - 1);
						txt_audio_output_value.setText(String.valueOf(audio_attr.u32OutVol));
						// 0����������1���������
						if (audio_attr.u32InMode == 0) {
							mRbtnLinear.setChecked(true);
							mInputMethod=0;
						} else if (audio_attr.u32InMode == 1) {
							mRbtnmic.setChecked(true);
							mInputMethod=1;
						}
						mRgInput.setOnCheckedChangeListener(AudioSettingActivity.this);
						dismissjuHuaDialog();
						break;
					case HiChipDefines.HI_P2P_SET_AUDIO_ATTR:
						dismissjuHuaDialog();
						break;

					}
				}
			}
				break;
			}
		}
	};

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

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mCamera != null) {
			mCamera.registerIOSessionListener(this);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mCamera != null) {
			mCamera.unregisterIOSessionListener(this);
		}
	}

	private void sendToDevice() {
		if (audio_attr == null) {
			return;
		}
		int invol = seekbar_audio_input.getProgress() + 1;
		int outvol = seekbar_audio_output.getProgress() + 1;
		showjuHuaDialog();
		mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_AUDIO_ATTR,
				HiChipDefines.HI_P2P_S_AUDIO_ATTR.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, audio_attr.u32Enable, audio_attr.u32Stream, audio_attr.u32AudioType, mInputMethod, invol, outvol));
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (checkedId == R.id.radio_linear) {
			mInputMethod = 0;
			sendToDevice();
		} else if (checkedId == R.id.radio_microphone) {
			mInputMethod = 1;
			sendToDevice();
		}
	}

}
