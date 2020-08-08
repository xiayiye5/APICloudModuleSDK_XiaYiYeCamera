package com.hichip.thecamhi.activity.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.view.View;

import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hichip.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.hichip.widget.SwitchButton;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.bean.CamHiDefines;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;


/**
 * 人形报警界面
 */
public class AlarmHumanActivity extends HiActivity implements ICameraIOSessionCallback, CompoundButton.OnCheckedChangeListener {
    private MyCamera mCamera;
    private LinearLayout llHumanSet;
    private SwitchButton switch_alarm_human, switch_alarm_rect;
    private CamHiDefines.HI_P2P_GET_SMART_HSR_PARAM hsr_param;
    private TextView tvLinkWay;
    private RelativeLayout rlLinkWay;
    private int index = 0;//0 单独触发 1 联动触发
    private boolean isOpenMotion;//移动侦测是否打开

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_human);
        String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
        for (MyCamera camera : HiDataValue.CameraList) {
            if (uid.equals(camera.getUid())) {
                mCamera = camera;
                //获取人形参数
                mCamera.sendIOCtrl(CamHiDefines.HI_P2P_GET_SMART_HSR_PARAM, null);
                break;
            }
        }
        initView();
    }

    private void initView() {
        TitleView title = (TitleView) findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.humanoid_alarm));
        title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new TitleView.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        AlarmHumanActivity.this.finish();
                        break;
                }

            }
        });
        llHumanSet = findViewById(R.id.ll_human_set);
        switch_alarm_human = findViewById(R.id.switch_alarm_human);
        switch_alarm_rect = findViewById(R.id.switch_alarm_rect);
        switch_alarm_human.setOnCheckedChangeListener(this);
        switch_alarm_rect.setOnCheckedChangeListener(this);
        tvLinkWay = findViewById(R.id.tv_link_way);
        rlLinkWay = findViewById(R.id.rl_link_way);
        rlLinkWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlarmHumanActivity.this, AlarmHumanWayActivity.class);
                intent.putExtra("isOpenMotion", isOpenMotion);
                intent.putExtra("index", index);
                startActivityForResult(intent, 100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    index = extras.getInt("index");
                    if (index == 0) {
                        tvLinkWay.setText(getResources().getString(R.string.alone_trigger));
                    } else {
                        tvLinkWay.setText(getResources().getString(R.string.linkage_trigger));
                    }
                    setHumanParms();
                }
            }
        }
    }

    @Override
    public void receiveSessionState(HiCamera hiCamera, int i) {

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

    @SuppressLint("HandlerLeak") private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL: {
                    if (msg.arg2 == 0) {
                        Bundle bundle = msg.getData();
                        byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
                        if (data == null) {
                            return;
                        }
                        switch (msg.arg1) {
                            case CamHiDefines.HI_P2P_GET_SMART_HSR_PARAM:
                                HiChipDefines.HI_P2P_S_MD_PARAM mdparam = new HiChipDefines.HI_P2P_S_MD_PARAM(0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_1, 0, 0, 0, 0, 0, 0));
                                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam.parseContent());

                                switch_alarm_rect.setOnCheckedChangeListener(null);
                                switch_alarm_human.setOnCheckedChangeListener(null);
                                hsr_param = new CamHiDefines.HI_P2P_GET_SMART_HSR_PARAM(data);
                                Log.e("Human", "u32HSRenable=" + hsr_param.u32HSRenable + "\n" + "u32DrawRect=" + hsr_param.u32DrawRect + "\n" + "u32Link=" + hsr_param.u32Link

                                );
                                if (hsr_param.u32HSRenable == 1) {
                                    switch_alarm_human.setChecked(true);
                                    llHumanSet.setVisibility(View.VISIBLE);
                                } else {
                                    switch_alarm_human.setChecked(false);
                                    llHumanSet.setVisibility(View.GONE);
                                }
                                if (hsr_param.u32DrawRect == 1) {
                                    switch_alarm_rect.setChecked(true);
                                } else {
                                    switch_alarm_rect.setChecked(false);
                                }
                                if (hsr_param.u32Link == 1) {
                                    tvLinkWay.setText(getResources().getString(R.string.linkage_trigger));
                                    index = 1;
                                } else {
                                    tvLinkWay.setText(getResources().getString(R.string.alone_trigger));
                                    index = 0;
                                }
                                switch_alarm_rect.setOnCheckedChangeListener(AlarmHumanActivity.this);
                                switch_alarm_human.setOnCheckedChangeListener(AlarmHumanActivity.this);

                                break;
                            case CamHiDefines.HI_P2P_SET_SMART_HSR_PARAM:
                                dismissjuHuaDialog();
                                break;
                            case HiChipDefines.HI_P2P_GET_MD_PARAM:
                                dismissjuHuaDialog();
                                HiChipDefines.HI_P2P_S_MD_PARAM md_param_temp = new HiChipDefines.HI_P2P_S_MD_PARAM(data);
                                if (md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_1) {
                                    if (md_param_temp.struArea.u32Enable == 0) {
                                        isOpenMotion=false;
                                    } else {
                                        isOpenMotion = true;
                                    }
                                }
                                break;
                        }
                    } else {
                        switch (msg.arg1) {
                            case CamHiDefines.HI_P2P_SET_SMART_HSR_PARAM:
                                dismissjuHuaDialog();
                                HiToast.showToast(AlarmHumanActivity.this, getString(R.string.application_fail));
                                break;
                        }
                    }
                }
                break;
            }
        }

    };


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        int id = buttonView.getId();
        if (id == R.id.switch_alarm_rect) {//框住人形
            setHumanParms();
        } else if (id == R.id.switch_alarm_human) {//智能人形识别
            setHumanParms();
            if (isChecked) {
                llHumanSet.setVisibility(View.VISIBLE);
            } else {
                llHumanSet.setVisibility(View.GONE);
            }
        }
    }

    private void setHumanParms() {
        Log.e("Human", "set parms" + "index=" + index + "isopen==" + isOpenMotion);
        if (hsr_param == null) {
            return;
        }
        hsr_param.u32HSRenable = switch_alarm_human.isChecked() ? 1 : 0;
        hsr_param.u32DrawRect = switch_alarm_rect.isChecked() ? 1 : 0;
        if (index == 0) {
            hsr_param.u32Link = 0;
        } else {
            hsr_param.u32Link = 1;
        }
        Log.e("Human", "set parms" + "index=" + index + hsr_param.u32HSRenable + hsr_param.u32DrawRect + hsr_param.u32Link);
        showjuHuaDialog();
        mCamera.sendIOCtrl(CamHiDefines.HI_P2P_SET_SMART_HSR_PARAM, hsr_param.parseContent());

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
