/*
package com.hichip.thecamhi.activity;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hichip.R;
import com.hichip.base.HiLog;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.customview.dialog.NiftyDialogBuilder;
import com.hichip.hichip.activity.RF.SetUpAndAddRFActivity;
import com.hichip.sdk.HiChipSDK;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.bean.RFDevice;
import com.hichip.thecamhi.main.MainActivity;
import com.hichip.thecamhi.zxing.utils.UriUtils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import cn.bingoogolapple.qrcode.core.QRCodeView;


public class ScanQRCodeActivity extends AppCompatActivity implements QRCodeView.Delegate,ICameraIOSessionCallback {

    private MediaPlayer mediaPlayer;
    private static final float BEEP_VOLUME = 0.10f;
    private Button cancelScanButton;



    private ArrayList<MyCamera> mAnalyCameraList = new ArrayList<>();
    private int category = 0;  //category==3  是AddcameraActivity跳转过来的
    private ArrayList<RFDevice> list_rf_info = new ArrayList<>();
    private ArrayList<RFDevice> list_rf_device_key = new ArrayList<>();
    private List<HiChipDefines.HI_P2P_IPCRF_INFO> list_IPCRF = new ArrayList<HiChipDefines.HI_P2P_IPCRF_INFO>();

    private String mUid;
    private MyCamera mMyCamera;
    private String[] code_arr;
    private boolean playBeep;
    private boolean vibrate;
    private QRCodeView mZBarView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);
        getIntentData();
        cancelScanButton = (Button) findViewById(R.id.btn_cancel_scan);
        mZBarView = (QRCodeView) findViewById(R.id.zBar_view);
        mZBarView.setDelegate(this);

    }

    @SuppressWarnings("unchecked")
    private void getIntentData() {
        category = getIntent().getIntExtra("category", 0);
        list_rf_info = (ArrayList<RFDevice>) getIntent().getSerializableExtra("list_rf_info");
        list_rf_device_key = (ArrayList<RFDevice>) getIntent().getSerializableExtra("list_rf_device_key");
        mUid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
        for(MyCamera camera:HiDataValue.CameraList){
            if(!TextUtils.isEmpty(mUid)){
                if(mUid.equalsIgnoreCase(camera.getUid())){
                    this.mMyCamera=camera;
                    break;
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mZBarView.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
        mZBarView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.5秒后开始识别
    }

    @Override
    public void onScanQRCodeSuccess(String resultString) {
        playBeepSoundAndVibrate();
        if (TextUtils.isEmpty(resultString)) {
            Toast.makeText(ScanQRCodeActivity.this, getString(R.string.toast_scan_fail), Toast.LENGTH_SHORT).show();
        } else {
            if (!TextUtils.isEmpty(resultString) && resultString.length() > 8) {
                String sub = resultString.substring(0, 8);
                if (sub.equalsIgnoreCase(getString(R.string.app_name) + "_AC")) {// 二维码是加密分享的UID
                    handData(resultString);
                }
                else if (category == 1 && resultString.substring(0, 1).equalsIgnoreCase("0")) {
                    handRFData(resultString);
                } else {// 二维码是UID
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString(HiDataValue.EXTRAS_KEY_UID, resultString);
                    resultIntent.putExtras(bundle);
                    ScanQRCodeActivity.this.setResult(RESULT_OK, resultIntent);
                    ScanQRCodeActivity.this.finish();
                }
            }
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {

    }



    @Override
    protected void onResume() {
        super.onResume();
        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

        // quit the scan view
        cancelScanButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               ScanQRCodeActivity.this.finish();
            }
        });
        if (mMyCamera != null) {
            mMyCamera.registerIOSessionListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMyCamera != null) {
            mMyCamera.unregisterIOSessionListener(this);
        }
    }

    @Override
    protected void onStop() {
        mZBarView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mZBarView.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }



    private void handRFData(final String resultString) {
        // resultString: 02bbba2a000000
        // resultString: 08f47c82000000-07f47c84000000-09f47c81000000-0Af47c88000000
        code_arr = resultString.split("-");
        if (code_arr.length == 1) {
            handLen_1(resultString);
        }
        if (code_arr.length == 4) {
            if(list_rf_device_key!=null&&list_rf_device_key.size()>0){
                HiToast.showToast(this, "请删除添加的遥控器,再扫码添加！");
               ScanQRCodeActivity.this.finish();
                return;
            }
            StringBuffer sb = new StringBuffer();
            for (String str : code_arr) {
                sb.append(handCate(str) + "\n");
            }

            final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(this);
            dialog.withMessageLayoutWrap();
            dialog.withTitle("设别成功").withMessage("检测到传感器:\n\n" + sb.toString() + "\n确认是否添加?\n");
            dialog.withButton1Text("取消").withButton2Text("确认添加");
            dialog.setButton1Click(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                   ScanQRCodeActivity.this.finish();
                }
            });
            dialog.setButton2Click(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET, null);
                }
            });
            dialog.isCancelable(false);
            dialog.show();

        }


    }


    private void handLen_1(final String resultString) {
        final String resultCode = resultString.substring(2);
        if (list_rf_info != null && list_rf_info.size() > 0) {
            for (RFDevice device : list_rf_info) {
                if (resultCode.equalsIgnoreCase(device.code)) {
                    HiToast.showToast(this, "该设备已经添加过！");
                   ScanQRCodeActivity.this.finish();
                    return;
                }
            }
        }
        String cate = handCate(resultString);
        final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(this);
        dialog.withTitle("设别成功").withMessage("检测到传感器:  " + cate + "\n" + "确认是否添加?");
        dialog.withButton1Text("取消").withButton2Text("确认添加");
        dialog.setButton1Click(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
               ScanQRCodeActivity.this.finish();
                return;
            }
        });
        dialog.setButton2Click(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanQRCodeActivity.this, SetUpAndAddRFActivity.class);
                intent.putExtra(HiDataValue.EXTRAS_RF_TYPE, handCateType(resultString));
                intent.putExtra(HiDataValue.EXTRAS_KEY_DATA, resultCode.getBytes());
                intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mUid);
                startActivity(intent);
                return;
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private String handCateType(String resultString) {
        String sub = resultString.substring(0, 2);
        switch (sub) {
            case "01":
                sub = "infra";
                break;
            case "02":
                sub = "door";
                break;
            case "03":
                sub = "fire";
                break;
            case "04":
                sub = "gas";
                break;
            case "05":
                sub = "beep";
                break;
            case "06":
                sub = "beep";
                break;
            case "07":
                sub = "key1";
                break;
            case "08":
                sub = "key0";
                break;
            case "09":
                sub = "key2";
                break;
            case "0A":
                sub = "key3";
                break;
        }
        return sub;
    }

    private String handCate(String resultString) {
        String sub = resultString.substring(0, 2);
        switch (sub) {
            case "01":
                sub = "红外";
                break;
            case "02":
                sub = "门磁";
                break;
            case "03":
                sub = "烟雾";
                break;
            case "04":
                sub = "燃气";
                break;
            case "05":
                sub = "门铃";
                break;
            case "06":
                sub = "插座";
                break;
            case "07":
                sub = "RF报警:开";
                break;
            case "08":
                sub = "RF报警:关";
                break;
            case "09":
                sub = "SOS";
                break;
            case "0A":
                sub = "报警响铃";
                break;
        }
        return sub;
    }

    private void handData(String resultString) {
        String string = resultString.substring(8, resultString.length());
        byte[] buff = new byte[resultString.getBytes().length];
        byte[] datas = string.getBytes();
        System.arraycopy(datas, 0, buff, 0, datas.length);
        HiChipSDK.Aes_Decrypt(buff, datas.length);
        String decryptStr = new String(buff).trim();
        // 解析数据
        analyData(decryptStr);
    }

    private StringBuffer sbAddCamerUid = new StringBuffer();

    private void analyData(String string) {
        try {
            JSONArray jsonArray = new JSONArray(string);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String uid = jsonObject.getString("U").substring(0, jsonObject.getString("U").length() - 2);
                String username = jsonObject.getString("A").substring(0, jsonObject.getString("A").length() - 2);
                String password = jsonObject.getString("P").substring(0, jsonObject.getString("P").length() - 2);
                MyCamera camera = new MyCamera(ScanQRCodeActivity.this, getString(R.string.title_camera_fragment), uid, username, password);
                mAnalyCameraList.add(camera);
            }
            if (mAnalyCameraList != null && mAnalyCameraList.size() > 0) {
                for (MyCamera camera : HiDataValue.CameraList) {
                    for (int i = 0; i < mAnalyCameraList.size(); i++) {
                        if (camera.getUid().equalsIgnoreCase(mAnalyCameraList.get(i).getUid())) {
                            mAnalyCameraList.remove(i);
                        }
                    }
                }
                if (mAnalyCameraList.size() < 1) {
                    HiToast.showToast(ScanQRCodeActivity.this, getString(R.string.toast_device_added));
                   ScanQRCodeActivity.this.finish();
                } else {
                    for (int i = 0; i < mAnalyCameraList.size(); i++) {
                        MyCamera camera = mAnalyCameraList.get(i);
                        if (i < mAnalyCameraList.size() - 1) {
                            sbAddCamerUid.append(camera.getUid() + "\n");
                        } else {
                            sbAddCamerUid.append(camera.getUid());
                        }
                    }
                    final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(ScanQRCodeActivity.this);
                    if (mAnalyCameraList.size() > 3) {
                        dialog.withMessageLayoutWrap();
                    }
                    dialog.withTitle(getString(R.string.add_camera)).withMessage(sbAddCamerUid.toString()).withButton1Text(getString(R.string.cancel)).withButton2Text(getString(R.string.toast_confirm_add));
                    dialog.setButton1Click(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                           ScanQRCodeActivity.this.finish();
                        }
                    });
                    dialog.setButton2Click(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (MyCamera camera : mAnalyCameraList) {
                                camera.saveInDatabase(ScanQRCodeActivity.this);
                                camera.saveInCameraList();
                            }
                            dialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(HiDataValue.ACTION_CAMERA_INIT_END);
                            sendBroadcast(intent);

                            intent = new Intent(ScanQRCodeActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
                    dialog.isCancelable(false);
                    dialog.show();
                }
            }
        } catch (JSONException e) {
            HiToast.showToast(ScanQRCodeActivity.this, getString(R.string.toast_scan_fail));
            e.printStackTrace();
        }

    }

    */
/*
     * 获取带二维码的相片进行扫描
     *//*

    @SuppressLint("InlinedApi")
    public void pickPictureFromAblum(View v) {
        // 打开手机中的相册
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        innerIntent.setType("image/*");
        startActivityForResult(innerIntent, 0X22);
    }

    String photo_path;
    ProgressDialog mProgress;
    Bitmap scanBitmap;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0X22:
                    handleAlbumPic(data);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    */
/**
     * 处理选择的图片
     *
     * @param data
     *//*

    private void handleAlbumPic(Intent data) {
        // 获取选中图片的路径
        photo_path = UriUtils.getRealPathFromUri(ScanQRCodeActivity.this, data.getData());
        mProgress = new ProgressDialog(ScanQRCodeActivity.this);
        mProgress.setMessage(getString(R.string.toast_scanning));
        mProgress.setCancelable(false);
        mProgress.show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgress.dismiss();
                mZBarView.decodeQRCode(photo_path);
            }
        });
    }







    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    */
/**
     * When the beep has finished playing, rewind to queue up another one.
     *//*

    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    @Override
    public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
        if(mMyCamera!=arg0) return;
        Message msg=mHandler.obtainMessage();
        msg.what=HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL;
        msg.arg1=arg1;
        msg.arg2=arg3;
        Bundle bundle=new Bundle();
        bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

    }

    @Override
    public void receiveSessionState(HiCamera arg0, int arg1) {

    }

    private  int num=0;
    private int[] indexs=new int[4];
    private List<Integer> list_index=new ArrayList<>();

    @SuppressLint("HandlerLeak")
    private Handler mHandler=new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
                    if(msg.arg2==0){
                        byte[] data=msg.getData().getByteArray(HiDataValue.EXTRAS_KEY_DATA);
                        switch (msg.arg1) {
                            case HiChipDefines. HI_P2P_IPCRF_SINGLE_INFO_SET:
                                num++;
                                if(num==code_arr.length){
                                    HiToast.showToast(ScanQRCodeActivity.this, "添加成功！");
                                   ScanQRCodeActivity.this.finish();
                                }
                                break;
                            case HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET:
                                HiChipDefines.HI_P2P_IPCRF_ALL_INFO allRfInfo = new HiChipDefines.HI_P2P_IPCRF_ALL_INFO(data);
                                for (int i = 0; i < allRfInfo.sRfInfo.length; i++) {
                                    HiChipDefines.HI_P2P_IPCRF_INFO info = allRfInfo.sRfInfo[i];
                                    list_IPCRF.add(info);
                                }
                                if (allRfInfo.u32Flag == 1) {// 数据收结束了
                                    if(code_arr.length>1){
                                        //找四个可用的index
                                        for (int j = 0; j < list_IPCRF.size(); j++) {
                                            String strCode = new String(list_IPCRF.get(j).sRfCode).trim();
                                            if (TextUtils.isEmpty(strCode) || strCode.length() < 10) {
                                                // 找到了可用的index
                                                if(list_index.size()<5){
                                                    list_index.add(list_IPCRF.get(j).u32Index);
                                                }else {
                                                    break;
                                                }
                                            }
                                        }
                                        if(list_index.size()<4){
                                            HiToast.showToast(ScanQRCodeActivity.this, "已到达RF设备添加的上限,如果想继续添加,请删除之前添加的设备！");
                                            return;
                                        }
                                        for(int i=0;i<code_arr.length;i++){
                                            String str=code_arr[i];
                                            String code=str.substring(2);
                                            handIndexAndAdd(list_index.get(i),handCateType(str), (byte)0, handCate(str), code);
                                        }
                                    }
                                }
                                break;
                        }
                    }else {
                        switch (msg.arg1) {
                            case HiChipDefines. HI_P2P_IPCRF_SINGLE_INFO_SET:
                            case HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET:
                                HiToast.showToast(ScanQRCodeActivity.this, getString(R.string.toast_scan_fail));
                               ScanQRCodeActivity.this.finish();
                                break;

                        }

                    }

                    break;

            }

        };
    };

    private void handIndexAndAdd(int index,String type, byte ptzLink,String mRfName,String mCode) {
        int inde = index;
        int enable = 1;
        String code = mCode;
        String typeu = type;
        String name = mRfName;
        byte voiceLink = (byte)1;
        byte ptzLinkf = ptzLink;
        mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_SET, HiChipDefines.HI_P2P_IPCRF_INFO.parseContent(inde, enable, code, typeu, name, voiceLink, ptzLinkf));
    }


}*/
