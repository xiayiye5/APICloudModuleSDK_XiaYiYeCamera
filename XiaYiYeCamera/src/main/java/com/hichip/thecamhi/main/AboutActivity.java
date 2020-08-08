package com.hichip.thecamhi.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.hichip.R;
import com.hichip.sdk.HiChipSDK;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.bean.HiDataValue;

import java.util.Locale;

/**
 * @author xiayiye
 */
public class AboutActivity extends Activity {
    private int mClickNum;
    private TextView tvUserAgreement, tvPrivacyAgreement;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_about);
        initView();
    }

    @Override
    public void onPause() {
        super.onPause();
        mClickNum = 0;
    }

    private void initView() {
        TitleView title = (TitleView) findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.title_about_fragment));

        PackageManager manager = getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = " ";
        if (info != null) {
            version = info.versionName;
        }

        TextView app_version_tv = (TextView) findViewById(R.id.app_version_tv);
        app_version_tv.setText(version);
        app_version_tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickNum++;
                if (mClickNum >= 10 && mClickNum <= 15) {
                    HiDataValue.shareIsOpen = true;
                }
            }
        });

        TextView txt_SDK_version = (TextView) findViewById(R.id.txt_SDK_version);
        txt_SDK_version.setText(HiChipSDK.getSDKVersion());

        tvPrivacyAgreement = findViewById(R.id.tv_privacy_agreement);
        tvUserAgreement = findViewById(R.id.tv_user_agreement);
        tvUserAgreement.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this, WebViewActivity.class);
                intent.putExtra("title", getResources().getString(R.string.about_user_agreement));
                if (isZh(AboutActivity.this)) {
                    intent.putExtra("webUrl", "http://www.hichip.org/service_ch.html");
                } else {
                    intent.putExtra("webUrl", "http://www.hichip.org/service_en.html");
                }
                startActivity(intent);
            }
        });
        tvPrivacyAgreement.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this, WebViewActivity.class);
                intent.putExtra("title", getResources().getString(R.string.about_user_privacy));
                if (isZh(AboutActivity.this)) {
                    intent.putExtra("webUrl", "http://www.hichip.org/privacy_ch.html");
                } else {
                    intent.putExtra("webUrl", "http://www.hichip.org/privacy_en.html");
                }
                startActivity(intent);
            }
        });
    }

    public boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }
}
