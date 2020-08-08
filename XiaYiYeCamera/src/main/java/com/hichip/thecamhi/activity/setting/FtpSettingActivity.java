package com.hichip.thecamhi.activity.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.hichip.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.content.HiChipDefines.HI_P2P_S_FTP_PARAM_EXT;
import com.hichip.content.HiChipDefines.HI_P2P_S_FTP_PARAM;
import com.hichip.control.HiCamera;
import com.hichip.tools.Packet;
import com.hichip.hichip.widget.SwitchButton;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.utils.EmojiFilter;
import com.hichip.thecamhi.utils.FullCharFilter;
import com.hichip.thecamhi.utils.SpcialCharFilterEmailFTP;

public class FtpSettingActivity extends HiActivity implements ICameraIOSessionCallback {
    private MyCamera mCamera;
    private boolean isCheck = false;
    private EditText ftp_setting_server_edt, ftp_setting_port_edt, ftp_setting_username_edt, ftp_setting_psw_edt, ftp_setting_path_edt;
    private SwitchButton ftp_setting_mode_tgbtn;
    private HI_P2P_S_FTP_PARAM_EXT param;
    private HI_P2P_S_FTP_PARAM param_default;
    private HiChipDefines.HI_P2P_S_FTP_PARAM_EXT_NEWPWD255_EXT param_ext;
    private boolean isSupportLenExt = false;
    private boolean isSupportFullChar = false;
    private boolean isSupportGetParamExt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp_setting);

        String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);

        for (MyCamera camera : HiDataValue.CameraList) {
            if (uid.equals(camera.getUid())) {
                mCamera = camera;
                isSupportLenExt = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_FTP_PARAM_EXT_NEWPWD255_EXT);
                isSupportFullChar = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_CHAR);
                isSupportGetParamExt = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_FTP_PARAM_EXT);
                if (isSupportLenExt) {

                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_FTP_PARAM_EXT_NEWPWD255_EXT, null);
                } else if (isSupportGetParamExt){

                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_FTP_PARAM_EXT, null);
                }else {

                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_FTP_PARAM, null);
                }
                break;
            }
        }

        initView();
    }

    private void initView() {
        TitleView title = (TitleView) findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.title_ftp_settings));
        title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        FtpSettingActivity.this.finish();
                        break;

                }

            }
        });

        ftp_setting_server_edt = (EditText) findViewById(R.id.ftp_setting_server_edt);
        ftp_setting_port_edt = (EditText) findViewById(R.id.ftp_setting_port_edt);
        ftp_setting_username_edt = (EditText) findViewById(R.id.ftp_setting_username_edt);
        ftp_setting_psw_edt = (EditText) findViewById(R.id.ftp_setting_psw_edt);
        ftp_setting_path_edt = (EditText) findViewById(R.id.ftp_setting_path_edt);
        if (isSupportFullChar && isSupportLenExt) {
            ftp_setting_username_edt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(63), new FullCharFilter(this), new EmojiFilter()});
            ftp_setting_psw_edt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(63), new FullCharFilter(this), new EmojiFilter()});
        } else {
            ftp_setting_username_edt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(31), new SpcialCharFilterEmailFTP(this)});
            ftp_setting_psw_edt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(31), new SpcialCharFilterEmailFTP(this)});

        }
        ftp_setting_mode_tgbtn = (SwitchButton) findViewById(R.id.ftp_setting_mode_tgbtn);

        Button testBtn = (Button) findViewById(R.id.ftp_setting_test_btn);

        if (!isSupportLenExt && !isSupportGetParamExt) {
            testBtn.setVisibility(View.GONE);
        }

        testBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (param == null && param_ext == null) {
                    return;
                }
                isCheck = true;
                sendFTPSetting(isCheck);
            }
        });

        Button ftp_setting_application_btn = (Button) findViewById(R.id.ftp_setting_application_btn);
        ftp_setting_application_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isSupportLenExt || isSupportGetParamExt) {
                    if (param == null && param_ext == null) {
                        return;
                    }
                } else {
                    param = new HI_P2P_S_FTP_PARAM_EXT(new byte[476]);
                }

                isCheck = false;
                sendFTPSetting(isCheck);

            }
        });

    }

    protected void sendFTPSetting(boolean check) {

        String server = ftp_setting_server_edt.getText().toString();

        String port = ftp_setting_port_edt.getText().toString();
        String username = ftp_setting_username_edt.getText().toString();
        String psw = ftp_setting_psw_edt.getText().toString();
        String path = ftp_setting_path_edt.getText().toString();
        if (isSupportFullChar && isSupportLenExt) {
            if (HiTools.isMaxLength(username, 63) || HiTools.isMaxLength(psw, 63)) {
                HiToast.showToast(this, getString(R.string.tips_input_tolong));
                return;
            }
        } else {
            if ((!TextUtils.isEmpty(username) && username.getBytes().length > 31) || (!TextUtils.isEmpty(psw) && psw.getBytes().length > 31)) {
                HiToast.showToast(this, getString(R.string.tips_input_tolong));
                return;
            }
        }

        if (!TextUtils.isEmpty(port)) {
            if (Integer.parseInt(port) > 65535 || Integer.parseInt(port) <= 0) {
                if (isSupportLenExt) {
                    ftp_setting_port_edt.setText(String.valueOf(param_ext.u32Port));
                } else {
                    ftp_setting_port_edt.setText(String.valueOf(param.u32Port));
                }
                HiToast.showToast(FtpSettingActivity.this, getString(R.string.port_limit));
                return;
            } else {
                if (isSupportLenExt) {
                    param_ext.u32Port = Integer.valueOf(port);
                } else {
                    param.u32Port = Integer.valueOf(port);
                }
            }
        } else {
            HiToast.showToast(FtpSettingActivity.this, getString(R.string.tips_port_notnull));
            return;
        }
        byte[] sendParam = null;
        if (isSupportLenExt) {
            param_ext.setStrSvr(server);
            param_ext.setStrUsernm(username);
            param_ext.setStrPasswd(psw);
            param_ext.setStrFilePath(path);
            param_ext.u32Check = check ? 1 : 0;
            param_ext.u32Mode = ftp_setting_mode_tgbtn.isChecked() ? 1 : 0;
            sendParam = param_ext.parseContent();
        } else if (isSupportGetParamExt){
            param.setStrSvr(server);
            param.setStrUsernm(username);
            param.setStrPasswd(psw);
            param.setStrFilePath(path);
            param.u32Check = check ? 1 : 0;
            param.u32Mode = ftp_setting_mode_tgbtn.isChecked() ? 1 : 0;
            sendParam = param.parseContent();
        }else {
            param_default.setStrSvr(server);
            param_default.setStrUsernm(username);
            param_default.setStrPasswd(psw);
            param_default.setStrFilePath(path);
            sendParam = param_default.parseContent();
        }
        showLoadingProgress();
        if (isSupportLenExt) {

            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_FTP_PARAM_EXT_NEWPWD255_EXT, sendParam);
        } else if (isSupportGetParamExt) {

            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_FTP_PARAM_EXT, sendParam);
        } else {

            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_FTP_PARAM, sendParam);
        }
    }

    @SuppressLint("HandlerLeak")
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
                            case HiChipDefines.HI_P2P_GET_FTP_PARAM:

                                param_default = new HI_P2P_S_FTP_PARAM(data);
                                ftp_setting_server_edt.setText(new String(param_default.strSvr).trim());
                                ftp_setting_port_edt.setText(String.valueOf(param_default.u32Port).trim());
                                ftp_setting_username_edt.setText(new String(param_default.strUsernm).trim());
                                ftp_setting_psw_edt.setText(Packet.getString(param_default.strPasswd));
                                ftp_setting_path_edt.setText(Packet.getString(param_default.strFilePath));
                                ftp_setting_mode_tgbtn.setChecked(false);
                                break;
                            case HiChipDefines.HI_P2P_GET_FTP_PARAM_EXT:

                                param = new HI_P2P_S_FTP_PARAM_EXT(data);
                                ftp_setting_server_edt.setText(new String(param.strSvr).trim());
                                ftp_setting_port_edt.setText(String.valueOf(param.u32Port).trim());
                                ftp_setting_username_edt.setText(new String(param.strUsernm).trim());
                                ftp_setting_psw_edt.setText(Packet.getString(param.strPasswd));
                                ftp_setting_path_edt.setText(Packet.getString(param.strFilePath));
                                ftp_setting_mode_tgbtn.setChecked(param.u32Mode == 1);
                                break;
                            case HiChipDefines.HI_P2P_GET_FTP_PARAM_EXT_NEWPWD255_EXT:

                                param_ext = new HiChipDefines.HI_P2P_S_FTP_PARAM_EXT_NEWPWD255_EXT(data);
                                ftp_setting_server_edt.setText(new String(param_ext.strSvr).trim());
                                ftp_setting_port_edt.setText(String.valueOf(param_ext.u32Port).trim());
                                ftp_setting_username_edt.setText(new String(param_ext.strUsernm).trim());
                                ftp_setting_psw_edt.setText(Packet.getString(param_ext.strPasswd));
                                ftp_setting_path_edt.setText(Packet.getString(param_ext.strFilePath));
                                ftp_setting_mode_tgbtn.setChecked(param_ext.u32Mode == 1);
                                break;
                            case HiChipDefines.HI_P2P_SET_FTP_PARAM:
                            case HiChipDefines.HI_P2P_SET_FTP_PARAM_EXT:
                            case HiChipDefines.HI_P2P_SET_FTP_PARAM_EXT_NEWPWD255_EXT:
                                dismissLoadingProgress();
                                if (!isCheck) {
                                    HiToast.showToast(FtpSettingActivity.this, getResources().getString(R.string.ftp_setting_save_success));
                                    // finish();
                                } else {
                                    HiToast.showToast(FtpSettingActivity.this, getResources().getString(R.string.mailbox_setting_check_success));
                                }
                                break;
                        }
                    } else {
                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_SET_FTP_PARAM:
                            case HiChipDefines.HI_P2P_SET_FTP_PARAM_EXT:
                            case HiChipDefines.HI_P2P_SET_FTP_PARAM_EXT_NEWPWD255_EXT:
                                dismissLoadingProgress();
                                if (!isCheck) {
                                    HiToast.showToast(FtpSettingActivity.this, getResources().getString(R.string.ftp_setting_save_failed));
                                } else {
                                    HiToast.showToast(FtpSettingActivity.this, getResources().getString(R.string.mailbox_setting_check_failed));
                                }
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
}
