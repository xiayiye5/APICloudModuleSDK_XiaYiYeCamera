package com.hichip.thecamhi.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.hichip.R;
import com.hichip.base.HiLog;
import com.hichip.customview.dialog.NiftyDialogBuilder;
import com.hichip.hichip.widget.FragmentTabHost;
import com.hichip.sdk.HiChipSDK;
import com.hichip.thecamhi.base.DatabaseManager;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.base.LogcatHelper;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.utils.SharePreUtils;

import java.io.File;
import java.util.Timer;

public class MainActivity extends FragmentActivity {

    private final static int HANDLE_MESSAGE_INIT_END = 0x90000001;
    private Class<?> fraList[] = {CameraFragment.class, PictureFragment.class, VideoFragment.class, AboutFragment.class};
    private int drawable[] = {R.drawable.selector_camera, R.drawable.selector_picture, R.drawable.selector_video, R.drawable.selector_about};
    private ImageView welcom_imv;
    private long initSdkTime;
    private RelativeLayout root_view;

    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_camera);
        if (HiDataValue.isDebug) {
            LogcatHelper.getInstance(this).start();
        }
//        HiSystemValue.DEBUG_MODE = true;
        initview();
        initTabHost();
        initCamera();
    }


    // 搭载4个fragment
    private void initTabHost() {
        String[] tabString = getResources().getStringArray(R.array.tab_name);
        FragmentTabHost tabHost = (FragmentTabHost) findViewById(R.id.main_fragment_tabhost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.fragment_main_content);
        tabHost.getTabWidget().setDividerDrawable(android.R.color.transparent);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < fraList.length; i++) {
            View view = inflater.inflate(R.layout.fragment_tabhost_switch_image, null);
            ImageView iv = (ImageView) view.findViewById(R.id.main_tabhost_imv);
            TextView tv = (TextView) view.findViewById(R.id.main_tabhost_tv);
            iv.setImageResource(drawable[i]);
            tv.setText(tabString[i]);
            TabSpec tabItem = tabHost.newTabSpec(tabString[i]).setIndicator(view);
            tabHost.addTab(tabItem, fraList[i], null);
        }
    }


    @SuppressLint("HandlerLeak") private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_MESSAGE_INIT_END:
                    long spendingTime = System.currentTimeMillis() - initSdkTime;
                    if (spendingTime < 2000 && spendingTime > 0) {
                        this.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // requestEnd();
                                initCamera();
                                welcom_imv.setVisibility(View.GONE);
                                root_view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                            }
                        }, 2000 - spendingTime);

                    } else {
                        // requestEnd();
                        initCamera();
                        welcom_imv.setVisibility(View.GONE);
                        root_view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                    break;

            }

        }
    };
    private AudioManager audioManager;

    // 获取本地数据
    private void initCamera() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            Log.i("", "---------当前版本号为-------->>" + info.versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        DatabaseManager manager = new DatabaseManager(this);
        SQLiteDatabase db = manager.getReadableDatabase();
        Cursor cursor = db.query(DatabaseManager.TABLE_DEVICE, new String[]{"dev_nickname", "dev_uid", "view_acc", "view_pwd", "dev_videoQuality", "dev_alarmState", "dev_pushState", "dev_serverData"}, null, null, null, null, null);
        HiLog.e("step1 " + "cursor is null" + (cursor == null ? "true" : "false"));
        try {
            while (cursor != null && cursor.moveToNext()) {
                if (HiDataValue.isDebug)
                    HiLog.e("step2 " + "cursor is null=" + (cursor == null ? "true" : "false"));
                String dev_nickname = cursor.getString(0);
                String dev_uid = cursor.getString(1);

                // 兼容很久之前老版本,没有做UID装换和添加"-"的版本
                dev_uid = dev_uid.toUpperCase();
                dev_uid = HiTools.handUid(dev_uid);
                if (TextUtils.isEmpty(dev_uid)) {
                    throw new IllegalArgumentException("--UID invalid--");
                }

                String dev_name = cursor.getString(2);
                String dev_pwd = cursor.getString(3);
                int dev_videoQuality = cursor.getInt(4);
                int dev_alarmState = cursor.getInt(5);
                int dev_pushState = cursor.getInt(6);
                String dev_serverData = cursor.getString(7);
                MyCamera camera = new MyCamera(getApplicationContext(), dev_nickname, dev_uid, dev_name, dev_pwd);
                camera.setVideoQuality(dev_videoQuality);
                camera.setAlarmState(dev_alarmState);
                camera.setPushState(dev_pushState);
                camera.snapshot = loadImageFromUrl(MainActivity.this, camera);
                camera.setServerData(dev_serverData);
                camera.saveInCameraList();
                if (camera.getPushState() == 0) {
                    String pDID = camera.getUid();
                    SharedPreferences setting = MainActivity.this.getSharedPreferences("Subid_" + pDID, MainActivity.MODE_PRIVATE);
                    int subID = setting.getInt("pushon", -1);
                    if (subID == 1) {
                        camera.setPushState(1);
                    } else {
                        camera.setPushState(0);
                    }
                }
                // 是不是鱼眼新壁装镜头
                camera.isWallMounted = SharePreUtils.getBoolean("cache", this, camera.getUid() + "isWallMounted");
            }

        } catch (Exception e) {
            // 删除snapshot数据;
            // initCamera();
        } finally {
            cursor.close();
            cursor = null;
            db.close();
        }

        requestEnd();
        Log.e("==ConnectState=", "requestEnd");

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void requestEnd() {
        // 获取数据完毕，发送广播到CameraFragment界面去刷新adapter
        root_view.postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction(HiDataValue.ACTION_CAMERA_INIT_END);
                sendBroadcast(intent);
                Log.e("==ConnectState=", "requestSend");

            }
        }, 1000);

    }

    private void initview() {
        //
        root_view = (RelativeLayout) findViewById(R.id.root_view);
        //		root_view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        HiDataValue.ANDROID_VERSION = HiTools.getAndroidVersion();

        if (HiDataValue.ANDROID_VERSION >= 23) {
            HiTools.checkPermissionAll(MainActivity.this);
        }

        welcom_imv = (ImageView) findViewById(R.id.welcome_imv);

        audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

    }

    public boolean isFirstTime() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstTime = prefs.getBoolean("first_time", true);
        if (firstTime) {
            Editor pEdit = prefs.edit();
            pEdit.putBoolean("first_time", false);
            pEdit.commit();
        }

        return firstTime;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (final MyCamera camera : HiDataValue.CameraList) {
            if (camera.isSetValueWithoutSave()) {
                camera.updateInDatabase(this);
            }
            DatabaseManager db = new DatabaseManager(MainActivity.this);
            if (db != null) {
                db.updateAlarmStateByUID(camera.getUid(), 0);
            }
            camera.unregisterIOSessionListener();
            new Thread() {
                public void run() {
                    camera.disconnect(1);// 有点耗时 阻塞
                }

                ;
            }.start();
        }
        HiChipSDK.uninit();
        if (HiDataValue.isDebug) {
            LogcatHelper.getInstance(this).stop();
        }
        // 杀掉进程,可以让app白屏时间更短,camera列表不会重复。
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(MainActivity.this);
                dialog.setIsMain(true);
                //dialog.setContentBootom(true);
                dialog.withMessage(getString(R.string.sure_to_exit)).setButton1Click(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                }).setButton2Click(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean ismain = dialog.getCb();
                        Log.e("TAG", "ischeck=" + ismain);
                        if (ismain) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            startActivity(intent);
                            dialog.dismiss();
                        } else {
                            dialog.dismiss();
                            finish();
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }
                }).show();
                break;

            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
                return true;

        }

        return true;
    }

    public Bitmap loadImageFromUrl(Context context, MyCamera camera) {
        // 是否SD卡可用
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 检查是或有保存图片的文件夹，没有就创建一个
            String FileUrl = Environment.getExternalStorageDirectory() + "/android/data/" + context.getResources().getString(R.string.app_name) + "/";
            File folder = new File(FileUrl);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File f = new File(FileUrl + camera.getUid());
            // SD卡中是否有该文件，有则直接读取返回
            if (f.exists()) {
                Options opts = new Options();
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                if (f.length() > 500 * 1024) {
                    opts.inSampleSize = 3;
                } else {
                    opts.inSampleSize = 2;
                }
                Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
                return bitmap;
            } else {
                return null;
            }
        }

        return null;
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.tips_no_permission));
        builder.setPositiveButton(getString(R.string.setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.MAIN");
                intent.setClassName("com.android.settings", "com.android.settings.ManageApplications");
                startActivity(intent);

            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();

    }
}
