package com.hichip.thecamhi.activity.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hichip.R;
import com.hichip.hichip.activity.AlarmVoiceTypeActivity;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.data.HiDeviceInfo;
import com.hichip.hichip.widget.SwitchButton;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.main.MainActivity;
import com.hichip.thecamhi.utils.DialogUtils;
import com.hichip.thecamhi.utils.SharePreUtils;

/**
 * 计划录像 Activity
 *
 * @author lt
 */
public class TimeVideoActivity extends HiActivity implements ICameraIOSessionCallback {
    private MyCamera mCamera = null;

    private HiChipDefines.HI_P2P_QUANTUM_TIME quantum_time;
    private HiChipDefines.HI_P2P_S_REC_AUTO_PARAM rec_param;
    private boolean isAllDayRec = true;

    private static final int GOKE_TIME = 600;
    private static final int HISI_TIME = 900;
    private int recordTime = HISI_TIME;
    private EditText video_time_et;
    private SwitchButton togbtn_motion_detection;
    private RadioGroup mRgTimingVideo, rgBitRate;
    private RadioButton mRbtnNone, mRbtnAllDay;
    private int mRecordingTime;
    private Button btn_record_application;
    private RadioButton first_bit_rate;
    private RadioButton second_bit_rate;
    private int bit_type, preBitType;
    private boolean changedString;
    private LinearLayout strim_setting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timing_video);

        String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
        for (MyCamera camera : HiDataValue.CameraList) {
            if (uid.equals(camera.getUid())) {
                mCamera = camera;
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_REC_AUTO_PARAM, new byte[0]);
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_REC_AUTO_SCHEDULE, new byte[0]);
                break;

            }
        }

        initView();

    }

    private void initView() {
        strim_setting = (LinearLayout) findViewById(R.id.strim_setting);
        if (mCamera.isFishEye() && mCamera != null) {
            strim_setting.setVisibility(View.GONE);
        } else {
            strim_setting.setVisibility(View.VISIBLE);
        }
        TitleView title = (TitleView) findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.title_timing_video));
        title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        TimeVideoActivity.this.finish();
                        break;

                }
            }
        });


        if (mCamera.getChipVersion() == HiDeviceInfo.CHIP_VERSION_GOKE) {
            recordTime = GOKE_TIME;
        }
        @SuppressLint({"StringFormatInvalid", "LocalSuppress"})
        String recording_time_str = String.format(getResources().getString(R.string.tips_recording_time_range), recordTime);
        TextView tips_recording_time = (TextView) findViewById(R.id.tips_recording_time);
        tips_recording_time.setText(recording_time_str);

        video_time_et = (EditText) findViewById(R.id.video_time_et);
        togbtn_motion_detection = (SwitchButton) findViewById(R.id.togbtn_motion_detection);
        initRGTVView();

        btn_record_application = (Button) findViewById(R.id.btn_record_application);
        btn_record_application.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //                if (togbtn_motion_detection.isChecked()){
                //					//打开计划录像前 查询报警录像的参数
                //					mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_ALARM_PARAM, null);
                //				}
                updateRecodingParam();

            }
        });

    }

    private void closeAlarmRecord(HiChipDefines.HI_P2P_S_ALARM_PARAM param) {
        param.u32SDRec = 0;
        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_ALARM_PARAM, param.parseContent());
        HiToast.showToast(TimeVideoActivity.this, getString(R.string.closed_alarm_record));
    }

    private void initRGTVView() {
        String[] menuNameArrays = this.getResources().getStringArray(R.array.recording_time);
        mRgTimingVideo = (RadioGroup) findViewById(R.id.radioGroup_timing_video);
        rgBitRate = (RadioGroup) findViewById(R.id.radioGroup_bit_rate);

        mRbtnNone = (RadioButton) findViewById(R.id.radio_none);
        mRbtnNone.setText(menuNameArrays[0]);
        mRbtnAllDay = (RadioButton) findViewById(R.id.radio_all_day);
        mRbtnAllDay.setText(menuNameArrays[1]);

        first_bit_rate = (RadioButton) findViewById(R.id.first_bit_rate);
        first_bit_rate.setText(getString(R.string.video_setting_first_stream));
        second_bit_rate = (RadioButton) findViewById(R.id.second_bit_rate);
        second_bit_rate.setText(getString(R.string.video_setting_second_stream));


        mRgTimingVideo.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_none) {
                    mRecordingTime = 1;
                } else if (checkedId == R.id.radio_all_day) {
                    mRecordingTime = 2;
                }

            }
        });

        rgBitRate.setOnCheckedChangeListener(new OnCheckedChangeListener() {


            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.first_bit_rate) {
                    bit_type = 1;
                } else if (checkedId == R.id.second_bit_rate) {
                    bit_type = 0;
                }

            }
        });

    }

    public void updateRecodingParam() {
        if (quantum_time == null || rec_param == null) {
            return;
        }

        String str = video_time_et.getText().toString().trim();
        int rec_time_val = 0;
        if (str != null && str.length() > 0) {
            rec_time_val = Integer.valueOf(str);
        }

        if (rec_time_val < 15 || rec_time_val > recordTime) {
            String showText = String.format(getText(R.string.tips_recording_time_range).toString(), recordTime);
            HiToast.showToast(TimeVideoActivity.this, showText);
            return;
        }
        rec_param.u32Enable = togbtn_motion_detection.isChecked() ? 1 : 0;
        rec_param.u32FileLen = rec_time_val;
        rec_param.u32Stream = bit_type;

        if (bit_type != preBitType) {
            changedString = true;
            new DialogUtils(TimeVideoActivity.this).title(getString(R.string.tip_hint)).message(getString(R.string.change_bit_hint)).cancelText(getString(R.string.cancel)).sureText(getString(R.string.sure)).setCancelOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            }).setSureOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_REC_AUTO_PARAM, rec_param.parseContent());
                    setRecTime();
                }
            }).build().show();
        } else {
            changedString = false;
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_REC_AUTO_PARAM, rec_param.parseContent());
            setRecTime();
        }
    }

    private void setRecTime() {
        byte val = 78;
        if (mRecordingTime == 2) {
            val = 80;
        }

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 48; j++) {
                quantum_time.sDayData[i][j] = val;
            }
        }

        quantum_time.u32QtType = HiChipDefines.HI_P2P_TYPE_PLAN;
        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_REC_AUTO_SCHEDULE, quantum_time.parseContent());
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
                        //					MyCamera camera = (MyCamera)msg.obj;
                        Bundle bundle = msg.getData();
                        byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
                        switch (msg.arg1) {

                            case HiChipDefines.HI_P2P_GET_ALARM_PARAM:
                                HiChipDefines.HI_P2P_S_ALARM_PARAM param = new HiChipDefines.HI_P2P_S_ALARM_PARAM(data);
                                if (param.u32SDRec == 1) {//报警录像是开启的状态，关闭报警录像
                                    //关闭报警录像
                                    closeAlarmRecord(param);
                                }
                                break;

                            case HiChipDefines.HI_P2P_GET_REC_AUTO_PARAM:
                                rec_param = new HiChipDefines.HI_P2P_S_REC_AUTO_PARAM(data);
                                video_time_et.setText(String.valueOf(rec_param.u32FileLen));

                                boolean isOpenRec = rec_param.u32Enable == 1 ? true : false;

                                togbtn_motion_detection.setChecked(isOpenRec);


                                if (rec_param.u32Stream == 1) {
                                    first_bit_rate.setChecked(true);
                                    preBitType = 1;
                                    bit_type = 1;
                                } else {
                                    second_bit_rate.setChecked(true);
                                    preBitType = 0;
                                    bit_type = 0;
                                }

                                break;

                            case HiChipDefines.HI_P2P_GET_REC_AUTO_SCHEDULE:

                                quantum_time = new HiChipDefines.HI_P2P_QUANTUM_TIME(data);
                                for (int i = 0; i < 7; i++) {
                                    for (int j = 0; j < 48; j++) {
                                        if (quantum_time.sDayData[i][j] == 78) {
                                            isAllDayRec = false;
                                            mRbtnNone.setChecked(true);
                                            break;
                                        }
                                    }
                                }


                                if (!isAllDayRec) {
                                    dismissLoadingProgress();
                                    break;
                                }
                                mRbtnAllDay.setChecked(true);

                                dismissLoadingProgress();

                                break;
                            case HiChipDefines.HI_P2P_GET_SD_INFO: {
                                HiChipDefines.HI_P2P_S_SD_INFO sd_info = new HiChipDefines.HI_P2P_S_SD_INFO(data);

                                int used = sd_info.u32Space - sd_info.u32LeftSpace;
                            }
                            break;
                            case HiChipDefines.HI_P2P_SET_REC_AUTO_SCHEDULE:
                                HiToast.showToast(TimeVideoActivity.this, getString(R.string.tips_time_video));
                                /*�ı���������������������ص�������б�*/
                                if (changedString) {
                                    mCamera.isSystemState = 1;
                                    startActivity(new Intent(TimeVideoActivity.this, MainActivity.class));
                                    finish();
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
