package com.hichip.thecamhi.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hichip.customview.dialog.NiftyDialogBuilder;
import com.hichip.R;
import com.hichip.thecamhi.main.MainActivity;
import com.hichip.tools.HiSinVoiceData;
import com.hichip.tools.HiSmartWifiSet;
import com.hichip.hichip.widget.NotCopyAndPaste;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.utils.EmojiFilter;
import com.hichip.thecamhi.utils.FullCharFilter;

/**
 * WIFI一键设置界面
 *
 * @author lt
 */
public class WifiOneKeySettingActivity extends HiActivity {
    private EditText wifi_ssid_et;
    private EditText psw_wifi_et;
    private Button setting_wifi_btn;
    private SeekBar prs_loading;
    private TextView wifi_rate, wifi_rate_2;
    private int cur = 0;
    private String ssid;
    private HiSinVoiceData sv;
    private boolean noice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_set_one_key);
        sv = new HiSinVoiceData(WifiOneKeySettingActivity.this);
        initView();
    }

    private void getInfo() {
        if (!HiTools.checkPermission(WifiOneKeySettingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(WifiOneKeySettingActivity.this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 0);
        } else {
            getSsid();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSsid();
            } else {
                finish();
            }
        }
    }

    private void getSsid() {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || Build.VERSION.SDK_INT == 28) {
            WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
           if(mWifiManager!=null){
               WifiInfo info = mWifiManager.getConnectionInfo();
               String systemSsid=info.getSSID();
               if(systemSsid.startsWith("\"")&&systemSsid.endsWith("\"")&&systemSsid.length()>=3){
                   ssid=systemSsid.substring(1,systemSsid.length()-1);
               }else {//16进制
                   ssid = info.getSSID().replace("\"", "");
               }

           }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            ConnectivityManager connManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connManager != null;
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo.isConnected()) {
                if (networkInfo.getExtraInfo() != null) {
                    String systemSsid= networkInfo.getExtraInfo();
                    if(systemSsid.startsWith("\"")&&systemSsid.endsWith("\"")&&systemSsid.length()>=3){
                        ssid=systemSsid.substring(1,systemSsid.length()-1);
                    }else {
                        ssid = networkInfo.getExtraInfo().replace("\"", "");
                    }
                }
            }
        }
        Log.e("SSID","ssid::"+ssid);
        if (ssid != null) {
            wifi_ssid_et.setText(ssid);
        } else {
            wifi_ssid_et.setText("unknown");
        }
    }

    private void initView() {
        TitleView title_top = (TitleView) findViewById(R.id.title_top);
        title_top.setTitle(getString(R.string.one_key_setting_wifi));
        title_top.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title_top.setNavigationBarButtonListener(new NavigationBarButtonListener() {
            @Override
            public void OnNavigationButtonClick(int which) {
                finish();
            }
        });

        wifi_ssid_et = (EditText) findViewById(R.id.wifi_ssid_et);
        psw_wifi_et = (EditText) findViewById(R.id.psw_wifi_et);
        psw_wifi_et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(63), new FullCharFilter(this), new EmojiFilter()});
        psw_wifi_et.setCustomSelectionActionModeCallback(new NotCopyAndPaste());
        setting_wifi_btn = (Button) findViewById(R.id.setting_wifi_btn);
        getInfo();
        psw_wifi_et.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        setting_wifi_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String psw = psw_wifi_et.getText().toString();
                if (psw.isEmpty()) {
                    HiToast.showToast(WifiOneKeySettingActivity.this, getString(R.string.psw_no_empty));
                    return;
                }
                if (HiTools.isMaxLength(ssid, 32)) {
                    HiToast.showToast(WifiOneKeySettingActivity.this, getString(R.string.toast_ssid_tolong));
                    return;
                }
                if (!ssid.equals(ssid.trim()) || TextUtils.isEmpty(psw) || (!psw.equals(psw.trim()))) {
                    HiToast.showToast(WifiOneKeySettingActivity.this, getString(R.string.toast_no_space));
                    return;
                }
                seekBarDialog();
            }
        });
    }

    protected void seekBarDialog() {
        final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(this);
        dialog.withMessage(getString(R.string.tips_wifi_one_key_set)).withButton1Text(getString(R.string.btn_no)).withButton2Text(getString(R.string.btn_yes)).setButton1Click(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showAlertDialog();
            }
        }).setButton2Click(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                cur = 0;
                showingProgressDialog();
                // wifi一键配置的api
                HiSmartWifiSet.HiStartSmartConnection(ssid, psw_wifi_et.getText().toString(), (byte) 4);
                sv.setValue(ssid, psw_wifi_et.getText().toString());
                sv.startSinVoice();
                noice = true;
            }
        }).show();
    }

    private void showAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(getString(R.string.tips_warning)).setMessage(getString(R.string.tips_long_press_preset_btn)).setNegativeButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();

    }

    protected void showingProgressDialog() {
        View customView = getLayoutInflater().inflate(R.layout.dialog_showing_progressbar, null, false);

        final AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        final AlertDialog dlgBuilder = dlg.create();
        dlgBuilder.setView(customView);
        dlgBuilder.setTitle(null);

        dlgBuilder.setCanceledOnTouchOutside(false);
        customView.findViewById(R.id.btn_wifi_onekey_cancel).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dlgBuilder.dismiss();
            }
        });
        dlgBuilder.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {

                HiSmartWifiSet.HiStopSmartConnection();
                if (noice) {
                    sv.stopSinVoice();
                    noice = false;
                }
                if (timer != null) {
                    timer.cancel();
                    cur = 0;

                }
            }
        });
        prs_loading = (SeekBar) customView.findViewById(R.id.wifi_progressbar);
        wifi_rate = (TextView) customView.findViewById(R.id.wifi_rate);
        wifi_rate_2 = (TextView) customView.findViewById(R.id.wifi_rate_2);
        dlgBuilder.show();
        showingProgress();

    }

    private void showingProgress() {
        prs_loading.setMax(100);
        prs_loading.setProgress(0);

        timer.start();

    }

    CountDownTimer timer = new CountDownTimer(100000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            cur += 1;
            prs_loading.setProgress(cur);
            wifi_rate.setText(cur + "%");
            wifi_rate_2.setText(cur + "/100");
        }

        @Override
        public void onFinish() {

            handler.sendEmptyMessage(0);
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            HiSmartWifiSet.HiStopSmartConnection();
            if (noice) {
                noice = false;
                sv.stopSinVoice();
            }
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HiSmartWifiSet.HiStopSmartConnection();
        if (noice) {
            noice = false;
            sv.stopSinVoice();
        }
    }

}
