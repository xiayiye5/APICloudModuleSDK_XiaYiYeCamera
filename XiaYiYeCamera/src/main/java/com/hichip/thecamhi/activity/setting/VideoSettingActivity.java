package com.hichip.thecamhi.activity.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.hichip.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.data.HiDeviceInfo;
import com.hichip.sdk.HiChipP2P;
import com.hichip.system.HiSystemValue;
import com.hichip.thecamhi.adapter.SpAdapter;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.CamHiDefines;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.main.MainActivity;
import com.hichip.thecamhi.utils.DialogUtils;
import com.hichip.thecamhi.zxing.utils.Utils;
import com.hichip.tools.Packet;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 视频设置 Activity
 *
 * @author lt
 */
public class VideoSettingActivity extends HiActivity implements ICameraIOSessionCallback {

    private MyCamera mCamera;
    private HiChipDefines.HI_P2P_CODING_PARAM coding_param;
    private HiChipDefines.HI_P2P_S_VIDEO_PARAM video_param_hd;
    private HiChipDefines.HI_P2P_S_VIDEO_PARAM video_param_sd;
//    private HiChipDefines.HI_P2P_RESOLUTION video_param_re;
    private EditText first_code_rate_et, first_frame_rate_et, first_video_level_et, second_code_rate_et, second_frame_rate_et, second_video_level_et;
    private Button video_setting_application_btn;
    private RadioGroup mRgHz;
    private RadioButton mRbtnFifty, mRbtnSixty;
    private int mFrequency = 50;
    private TextView first_frame_rate_range, second_frame_rate_range;


    public static int FRAME_RATE_LOW = 25;
    public static int FRAME_RATE_HIGH = 30;
    public int maxFrameRate = FRAME_RATE_LOW;

    private int preFrequency;
    private int frame_rate_first;
    private int frame_rate_second;
    private boolean supportFrequency = true;
    private LinearLayout ll_frequency;
    private int count;
    private Spinner spinner;//分辨率下拉列表
    private List<String> resolutioList;
    private ArrayAdapter<String> resolutionAdapter;
    private  LinearLayout ll_resolution;
    private boolean isSuportRes=false;//支持分辨率
    HiChipDefines.HI_P2P_RESOLUTION hi_p2P_get_resolution;
    SpAdapter spAdapter;
    private boolean isChangeResolution;//分辨率是否改变
    private String oldResolution,nowResolution;//原来分辨率  应用前选中的分辨率
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_setting);

        String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);


        for (MyCamera camera : HiDataValue.CameraList) {
            if (uid.equals(camera.getUid())) {
                mCamera = camera;

              //  HiSystemValue.DEBUG_MODE = true;
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_VIDEO_IMAGE_PARAM_SCOPE, new byte[0]);//获取是否支持分辨率
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_RESOLUTION, HiChipDefines.HI_P2P_RESOLUTION.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_STREAM_1));//获取分辨率
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_VIDEO_PARAM, HiChipDefines.HI_P2P_S_VIDEO_PARAM.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_STREAM_1, 0, 0, 0, 0, 0));
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_VIDEO_PARAM, HiChipDefines.HI_P2P_S_VIDEO_PARAM.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_STREAM_2, 0, 0, 0, 0, 0));
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_VIDEO_CODE, new byte[0]);
                break;
            }
        }
        initView();
        showjuHuaDialog();
    }

    public void setVideoInfo() {
        if (supportFrequency) {
            if (coding_param == null || video_param_hd == null || video_param_sd == null) {
                return;
            }
        }

        int code_rate_first = 0;
        int code_rate_second = 0;
        frame_rate_first = 0;
        frame_rate_second = 0;
        int video_level_first = 0;
        int video_level_second = 0;

        String first_code_rate_str = first_code_rate_et.getText().toString().trim();
        String second_code_rate_str = second_code_rate_et.getText().toString().trim();
        final String first_frame_rate_str = first_frame_rate_et.getText().toString().trim();
        final String second_frame_rate_str = second_frame_rate_et.getText().toString().trim();
        final String first_str_level_str = first_video_level_et.getText().toString().trim();
        final String second_str_level_str = second_video_level_et.getText().toString().trim();

        if (first_frame_rate_str != null && first_frame_rate_str.length() > 0) {
            frame_rate_first = Integer.valueOf(first_frame_rate_str);
        }

        if (second_frame_rate_str != null && second_frame_rate_str.length() > 0) {
            frame_rate_second = Integer.valueOf(second_frame_rate_str);
        }

        if (first_code_rate_str != null && first_code_rate_str.length() > 0) {
            code_rate_first = Integer.valueOf(first_code_rate_str);
        }

        if (second_code_rate_str != null && second_code_rate_str.length() > 0) {
            code_rate_second = Integer.valueOf(second_code_rate_str);
        }

        int max_rate_first = 6144;
        if (code_rate_first < 32 || code_rate_first > max_rate_first) {
            HiToast.showToast(VideoSettingActivity.this, getText(R.string.first_tips_code_rate_range).toString());
            return;
        }
        int max_rate_second = 2048;

        if (code_rate_second < 32 || code_rate_second > max_rate_second) {
            HiToast.showToast(VideoSettingActivity.this, getText(R.string.second_tips_code_rate_range).toString());
            return;
        }

        String rangStr = getResources().getString(R.string.first_tips_frame_rate_range);
        String rangTips = String.format(rangStr, maxFrameRate);
        if (frame_rate_first < 1 || frame_rate_first > maxFrameRate) {

            HiToast.showToast(VideoSettingActivity.this, rangTips);
            return;
        }

        String rangStr1 = getResources().getString(R.string.second_tips_frame_rate_range);
        String rangTips1 = String.format(rangStr1, maxFrameRate);
        if (frame_rate_second < 1 || frame_rate_second > maxFrameRate) {

            HiToast.showToast(VideoSettingActivity.this, rangTips1);
            return;
        }

        if (first_str_level_str != null && first_str_level_str.length() > 0) {
            video_level_first = Integer.valueOf(first_str_level_str);
        }

        if (second_str_level_str != null && second_str_level_str.length() > 0) {
            video_level_second = Integer.valueOf(second_str_level_str);
        }


        int max_level = 6;
        if (video_level_first < 1 || video_level_first > max_level) {
            HiToast.showToast(VideoSettingActivity.this, getText(R.string.first_tips_video_level_range).toString());
            return;
        }
        if (video_level_second < 1 || video_level_second > max_level) {
            HiToast.showToast(VideoSettingActivity.this, getText(R.string.second_tips_video_level_range).toString());
            return;
        }

        /*国科的机器改变了视频制式增加重启提示语*/
        if (preFrequency != mFrequency && mCamera.getChipVersion() == HiDeviceInfo.CHIP_VERSION_GOKE) {

            new DialogUtils(VideoSettingActivity.this).title(getString(R.string.tip_hint)).message(getString(R.string.change_format_hint)).cancelText(getString(R.string.cancel)).sureText(getString(R.string.sure)).setCancelOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            }).setSureOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    setVideoParams(frame_rate_first, frame_rate_second, first_str_level_str, second_str_level_str);
                    startActivity(new Intent(VideoSettingActivity.this, MainActivity.class));
                    finish();
                }
            }).build().show();


        } else {
            if(spinner.getSelectedItem().toString().equals("2560x1440")){
                nowResolution="2560x1440";
            }else {
                nowResolution="2560x1920";
            }
            isChangeResolution=!nowResolution.equalsIgnoreCase(oldResolution);
            if(isChangeResolution&&mCamera.isIngenic){//分辨率改变了 而且是君正 则提示设备重启

                new DialogUtils(VideoSettingActivity.this).title(getString(R.string.tip_hint)).message(getString(R.string.sysreboot_change_resolution)).cancelText(getString(R.string.cancel)).sureText(getString(R.string.sure)).setCancelOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).setSureOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setVideoParams(frame_rate_first, frame_rate_second, first_str_level_str, second_str_level_str);
                    }
                }).build().show();
            }else {
                setVideoParams(frame_rate_first, frame_rate_second, first_str_level_str, second_str_level_str);
            }


        }


    }

    private void setVideoParams(int frame_rate_first, int frame_rate_second, String first_str_level_str, String second_str_level_str) {
        video_param_hd.u32BitRate = Integer.valueOf(first_code_rate_et.getText().toString());
        video_param_sd.u32BitRate = Integer.valueOf(second_code_rate_et.getText().toString());

        video_param_hd.u32Frame = frame_rate_first;
        video_param_sd.u32Frame = frame_rate_second;

        video_param_hd.u32Quality = Integer.valueOf(first_str_level_str);
        video_param_sd.u32Quality = Integer.valueOf(second_str_level_str);

        //frequency = video_format_spinner.getSelectedItemPosition() == 0?50:60;
        coding_param.u32Frequency = mFrequency;


        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_VIDEO_PARAM, HiChipDefines.HI_P2P_S_VIDEO_PARAM.
                parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, video_param_hd.u32Stream, video_param_hd.u32Cbr, video_param_hd.u32Frame, video_param_hd.u32BitRate, video_param_hd.u32Quality, video_param_hd.u32Frame * 2));

        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_VIDEO_PARAM, HiChipDefines.HI_P2P_S_VIDEO_PARAM.
                parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, video_param_sd.u32Stream, video_param_sd.u32Cbr, video_param_sd.u32Frame, video_param_sd.u32BitRate, video_param_sd.u32Quality, video_param_sd.u32Frame * 2));

        if (supportFrequency) {
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_VIDEO_CODE, HiChipDefines.HI_P2P_CODING_PARAM.
                    parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, coding_param.u32Frequency, coding_param.u32Profile));
        }
        if(isSuportRes){
            if(spinner.getSelectedItem().toString().equals("2560x1440")){
                hi_p2P_get_resolution.u32Resolution=16;
            }else {
                hi_p2P_get_resolution.u32Resolution=20;
            }
            byte[] resolution1 =  HiChipDefines.HI_P2P_RESOLUTION.parseContent(0,1);
            byte[] Resolutio = Packet.intToByteArray_Little(hi_p2P_get_resolution.u32Resolution);
            System.arraycopy(Resolutio, 0, resolution1, 8, 4);
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_RESOLUTION,resolution1);
            byte[] resolution2 =  HiChipDefines.HI_P2P_RESOLUTION.parseContent(0,0);
            if(hi_p2P_get_resolution.u32Resolution == 16) {
                byte[] Resolutio2 = Packet.intToByteArray_Little(17);
                System.arraycopy(Resolutio2, 0, resolution2, 8, 4);
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_RESOLUTION, resolution2);
            }else if(hi_p2P_get_resolution.u32Resolution == 20)
            {
                byte[] Resolutio2 = Packet.intToByteArray_Little(18);
                System.arraycopy(Resolutio2, 0, resolution2, 8, 4);
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_RESOLUTION, resolution2);
            }
        }

    }


    private void initView() {
        TitleView title = (TitleView) findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.item_video_settings));
        title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        VideoSettingActivity.this.finish();
                        break;
                }
            }
        });

        ll_frequency = (LinearLayout) findViewById(R.id.ll_frequency);

        first_code_rate_et = (EditText) findViewById(R.id.first_code_rate_et);
        first_frame_rate_et = (EditText) findViewById(R.id.first_frame_rate_et);
        first_video_level_et = (EditText) findViewById(R.id.first_video_level_et);
        second_code_rate_et = (EditText) findViewById(R.id.second_code_rate_et);
        second_frame_rate_et = (EditText) findViewById(R.id.second_frame_rate_et);
        second_video_level_et = (EditText) findViewById(R.id.second_video_level_et);

        first_frame_rate_range = (TextView) findViewById(R.id.first_frame_rate_range);
        second_frame_rate_range = (TextView) findViewById(R.id.second_frame_rate_range);


        if (mCamera != null && mCamera.isFishEye()) {
            /*鱼眼隐藏第二码流设置*/
            LinearLayout second_rate = (LinearLayout) findViewById(R.id.second_rate);
            second_rate.setVisibility(View.GONE);
        }


        //		video_format_spinner=(Spinner)findViewById(R.id.video_format_spinner);
        //		ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.video_frequency, android.R.layout.simple_spinner_item);
        //		adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        //		video_format_spinner.setAdapter(adapter);
        //
        //		video_format_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
        //
        //			@Override
        //			public void onItemSelected(AdapterView<?> arg0, View arg1,
        //					int position, long arg3) {
        //				if(position==0){
        //					maxFrameRate=FRAME_RATE_LOW;
        //				}else{
        //					maxFrameRate=FRAME_RATE_HIGH;
        //				}
        //				ChangedFrameRange(maxFrameRate);
        //			}
        //
        //			@Override
        //			public void onNothingSelected(AdapterView<?> arg0) {
        //				// TODO Auto-generated method stub
        //
        //			}
        //		});
        String[] menuNameArrays = this.getResources().getStringArray(R.array.video_frequency);
        mRgHz = (RadioGroup) findViewById(R.id.radiogroup_video_setting);
        mRbtnFifty = (RadioButton) findViewById(R.id.radio_fifty_hz);
        mRbtnSixty = (RadioButton) findViewById(R.id.radio_sixty_hz);
        mRbtnFifty.setText(menuNameArrays[0]);
        mRbtnSixty.setText(menuNameArrays[1]);
        mRgHz.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_fifty_hz) {
                    maxFrameRate = FRAME_RATE_LOW;
                    mFrequency = 50;
                    ChangedFrameRange(maxFrameRate);
                } else if (checkedId == R.id.radio_sixty_hz) {
                    maxFrameRate = FRAME_RATE_HIGH;
                    mFrequency = 60;
                    ChangedFrameRange(maxFrameRate);
                }
            }
        });


        video_setting_application_btn = (Button) findViewById(R.id.video_setting_application_btn);
        video_setting_application_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setVideoInfo();

            }
        });

        spinner = findViewById(R.id.sp_resolution);
        ll_resolution=findViewById(R.id.ll_resolution);
        //数据
        resolutioList = new ArrayList<String>();
        resolutioList.add("2560x1920");
        resolutioList.add("2560x1440");
        spAdapter=new SpAdapter(this,resolutioList);
        //设置样式
       spinner.setDropDownVerticalOffset(Utils.dip2px(this,30));
        //加载适配器
        spinner.setAdapter(spAdapter);

    }

    private void ChangedFrameRange(int maxFrame) {

        String strRange = getResources().getString(R.string.range_video_setting_frame_rate);
        String summary = String.format(strRange, maxFrame);

        first_frame_rate_range.setText(summary);
        second_frame_rate_range.setText(summary);


    }

    private int getBitRateValue(int stream, int level) {

        if (stream == HiChipDefines.HI_P2P_STREAM_1) {
            if (level == 0) {
                return 6144;
            } else if (level == 1) {
                return 3072;
            } else if (level == 2) {
                return 2048;
            } else if (level == 3) {
                return 1024;
            } else if (level == 4) {
                return 512;
            }
        } else if (stream == HiChipDefines.HI_P2P_STREAM_2) {
            if (level == 0) {
                return 2048;
            } else if (level == 1) {
                return 1024;
            } else if (level == 2) {
                return 512;
            } else if (level == 3) {
                return 256;
            } else if (level == 4) {
                return 128;
            }
        }

        return 0;
    }


    @SuppressLint("HandlerLeak") private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL: {
                    if (msg.arg2 == 0) {
                        //					MyCamera camera = (MyCamera)msg.obj;
                        Bundle bundle = msg.getData();
                        byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
                        switch (msg.arg1) {

                            case HiChipDefines.HI_P2P_GET_VIDEO_CODE:
                                coding_param = new HiChipDefines.HI_P2P_CODING_PARAM(data);

                                if (coding_param.u32Frequency == 50) {
                                    //video_format_spinner.setSelection(0);
                                    mRbtnFifty.setChecked(true);
                                    mFrequency = 50;
                                    preFrequency = 50;
                                    maxFrameRate = FRAME_RATE_LOW;
                                } else if (coding_param.u32Frequency == 60) {
                                    //video_format_spinner.setSelection(1);
                                    mRbtnSixty.setChecked(true);
                                    mFrequency = 60;
                                    preFrequency = 60;
                                    maxFrameRate = FRAME_RATE_HIGH;
                                }
                                ChangedFrameRange(maxFrameRate);
                                dismissjuHuaDialog();
                                break;
                            case HiChipDefines.HI_P2P_GET_VIDEO_PARAM:
                                HiChipDefines.HI_P2P_S_VIDEO_PARAM video_param = new HiChipDefines.HI_P2P_S_VIDEO_PARAM(data);
                                if (video_param.u32Stream == HiChipDefines.HI_P2P_STREAM_1) {
                                    video_param_hd = video_param;
                                    Log.e("===11==", video_param_hd.u32BitRate + "==" + video_param_hd.u32Frame + "==" + video_param_hd.u32Quality);
                                    first_code_rate_et.setText(video_param_hd.u32BitRate + "");
                                    first_frame_rate_et.setText(video_param_hd.u32Frame + "");
                                    first_video_level_et.setText(video_param_hd.u32Quality + "");
                                } else if (video_param.u32Stream == HiChipDefines.HI_P2P_STREAM_2) {

                                    video_param_sd = video_param;
                                    Log.e("===22==", video_param_sd.u32BitRate + "==" + video_param_sd.u32Frame + "==" + video_param_sd.u32Quality);
                                    second_code_rate_et.setText(video_param_sd.u32BitRate + "");
                                    second_frame_rate_et.setText(video_param_sd.u32Frame + "");
                                    second_video_level_et.setText(video_param_sd.u32Quality + "");

                                }
                                break;
                            case HiChipDefines.HI_P2P_SET_VIDEO_PARAM:
                                count += 1;
                                if (count == 2) {
                                    HiToast.showToast(VideoSettingActivity.this, getString(R.string.tips_video_setting));
                                    count = 0;
                                    Log.e("Video","isChange="+isChangeResolution+"  isIngenic="+mCamera.isIngenic);
                                    if(isChangeResolution&&mCamera.isIngenic){
                                        startActivity(new Intent(VideoSettingActivity.this, MainActivity.class));
                                        finish();
                                    }
                                }

                                break;
                            case HiChipDefines.HI_P2P_GET_RESOLUTION://分辨率
                               hi_p2P_get_resolution = new HiChipDefines.HI_P2P_RESOLUTION(data);
                                Log.e("TAG","ss="+hi_p2P_get_resolution.u32Resolution);
                                if (hi_p2P_get_resolution.u32Resolution == 16) {
                                    spinner.setSelection(1,true);
                                    oldResolution="2560x1440";
                                } else if (hi_p2P_get_resolution.u32Resolution == 20) {
                                    spinner.setSelection(0,true);
                                    oldResolution="2560x1920";
                                }
                                break;
                            case HiChipDefines.HI_P2P_GET_VIDEO_IMAGE_PARAM_SCOPE: {//是否支持分辨率
                                Log.e("TAG", ""+data.length);
                                Log.e("TAG", ""+new String(data));
                                CamHiDefines.HI_P2P_VIDEO_IMAGE_PARAM_SCOPE hi_p2P_get_video_image_param_scope = new CamHiDefines.HI_P2P_VIDEO_IMAGE_PARAM_SCOPE(data);
                                String sp=new String(hi_p2P_get_video_image_param_scope.resolution);
                                if(sp.contains("81;82;101;102;")){
                                    ll_resolution.setVisibility(View.VISIBLE);
                                    isSuportRes=true;
                                }else {
                                    ll_resolution.setVisibility(View.GONE);
                                    isSuportRes=false;
                                }

                            }

                        }
                    } else {
                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_GET_VIDEO_CODE:
                                dismissjuHuaDialog();
                                supportFrequency = false;
                                maxFrameRate = FRAME_RATE_HIGH;
                                ll_frequency.setVisibility(View.GONE);
                                ChangedFrameRange(FRAME_RATE_HIGH);
                                coding_param = new HiChipDefines.HI_P2P_CODING_PARAM(new byte[12]);
                                break;
                            case HiChipDefines.HI_P2P_SET_VIDEO_PARAM:
                                HiToast.showToast(VideoSettingActivity.this, getString(R.string.application_fail));
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
        // TODO Auto-generated method stub

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
