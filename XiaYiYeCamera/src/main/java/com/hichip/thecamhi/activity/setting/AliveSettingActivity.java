package com.hichip.thecamhi.activity.setting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hichip.R;
import com.hichip.hichip.activity.AlarmVoiceTypeActivity;
import com.hichip.hichip.activity.RF.RFActivity;
import com.hichip.hichip.activity.RF.RFAlarmlog;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.CamHiDefines;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.main.MainActivity;
import com.hichip.thecamhi.utils.BitmapUtils;

/**
 * 摄像机设置
 *
 * @author lt
 */
public class AliveSettingActivity extends HiActivity implements OnClickListener, ICameraIOSessionCallback {
    private MyCamera mCamera;
    // private boolean mIsSupportRF = false;
    public static Activity mActivity;
    private ImageView mIvRF;
    //private View red_spot;
    private LinearLayout ll_parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alive_setting_activity);
        String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);

        for (MyCamera camera : HiDataValue.CameraList) {
            if (uid.equals(camera.getUid())) {
                mCamera = camera;
                break;
            }
        }
        if (mCamera == null) {
            AliveSettingActivity.this.finish();
            HiToast.showToast(AliveSettingActivity.this, getString(R.string.disconnect));
            return;
        }

        //		if (mCamera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
        //			// mIsSupportRF = mCamera.getCommandFunction(HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET);
        //			// SharePreUtils.putBoolean("isSupportRF", AliveSettingActivity.this, mCamera.getUid(), mIsSupportRF);
        //		} else {
        //			// mIsSupportRF=SharePreUtils.getBoolean("isSupportRF", AliveSettingActivity.this, mCamera.getUid());
        //		}

        mActivity = this;

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera != null) {
            // if(mIsSupportRF){
            // red_spot.setVisibility(mCamera.isAlarmLog()?View.VISIBLE:View.GONE);
            // }
            mCamera.registerIOSessionListener(this);
        }
        if (mCamera.getConnectState() != HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
            for (int i = 0; i < ll_parent.getChildCount(); i++) {
                View view = ll_parent.getChildAt(i);
                view.setEnabled(false);
            }
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.unregisterIOSessionListener(this);

        }
    }

    private void initView() {
        TitleView titleView = (TitleView) findViewById(R.id.title_top);
        titleView.setTitle(getString(R.string.camera_setup));
        titleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        titleView.setNavigationBarButtonListener(new NavigationBarButtonListener() {
            @Override
            public void OnNavigationButtonClick(int which) {
                finish();
            }
        });
        ImageView snapshot_alive_setting = (ImageView) findViewById(R.id.snapshot_alive_setting);
        if (mCamera != null) {
            if (mCamera.snapshot != null) {
                Bitmap bitmap = BitmapUtils.setRoundedCorner(mCamera.snapshot, 50);
                snapshot_alive_setting.setImageBitmap(bitmap);
            }
            TextView nickname_alive_setting = (TextView) findViewById(R.id.nickname_alive_setting);
            nickname_alive_setting.setText(mCamera.getNikeName());
            TextView uid_alive_setting = (TextView) findViewById(R.id.uid_alive_setting);
            uid_alive_setting.setText(mCamera.getUid());
        }

        TextView tv_rf = (TextView) findViewById(R.id.tv_rf);
        ImageView iv_rf = (ImageView) findViewById(R.id.iv_rf);

        TextView modify_password = (TextView) findViewById(R.id.modify_password);
        modify_password.setOnClickListener(this);
        // if (mIsSupportRF) {
        // tv_rf.setVisibility(View.VISIBLE);
        // tv_rf.setOnClickListener(this);
        // iv_rf.setVisibility(View.VISIBLE);
        // iv_rf.setOnClickListener(this);
        // modify_password.setBackgroundResource(R.drawable.alive_setting_item_bg);
        // modify_password.setPadding(HiTools.dip2px(AliveSettingActivity.this, 30), HiTools.dip2px(AliveSettingActivity.this, 10), HiTools.dip2px(AliveSettingActivity.this, 10),
        // HiTools.dip2px(AliveSettingActivity.this, 10));
        // }
        LinearLayout alarm_motion_detection = (LinearLayout) findViewById(R.id.alarm_motion_detection);
        alarm_motion_detection.setOnClickListener(this);

        TextView action_with_alarm = (TextView) findViewById(R.id.action_with_alarm);
        action_with_alarm.setOnClickListener(this);
        // ��ʱ¼��
        TextView timing_video = (TextView) findViewById(R.id.timing_video);
        //		if (!mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_REC_AUTO_PARAM) && !mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_REC_AUTO_SCHEDULE)) {
        //			timing_video.setVisibility(View.GONE);
        //		}
        timing_video.setOnClickListener(this);

        TextView audio_setup = (TextView) findViewById(R.id.audio_setup);
        //		if (!mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_AUDIO_ATTR)) {
        //			audio_setup.setVisibility(View.GONE);
        //		}
        audio_setup.setOnClickListener(this);

        TextView video_settings = (TextView) findViewById(R.id.video_settings);
        //		if (!mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_VIDEO_PARAM) && !mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_VIDEO_CODE)) {
        //			video_settings.setVisibility(View.GONE);
        //		}
        video_settings.setOnClickListener(this);

        TextView wifi_settings = (TextView) findViewById(R.id.wifi_settings);
        wifi_settings.setOnClickListener(this);

        TextView sd_card_set = (TextView) findViewById(R.id.sd_card_set);
        sd_card_set.setOnClickListener(this);

        TextView equipment_time_setting = (TextView) findViewById(R.id.equipment_time_setting);
        equipment_time_setting.setOnClickListener(this);

        TextView mailbox_settings = (TextView) findViewById(R.id.mailbox_settings);
        //		if (!mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_EMAIL_PARAM)) {
        //			mailbox_settings.setVisibility(View.GONE);
        //		}
        mailbox_settings.setOnClickListener(this);

        TextView ftp_settings = (TextView) findViewById(R.id.ftp_settings);
        //		if (!mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_FTP_PARAM_EXT)) {
        //			ftp_settings.setVisibility(View.GONE);
        //		}
        ftp_settings.setOnClickListener(this);

        TextView system_settings = (TextView) findViewById(R.id.system_settings);
        //		if (!mCamera.getCommandFunction(HiChipDefines.HI_P2P_SET_RESET) && !mCamera.getCommandFunction(HiChipDefines.HI_P2P_SET_REBOOT)) {
        //			system_settings.setVisibility(View.GONE);
        //		}
        system_settings.setOnClickListener(this);

        TextView equipment_information = (TextView) findViewById(R.id.equipment_information);
        equipment_information.setOnClickListener(this);

        mIvRF = (ImageView) findViewById(R.id.iv_rf);
        mIvRF.setOnClickListener(this);
        //red_spot = findViewById(R.id.red_spot);
        ll_parent = (LinearLayout) findViewById(R.id.ll_parent);
        TextView tvHuman = findViewById(R.id.action_human_alarm);//人形报警
        tvHuman.setOnClickListener(this);
        if (mCamera.commandFunction.getCmdFunction(CamHiDefines.HI_P2P_GET_SMART_HSR_PARAM)) {
            tvHuman.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_rf) {
            Intent intent = new Intent(AliveSettingActivity.this, RFActivity.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.modify_password) {
            Intent intent = new Intent(AliveSettingActivity.this, PasswordSettingActivity.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.alarm_motion_detection) {
            Intent intent = new Intent();
            if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_SET_MD_PARAM_NEW) && !mCamera.isFishEye()) {
                intent = new Intent(this, AlarmSettingExtActivity.class);
            } else {
                intent = new Intent(this, AlarmSettingActivity.class);
            }
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.action_with_alarm) {
            Intent intent;
//            intent = new Intent(AliveSettingActivity.this, AlarmActionActivity.class);
//            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
//            startActivity(intent);
        } else if (id == R.id.timing_video) {
            Intent intent;
            intent = new Intent(AliveSettingActivity.this, TimeVideoActivity.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.audio_setup) {
            Intent intent;
            intent = new Intent(AliveSettingActivity.this, AudioSettingActivity.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.video_settings) {
            Intent intent;
            intent = new Intent(AliveSettingActivity.this, VideoSettingActivity.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.wifi_settings) {
            Intent intent;
            intent = new Intent(AliveSettingActivity.this, WifiSettingActivity.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.sd_card_set) {
            Intent intent;
            intent = new Intent(AliveSettingActivity.this, SDCardSettingActivity.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.equipment_time_setting) {
            Intent intent;
            intent = new Intent(AliveSettingActivity.this, TimeSettingActivity.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.mailbox_settings) {
            Intent intent;
            intent = new Intent(AliveSettingActivity.this, EmailSettingActivity.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.ftp_settings) {
            Intent intent;
            intent = new Intent(AliveSettingActivity.this, FtpSettingActivity.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.system_settings) {
            Intent intent;
            intent = new Intent(AliveSettingActivity.this, SystemSettingActivity.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.equipment_information) {
            Intent intent;
            intent = new Intent(AliveSettingActivity.this, DeviceInfoActivity.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.iv_rf) {// RF Alarm log
            Intent intent;
            mCamera.setAlarmLog(false);
            intent = new Intent(AliveSettingActivity.this, RFAlarmlog.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        } else if (id == R.id.action_human_alarm) {//人形报警
            Intent intent;
            intent = new Intent(AliveSettingActivity.this, AlarmHumanActivity.class);
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
            startActivity(intent);
        }

    }

    @Override
    public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {

    }

    @Override
    public void receiveSessionState(HiCamera arg0, int arg1) {
        Message message = Message.obtain();
        message.what = HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
        message.obj = arg0;
        message.arg1 = arg1;
        mHandler.sendMessage(message);

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
                    if (msg.arg1 == HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED || msg.arg1 == HiCamera.CAMERA_CONNECTION_STATE_WRONG_PASSWORD) {
                        // finish();
                        for (int i = 0; i < ll_parent.getChildCount(); i++) {
                            View view = ll_parent.getChildAt(i);
                            view.setEnabled(false);
                        }
                    } else if (msg.arg1 == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                        for (int i = 0; i < ll_parent.getChildCount(); i++) {
                            View view = ll_parent.getChildAt(i);
                            view.setEnabled(true);
                        }
                    }
                    break;
            }
        }

        ;
    };

}
