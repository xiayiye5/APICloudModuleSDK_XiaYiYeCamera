package com.hichip.thecamhi.activity.setting;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import com.hichip.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.hichip.widget.NotCopyAndPaste;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.utils.EmojiFilter;
import com.hichip.thecamhi.utils.FullCharFilter;
import com.hichip.thecamhi.utils.FullCharUnionFilter;
import com.hichip.thecamhi.utils.SpcialCharFilter;
/**
 * �޸����봰��
 * @author lt
 */
public class PasswordSettingActivity extends HiActivity implements ICameraIOSessionCallback, TextWatcher {
	private MyCamera mCamera = null;
	private EditText edt_current_password;
	private EditText edt_new_password;
	private EditText edt_confirm_password;
	private String newPassword;
	private boolean isSupportLenExt = false;// �Ƿ�֧�����볤����չ
	private boolean isSupportFullChar=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_password);

		String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);

		for (MyCamera camera : HiDataValue.CameraList) {
			if (uid.equals(camera.getUid())) {
				mCamera = camera;
				isSupportLenExt = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_USER_PARAM_EXT);
				isSupportFullChar=mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_CHAR);
				break;
			}
		}

		initView();
	}

	private void initView() {
		TitleView nb = (TitleView) findViewById(R.id.title_top);

		nb.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		nb.setTitle(getString(R.string.title_modify_password));
		nb.setNavigationBarButtonListener(new TitleView.NavigationBarButtonListener() {

			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					PasswordSettingActivity.this.finish();
					break;

				}
			}
		});

		edt_current_password = (EditText) findViewById(R.id.edt_current_password);
		edt_current_password.setCustomSelectionActionModeCallback(new NotCopyAndPaste());
		edt_new_password = (EditText) findViewById(R.id.edt_new_password);
		edt_new_password.setCustomSelectionActionModeCallback(new NotCopyAndPaste());
		edt_confirm_password = (EditText) findViewById(R.id.edt_confirm_password);
		edt_confirm_password.setCustomSelectionActionModeCallback(new NotCopyAndPaste());
		
		edt_current_password.setFilters(new InputFilter[] { new InputFilter.LengthFilter(63), new FullCharUnionFilter(PasswordSettingActivity.this), new EmojiFilter() });
		
		if(isSupportFullChar&&isSupportLenExt){
			edt_new_password.setFilters(new InputFilter[] { new InputFilter.LengthFilter(63), new FullCharFilter(PasswordSettingActivity.this), new EmojiFilter() });
			edt_confirm_password.setFilters(new InputFilter[] { new InputFilter.LengthFilter(63), new FullCharFilter(PasswordSettingActivity.this), new EmojiFilter() });
		}else {
			edt_new_password.setFilters(new InputFilter[] { new InputFilter.LengthFilter(31), new SpcialCharFilter(PasswordSettingActivity.this), new EmojiFilter() });
			edt_confirm_password.setFilters(new InputFilter[] { new InputFilter.LengthFilter(31), new SpcialCharFilter(PasswordSettingActivity.this), new EmojiFilter() });
		}
		
		//edt_current_password.addTextChangedListener(this);
		edt_new_password.addTextChangedListener(this);
		edt_confirm_password.addTextChangedListener(this);

		CheckBox show_psw_cb = (CheckBox) findViewById(R.id.show_psw_cb);

		show_psw_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton btn, boolean check) {
				if (check) {
					// ��ʾ����
					edt_current_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					edt_new_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					edt_confirm_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				} else {
					edt_current_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
					edt_new_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
					edt_confirm_password.setTransformationMethod(PasswordTransformationMethod.getInstance());

				}
			}
		});

		Button update_password_btn = (Button) findViewById(R.id.update_password_btn);
		update_password_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				updatePassword();

			}
		});

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

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL: {
				if (msg.arg2 == 0) {
					switch (msg.arg1) {
					case HiChipDefines.HI_P2P_SET_USER_PARAM_EXT:
					case HiChipDefines.HI_P2P_SET_USER_PARAM:
						mCamera.setPassword(newPassword);
						mCamera.updateInDatabase(PasswordSettingActivity.this);

						mCamera.disconnect(1);
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								mCamera.connect();
							}
						}, 1000);
						dismissLoadingProgress();
						finish();
						HiToast.showToast(PasswordSettingActivity.this, getString(R.string.tips_modify_security_code_ok));
						}
				} else {
					dismissLoadingProgress();
					HiToast.showToast(PasswordSettingActivity.this, "failed");
				}
			}
				break;
			}
		}
	};

	public void updatePassword() {
		String oldPwd = edt_current_password.getText().toString();
		newPassword = edt_new_password.getText().toString();
		String confirmPwd = edt_confirm_password.getText().toString();

		if (!oldPwd.equals(mCamera.getPassword())) {
			HiToast.showToast(PasswordSettingActivity.this, getText(R.string.tips_old_password_is_wrong).toString());
			return;
		}
		if (!newPassword.equals(confirmPwd)) {
			HiToast.showToast(PasswordSettingActivity.this, getText(R.string.tips_new_passwords_do_not_match).toString());
			return;
		}
		if ((TextUtils.isEmpty(newPassword) && TextUtils.isEmpty(confirmPwd))) {
			HiToast.showToast(PasswordSettingActivity.this, getString(R.string.tip_not_empty));
			return;
		}
		if (isSupportLenExt) {
			if (newPassword.getBytes().length > 63 || confirmPwd.getBytes().length > 63) {
				HiToast.showToast(PasswordSettingActivity.this, getString(R.string.tips_input_tolong));
				return;
			}
		} else {
			if (newPassword.getBytes().length > 31 || confirmPwd.getBytes().length > 31) {
				HiToast.showToast(PasswordSettingActivity.this, getString(R.string.tips_input_tolong));
				return;
			}
		}
		if (mCamera != null) {
			//֮ǰ�޸������ǰ������������һ����ȥ,����ֻ����������û�����ȥ
			if (isSupportLenExt) {
				byte[] newuser = HiChipDefines.HI_P2P_S_AUTH_EXT.parseContent(0, mCamera.getUsername(), newPassword);
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_USER_PARAM_EXT, HiChipDefines.HI_P2P_SET_AUTH_EXT.parseContent(newuser));
			} else {
				byte[] old_auth = HiChipDefines.HI_P2P_S_AUTH.parseContent(0, mCamera.getUsername(), mCamera.getPassword());
				byte[] new_auth = HiChipDefines.HI_P2P_S_AUTH.parseContent(0, mCamera.getUsername(), newPassword);
				mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_USER_PARAM, HiChipDefines.HI_P2P_SET_AUTH.parseContent(new_auth, old_auth));
			}
		}
		showLoadingProgress();
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

	/**
	 * ����EditText�ļ���
	 */
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		if (isSupportFullChar&&isSupportLenExt) {
			if (s.toString().getBytes().length > 63) {
				HiToast.showToast(PasswordSettingActivity.this, getString(R.string.tip_password_limit));
				return;
			}
		} else {
			if (s.toString().length() > 31) {
				HiToast.showToast(PasswordSettingActivity.this, getString(R.string.tip_password_limit));
				return;
			}
		}

	}

}
