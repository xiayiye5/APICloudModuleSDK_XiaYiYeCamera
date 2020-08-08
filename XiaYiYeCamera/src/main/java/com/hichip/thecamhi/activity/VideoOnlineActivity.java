package com.hichip.thecamhi.activity;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hichip.R;
import com.hichip.hichip.activity.FishEye.FishPlaybackOnlineActivity;
import com.hichip.hichip.activity.WallMounted.WallMountedOnlineActivity;
import com.hichip.callback.ICameraDownloadCallback;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.content.HiChipDefines.HI_P2P_FILE_INFO;
import com.hichip.content.HiChipDefines.HI_P2P_S_SD_INFO;
import com.hichip.content.HiChipDefines.SD_STATE;
import com.hichip.control.HiCamera;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.HiActivity;

/**
 * 远程--->录像列表界面
 *
 * @author lt
 */
public class VideoOnlineActivity extends HiActivity
        implements ICameraIOSessionCallback, OnItemClickListener, ICameraDownloadCallback {

    public final static int SEARCH_ACTIVITY_RESULT = 1;
    public final static String SEARCH_ACTIVITY_START_TIME = "START TIME";
    public final static String SEARCH_ACTIVITY_END_TIME = "END TIME";

    public final static String VIDEO_PLAYBACK_START_TIME = "VIDEO START TIME";
    public final static String VIDEO_PLAYBACK_END_TIME = "VIDEO END TIME";

    public final static int HANDLE_MESSAGE_NETWORK_CHANGED = 0x100001;
    private MyCamera mCamera;

    private ListView list_video_online;
    private TextView noSdCard;

    private List<HI_P2P_FILE_INFO> file_list = Collections
            .synchronizedList(new ArrayList<HI_P2P_FILE_INFO>());

    private VideoOnlineListAdapter adapter;
    // 回调的下载文件路径
    private String path;
    private String download_path;
    private String fileName;
    private View searchTimeView = null;
    private View loadingView = null;
    private View noResultView = null;
    private ConnectionChangeReceiver myReceiver;
    private boolean isDownloading = false;
    private String uid;
    // private AlertDialog mDownDialog;
    private SimpleDateFormat sdf = new SimpleDateFormat("00:mm:ss");
    private int mType = 0;// 当前搜索的类型
    private TitleView nb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video_online);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            uid = bundle.getString(HiDataValue.EXTRAS_KEY_UID);
        }

        if (HiDataValue.ANDROID_VERSION >= 23) {
            if (!HiTools.checkPermission(VideoOnlineActivity.this, Manifest.permission.ACCESS_NETWORK_STATE)) {
                ActivityCompat.requestPermissions(VideoOnlineActivity.this,
                        new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 0);
            }

            if (!HiTools.checkPermission(VideoOnlineActivity.this, Manifest.permission.ACCESS_WIFI_STATE)) {
                ActivityCompat.requestPermissions(VideoOnlineActivity.this,
                        new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 0);
            }
        }
        for (MyCamera camera : HiDataValue.CameraList) {
            if (camera.getUid().equals(uid)) {
                mCamera = camera;
                break;
            }
        }
        HiTools.cameraWhetherNull(VideoOnlineActivity.this, mCamera);
        initView();
        searchVideo();
    }

    private void checkSdCard() {
        if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_SDSTATE)) {
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_SDSTATE, new byte[0]);
        } else {
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_SD_INFO, new byte[0]);
        }
    }

    private void handleNoFile() {
        list_video_online.removeHeaderView(searchTimeView);
        list_video_online.addFooterView(noResultView);
        TextView tv_no_result_found = (TextView) noResultView.findViewById(R.id.tv_no_result_found);
        switch (mType) {
            case 0:// 默认
                tv_no_result_found.setText(getString(R.string.search_six_hours));
                break;
            case 1:// 一个小时
                tv_no_result_found.setText(getString(R.string.search_one_hours));
                break;
            case 2:// 半天
                tv_no_result_found.setText(getString(R.string.search_half_day));
                break;
            case 3:// 一天
                tv_no_result_found.setText(getString(R.string.search_one_day));
                break;
            case 4:// 一周
                tv_no_result_found.setText(getString(R.string.search_one_week));
                break;
            case 5:// 自定义
                tv_no_result_found.setText(getString(R.string.search_custom));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ע��
        registerCompent();
        if (mCamera == null) {
            finish();
        } else {
            if (mCamera.getConnectState() != HiCamera.CAMERA_CONNECTION_STATE_LOGIN)
                finish();
        }
    }

    private void registerCompent() {
        if (mCamera != null) {
            mCamera.registerIOSessionListener(this);
            mCamera.registerDownloadListener(this);
        }
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        myReceiver = new ConnectionChangeReceiver();
        this.registerReceiver(myReceiver, filter);
    }

    public class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mobNetInfo != null && !mobNetInfo.isConnected() && wifiNetInfo != null && !wifiNetInfo.isConnected()) {
                if (isDownloading) {
                    handler.sendEmptyMessage(HANDLE_MESSAGE_NETWORK_CHANGED);
                }
            } else {

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//		isDownloading = false;
        // if (mCamera != null) {
        // mCamera.stopDownloadRecording();
        // }
        dismissLoadingProgress();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);

        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        /*用于解决下载中按home键，页面卡住的问题*/
		if (mCamera != null&&isDownloading) {
			isDownloading = false;
			mCamera.stopDownloadRecording();
			dlgBuilder.dismiss();
			deleteLoadingFile();
		}

        // 解册
        unregisterCompent();

    }

    private void unregisterCompent() {
        if (mCamera != null) {
            mCamera.unregisterIOSessionListener(this);
            mCamera.unregisterDownloadListener(this);
        }
        if (myReceiver != null) {
            try {
                unregisterReceiver(myReceiver);
            } catch (Exception e) {
            }
        }
    }

    private void initView() {
        nb = (TitleView) findViewById(R.id.title_top);
        noSdCard = (TextView) findViewById(R.id.no_sd_card_hint);

        nb.setTitle(getString(R.string.tip_recording_list));
        nb.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        nb.setButton(TitleView.NAVIGATION_BUTTON_RIGHT);
        nb.setRightBtnTextBackround(R.drawable.search);
        nb.setNavigationBarButtonListener(new TitleView.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        finish();
                        break;
                    case TitleView.NAVIGATION_BUTTON_RIGHT:
                        if (list_video_online.getChildAt(1) == loadingView) {
                            return;
                        }
                        Intent intent = new Intent(VideoOnlineActivity.this, SearchSDCardVideoActivity.class);
                        intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
                        startActivityForResult(intent, SEARCH_ACTIVITY_RESULT);
                        break;
                }
            }
        });

        searchTimeView = getLayoutInflater().inflate(R.layout.search_event_result, null);
        loadingView = getLayoutInflater().inflate(R.layout.loading_events, null);
        noResultView = getLayoutInflater().inflate(R.layout.no_result, null);

        list_video_online = (ListView) findViewById(R.id.list_video_online);

    }

    private void searchVideo() {

        long startTime = getBeforeHourTime(6);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
        list_video_online.addFooterView(loadingView);

        if (list_video_online.getHeaderViewsCount() == 0) {
            list_video_online.addHeaderView(searchTimeView);
        }

        adapter = new VideoOnlineListAdapter(this);
        if (mCamera == null || mCamera.getConnectState() != HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
            list_video_online.addFooterView(noResultView);
        }
        list_video_online.setAdapter(adapter);
        list_video_online.setOnItemClickListener(this);

        // String timeStr = HiTools.sdfTimeDay(System.currentTimeMillis()) + " -
        // "+ HiTools.sdfTimeSec(System.currentTimeMillis());
        // 改为默认搜索6个小时时间内的录像
        String timeStr = sdf.format(new java.util.Date(startTime)) + " - "
                + HiTools.sdfTimeSec(System.currentTimeMillis());
        TextView txtSearchTime = (TextView) searchTimeView.findViewById(R.id.txtSearchTimeDuration);
        txtSearchTime.setText(timeStr);
        /*
         * String timeStr= HiTools.sdfTimeDay(System.currentTimeMillis()) +" - "+
         * HiTools.sdfTimeSec(System.currentTimeMillis());
         * tv_search_duration.setText(timeStr);
         */
        if (mCamera != null) {
            if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_QUERY_START_NODST)) {
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_QUERY_START_NODST, HiChipDefines.HI_P2P_S_PB_LIST_REQ
                        .parseContent(0, startTime, System.currentTimeMillis(), HiChipDefines.HI_P2P_EVENT_ALL, 0));
            } else if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_QUERY_START_NEW)) {
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_QUERY_START_NEW, HiChipDefines.HI_P2P_S_PB_LIST_REQ
                        .parseContent(0, startTime, System.currentTimeMillis(), HiChipDefines.HI_P2P_EVENT_ALL, 1));
            } else if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_QUERY_START_EXT)) {
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_QUERY_START_EXT, HiChipDefines.HI_P2P_S_PB_LIST_REQ
                        .parseContent(0, startTime, System.currentTimeMillis(), HiChipDefines.HI_P2P_EVENT_ALL, 1));
            } else {
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_QUERY_START, HiChipDefines.HI_P2P_S_PB_LIST_REQ
                        .parseContent(0, startTime, System.currentTimeMillis(), HiChipDefines.HI_P2P_EVENT_ALL, 1));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            Bundle bundle = intent.getBundleExtra(HiDataValue.EXTRAS_KEY_DATA);
            long startTime = bundle.getLong(SEARCH_ACTIVITY_START_TIME);
            long endTime = bundle.getLong(SEARCH_ACTIVITY_END_TIME);
            mType = intent.getIntExtra("type", 5);
            file_list.clear();
            if(adapter!=null){
                adapter.notifyDataSetChanged();
            }
            if (mCamera != null) {
                if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_QUERY_START_NODST)) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_QUERY_START_NODST, HiChipDefines.HI_P2P_S_PB_LIST_REQ
                            .parseContent(0, startTime, endTime, HiChipDefines.HI_P2P_EVENT_ALL, 0));
                } else {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_QUERY_START, HiChipDefines.HI_P2P_S_PB_LIST_REQ
                            .parseContent(0, startTime, endTime, HiChipDefines.HI_P2P_EVENT_ALL, 1));
                }
            }

            if (list_video_online.getFooterViewsCount() != 0) {
                list_video_online.removeFooterView(noResultView);
            }

            if (list_video_online.getHeaderViewsCount() == 0) {
                list_video_online.addHeaderView(searchTimeView);
            }

            if (list_video_online.getFooterViewsCount() == 0) {
                list_video_online.addFooterView(loadingView);
            }

            adapter.notifyDataSetChanged();
            String timeStr = HiTools.sdfTimeSec(startTime) + " - " + HiTools.sdfTimeSec(endTime);

            TextView txtSearchTime = (TextView) searchTimeView.findViewById(R.id.txtSearchTimeDuration);
            txtSearchTime.setText(timeStr);

            mCamera.registerIOSessionListener(this);
        }
    }

    @Override
    public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {

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
    public void callbackDownloadState(HiCamera camera, int total, int curSize, int state, String path) {
        if (camera != mCamera)
            return;
        Bundle bundle = new Bundle();
        bundle.putLong("total", total);
        bundle.putLong("curSize", curSize);
        bundle.putString("path", path);

        Message msg = handler.obtainMessage();
        msg.what = HiDataValue.HANDLE_MESSAGE_DOWNLOAD_STATE;
        msg.arg1 = state;
        msg.setData(bundle);
        handler.sendMessage(msg);

    }

    @Override
    public void callbackDownloadSnapData(HiCamera hiCamera, int i, byte[] bytes, int i1, int i2) {

    }

    @Override
    public void receiveSessionState(HiCamera arg0, int arg1) {
        Message msg = handler.obtainMessage();
        msg.what = HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
        msg.arg1 = arg1;
        msg.obj = arg0;
        handler.sendMessage(msg);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_MESSAGE_NETWORK_CHANGED:
                    handNETWORK_CHANGED();
                    break;
                case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
                    switch (msg.arg1) {
                        case HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED:
                            /*解决正在下载断网的情况，删除if语句*/
//                            if (isDownloading) {
//                                if (!TextUtils.isEmpty(path)) {
//                                    File file = new File(path);
//                                    if (file != null && file.isFile() && file.exists()) {
//                                        file.delete();
//                                    }
//                                }
//                            }
                            dismissLoadingProgress();
                            HiToast.showToast(VideoOnlineActivity.this, getString(R.string.disconnect));
                            VideoOnlineActivity.this.finish();
                            break;
                    }
                    break;
                case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
                    if (msg.arg2 == -1) {// IO的错误码
                        dismissLoadingProgress();
                        HiToast.showToast(VideoOnlineActivity.this, getString(R.string.tips_update_system_failed));
                        return;
                    }
                    if (msg.arg2 == 0) {
                        Bundle bundle = msg.getData();
                        byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);

                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_GET_SD_INFO:
                                HI_P2P_S_SD_INFO sd_info = new HI_P2P_S_SD_INFO(data);
                                if (sd_info.u32Status == 0) {

                                    noSdCard.setVisibility(View.VISIBLE);
                                    nb.getRightButton().setVisibility(View.GONE);
                                    list_video_online.removeHeaderView(searchTimeView);
                                    if (adapter != null) {
                                        adapter.notifyDataSetChanged();
                                    }
                                } else {

                                    noSdCard.setVisibility(View.GONE);
                                    nb.getRightButton().setVisibility(View.VISIBLE);
                                    handleNoFile();
                                }
                                break;
                            case HiChipDefines.HI_P2P_GET_SDSTATE:
                                SD_STATE sd_info_4178 = new SD_STATE(data);
                                // sd_info.u32Status 1表示有SD卡 0表示没有
                                if (sd_info_4178.u32Status == 0) {

                                    noSdCard.setVisibility(View.VISIBLE);
                                    nb.getRightButton().setVisibility(View.GONE);
                                    list_video_online.removeHeaderView(searchTimeView);
                                    if(adapter!=null){
                                        adapter.notifyDataSetChanged();
                                    }
                                } else {

                                    noSdCard.setVisibility(View.GONE);
                                    nb.getRightButton().setVisibility(View.VISIBLE);
                                    handleNoFile();
                                }
                                break;
                            case HiChipDefines.HI_P2P_START_REC_UPLOAD_EXT:// 下载
                                break;
                            case HiChipDefines.HI_P2P_PB_QUERY_START_NODST:
                            case HiChipDefines.HI_P2P_PB_QUERY_START:
                            case HiChipDefines.HI_P2P_PB_QUERY_START_NEW:
                            case HiChipDefines.HI_P2P_PB_QUERY_START_EXT:

                                if (data.length >= 12) {
                                    byte flag = data[8];// 数据发送的结束标识符
                                    int cnt = data[9]; // 当前包的文件个数
                                    if (cnt > 0) {
                                        for (int i = 0; i < cnt; i++) {
                                            int pos = 12;
                                            int size = HI_P2P_FILE_INFO.sizeof();
                                            byte[] t = new byte[24];
                                            if (data.length >= i * size + pos + 24) {
                                                System.arraycopy(data, i * size + pos, t, 0, 24);
                                                HI_P2P_FILE_INFO file_info = new HI_P2P_FILE_INFO(
                                                        t);
                                                long duration = file_info.sEndTime.getTimeInMillis()
                                                        - file_info.sStartTime.getTimeInMillis();
                                                if (duration <= 1500 * 1000 && duration > 0) { // 1000秒，文件录像一般为15分钟，但是有可能会长一点所有就设置为1000
                                                    file_list.add(file_info);
                                                }
                                            }
                                        }
                                    }
                                    if (flag == 1) {// 表示数据收完了
                                        list_video_online.removeFooterView(loadingView);
                                        list_video_online.removeFooterView(noResultView);
                                        if (file_list != null && file_list.size() <= 0) {
                                            checkSdCard();

                                            Log.e("====", "没有文件");
                                        }
                                       dismissLoadingProgress();
                                        Collections.reverse(file_list);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                                break;
                        }
                    }
                    break;
                case HiDataValue.HANDLE_MESSAGE_DOWNLOAD_STATE:
                    handDownLoad(msg);
                    break;
            }
        }

        private void handDownLoad(Message msg) {
            Bundle bundle = msg.getData();
            switch (msg.arg1) {
                case DOWNLOAD_STATE_START:
                    dismissLoadingProgress();
                    showingProgressDialog();
                    isDownloading = true;
                    path = bundle.getString("path");
                    break;
                case DOWNLOAD_STATE_DOWNLOADING:
                    if (!isDownloading) {
                        return;
                    }
                    float d;
                    long total = bundle.getLong("total");
                    if (total == 0) {
                        d = bundle.getLong("curSize") * 100 / (1024 * 1024);
                    } else {
                        d = bundle.getLong("curSize") * 100 / total;
                    }
                    if (d >= 100) {
                        d = 99;
                    }
                    int rate = (int) d;
                    String rateStr = "";
                    if (rate < 10) {
                        rateStr = " " + rate + "%";
                    } else {
                        rateStr = rate + "%";
                    }
                    prs_loading.setProgress(rate);

                    rate_loading_video.setText(rateStr);

                    break;
                case DOWNLOAD_STATE_END:
                    prs_loading.setProgress(100);
                    rate_loading_video.setText(100 + "%");
                    isDownloading = false;
                    cancel_btn_downloading_video.setText(R.string.continue_down);
                    goto_btn_downloading_video.setVisibility(View.VISIBLE);
                    popu_tips_down.setText(getString(R.string.tips_down_file_route));
                    break;
                case DOWNLOAD_STATE_ERROR_PATH:
                    showAlert(getResources().getString(R.string.tips_wifi_connect_failed),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    if (dlgBuilder != null) {
                                        dlgBuilder.dismiss();
                                        mDlgBuilder = null;
                                    }
                                }
                            }, false);
                    break;
                case DOWNLOAD_STATE_ERROR_DATA:
                    if (mCamera != null && isDownloading) {
                        mCamera.stopDownloadRecording();
                        isDownloading = false;
                        mCamera.disconnect(1);
                        mCamera.connect();
                    }
                    break;

            }
        }

        private void handNETWORK_CHANGED() {
            if (!isDownloading) {
                return;
            }
            isDownloading = false;
            if (mCamera != null) {
                mCamera.stopDownloadRecording();
                /*解决下载文件工程断网，错误文件保存的问题，放开注释即可*/
				deleteLoadingFile();
            }

            showAlert(getResources().getString(R.string.tips_network_disconnect),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            if (dlgBuilder != null) {
                                dlgBuilder.dismiss();
                            }
                        }
                    }, false);
        }
    };

    private AlertDialog dlgBuilder;
    private SeekBar prs_loading;
    private TextView rate_loading_video;
    private AlertDialog.Builder dlg;
    private Button cancel_btn_downloading_video, goto_btn_downloading_video;
    private TextView popu_tips_down;
    private HI_P2P_FILE_INFO evt;

    protected void showingProgressDialog() {
        View customView = getLayoutInflater().inflate(R.layout.popview_loading_video, null, false);
        dlg = new AlertDialog.Builder(this);
        dlgBuilder = dlg.create();
        dlgBuilder.setView(customView);
        dlgBuilder.setCancelable(false);
        dlgBuilder.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                isDownloading = false;
                if (mCamera != null) {
                    mCamera.stopDownloadRecording();
                }
            }
        });

        prs_loading = (SeekBar) customView.findViewById(R.id.sb_downloading_video);
        prs_loading.setMax(100);
        prs_loading.setProgress(0);

        rate_loading_video = (TextView) customView.findViewById(R.id.rate_loading_video);
        popu_tips_down = (TextView) customView.findViewById(R.id.popu_tips_down);
        cancel_btn_downloading_video = (Button) customView.findViewById(R.id.cancel_btn_downloading_video);
        goto_btn_downloading_video = (Button) customView.findViewById(R.id.goto_btn_downloading_video);
        cancel_btn_downloading_video.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 显示取消下载的对话框
                // cancelDownloadVideo();
                if (getString(R.string.continue_down).equals(cancel_btn_downloading_video.getText().toString().trim())) {
                    dlgBuilder.dismiss();
                } else {
                    dlgBuilder.dismiss();
                    deleteLoadingFile();
                }
            }
        });
        // 前往查看
        goto_btn_downloading_video.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dlgBuilder.dismiss();
                Bundle bundle = new Bundle();
                bundle.putString(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
                bundle.putBoolean("goto", true);
                Intent intent = new Intent(VideoOnlineActivity.this, VideoLocalActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });

        dlgBuilder.show();

    }

    private void deleteLoadingFile() {
        if (path == null)
            return;
        File file = new File(path);
        if (file != null && file.exists()) {
            file.delete();
        }

    }

    public void cancelDownloadVideo() {

        showAlertDialog();

    }

    public void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoOnlineActivity.this);

        builder.setTitle(getString(R.string.tips_warning));
        builder.setMessage(getResources().getString(R.string.tips_cancel_download_file));
        builder.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                deleteLoadingFile();
                dlgBuilder.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    public class VideoOnlineListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public VideoOnlineListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        public int getCount() {

            return file_list.size();
        }

        public Object getItem(int position) {
            return file_list.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {

            if (file_list.size() == 0)
                return false;
            return super.isEnabled(position);
        }

        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup parent) {

            final HI_P2P_FILE_INFO evt = (HI_P2P_FILE_INFO) getItem(position);

            ViewHolder holder = null;

            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.list_video_online, null);

                holder = new ViewHolder();
                holder.event = (TextView) convertView.findViewById(R.id.txt_event);
                holder.time = (TextView) convertView.findViewById(R.id.txt_time);
                holder.fileSize = (TextView) convertView.findViewById(R.id.txt_file_size);
                holder.tvLong = (TextView) convertView.findViewById(R.id.txt_long);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final String[] mTextArray = getResources().getStringArray(R.array.online_recording_type);

            if (evt.EventType <= 3 && evt.EventType >= 0) {
                holder.event.setText(mTextArray[evt.EventType]);
                switch (evt.EventType) {
                    case 1:
                        holder.event.setTextColor(getResources().getColor(R.color.color_connecting));
                        break;
                    case 2:// 报警录像
                        holder.event.setTextColor(getResources().getColor(R.color.color_pass_word));
                        break;
                    case 3:// 计划录像
                        holder.event.setTextColor(getResources().getColor(R.color.color_login));
                        break;

                }
            } else {
                holder.event.setText("");
            }
            String string1 = evt.sEndTime.toString();
            String[] strings = string1.split(" ");
            String string2 = evt.sStartTime.toString();
            String[] strings2 = string2.split(" ");
            // holder.time.setText(evt.sStartTime.toString() + " - " + strings[1]);
            holder.time.setText(strings2[0] + "        " + strings2[1] + " - " + strings[1]);

            if (evt.u32size < 1) {
                holder.fileSize.setText("1 MB");
            } else {
                holder.fileSize.setText(evt.u32size + " MB");
            }
            long duration = evt.sEndTime.getTimeInMillis() - evt.sStartTime.getTimeInMillis();
            String string = sdf.format(new java.util.Date(duration));
            // String string =formatDuring(duration);
            holder.tvLong.setText(string);
            return convertView;

        }

        private final class ViewHolder {
            public TextView event;
            public TextView time;
            public TextView fileSize;
            public TextView tvLong;
        }
    }


    public static String formatDuring(long mss) {
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        return hours + " : " + minutes + " : " + seconds;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        dialogMethod(position);
    }

    private void dialogMethod(final int position) {
        final int pos = position - list_video_online.getHeaderViewsCount();
        if (pos < 0) {
            return;
        }
        evt = file_list.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoOnlineActivity.this);
        final AlertDialog mDownDialog = builder.create();
        mDownDialog.show();
        // mDownDialog.setCancelable(false);
        mDownDialog.setContentView(R.layout.popview_video_online);
        // mDownDialog.setOnKeyListener(new OnKeyListener() {
        // @Override
        // public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        // if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() ==
        // KeyEvent.ACTION_DOWN) {
        // mDownDialog.dismiss();
        // return true;
        // }
        // return false;
        // }
        // });
        TextView tvState = (TextView) mDownDialog.findViewById(R.id.pup_vo_item_tv_state);

        final String[] mTextArray = getResources().getStringArray(R.array.online_recording_type);

        if (evt.EventType <= 3 && evt.EventType >= 0) {
            tvState.setText(mTextArray[evt.EventType]);
        }

        TextView tvTime = (TextView) mDownDialog.findViewById(R.id.pup_vo_item_tv_time);
        String[] strings1 = evt.sStartTime.toString().split(" ");
        String[] strings2 = evt.sEndTime.toString().split(" ");
        tvTime.setText(strings1[0] + "   " + strings1[1] + " - " + strings2[1]);
        // tvTime.setText(evt.sStartTime.toString() + " - " + evt.sEndTime.toString());

        // 1.回放
        Button btnPlay = (Button) mDownDialog.findViewById(R.id.btn_play_video_online);
        btnPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownDialog.dismiss();
                playbackRecording(pos);
            }
        });
        // 2.下载
        mDownDialog.findViewById(R.id.btn_downlinad_video_online).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownDialog.dismiss();
                // 检查存储权限
                if (HiTools.checkPermission(VideoOnlineActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    downloadRecording(pos, evt);
                } else {
                    ActivityCompat.requestPermissions(VideoOnlineActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            }
        });
        // 3.取消
        mDownDialog.findViewById(R.id.btn_downlinad_video_cancel).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mDownDialog.dismiss();
            }
        });
    }

    private void downloadRecording(int position, final HI_P2P_FILE_INFO file_infos) {
        if (HiTools.isSDCardValid()) {

            File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
            File downloadFolder = new File(HiDataValue.ONLINE_VIDEO_PATH);
            File uidFolder = new File(downloadFolder.getAbsolutePath() + "/" + mCamera.getUid() + "");
            if (!rootFolder.exists()) {
                rootFolder.mkdirs();
            }
            if (!downloadFolder.exists()) {
                downloadFolder.mkdirs();
            }
            if (!uidFolder.exists()) {
                uidFolder.mkdirs();
            }

            download_path = uidFolder.getAbsoluteFile() + "/";

            // 创建UID文件夹
            fileName = splitFileName(file_infos.sStartTime.toString());
            File file = new File(download_path + fileName + ".avi");
            File file2 = new File(download_path + fileName + ".mp4");
            File file3 = new File(download_path + fileName + ".h264");
            File file4 = new File(download_path + fileName + ".h265");

            if (file.exists() || file2.exists() || file3.exists() || file4.exists()) {// �ļ������ع�
                View view = View.inflate(VideoOnlineActivity.this, R.layout.popuwindow_aleary_down, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoOnlineActivity.this);
                final AlertDialog dialog = builder.create();
                dialog.show();
                dialog.setCancelable(false);
                dialog.getWindow().setContentView(view);
                TextView tvKnow = (TextView) dialog.findViewById(R.id.item_tv_know);
                tvKnow.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                return;
            }
            showLoadingProgress();
            // //因为下载SDK加了耗时操作,所以放在要放在异步里处理
            new Thread() {
                public void run() {
                    // mCamera.startDownloadRecording(file_infos.sStartTime, download_path,
                    // fileName);
                    // 默认都下载264的文件(如果是文件本身是avi 则还是avi格式,如果是其他格式则变成264格式)
                    mCamera.startDownloadRecording2(file_infos.sStartTime, download_path, fileName, 2);
                }

                ;
            }.start();
            // mCamera.startDownloadRecording(file_infos.sStartTime, download_path,
            // fileName);
        } else {
            HiToast.showToast(VideoOnlineActivity.this, getText(R.string.tips_no_sdcard).toString());
        }
    }

    private String splitFileName(String str) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long time = 0;
        try {
            time = sdf.parse(str).getTime();
        } catch (ParseException e) {

            e.printStackTrace();
        }

        SimpleDateFormat sf2 = new SimpleDateFormat("yyyyMMdd_HHmmss");

        return sf2.format(time);
    }

    private void playbackRecording(int position) {
        if (mCamera == null)
            return;
        HI_P2P_FILE_INFO file_info = file_list.get(position);
        Bundle extras = new Bundle();
        extras.putString(HiDataValue.EXTRAS_KEY_UID, mCamera.getUid());
        byte[] b_startTime = file_info.sStartTime.parseContent();
        extras.putByteArray("st", b_startTime);
        long startTimeLong = file_info.sStartTime.getTimeInMillis2();
        long endTimeLong = file_info.sEndTime.getTimeInMillis2();
        extras.putLong(VIDEO_PLAYBACK_START_TIME, startTimeLong);
        extras.putLong(VIDEO_PLAYBACK_END_TIME, endTimeLong);

        String[] strings = file_info.sEndTime.toString().split(" ");
        String[] strings2 = file_info.sStartTime.toString().split(" ");
        String str = strings2[0] + "   " + strings2[1] + "-" + strings[1];
        extras.putString("title", str);

        Intent intent = new Intent();
        intent.putExtras(extras);

        if (mCamera.isWallMounted) {
            intent.setClass(VideoOnlineActivity.this, WallMountedOnlineActivity.class);
            startActivity(intent);
            return;
        }

        if (mCamera.isFishEye()) {
            intent.setClass(VideoOnlineActivity.this, FishPlaybackOnlineActivity.class);
        } else {
            intent.setClass(VideoOnlineActivity.this, PlaybackOnlineActivity.class);
        }
        startActivity(intent);
    }

    /**
     * 获取当前时间制定一个小时之前
     */
    public long getBeforeHourTime(int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - hour);
        return calendar.getTimeInMillis();

    }

}
