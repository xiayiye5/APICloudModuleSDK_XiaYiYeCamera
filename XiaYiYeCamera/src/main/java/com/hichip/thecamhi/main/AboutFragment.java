package com.hichip.thecamhi.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hichip.R;
import com.hichip.sdk.HiChipSDK;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.bean.HiDataValue;

import java.util.Locale;

public class AboutFragment extends Fragment {
    private View view;
    private int mClickNum;
    private TextView tvUserAgreement, tvPrivacyAgreement;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_about, null);
        initView();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mClickNum = 0;
    }

    private void initView() {
        TitleView title = (TitleView) view.findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.title_about_fragment));

        PackageManager manager = getActivity().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getActivity().getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = " ";
        if (info != null) {
            version = info.versionName;
        }

        TextView app_version_tv = (TextView) view.findViewById(R.id.app_version_tv);
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

        TextView txt_SDK_version = (TextView) view.findViewById(R.id.txt_SDK_version);
        txt_SDK_version.setText(HiChipSDK.getSDKVersion());

        tvPrivacyAgreement = view.findViewById(R.id.tv_privacy_agreement);
        tvUserAgreement = view.findViewById(R.id.tv_user_agreement);
        tvUserAgreement.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra("title", getActivity().getResources().getString(R.string.about_user_agreement));
                if (isZh(getActivity())) {
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
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra("title", getActivity().getResources().getString(R.string.about_user_privacy));
                if (isZh(getActivity())) {
                    intent.putExtra("webUrl", "http://www.hichip.org/privacy_ch.html");
                } else {
                    intent.putExtra("webUrl", "http://www.hichip.org/privacy_en.html");
                }
                startActivity(intent);
            }
        });

    }

    public  boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }
}
