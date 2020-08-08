package com.hichip.thecamhi.activity.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hichip.R;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.main.HiActivity;

/**
 * 人形触发方式界面
 */
public class AlarmHumanWayActivity extends HiActivity implements View.OnClickListener {
    private RelativeLayout rlAlone, rlJoint;
    private ImageView ivAlone, ivJoint;
    private int index = 0;
    private boolean isOpenMotion;//移动侦测是否打开

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_human_way);
        initView();
    }

    private void initView() {
        index = getIntent().getIntExtra("index", 0);
        isOpenMotion = getIntent().getBooleanExtra("isOpenMotion", false);
        TitleView title = (TitleView) findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.human_trigger_mode));
        title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new TitleView.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        AlarmHumanWayActivity.this.finish();
                        break;
                }

            }
        });
        rlAlone = findViewById(R.id.rl_alone);
        rlJoint = findViewById(R.id.rl_joint);
        ivAlone = findViewById(R.id.iv_alone);
        ivJoint = findViewById(R.id.iv_joint);
        if (index == 0) {
            ivAlone.setVisibility(View.VISIBLE);
            ivJoint.setVisibility(View.GONE);
        } else {
            ivAlone.setVisibility(View.GONE);
            ivJoint.setVisibility(View.VISIBLE);
        }
        rlJoint.setOnClickListener(this);
        rlAlone.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.rl_alone) {
            ivAlone.setVisibility(View.VISIBLE);
            ivJoint.setVisibility(View.GONE);
            setBundleData(0);
        } else if (id == R.id.rl_joint) {
            if (!isOpenMotion) {
                HiToast.showToast(this, getResources().getString(R.string.open_the_motion_detection));
                return;
            }
            ivAlone.setVisibility(View.GONE);
            ivJoint.setVisibility(View.VISIBLE);
            setBundleData(1);
        }
    }

    private void setBundleData(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        // 返回intent
        setResult(RESULT_OK, intent);
        finish();
    }
}
