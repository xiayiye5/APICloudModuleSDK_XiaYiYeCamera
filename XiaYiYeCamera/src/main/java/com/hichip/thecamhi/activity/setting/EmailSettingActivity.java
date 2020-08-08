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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.hichip.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.content.HiChipDefines.HI_P2P_S_EMAIL_PARAM_NEWPWD255_EXT;
import com.hichip.control.HiCamera;
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

import java.util.Arrays;

public class EmailSettingActivity extends HiActivity implements ICameraIOSessionCallback {
    private MyCamera mCamera;
    private EditText mailbox_setting_server_edt, mailbox_setting_port_edt, mailbox_setting_username_edt, mailbox_setting_psw_edt, mailbox_setting_receive_address_edt, mailbox_setting_sending_address_edt, mailbox_setting_theme_edt, mailbox_setting_message_edt;
    private Spinner mailbox_setting_safety_spn;
    private SwitchButton mailbox_setting_check_tgbtn;
    HiChipDefines.HI_P2P_S_EMAIL_PARAM param;
    HI_P2P_S_EMAIL_PARAM_NEWPWD255_EXT param_ext;

    private boolean isCheck = false;
    private boolean isSupportLenExt = false;
    private boolean isSupportFullChar = false;
    private boolean isSupportParamExt, isSupportNewGet, isSupportNoFullNewGet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mailbox_setting);

        String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);

        for (MyCamera camera : HiDataValue.CameraList) {
            if (uid.equals(camera.getUid())) {
                mCamera = camera;
                isSupportLenExt = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_EMAIL_PARAM_NEWPWD255_EXT);//拓展
                isSupportFullChar = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_CHAR);//全字符
                isSupportParamExt = mCamera.getCommandFunction(HiChipDefines.HI_P2P_SET_EMAIL_PARAM_EXT);//*新增email检测, email参数不保存到配置文件
                isSupportNewGet = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_EAMIL_PARAM_NEW);//EMAIL获取新接口全字符
                isSupportNoFullNewGet = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_EAMIL_PARAM_NEW_NO_CHAR);//EMAIL获取 非全字符
                Log.e("TAG", "isSupportLenExt=" + isSupportLenExt);
                Log.e("TAG", "isSupportFullChar=" + isSupportFullChar);
                Log.e("TAG", "isSupportParamExt=" + isSupportParamExt);
                Log.e("TAG", "isSupportNew=" + isSupportNewGet);
                Log.e("TAG", "isSupportNoFullNewGet=" + isSupportNoFullNewGet);
                if (isSupportNewGet) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_EAMIL_PARAM_NEW, null);
                } else if (isSupportNoFullNewGet) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_EAMIL_PARAM_NEW_NO_CHAR, null);
                } else {
                    if (isSupportLenExt) {
                        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_EMAIL_PARAM_NEWPWD255_EXT, null);
                    } else {
                        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_EMAIL_PARAM, null);
                    }
                }
                break;
            }
        }

        initView();
    }

    private void initView() {
        TitleView title = (TitleView) findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.title_mailbox_settings));
        title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        EmailSettingActivity.this.finish();
                        break;
                }
            }
        });
        mailbox_setting_server_edt = (EditText) findViewById(R.id.mailbox_setting_server_edt);
        mailbox_setting_port_edt = (EditText) findViewById(R.id.mailbox_setting_port_edt);
        mailbox_setting_username_edt = (EditText) findViewById(R.id.mailbox_setting_username_edt);
        mailbox_setting_psw_edt = (EditText) findViewById(R.id.mailbox_setting_psw_edt);
        mailbox_setting_receive_address_edt = (EditText) findViewById(R.id.mailbox_setting_receive_address_edt);
        mailbox_setting_sending_address_edt = (EditText) findViewById(R.id.mailbox_setting_sending_address_edt);
        mailbox_setting_theme_edt = (EditText) findViewById(R.id.mailbox_setting_theme_edt);
        mailbox_setting_message_edt = (EditText) findViewById(R.id.mailbox_setting_message_edt);
        mailbox_setting_safety_spn = (Spinner) findViewById(R.id.mailbox_setting_safety_spn);
        if (isSupportNoFullNewGet || isSupportNewGet) {
            mailbox_setting_receive_address_edt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(191)});
        }
        if (isSupportFullChar && isSupportLenExt) {
            mailbox_setting_username_edt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(63), new FullCharFilter(this), new EmojiFilter()});
            mailbox_setting_psw_edt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(63), new FullCharFilter(this), new EmojiFilter()});
        } else {
            mailbox_setting_username_edt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(31), new SpcialCharFilterEmailFTP(this), new EmojiFilter()});
            mailbox_setting_psw_edt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(31), new SpcialCharFilterEmailFTP(this), new EmojiFilter()});
        }

        if (isSupportFullChar && isSupportLenExt) {
            mailbox_setting_server_edt.setFilters(new InputFilter[]{new FullCharFilter(this), new EmojiFilter()});
            mailbox_setting_sending_address_edt.setFilters(new InputFilter[]{new FullCharFilter(this), new EmojiFilter()});
            mailbox_setting_receive_address_edt.setFilters(new InputFilter[]{new FullCharFilter(this), new EmojiFilter()});
        }

        ArrayAdapter<CharSequence> adapter_frequency = ArrayAdapter.createFromResource(this, R.array.safety_connection, android.R.layout.simple_spinner_item);
        adapter_frequency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mailbox_setting_safety_spn.setAdapter(adapter_frequency);

        mailbox_setting_check_tgbtn = (SwitchButton) findViewById(R.id.mailbox_setting_check_tgbtn);

        Button testBtn = (Button) findViewById(R.id.mailbox_setting_test_btn);
        if (!isSupportLenExt && !isSupportParamExt) {
            testBtn.setVisibility(View.GONE);
        }

        testBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //                if (param == null) {
                //                    return;
                //                }
                isCheck = true;
                sendMailSetting(isCheck);
            }
        });

        Button mailbox_setting_application_btn = (Button) findViewById(R.id.mailbox_setting_application_btn);
        mailbox_setting_application_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //                if (param == null) {
                //                    return;
                //                }
                isCheck = false;
                sendMailSetting(isCheck);

            }
        });
    }

    protected void sendMailSetting(boolean check) {

        String serverStr = mailbox_setting_server_edt.getText().toString().trim();
        String portStr = mailbox_setting_port_edt.getText().toString().trim();
        String usernameStr = mailbox_setting_username_edt.getText().toString().trim();
        String pswStr = mailbox_setting_psw_edt.getText().toString().trim();

        String sendingStr = mailbox_setting_receive_address_edt.getText().toString().trim();
        String receiveStr = mailbox_setting_sending_address_edt.getText().toString().trim();
        String themeStr = mailbox_setting_theme_edt.getText().toString().trim();
        Log.e("==", (themeStr == null) + "---" + themeStr.isEmpty());
        if (themeStr == null || themeStr.isEmpty()) {
            themeStr = "\u00a0";
        }
        Log.e("==themeStr.length==", themeStr.length() + "");
        String messageStr = mailbox_setting_message_edt.getText().toString().trim();
        if (isSupportNewGet || isSupportNoFullNewGet) {
            if (HiTools.isMaxLength(sendingStr, 191))//63*3+2
            {
                Log.e("==sendingStr.length==", sendingStr.length() + "");
                HiToast.showToast(EmailSettingActivity.this, getString(R.string.toast_adress_toolong));
                return;
            }
        } else {
            if (HiTools.isMaxLength(serverStr, 63) || HiTools.isMaxLength(sendingStr, 63) || HiTools.isMaxLength(receiveStr, 63)) {
                HiToast.showToast(this, getString(R.string.toast_adress_toolong));
                return;
            }
        }
        if (!TextUtils.isEmpty(portStr)) {
            if (Integer.parseInt(portStr) > 65535 || Integer.parseInt(portStr) <= 0) {
                if (isSupportLenExt) {
                    mailbox_setting_port_edt.setText(String.valueOf(param_ext.u32Port));
                } else {
                    mailbox_setting_port_edt.setText(String.valueOf(param.u32Port));
                }
                HiToast.showToast(EmailSettingActivity.this, getString(R.string.port_limit));
                return;
            } else {
                if (isSupportLenExt) {
                    param_ext.u32Port = Integer.valueOf(portStr);
                } else {
                    param.u32Port = Integer.valueOf(portStr);
                }
            }
        } else {
            HiToast.showToast(EmailSettingActivity.this, getString(R.string.tips_port_notnull));
            return;
        }

        if (isSupportFullChar && isSupportLenExt) {
            if (HiTools.isMaxLength(usernameStr, 63) || HiTools.isMaxLength(pswStr, 63)) {
                HiToast.showToast(this, getString(R.string.toast_user_pass_tolong));
                return;
            }
        } else {
            if ((!TextUtils.isEmpty(usernameStr) && usernameStr.getBytes().length > 31) || (!TextUtils.isEmpty(pswStr) && pswStr.getBytes().length > 31)) {
                HiToast.showToast(this, getString(R.string.toast_user_pass_tolong));
                return;
            }
        }
        if (isSupportFullChar && isSupportLenExt) {
            if (HiTools.isMaxLength(messageStr, 127)) {
                HiToast.showToast(EmailSettingActivity.this, getString(R.string.tips_email_tolong));
                return;
            }
            if (HiTools.isMaxLength(themeStr, 127)) {
                HiToast.showToast(EmailSettingActivity.this, getString(R.string.toast_theme_tolong));
                return;
            }
        } else {
            if (messageStr.getBytes().length > 128) {
                HiToast.showToast(EmailSettingActivity.this, getString(R.string.tips_email_tolong));
                return;
            }
        }
        String[] address = sendingStr.split(";");
        if (isSupportLenExt) {
            param_ext.setStrSvr(serverStr);
            param_ext.setStrUsernm(usernameStr);
            param_ext.setStrPasswd(pswStr);
            param_ext.setStrFrom(receiveStr);
            if (isSupportNewGet) {
                initEmailParams(address, param_ext);
            } else {
                param_ext.setStrTo(sendingStr);
            }
            param_ext.setStrSubject(themeStr);
            param_ext.setStrText(messageStr);
            param_ext.u32LoginType = mailbox_setting_check_tgbtn.isChecked() ? 1 : 3;
            param_ext.u32Auth = mailbox_setting_safety_spn.getSelectedItemPosition();
        } else {
            param.setStrSvr(serverStr);
            param.setStrUsernm(usernameStr);
            param.setStrPasswd(pswStr);
            param.setStrFrom(receiveStr);
            if (isSupportNoFullNewGet) {
                initEmailParams(address, param);
            } else {
                param.setStrTo(sendingStr);
            }
            param.setStrSubject(themeStr);
            param.setStrText(messageStr);
            param.u32LoginType = mailbox_setting_check_tgbtn.isChecked() ? 1 : 3;
            param.u32Auth = mailbox_setting_safety_spn.getSelectedItemPosition();
        }
        showLoadingProgress();
        if (isSupportLenExt) {
            byte[] sendParam = HiChipDefines.HI_P2P_S_EMAIL_PARAM_EXT_NEWPWD255_EXT.parseContent(param_ext, check ? 1 : 0);
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_EMAIL_PARAM_NEWPWD255_EXT, sendParam);
        } else if (isSupportParamExt) {
            byte[] sendParam = HiChipDefines.HI_P2P_S_EMAIL_PARAM_EXT.parseContent(param, check ? 1 : 0);
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_EMAIL_PARAM_EXT, sendParam);
        } else {
            byte[] sendParam = HiChipDefines.HI_P2P_S_EMAIL_PARAM_EXT.parseContent(param, check ? 1 : 0);
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_EMAIL_PARAM, sendParam);
        }

    }

    private <T> void initEmailParams(String[] allAddress, T params) {
        for (int i = 0; i < allAddress.length; i++) {
            if (i == 3)
                break;
            String address = allAddress[i];
            if (address != null) {
                setStrTo(address, i, params);
            }
        }
    }

    private <T> void setStrTo(String str, int position, T params) {
        for (int j = position; j < 3; j++) {
            if (params instanceof HiChipDefines.HI_P2P_S_EMAIL_PARAM)
                Arrays.fill(param.strTo[j], (byte) 0);
            else
                Arrays.fill(param_ext.strTo[j], (byte) 0);
        }
        if (str != null) {
            byte[] bTo = str.getBytes();
            int len = bTo.length > 64 ? 64 : bTo.length;
            if (params instanceof HiChipDefines.HI_P2P_S_EMAIL_PARAM) {
                System.arraycopy(bTo, 0, param.strTo[position], 0, len);
            } else {
                System.arraycopy(bTo, 0, param_ext.strTo[position], 0, len);
            }
        }
    }

    @SuppressLint("HandlerLeak") private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL: {
                    if (msg.arg2 == 0) {
                        // MyCamera camera = (MyCamera)msg.obj;
                        Bundle bundle = msg.getData();
                        byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_GET_EAMIL_PARAM_NEW_NO_CHAR:
                            case HiChipDefines.HI_P2P_GET_EMAIL_PARAM:
                                param = new HiChipDefines.HI_P2P_S_EMAIL_PARAM(data);
                                mailbox_setting_server_edt.setText(new String(param.strSvr).trim());
                                mailbox_setting_port_edt.setText(String.valueOf(param.u32Port).trim());
                                mailbox_setting_username_edt.setText(new String(param.strUsernm).trim());
                                mailbox_setting_psw_edt.setText(new String(param.strPasswd).trim());

                                if (isSupportNoFullNewGet) {
                                    StringBuffer stringBuffer = new StringBuffer();
                                    for (int i = 0; i < param.strTo.length; i++) {
                                        stringBuffer.append(new String(param.strTo[i]).trim());
                                        stringBuffer.append(";");
                                        Log.e("TAG", "sss=" + new String(param.strTo[i]).trim());
                                    }
                                    Log.e("TAG", "sssbuff=" + stringBuffer.toString());
                                    if (";;;".equals(stringBuffer.toString().trim())) {//3个地址均为空
                                        mailbox_setting_receive_address_edt.setText("");
                                    } else {
                                        if (stringBuffer.toString().trim().endsWith(";;;")) {//只有第一个地址
                                            mailbox_setting_receive_address_edt.setText(stringBuffer.substring(0, stringBuffer.length() - 3));
                                        } else if (stringBuffer.toString().trim().endsWith(";;")) {//第一第二个地址
                                            mailbox_setting_receive_address_edt.setText(stringBuffer.substring(0, stringBuffer.length() - 2));
                                        } else {
                                            mailbox_setting_receive_address_edt.setText(stringBuffer.substring(0, stringBuffer.length() - 1));
                                        }
                                    }
                                } else {
                                    Log.e("TAG", "sss=" + new String(param.strTo[0]).trim());
                                    mailbox_setting_receive_address_edt.setText(new String(param.strTo[0]).trim());
                                }
                                mailbox_setting_sending_address_edt.setText(new String(param.strFrom).trim());
                                String sub = new String(param.strSubject).trim();
                                mailbox_setting_theme_edt.setText(sub);
                                Log.e("==", (sub == null) + sub + "  lenth===" + sub.length());
                                mailbox_setting_message_edt.setText(new String(param.strText).trim());
                                if (param.u32LoginType == 1) {
                                    mailbox_setting_check_tgbtn.setChecked(true);
                                } else if (param.u32LoginType == 3) {
                                    mailbox_setting_check_tgbtn.setChecked(false);
                                }
                                mailbox_setting_safety_spn.setSelection(param.u32Auth);
                                break;
                            case HiChipDefines.HI_P2P_GET_EAMIL_PARAM_NEW:
                            case HiChipDefines.HI_P2P_GET_EMAIL_PARAM_NEWPWD255_EXT:
                                param_ext = new HI_P2P_S_EMAIL_PARAM_NEWPWD255_EXT(data);
                                mailbox_setting_server_edt.setText(new String(param_ext.strSvr).trim());
                                mailbox_setting_port_edt.setText(String.valueOf(param_ext.u32Port).trim());
                                mailbox_setting_username_edt.setText(new String(param_ext.strUsernm).trim());
                                mailbox_setting_psw_edt.setText(new String(param_ext.strPasswd).trim());
                                if (isSupportNewGet) {
                                    StringBuffer stringBuffer1 = new StringBuffer();
                                    for (int i = 0; i < param_ext.strTo.length; i++) {
                                        stringBuffer1.append(new String(param_ext.strTo[i]).trim());
                                        stringBuffer1.append(";");
                                        Log.e("TAG", "sss=" + new String(param_ext.strTo[i]).trim());
                                    }
                                    Log.e("TAG", "sssbuff=" + stringBuffer1.toString());
                                    if (";;;".equals(stringBuffer1.toString().trim())) {
                                        mailbox_setting_receive_address_edt.setText("");
                                    } else {
                                        if (stringBuffer1.toString().trim().endsWith(";;;")) {
                                            mailbox_setting_receive_address_edt.setText(stringBuffer1.substring(0, stringBuffer1.length() - 3));
                                        } else if (stringBuffer1.toString().trim().endsWith(";;")) {//第一第二个地址
                                            mailbox_setting_receive_address_edt.setText(stringBuffer1.substring(0, stringBuffer1.length() - 2));
                                        } else {
                                            mailbox_setting_receive_address_edt.setText(stringBuffer1.substring(0, stringBuffer1.length() - 1));
                                        }
                                    }
                                } else {
                                    Log.e("TAG", "sss=" + new String(param_ext.strTo[0]).trim());
                                    mailbox_setting_receive_address_edt.setText(new String(param_ext.strTo[0]).trim());
                                }
                                mailbox_setting_sending_address_edt.setText(new String(param_ext.strFrom).trim());
                                mailbox_setting_theme_edt.setText(new String(param_ext.strSubject).trim());
                                // mailbox_setting_message_edt.setText(Packet.getString(param.strText));
                                mailbox_setting_message_edt.setText(new String(param_ext.strText).trim());
                                if (param_ext.u32LoginType == 1) {
                                    mailbox_setting_check_tgbtn.setChecked(true);
                                } else if (param_ext.u32LoginType == 3) {
                                    mailbox_setting_check_tgbtn.setChecked(false);
                                }
                                mailbox_setting_safety_spn.setSelection(param_ext.u32Auth);
                                break;
                            case HiChipDefines.HI_P2P_SET_EMAIL_PARAM_NEWPWD255_EXT:
                            case HiChipDefines.HI_P2P_SET_EMAIL_PARAM_EXT:
                            case HiChipDefines.HI_P2P_SET_EMAIL_PARAM:
                                dismissLoadingProgress();
                                if (!isCheck) {
                                    HiToast.showToast(EmailSettingActivity.this, getResources().getString(R.string.mailbox_setting_save_success));
                                    // finish();
                                } else {
                                    HiToast.showToast(EmailSettingActivity.this, getResources().getString(R.string.mailbox_setting_check_success));
                                }
                                break;

                        }
                    } else {
                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_SET_EMAIL_PARAM:
                            case HiChipDefines.HI_P2P_SET_EMAIL_PARAM_EXT:
                            case HiChipDefines.HI_P2P_SET_EMAIL_PARAM_NEWPWD255_EXT:
                                dismissLoadingProgress();
                                if (!isCheck) {
                                    HiToast.showToast(EmailSettingActivity.this, getResources().getString(R.string.mailbox_setting_save_failed));
                                } else {
                                    HiToast.showToast(EmailSettingActivity.this, getResources().getString(R.string.mailbox_setting_check_failed));
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

    public String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append((int) chars[i]).append(",");
            } else {
                sbu.append((int) chars[i]);
            }
        }
        return sbu.toString();
    }
}
