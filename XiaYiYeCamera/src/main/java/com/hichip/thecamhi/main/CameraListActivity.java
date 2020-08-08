package com.hichip.thecamhi.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hichip.R;
import com.hichip.base.HiLog;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.customview.dialog.Effectstype;
import com.hichip.customview.dialog.NiftyDialogBuilder;
import com.hichip.hichip.activity.FishEye.FishEyeActivity;
import com.hichip.hichip.activity.Share.SeleShaCameraListActivity;
import com.hichip.hichip.activity.WallMounted.WallMountedActivity;
import com.hichip.thecamhi.activity.AddCameraActivity;
import com.hichip.thecamhi.activity.EditCameraActivity;
import com.hichip.thecamhi.activity.LiveViewActivity;
import com.hichip.thecamhi.activity.setting.AliveSettingActivity;
import com.hichip.thecamhi.base.DatabaseManager;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.CamHiDefines;
import com.hichip.thecamhi.bean.CamHiDefines.HI_P2P_ALARM_ADDRESS;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.bean.MyCamera.OnBindPushResult;
import com.hichip.thecamhi.utils.SharePreUtils;
import com.hichip.thecamhi.widget.swipe.SwipeMenu;
import com.hichip.thecamhi.widget.swipe.SwipeMenuCreator;
import com.hichip.thecamhi.widget.swipe.SwipeMenuItem;
import com.hichip.thecamhi.widget.swipe.SwipeMenuListView;
import com.hichip.thecamhi.widget.swipe.SwipeMenuListView.OnMenuItemClickListener;
import com.hichip.thecamhi.widget.swipe.SwipeMenuListView.OnMenuItemOpenListener;
import com.hichip.tools.Packet;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class CameraListActivity extends HiActivity implements ICameraIOSessionCallback, OnItemClickListener {
    private static final int MOTION_ALARM = 0; // 移动侦测
    private static final int IO_ALARM = 1; // 外置报警
    private static final int AUDIO_ALARM = 2; // 声音报警
    private static final int UART_ALARM = 3; // 外置报警

    private CameraListAdapter adapter;
    private CameraBroadcastReceiver receiver;
    private SwipeMenuListView mListView;

    private String[] str_state;
    private boolean delModel = false;
    int ranNum;
    private TitleView titleView;

    HiThreadConnect connectThread = null;
    private int saveopenswipeindex = -1;
    private NotificationManager notificationManager;
    private int lastPosition;
    private int lastY;
    private boolean isNeedScorllToOldPosition;

    public interface OnButtonClickListener {
        void onButtonClick(int btnId, MyCamera camera);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_camera);
        initView();
        ranNum = (int) (Math.random() * 10000);
        if (receiver == null) {
            receiver = new CameraBroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(HiDataValue.ACTION_CAMERA_INIT_END);
            registerReceiver(receiver, filter);
        }
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {

                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                deleteItem.setWidth(HiTools.dip2px(CameraListActivity.this, 80));
                deleteItem.setHeight(HiTools.dip2px(CameraListActivity.this, 200));
                menu.addMenuItem(deleteItem);
            }
        };

        mListView.setMenuCreator(creator);

        mListView.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                MyCamera camera = HiDataValue.CameraList.get(position);
                switch (index) {
                    case 0:
                        showDeleteCameraDialog(camera, Effectstype.Slidetop);
                        break;
                }
            }
        });
        mListView.setOnMenuItemOpenListener(new OnMenuItemOpenListener() {

            @Override
            public void OnSwipeOpen(int position, boolean state) {
                if (position >= 0 && position < HiDataValue.CameraList.size()) {
                    if (true == state)
                        saveopenswipeindex = position;
                    else
                        saveopenswipeindex = -1;
                }
            }
        });


        //  适配8.0通知栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 创建通知渠道
            String channelId = "camera_notification";
            String channelName =getResources().getString(R.string.notification_channel_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        notificationManager = (NotificationManager) Objects.requireNonNull(this).getSystemService(NOTIFICATION_SERVICE);

        notificationManager.createNotificationChannel(channel);

    }

    private void showDeleteCameraDialog(final MyCamera camera, Effectstype type) {
        final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(this);
        dialog.withTitle(getString(R.string.tip_reminder)).withMessage(getString(R.string.tips_msg_delete_camera)).withEffect(type).setButton1Click(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        }).setButton2Click(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showjuHuaDialog();
                camera.bindPushState(false, bindPushResult);
                SharePreUtils.removeKey("cache", CameraListActivity.this, camera.getUid());
                SharePreUtils.putBoolean("cache", CameraListActivity.this, "isFirstPbOnline", false);
                SharePreUtils.putBoolean("cache", CameraListActivity.this, camera.getUid() + "pb", false);
                sendUnRegister(camera, 0);
                Message msg = handler.obtainMessage();
                msg.what = HiDataValue.HANDLE_MESSAGE_DELETE_FILE;
                msg.obj = camera;
                handler.sendMessageDelayed(msg, 1000);
            }
        }).show();
    }

    private void initView() {
        titleView = (TitleView) findViewById(R.id.fg_ca_title);
        titleView.setTitle(getString(R.string.title_camera_fragment));
        titleView.setButton(TitleView.NAVIGATION_TEXT_RIGHT);
        if (HiDataValue.shareIsOpen) {
            titleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
            titleView.setLeftBtnTextBackround(R.drawable.share);
            titleView.setLeftBackroundPadding(2, 2, 2, 2);
        }
        titleView.setNavigationBarButtonListener(new NavigationBarButtonListener() {
            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_TEXT_RIGHT:
                        if (delModel) {
                            titleView.setRightText(R.string.btn_edit);
                        } else {
                            titleView.setRightText(R.string.finish);
                        }
                        delModel = !delModel;
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        if (HiDataValue.CameraList.size() > 0) {
                            Intent intent = new Intent(CameraListActivity.this, SeleShaCameraListActivity.class);
                            startActivity(intent);

                        } else {
                            HiToast.showToast(CameraListActivity.this, getString(R.string.tips_goto_add_camera));
                        }
                        break;
                }

            }
        });
        mListView = (SwipeMenuListView) findViewById(R.id.lv_swipemenu);
        LinearLayout add_camera_ll = (LinearLayout) findViewById(R.id.add_camera_ll);
        add_camera_ll.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraListActivity.this, AddCameraActivity.class);
                startActivity(intent);
            }
        });
        str_state = this.getResources().getStringArray(R.array.connect_state);
        adapter = new CameraListAdapter(this);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
        adapter.setOnButtonClickListener(new OnButtonClickListener() {
            @Override
            public void onButtonClick(int btnId, final MyCamera camera) {
                if (btnId == R.id.setting_camera_item) {
                    if (delModel) {
                        Intent intent = new Intent();
                        intent.putExtra(HiDataValue.EXTRAS_KEY_UID, camera.getUid());
                        intent.setClass(CameraListActivity.this, EditCameraActivity.class);
                        startActivity(intent);


                    } else {
                        if (camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                            Intent intent = new Intent();
                            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, camera.getUid());
                            intent.setClass(CameraListActivity.this, AliveSettingActivity.class);
                            startActivity(intent);
                        } else {
                            HiToast.showToast(CameraListActivity.this, getString(R.string.click_offline_setting));
                        }
                    }
                } else if (btnId == R.id.delete_icon_camera_item) {
                    showDeleteCameraDialog(camera, Effectstype.Slidetop);
                }
            }

        });
    }

    private void sendUnRegister(MyCamera mCamera, int enable) {
        if (mCamera.getPushState() == 1) {
            return;
        }

        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_TOKEN_UNREGIST)) {
            return;
        }

        byte[] info = CamHiDefines.HI_P2P_ALARM_TOKEN_INFO.parseContent(0, mCamera.getPushState(), (int) (System.currentTimeMillis() / 1000 / 3600), enable);
        mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_TOKEN_UNREGIST, info);
    }

    protected void sendRegisterToken(MyCamera mCamera) {
        if (mCamera.getPushState() == 1 || mCamera.getPushState() == 0) {
            return;
        }

        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_TOKEN_REGIST)) {
            return;
        }

        byte[] info = CamHiDefines.HI_P2P_ALARM_TOKEN_INFO.parseContent(0, mCamera.getPushState(), (int) (System.currentTimeMillis() / 1000 / 3600), 1);

        mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_TOKEN_REGIST, info);
    }

    OnBindPushResult bindPushResult = new OnBindPushResult() {
        @Override
        public void onBindSuccess(MyCamera camera) {

            if (!camera.handSubXYZ()) {


                if (camera.handSubWTU()) {
                    camera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122);
                } else {
                    camera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_233);
                }
            } else {
                camera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173);
            }

            //            if (camera.isDEAA()) {
            //                camera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_DERICAM_148);
            //            } else if (camera.isFDTAA()) {
            //                camera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_FDT_221);
            //            } else if (camera.handSubXYZ()) {
            //                camera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173);
            //            } else if (camera.handSubWTU()) {
            //                camera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122);
            //            } else {
            //                camera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_233);
            //            }

            camera.updateServerInDatabase(CameraListActivity.this);
            sendServer(camera);
            sendRegisterToken(camera);
        }

        @Override
        public void onBindFail(MyCamera camera) {
            Log.e("==333==", "onBindFail");
        }

        @Override
        public void onUnBindSuccess(MyCamera camera) {
            //    camera.bindPushState(true, bindPushResult);
            Log.e("==333==", "onUnBindSuccess");
            camera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_ADDRESS_GET, null);
        }

        @Override
        public void onUnBindFail(MyCamera camera) {
            // 把SubId存放到sharePrefence
            Log.e("==333==", "onUnBindFail");
            if (camera.getPushState() > 0) {
                SharePreUtils.putInt("subId", CameraListActivity.this, camera.getUid(), camera.getPushState());
            }

        }

    };

    private class CameraBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e("==ConnectState=", "CameraBroadcastReceiver");

            if (intent.getAction().equals(HiDataValue.ACTION_CAMERA_INIT_END)) {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                if (HiDataValue.ANDROID_VERSION >= 23 && !HiTools.checkPermission(CameraListActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    return;
                }
                if (connectThread == null) {
                    connectThread = new HiThreadConnect();
                    connectThread.start();
                }
            }
        }
    }

    public class HiThreadConnect extends Thread {
        private int connnum = 0;

        public synchronized void run() {
            for (connnum = 0; connnum < HiDataValue.CameraList.size(); connnum++) {
                MyCamera camera = HiDataValue.CameraList.get(connnum);
                Log.e("==ConnectState=", camera.getConnectState() + "");
                if (camera != null) {
                    if (camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED) {
                        camera.registerIOSessionListener(CameraListActivity.this);
                        camera.connect();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (connectThread != null) {
                connectThread = null;
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        delToNor();

        /*恢复list位置*/
        if (isNeedScorllToOldPosition) {
            mListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mListView.setSelectionFromTop(lastPosition, lastY);
                    isNeedScorllToOldPosition = false;
                }
            }, 500);
        }
    }


    public void delToNor() {
        delModel = false;
        titleView.setRightText(R.string.btn_edit);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }


    public class CameraListAdapter extends BaseAdapter {
        Context context;
        private LayoutInflater mInflater;
        OnButtonClickListener mListener;
        private String strState;

        public void setOnButtonClickListener(OnButtonClickListener listener) {
            mListener = listener;
        }

        public CameraListAdapter(Context context) {

            mInflater = LayoutInflater.from(context);
            this.context = context;
        }

        @Override
        public int getCount() {
            return HiDataValue.CameraList.size();
        }

        @Override
        public Object getItem(int position) {
            return HiDataValue.CameraList.get(position);
        }

        @Override
        public long getItemId(int arg0) {

            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final MyCamera camera = HiDataValue.CameraList.get(position);
            if (camera == null) {
                return null;
            }
            ViewHolder holder = new ViewHolder();
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.camera_main_item, null);
                holder.setting = (ImageView) convertView.findViewById(R.id.setting_camera_item);
                holder.img_snapshot = (ImageView) convertView.findViewById(R.id.snapshot_camera_item);
                holder.txt_nikename = (TextView) convertView.findViewById(R.id.nickname_camera_item);
                holder.txt_uid = (TextView) convertView.findViewById(R.id.uid_camera_item);
                holder.txt_state = (TextView) convertView.findViewById(R.id.state_camera_item);
                holder.img_alarm = (ImageView) convertView.findViewById(R.id.img_alarm);
                holder.delete_icon = (ImageView) convertView.findViewById(R.id.delete_icon_camera_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (holder != null) {
                if (camera.snapshot == null) {
                    holder.img_snapshot.setImageResource(R.drawable.videoclip);
                } else {
                    //                    Bitmap bitmap = BitmapUtils.setRoundedCorner(camera.snapshot, HiTools.dip2px(this, 5));
                    //                    holder.img_snapshot.setImageBitmap(bitmap);

                    RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), camera.snapshot);
                    drawable.setCornerRadius(15);
                    holder.img_snapshot.setImageDrawable(drawable);
                }

                holder.txt_nikename.setText(camera.getNikeName());
                holder.txt_uid.setText(camera.getUid());
                int state = camera.getConnectState();

                switch (state) {
                    case 0:// DISCONNECTED
                        holder.txt_state.setTextColor(getResources().getColor(R.color.color_disconnected));
                        break;
                    case -8:
                    case 1:// CONNECTING
                        holder.txt_state.setTextColor(getResources().getColor(R.color.color_connecting));
                        break;
                    case 2:// CONNECTED
                        holder.txt_state.setTextColor(getResources().getColor(R.color.color_connected));
                        break;
                    case 3:// WRONG_PASSWORD
                        holder.txt_state.setTextColor(getResources().getColor(R.color.color_pass_word));
                        break;
                    case 4:// STATE_LOGIN
                        holder.txt_state.setTextColor(getResources().getColor(R.color.color_login));
                        break;
                }
                if (state >= 0 && state <= 4) {
                    strState = str_state[state];
                    holder.txt_state.setText(strState);
                }
                if (state == -8) {
                    holder.txt_state.setText(str_state[2]);
                }
                if (camera.isSystemState == 1 && camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                    holder.txt_state.setText(getString(R.string.tips_restart));
                }
                if (camera.isSystemState == 2 && camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                    holder.txt_state.setText(getString(R.string.tips_recovery));
                }
                if (camera.isSystemState == 3 && camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                    holder.txt_state.setText(getString(R.string.tips_update));
                }
                holder.setting.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onButtonClick(R.id.setting_camera_item, camera);
                        }
                    }
                });

                holder.delete_icon.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (mListener != null) {
                            mListener.onButtonClick(R.id.delete_icon_camera_item, camera);
                        }

                    }
                });

                if (delModel) {
                    holder.delete_icon.setVisibility(View.VISIBLE);
                } else {
                    holder.delete_icon.setVisibility(View.GONE);
                }

                if (camera.getAlarmState() == 0) {
                    holder.img_alarm.setVisibility(View.GONE);
                } else {
                    holder.img_alarm.setVisibility(View.VISIBLE);
                }
            }

            return convertView;
        }

        public void notifyItem(MyCamera CameraItem) {
            MyCamera camera = null;
            View view = null;
            int i = 0;
            for (i = 0; i < mListView.getChildCount(); i++) {
                view = mListView.getChildAt(i);
                if (view == null)
                    return;
                TextView txt_uid = (TextView) view.findViewById(R.id.uid_camera_item);
                if (!TextUtils.isEmpty(txt_uid.getText().toString().trim())) {
                    if (txt_uid.getText().toString().equals(CameraItem.getUid())) {
                        break;
                    }
                }
            }
            if (i == mListView.getChildCount()) {
                return;
            }
            ImageView img_alarm = (ImageView) view.findViewById(R.id.img_alarm);
            TextView txt_state = (TextView) view.findViewById(R.id.state_camera_item);
            int state = CameraItem.getConnectState();
            switch (state) {
                case 0:// DISCONNECTED
                    txt_state.setTextColor(getResources().getColor(R.color.color_disconnected));
                    break;
                case -8:
                case 1:// CONNECTING
                    txt_state.setTextColor(getResources().getColor(R.color.color_connecting));
                    break;
                case 2:// CONNECTED
                    txt_state.setTextColor(getResources().getColor(R.color.color_connected));
                    break;
                case 3:// WRONG_PASSWORD
                    txt_state.setTextColor(getResources().getColor(R.color.color_pass_word));
                    break;
                case 4:// STATE_LOGIN
                    txt_state.setTextColor(getResources().getColor(R.color.color_login));
                    break;
            }
            if (state >= 0 && state <= 4) {
                strState = str_state[state];
                txt_state.setText(strState);
            }
            if (state == -8) {
                txt_state.setText(str_state[2]);
            }
            if (CameraItem.isSystemState == 1 && CameraItem.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                txt_state.setText(getString(R.string.tips_restart));
            }
            if (CameraItem.isSystemState == 2 && CameraItem.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                txt_state.setText(getString(R.string.tips_recovery));
            }
            if (CameraItem.isSystemState == 3 && CameraItem.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                txt_state.setText(getString(R.string.tips_update));
            }
            if (CameraItem.getAlarmState() == 0) {
                img_alarm.setVisibility(View.GONE);
            } else {
                img_alarm.setVisibility(View.VISIBLE);
            }
            if (saveopenswipeindex != -1 && saveopenswipeindex < HiDataValue.CameraList.size()) {
                camera = HiDataValue.CameraList.get(saveopenswipeindex);
                if (camera.getUid().equals(CameraItem.getUid())) {
                    mListView.smoothOpenMenu(saveopenswipeindex);
                }
            }

        }

        public class ViewHolder {
            public ImageView img_snapshot;
            public TextView txt_nikename;
            public TextView txt_uid;
            public TextView txt_state;
            public ImageView img_alarm;

            public ImageView setting;
            public ImageView delete_icon;

        }

    }

    @Override
    public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
        if (arg1 == HiChipDefines.HI_P2P_GET_SNAP && arg3 == 0) {
            MyCamera camera = (MyCamera) arg0;
            if (!camera.reciveBmpBuffer(arg2)) {
                return;
            }
        }
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

        if (HiDataValue.isDebug)
            HiLog.v("uid:" + arg0.getUid() + "  state:" + arg1);

        Message msg = handler.obtainMessage();
        msg.what = HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
        msg.arg1 = arg1;
        msg.obj = arg0;
        handler.sendMessage(msg);

    }

    @SuppressLint("HandlerLeak") private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MyCamera camera = (MyCamera) msg.obj;
            switch (msg.what) {
                case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
                    if (adapter != null) {
                        camera.isSystemState = 0;
                        if (this != null) {
                            adapter.notifyItem(camera);
                        }
                    }
                    switch (msg.arg1) {
                        case HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED:
                            camera.mIsReceived_4179 = false;
                            break;
                        case HiCamera.CAMERA_CONNECTION_STATE_LOGIN:
                            if (camera.getCommandFunction(HiChipDefines.HI_P2P_SET_MD_PARAM_NEW)) {
                                camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_RESOLUTION, HiChipDefines.HI_P2P_RESOLUTION.parseContent(0, 1));
                            }
                            setTime(camera);
                            if (camera.getPushState() > 0) {
                                camera.bindPushState(true, bindPushResult);
                                setServer(camera);
                            }
                            if (!camera.getCommandFunction(HiChipDefines.HI_P2P_PB_QUERY_START_NODST)) {
                                if (camera.getCommandFunction(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT)) {
                                    camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT, new byte[0]);
                                } else {
                                    camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE, new byte[0]);
                                }
                            }

                            break;
                        case HiCamera.CAMERA_CONNECTION_STATE_WRONG_PASSWORD:
                            break;
                        case HiCamera.CAMERA_CONNECTION_STATE_CONNECTING:
                            break;
                    }
                    break;
                case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
                    if (msg.arg2 == 0) {
                        handIOCTRLSucce(msg, camera);
                    } else {
                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_GET_DEVICE_FISH_PARAM:
                                Log.i("tedu", "--4179返回了但状态错误--");
                                camera.mIsReceived_4179 = true;
                                camera.isWallMounted = false;
                                SharePreUtils.putBoolean("cache", CameraListActivity.this, camera.getUid() + "isWallMounted", false);
                                break;
                        }
                    }
                    break;

                case HiDataValue.HANDLE_MESSAGE_DELETE_FILE:
                    camera.disconnect(1);
                    camera.deleteInCameraList();
                    camera.deleteInDatabase(CameraListActivity.this);
                    adapter.notifyDataSetChanged();
                    dismissjuHuaDialog();
                    HiToast.showToast(CameraListActivity.this, getString(R.string.tips_remove_success));
                    break;
            }
        }

        private void handIOCTRLSucce(Message msg, MyCamera camera) {
            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);

            switch (msg.arg1) {

                case HiChipDefines.HI_P2P_GET_FUNCTION:
                    camera.commandFunction.setCmdfunction(new HiChipDefines.HI_P2P_FUNCTION(data));

                    break;

                case HiChipDefines.HI_P2P_GET_SNAP:
                    adapter.notifyDataSetChanged();
                    if (camera.snapshot != null) {
                        File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
                        File sargetFolder = new File(rootFolder.getAbsolutePath() + "/android/data/" + getResources().getString(R.string.app_name));

                        if (!rootFolder.exists()) {
                            rootFolder.mkdirs();
                        }
                        if (!sargetFolder.exists()) {
                            sargetFolder.mkdirs();
                        }
                    }
                    break;

                case HiChipDefines.HI_P2P_GET_TIME_ZONE: {

                    HiChipDefines.HI_P2P_S_TIME_ZONE timezone = new HiChipDefines.HI_P2P_S_TIME_ZONE(data);

                    if (timezone.u32DstMode == 1) {
                        camera.setSummerTimer(true);
                    } else {
                        camera.setSummerTimer(false);
                    }

                }
                break;
                case HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT: {
                    HiChipDefines.HI_P2P_S_TIME_ZONE_EXT timezone = new HiChipDefines.HI_P2P_S_TIME_ZONE_EXT(data);
                    if (timezone.u32DstMode == 1) {
                        camera.setSummerTimer(true);
                    } else {
                        camera.setSummerTimer(false);
                    }
                    break;
                }
                case CamHiDefines.HI_P2P_ALARM_TOKEN_REGIST:

                    break;
                case CamHiDefines.HI_P2P_ALARM_TOKEN_UNREGIST:
                    break;
                case CamHiDefines.HI_P2P_ALARM_ADDRESS_SET:
                    camera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_ADDRESS_GET, null);
                    break;
                case CamHiDefines.HI_P2P_ALARM_ADDRESS_GET:
                    HI_P2P_ALARM_ADDRESS ADDRESS = new HI_P2P_ALARM_ADDRESS(data);
                    String add = new String(ADDRESS.szAlarmAddr).trim().toString();
                    Log.e("==getAddress", "HI_P2P_ALARM_ADDRESS_GET    :" + add);
                    break;

                case HiChipDefines.HI_P2P_ALARM_EVENT: {//直推
                    if (camera.getPushState() == 0) {
                        return;
                    }
                    camera.setLastAlarmTime(System.currentTimeMillis());
                    HiChipDefines.HI_P2P_EVENT event = new HiChipDefines.HI_P2P_EVENT(data);
                    showAlarmNotification(camera, event, System.currentTimeMillis());
                    saveAlarmData(camera, event.u32Event, (int) (System.currentTimeMillis() / 1000));
                    camera.setAlarmState(1);
                    camera.setAlarmLog(true);
                    if (this != null) {
                        if (adapter != null) {//change by #7568321 #175771 #176090
                            adapter.notifyItem(camera);
                        }
                    }
                }
                break;
                case HiChipDefines.HI_P2P_GET_DEVICE_FISH_PARAM:
                    Log.i("tedu", "--4179返回成功 成功--");
                    camera.mIsReceived_4179 = true;
                    HiChipDefines.HI_P2P_DEV_FISH fishmod = new HiChipDefines.HI_P2P_DEV_FISH(data);
                    float xcircle = fishmod.xcircle;
                    float ycircle = fishmod.ycircle;
                    float rcircle = fishmod.rcircle;
                    SharePreUtils.putFloat("chche", CameraListActivity.this, camera.getUid() + "xcircle", xcircle);
                    SharePreUtils.putFloat("chche", CameraListActivity.this, camera.getUid() + "ycircle", ycircle);
                    SharePreUtils.putFloat("chche", CameraListActivity.this, camera.getUid() + "rcircle", rcircle);
                    SharePreUtils.putInt("mInstallMode", CameraListActivity.this, camera.getUid(), fishmod.mold);
                    if (fishmod.fish == 1 && (fishmod.type == 2 || fishmod.type == 4)) {//1: 兼容UID互换,普通机器读成鱼眼新壁装镜头
                        camera.isWallMounted = true;
                        SharePreUtils.putBoolean("cache", CameraListActivity.this, camera.getUid() + "isWallMounted", true);
                    } else {
                        camera.isWallMounted = false;
                        SharePreUtils.putBoolean("cache", CameraListActivity.this, camera.getUid() + "isWallMounted", false);
                    }
                    camera.putFishModType(fishmod.type);
                    break;
                case HiChipDefines.HI_P2P_GET_RESOLUTION:
                    HiChipDefines.HI_P2P_RESOLUTION param = new HiChipDefines.HI_P2P_RESOLUTION(data);
                    camera.u32Resolution = param.u32Resolution;
                    break;
                case HiChipDefines.HI_P2P_GET_DEV_INFO_EXT://设备信息获取版本号ext
                    HiChipDefines.HI_P2P_GET_DEV_INFO_EXT deviceInfo = new HiChipDefines.HI_P2P_GET_DEV_INFO_EXT(data);
                    String extVersion = Packet.getString(deviceInfo.aszSystemSoftVersion);
                    Log.e("versionCam", "EXTVresion=" + extVersion);
                    checkIsIngenicByVersion(extVersion, camera);
                    break;
                case HiChipDefines.HI_P2P_GET_DEV_INFO://设备信息获取版本号
                    HiChipDefines.HI_P2P_S_DEV_INFO info = new HiChipDefines.HI_P2P_S_DEV_INFO(data);
                    String version = Packet.getString(info.strSoftVer);
                    Log.e("versionCam", "Vresion=" + version);
                    checkIsIngenicByVersion(version, camera);
                    break;
            }
        }
    };

    /***
     * 根据版本号检查是否为君正 君正切换分辨率提示重启
     */
    private void checkIsIngenicByVersion(String version, MyCamera camera) {
        if (TextUtils.isEmpty(version)) {
            return;
        }
        if (version.startsWith("V17") || version.startsWith("V18")) {
            camera.isIngenic = true;
        }
    }

    @SuppressWarnings("deprecation")
    private void showAlarmNotification(MyCamera camera, HiChipDefines.HI_P2P_EVENT event, long evtTime) {
        try {
            NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            Bundle extras = new Bundle();
            extras.putString(HiDataValue.EXTRAS_KEY_UID, camera.getUid());
            extras.putInt("type", 1);
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            //			intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.putExtras(extras);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            String[] alarmList = getResources().getStringArray(R.array.tips_alarm_list_array);
            String type = null;

            switch (event.u32Event) {
                case 0:
                    type = alarmList[0];
                    break;
                case 1:
                    type = alarmList[1];
                    break;
                case 2:
                    type = alarmList[2];
                    break;
                case 3:
                    type = alarmList[3];
                    break;
                case 6:
                    String sType = new String(event.sType).trim();
                    if ("key2".equals(sType)) {
                        type = getString(R.string.alarm_sos);
                    } else if ("key3".equals(sType)) {
                        type = getString(R.string.alarm_ring);
                    } else if ("door".equals(sType)) {
                        type = getString(R.string.alarm_door);
                    } else if ("infra".equals(sType)) {
                        type = getString(R.string.alarm_infra);
                    } else if ("beep".equals(sType)) {
                        type = getString(R.string.alarm_doorbell);
                    } else if ("fire".equals(sType)) {
                        type = getString(R.string.alarm_smoke);
                    } else if ("gas".equals(sType)) {
                        type = getString(R.string.alarm_gas);
                    } else if ("socket".equals(sType)) {
                        type = getString(R.string.alarm_socket);
                    } else if ("temp".equals(sType)) {
                        type = getString(R.string.alarm_temp);
                    } else if ("humi".equals(sType)) {
                        type = getString(R.string.alarm_humi);
                    }
                    break;
                case 12://人形识别推送 add by 20190819 version 5.1.76
                    type = alarmList[4];
                    break;
            }

            Log.e("==CameraFragment==", "==\n" + "u32Event==" + event.u32Event + "\n" + "u32Time==" + event.u32Time + "\n" + "u32Channel==" + event.u32Channel + "\n" + "sType==" + Arrays.toString(event.sType) + "\n" + "sReserved==" + Arrays.toString(event.sReserved) + "\n" + "type==" + type);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(CameraListActivity.this);
            boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();

            // 通知开关未打开
            if (!areNotificationsEnabled) {
                //提示手动打开
                HiToast.showToast(this, this.getResources().getString(R.string.tips_open_notification));
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //              Log.e("==CameraFragment==1",event.u32Event+"--"+type);
                //未打开该渠道通知
                assert manager != null;
                NotificationChannel channel = manager.getNotificationChannel("camera_notification");
                if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                    //提示手动打开
                    HiToast.showToast(this, this.getResources().getString(R.string.tips_open_notification));
                } else {

                    //创建通知
                    NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notification = new NotificationCompat.Builder(CameraListActivity.this, "camera_notification").setSmallIcon(R.drawable.ic_launcher).setTicker(camera.getNikeName()).setContentTitle(camera.getUid()).setContentText(type).setContentIntent(pendingIntent).build();
                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                    notification.defaults = Notification.DEFAULT_LIGHTS;
                    ranNum++;
                    notificationManager.notify(ranNum, notification);
                }

            } else {
                // Log.e("==CameraFragment==2",event.u32Event+"--"+type);
               /* Notification notification = new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setTicker(camera.getNikeName())
                        .setContentTitle(camera.getUid())
                        .setContentText(type)
                        .setContentIntent(pendingIntent).getNotification();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.defaults = Notification.DEFAULT_ALL;
                ranNum++;
                manager.notify(ranNum, notification);*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setServer(MyCamera mCamera) {
        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET)) {
            return;
        }
        //        // 如果数据库保存的还是老地址就解绑并绑定新的地址
        //        if (mCamera.getServerData() != null && !mCamera.getServerData().equals(HiDataValue.CAMERA_ALARM_ADDRESS_233)) {
        //            if (mCamera.getPushState() > 1) {
        if (HiDataValue.XGToken == null || HiDataValue.XGToken.isEmpty()) {
            if (HiDataValue.ANDROID_VERSION >= 23) {
                if (!HiTools.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            }

            //                    HiDataValue.XGToken = XGPushConfig.getToken(this);
            //                    if (HiDataValue.XGToken != null && !HiDataValue.XGToken.isEmpty()) {
            //                        SharePreUtils.putString("XGToken", getContext(), "xgtoken", HiDataValue.XGToken);
            //                    }
            // }

            //                Log.e("CameraFragment","bind");
            //                mCamera.bindPushState(false, bindPushResult);
            //               return;
        }
        //        }
    }

    protected void sendServer(MyCamera mCamera) {
        // //测试
        // mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_ADDRESS_GET, null);
        if (mCamera.getServerData() == null) {
            mCamera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_233);
            mCamera.updateServerInDatabase(this);
        }
        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET)) {
            return;
        }
        if (mCamera.push != null) {
            String[] strs = mCamera.push.getPushServer().split("\\.");
            if (strs.length == 4 && isInteger(strs[0]) && isInteger(strs[1]) && isInteger(strs[2]) && isInteger(strs[3])) {
                byte[] info = HI_P2P_ALARM_ADDRESS.parseContent(mCamera.push.getPushServer());
                Log.e("==push.getPushServer()", "sendServer()" + mCamera.push.getPushServer());
                mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET, info);
            }

        }
    }

    /*
     * 推荐，速度最快 判断是否为整数
     *
     * @param str 传入的字符串
     *
     * @return 是整数返回true,否则返回false
     */

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    private void saveAlarmData(MyCamera camera, int evtType, int evtTime) {

        DatabaseManager manager = new DatabaseManager(this);
        manager.addAlarmEvent(camera.getUid(), evtTime, evtType);

    }

    private void setTime(MyCamera camera) {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.setTimeInMillis(System.currentTimeMillis());

        byte[] time = HiChipDefines.HI_P2P_S_TIME_PARAM.parseContent(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));

        camera.sendIOCtrl(HiChipDefines.HI_P2P_SET_TIME_PARAM, time);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            this.unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (connectThread != null) {
            connectThread.interrupt();
        }

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final MyCamera selectedCamera = HiDataValue.CameraList.get(position);
        if (delModel) {
            Intent intent = new Intent();
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, selectedCamera.getUid());
            intent.setClass(this, EditCameraActivity.class);
            startActivity(intent);
        } else {
            if (selectedCamera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN && selectedCamera.mIsReceived_4179 == true) {
                Bundle extras = new Bundle();
                extras.putString(HiDataValue.EXTRAS_KEY_UID, selectedCamera.getUid());
                Intent intent = new Intent();
                intent.putExtras(extras);
                if (selectedCamera.isFishEye() && selectedCamera.isWallMounted) {// 壁装新镜头  //必须联合判断,规避两个不同类型的摄像机UID互换的一些问题。
                    intent.setClass(this, WallMountedActivity.class);
                } else if (selectedCamera.isFishEye()) {// 是鱼眼的话 就跳转鱼眼界面
                    // 初始化鱼眼顶装和壁装的模式
                    int num = SharePreUtils.getInt("mInstallMode", this, selectedCamera.getUid());
                    selectedCamera.mInstallMode = num == -1 ? 0 : num;
                    boolean bl = SharePreUtils.getBoolean("cache", this, selectedCamera.getUid());
                    selectedCamera.isFirst = bl;
                    intent.setClass(this, FishEyeActivity.class);
                } else {
                    intent.setClass(this, LiveViewActivity.class);
                }
                startActivity(intent);
                RecordLastPosition();
                HiDataValue.isOnLiveView = true;
                selectedCamera.setAlarmState(0);
                adapter.notifyDataSetChanged();
            } else if (selectedCamera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED || selectedCamera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_WRONG_PASSWORD) {
                if (HiDataValue.ANDROID_VERSION >= 23 && !HiTools.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showAlertDialog();
                    return;
                }
                selectedCamera.connect();
                selectedCamera.registerIOSessionListener(CameraListActivity.this);//必须要调用
                adapter.notifyDataSetChanged();
            } else {
                HiToast.showToast(this, getString(R.string.click_offline_setting));
                return;
            }
        }

    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.tips_no_permission));
        builder.setPositiveButton(getString(R.string.setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_SETTINGS);
                // intent.setAction("android.intent.action.MAIN");
                //intent.setClassName("com.android.settings", "com.android.settings.ManageApplications");
                startActivity(intent);

            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.setCancelable(false);
        builder.show();

    }

    private void RecordLastPosition() {
        isNeedScorllToOldPosition = true;
        if (mListView.getChildCount() < 1)
            return;
        try {
            lastPosition = mListView.getFirstVisiblePosition();
            lastY = mListView.getChildAt(0).getTop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
