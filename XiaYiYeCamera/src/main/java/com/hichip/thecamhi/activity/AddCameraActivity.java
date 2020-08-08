package com.hichip.thecamhi.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hichip.R;
import com.hichip.sdk.HiChipSDK;
import com.hichip.thecamhi.zxing.CaptureActivity;
import com.hichip.tools.HiSearchSDK.HiSearchResult;
import com.hichip.hichip.widget.NotCopyAndPaste;
import com.hichip.thecamhi.base.A2bigA;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;
import com.hichip.thecamhi.utils.EmojiFilter;
import com.hichip.thecamhi.utils.FullCharUnionFilter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.TextUtils;
import android.transition.Fade;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class AddCameraActivity extends HiActivity implements OnClickListener {
    private final static int REQUEST_SCANNIN_GREQUEST_CODE = 1;
    private final static int REQUEST_WIFI_CODE = 2;
    private final static int REQUEST_SEARCH_CAMERA_IN_WIFI = 3;
    // private ScanResultAdapter adapter;
    private EditText add_camera_uid_edt, add_camera_name_et, add_camera_username_et, add_camera_psw_et;
    private List<HiSearchResult> list = new ArrayList<HiSearchResult>();
    private MyCamera camera;
    private boolean isSearch;// 用于记录是否正在搜索的状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_camera_view);
        initView();

    }

    private void initView() {
        TitleView title = (TitleView) findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.add_camera));
        title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title.setButton(TitleView.NAVIGATION_BUTTON_RIGHT);
        title.setNavigationBarButtonListener(new NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        AddCameraActivity.this.finish();
                        break;
                    case TitleView.NAVIGATION_BUTTON_RIGHT:
                        chickDone();
                        break;
                    default:

                }
            }
        });
        LinearLayout scanner_QRcode_ll = (LinearLayout) findViewById(R.id.scanner_QRcode_ll);
        scanner_QRcode_ll.setOnClickListener(this);

        LinearLayout search_in_lan_ll = (LinearLayout) findViewById(R.id.search_in_lan_ll);
        search_in_lan_ll.setOnClickListener(this);

        LinearLayout one_key_setting_wifi_ll = (LinearLayout) findViewById(R.id.one_key_setting_wifi_ll);
        one_key_setting_wifi_ll.setOnClickListener(this);

        LinearLayout ll_scan_it_addsharecamer = (LinearLayout) findViewById(R.id.ll_scan_it_addsharecamer);
        if (HiDataValue.shareIsOpen == true) {
            ll_scan_it_addsharecamer.setVisibility(View.VISIBLE);
        } else {
            ll_scan_it_addsharecamer.setVisibility(View.GONE);

        }
        ll_scan_it_addsharecamer.setOnClickListener(this);

        add_camera_name_et = (EditText) findViewById(R.id.add_camera_name_et);
        add_camera_name_et.setCustomSelectionActionModeCallback(new NotCopyAndPaste());
        add_camera_username_et = (EditText) findViewById(R.id.add_camera_username_et);
        add_camera_username_et.setCustomSelectionActionModeCallback(new NotCopyAndPaste());
        add_camera_username_et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(63), new FullCharUnionFilter(AddCameraActivity.this), new EmojiFilter()});
        add_camera_uid_edt = (EditText) findViewById(R.id.add_camera_uid_edt);
        add_camera_uid_edt.setTransformationMethod(new A2bigA());
        add_camera_psw_et = (EditText) findViewById(R.id.add_camera_psw_et);
        add_camera_psw_et.setCustomSelectionActionModeCallback(new NotCopyAndPaste());
        add_camera_psw_et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(63), new FullCharUnionFilter(AddCameraActivity.this), new EmojiFilter()});

        setOnLoadingProgressDismissListener(new MyDismiss() {
            @Override
            public void OnDismiss() {
                isSearch = false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SCANNIN_GREQUEST_CODE: {
                    Bundle extras = data.getExtras();
                    String text = extras.getString(HiDataValue.EXTRAS_KEY_UID).trim();
                    if (!TextUtils.isEmpty(text) && text.length() > 8) {
                        String sub = text.substring(0, 8);
                        if (sub.equalsIgnoreCase(getString(R.string.app_name) + "_AC")) {//二维码是加密分享的UID
                            byte[] datas = text.getBytes();
                            int len = datas.length;
                            HiChipSDK.Aes_Decrypt(datas, len);
                        } else {//二维码是UID
                            add_camera_uid_edt.setText(text.toUpperCase());
                        }
                    }
                }
                break;
                case REQUEST_WIFI_CODE:
                    isSearch = true;
                    Intent intent = new Intent();
                    intent.setClass(AddCameraActivity.this, SearchCameraActivity.class);
                    startActivityForResult(intent, REQUEST_SEARCH_CAMERA_IN_WIFI);
                    break;
                case REQUEST_SEARCH_CAMERA_IN_WIFI: {
                    Bundle extras = data.getExtras();
                    String uid = extras.getString(HiDataValue.EXTRAS_KEY_UID).trim();

                    add_camera_uid_edt.setText(uid.toUpperCase());
                }
                break;
                default:
            }
        }

    }

    private boolean checkPermission(String permission) {

        int checkCallPhonePermission = ContextCompat.checkSelfPermission(AddCameraActivity.this, permission);
        if (checkCallPhonePermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.scanner_QRcode_ll) {
            if (HiDataValue.ANDROID_VERSION >= 23 && (!checkPermission(Manifest.permission.CAMERA))) {
                HiToast.showToast(AddCameraActivity.this, getString(R.string.tips_no_permission));
                return;
            }
            Intent intent = new Intent();
            intent.setClass(AddCameraActivity.this, CaptureActivity.class);
            //	intent.setClass(AddCameraActivity.this, ScanQRCodeActivity.class);
            intent.putExtra("category", 3);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_SCANNIN_GREQUEST_CODE);
        } else if (id == R.id.search_in_lan_ll) {
            Intent intent = new Intent();
            intent.setClass(AddCameraActivity.this, SearchCameraActivity.class);
            startActivityForResult(intent, REQUEST_SEARCH_CAMERA_IN_WIFI);
        } else if (id == R.id.one_key_setting_wifi_ll) {
            if (isWifiConnected(AddCameraActivity.this)) {
                Intent intent = new Intent(AddCameraActivity.this, WifiOneKeySettingActivity.class);
                startActivityForResult(intent, REQUEST_WIFI_CODE);
            } else {
                HiToast.showToast(AddCameraActivity.this, getString(R.string.connect_to_WIFI_first));
            }
        } else if (id == R.id.ll_scan_it_addsharecamer) {
            if (HiDataValue.ANDROID_VERSION >= 23 && (!checkPermission(Manifest.permission.CAMERA))) {
                HiToast.showToast(AddCameraActivity.this, getString(R.string.tips_no_permission));
                return;
            }
            Intent intent = new Intent();
            intent.setClass(AddCameraActivity.this, CaptureActivity.class);
//			intent.setClass(AddCameraActivity.this, ScanQRCodeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_SCANNIN_GREQUEST_CODE);
        }
    }

    public boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }

        return false;
    }

    private void chickDone() {
        String str_nike = add_camera_name_et.getText().toString();
        String str_uid = add_camera_uid_edt.getText().toString().trim().toUpperCase();
        String str_password = add_camera_psw_et.getText().toString();
        String str_username = add_camera_username_et.getText().toString();

        if (str_nike.length() == 0) {
            showAlert(getText(R.string.tips_null_nike));
            return;
        }

        if (str_username.length() == 0) {
            showAlert(getText(R.string.tips_null_username));
            return;
        }

        for (int i = 0; i < HiDataValue.zifu.length; i++) {
            if (str_uid.contains(HiDataValue.zifu[i])) {
                HiToast.showToast(AddCameraActivity.this, getText(R.string.tips_invalid_uid).toString());
                return;
            }
        }
        if (HiDataValue.CameraList != null && HiDataValue.CameraList.size() >= 64) {
            HiToast.showToast(AddCameraActivity.this, getString(R.string.tips_limit_add_camera));
            return;
        }

        if (TextUtils.isEmpty(str_uid)) {
            showAlert(getText(R.string.tips_null_uid));
            return;
        }

        String string = HiTools.handUid(str_uid);
        str_uid = string;
        if (str_uid == null) {
            HiToast.showToast(AddCameraActivity.this, getString(R.string.tips_invalid_uid));
            return;
        }
        if (str_username.getBytes().length > 63) {
            HiToast.showToast(AddCameraActivity.this, getString(R.string.tips_username_tolong));
            return;
        }
        if (str_password.getBytes().length > 63) {
            HiToast.showToast(AddCameraActivity.this, getString(R.string.tips_password_tolong));
            return;

        }
        for (MyCamera camera : HiDataValue.CameraList) {
            if (str_uid.equalsIgnoreCase(camera.getUid())) {
                showAlert(getText(R.string.tips_add_camera_exists));
                return;
            }
        }
        camera = new MyCamera(getApplicationContext(), str_nike, str_uid, str_username, str_password);
        camera.saveInDatabase(this);
        camera.saveInCameraList();
        Intent broadcast = new Intent();
        broadcast.setAction(HiDataValue.ACTION_CAMERA_INIT_END);
        sendBroadcast(broadcast);

        Bundle extras = new Bundle();
        extras.putString(HiDataValue.EXTRAS_KEY_UID, str_uid);
        Intent intent = new Intent();
        intent.putExtras(extras);
        this.setResult(RESULT_OK, intent);
        this.finish();
    }

}




