package com.hichip.thecamhi.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.hichip.control.HiCamera;
import com.hichip.push.HiPushSDK;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.tools.Packet;
import com.hichip.thecamhi.base.CommandFunction;
import com.hichip.thecamhi.base.DatabaseManager;
import com.hichip.thecamhi.main.CameraFragment;
import com.hichip.thecamhi.utils.SharePreUtils;

import static com.hichip.thecamhi.base.HiTools.checkAddressReNew;

public class MyCamera extends HiCamera {
    private String nikeName = "";
    private int videoQuality = HiDataValue.DEFAULT_VIDEO_QUALITY;// 1: 是标清 0: 是高清
    private int alarmState = HiDataValue.DEFAULT_ALARM_STATE;
    private int pushState = HiDataValue.DEFAULT_PUSH_STATE;
    private boolean hasSummerTimer;
    private boolean isFirstLogin = true;
    private byte[] bmpBuffer = null;
    public Bitmap snapshot = null;
    private long lastAlarmTime;
    private boolean isSetValueWithoutSave = false;
    private int style;
    private String serverData;
    public int isSystemState = 0;// 1重启中 2恢复出厂设置中 3检查更新中
    public boolean alarmLog = false;// 用于小红点是否显示
    public int mInstallMode = 0; // 0-为鱼眼顶装; 1-壁装
    public boolean isFirst = false;// 鱼眼是否第一次进入(指导界面)
    public boolean isChecked; // 分享界面选中状态
    public boolean isWallMounted = false;// 是否是鱼眼壁装新镜头
    public String uid;
    private Context mContext;
    public int u32Resolution = 0;
    public boolean mIsReceived_4179 = false;
    public CommandFunction commandFunction;
    public boolean isIngenic=false;//设备是否为君正

    public MyCamera(Context context, String nikename, String uid, String username, String password) {
        super(context, uid, username, password);
        this.nikeName = nikename;
        this.uid = uid;
        this.mContext = context;
        commandFunction = new CommandFunction();
    }

    public boolean isAlarmLog() {
        return alarmLog;
    }

    public void setAlarmLog(boolean alarmLog) {
        this.alarmLog = alarmLog;
    }

    public void saveInDatabase(Context context) {
        DatabaseManager db = new DatabaseManager(context);
        db.addDevice(nikeName, getUid(), getUsername(), getPassword(), videoQuality, alarmState, pushState);
    }

    public void setSummerTimer(boolean hasSummerTimer) {
        this.hasSummerTimer = hasSummerTimer;
    }

    public boolean getSummerTimer() {
        return this.hasSummerTimer;
    }

    public void setServerData(String serverData) {
        this.serverData = serverData;
    }

    public String getServerData() {
        return this.serverData;
    }

    public void saveInCameraList() {
        if (!HiDataValue.CameraList.contains(this)) {
            HiDataValue.CameraList.add(this);
        }
    }

    public void deleteInCameraList() {
        HiDataValue.CameraList.remove(this);
        this.unregisterIOSessionListener();
        this.unregisterDownloadListener();
        this.unregisterPlayStateListener();
        this.unregisterYUVDataListener();
        snapshot = null;
    }

    public long getLastAlarmTime() {
        return lastAlarmTime;
    }

    public void setLastAlarmTime(long lastAlarmTime) {
        this.lastAlarmTime = lastAlarmTime;
    }

    public void updateInDatabase(Context context) {
        DatabaseManager db = new DatabaseManager(context);
        db.updateDeviceByDBID(nikeName, getUid(), getUsername(), getPassword(), videoQuality, HiDataValue.DEFAULT_ALARM_STATE, pushState, getServerData());

        isSetValueWithoutSave = false;
    }

    public void updateServerInDatabase(Context context) {
        DatabaseManager db = new DatabaseManager(context);
        db.updateServerByUID(getUid(), getServerData());

        isSetValueWithoutSave = false;
    }

    public void deleteInDatabase(Context context) {
        DatabaseManager db = new DatabaseManager(context);
        db.removeDeviceByUID(this.getUid());
        db.removeDeviceAlartEvent(this.getUid());
    }

    public int getAlarmState() {
        return alarmState;
    }

    public void setAlarmState(int alarmState) {
        this.alarmState = alarmState;
    }

    public int getPushState() {
        return pushState;
    }

    public void setPushState(int pushState) {
        this.pushState = pushState;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public int getStyle() {

        return style;
    }

    public int getVideoQuality() {
        return videoQuality;
    }

    public void setVideoQuality(int videoQuality) {
        this.videoQuality = videoQuality;
    }

    public String getNikeName() {
        return nikeName;
    }

    public void setNikeName(String nikeName) {
        this.nikeName = nikeName;
    }

    private int curbmpPos = 0;

    public boolean reciveBmpBuffer(byte[] byt) {
        if (byt.length < 10) {
            return false;
        }
        if (bmpBuffer == null) {
            curbmpPos = 0;
            int buflen = Packet.byteArrayToInt_Little(byt, 0);
            if (buflen <= 0) {
                return false;
            }
            bmpBuffer = new byte[buflen];
        }
        int datalen = Packet.byteArrayToInt_Little(byt, 4);
        if (curbmpPos + datalen <= bmpBuffer.length)
            System.arraycopy(byt, 10, bmpBuffer, curbmpPos, datalen);
        curbmpPos += (datalen);
        short flag = Packet.byteArrayToShort_Little(byt, 8);
        if (flag == 1) {
            createSnapshot();
            return true;
        }
        return false;
    }

    private void createSnapshot() {
        Bitmap snapshot_temp = BitmapFactory.decodeByteArray(bmpBuffer, 0, bmpBuffer.length);
        if (snapshot_temp != null)
            snapshot = snapshot_temp;

        bmpBuffer = null;
        curbmpPos = 0;

    }

    public boolean isFirstLogin() {
        return isFirstLogin;
    }

    public void setFirstLogin(boolean isFirstLogin) {
        this.isFirstLogin = isFirstLogin;
    }

    public boolean isSetValueWithoutSave() {
        return isSetValueWithoutSave;
    }

    @Override
    public void connect() {
        // int i = 0;
        // if (getUid() != null && getUid().length() > 4) {
        // for (String str : HiDataValue.limit) {
        // i++;
        // String temp = getUid().substring(0, str.length());
        // if (temp.equalsIgnoreCase(str)) {
        // break;
        // }
        // }
        // if (i == HiDataValue.limit.length) {
        // super.connect();
        // return;
        // }
        // return;
        // } else {
        // return;
        // }
        if (getUid() != null && getUid().length() > 4) {
            // String temp = getUid().substring(0, 5);
            String str = getUid().substring(0, 4);
            // if (temp.equalsIgnoreCase("FDTAA") || str.equalsIgnoreCase("DEAA") ||
            // str.equalsIgnoreCase("AAES")) {
            if (str.equalsIgnoreCase("AAES")) {
                return;
            } else {
                super.connect();
                return;
            }
        } else {
            return;
        }
    }

    public interface OnBindPushResult {
        public void onBindSuccess(MyCamera camera);

        public void onBindFail(MyCamera camera);

        public void onUnBindSuccess(MyCamera camera);

        public void onUnBindFail(MyCamera camera);
    }

    private OnBindPushResult onBindPushResult;

    public HiPushSDK push;
    private HiPushSDK.OnPushResult pushResult = new HiPushSDK.OnPushResult() {
        @Override
        public void pushBindResult(int subID, int type, int result) {
            isSetValueWithoutSave = true;
            Log.e("final_Bind_address==", push.getPushServer());
            if (type == HiPushSDK.PUSH_TYPE_BIND) {
                if (HiPushSDK.PUSH_RESULT_SUCESS == result) {
                    pushState = subID;
                    if (onBindPushResult != null)
                        onBindPushResult.onBindSuccess(MyCamera.this);
                } else if (HiPushSDK.PUSH_RESULT_FAIL == result || HiPushSDK.PUSH_RESULT_NULL_TOKEN == result) {
                    if (onBindPushResult != null)
                        onBindPushResult.onBindFail(MyCamera.this);
                }
            } else if (type == HiPushSDK.PUSH_TYPE_UNBIND) {
                if (HiPushSDK.PUSH_RESULT_SUCESS == result) {
                    if (onBindPushResult != null)
                        onBindPushResult.onUnBindSuccess(MyCamera.this);
                } else if (HiPushSDK.PUSH_RESULT_FAIL == result) {
                    if (onBindPushResult != null)
                        onBindPushResult.onUnBindFail(MyCamera.this);
                }

            }

        }
    };

    public void bindPushState(boolean isBind, OnBindPushResult bindPushResult) {
        if (HiDataValue.XGToken == null) {
            return;
        }
        //		Log.e("=====", "XGToken=" + HiDataValue.XGToken + "---" + "company=" + HiDataValue.company + "ser"
        //				+ this.getServerData());

        /* 地址变更 解绑时 用旧的服务器 */

        /*原来的写法*/
        if (!isBind && this.getServerData() != null && !this.getServerData().equals(HiDataValue.CAMERA_ALARM_ADDRESS_233)) {
            push = new HiPushSDK(HiDataValue.XGToken, getUid(), HiDataValue.company, pushResult, this.getServerData());
            push.setPushServer(this.getServerData(),1,1);//解绑强制设置地址
        } else if (this.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET) && !handSubXYZ()) {
            if (handSubWTU()) {
                push = new HiPushSDK(HiDataValue.XGToken, getUid(), HiDataValue.company, pushResult, HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122);
                checkAddressReNew(push, HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122,push.getPushServer());
            } else {
                push = new HiPushSDK(HiDataValue.XGToken, getUid(), HiDataValue.company, pushResult, HiDataValue.CAMERA_ALARM_ADDRESS_233);
            }
        } else if (this.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET) && handSubXYZ()) {
            push = new HiPushSDK(HiDataValue.XGToken, getUid(), HiDataValue.company, pushResult, HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173);
            checkAddressReNew(push, HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173,push.getPushServer());
        } else {// old device
            if (handSubWTU()) {
                push = new HiPushSDK(HiDataValue.XGToken, getUid(), HiDataValue.company, pushResult, HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122);
                checkAddressReNew(push, HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122,push.getPushServer());
            } else {
                push = new HiPushSDK(HiDataValue.XGToken, getUid(), HiDataValue.company, pushResult, HiDataValue.CAMERA_ALARM_ADDRESS_233);
            }
        }

        Log.e("==push.getPushServer()", push.getPushServer());


        /*更新后的写法*/
        //        if (!isBind && this.getServerData() != null && !this.getServerData().equals(HiDataValue.CAMERA_ALARM_ADDRESS_233)) {
        //            push = new HiPushSDK(HiDataValue.XGToken, getUid(), HiDataValue.company, pushResult, this.getServerData());
        //        } else {
        //            if (isDEAA()) {
        //                push = new HiPushSDK(HiDataValue.XGToken, getUid(), HiDataValue.company, pushResult,
        //                        HiDataValue.CAMERA_ALARM_ADDRESS_DERICAM_148);
        //            } else if (isFDTAA()) {
        //                push = new HiPushSDK(HiDataValue.XGToken, getUid(), HiDataValue.company, pushResult,
        //                        HiDataValue.CAMERA_ALARM_ADDRESS_FDT_221);
        //            } else if (handSubXYZ()) {
        //                push = new HiPushSDK(HiDataValue.XGToken, getUid(), HiDataValue.company, pushResult,
        //                        HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173);
        //            } else if (handSubWTU()) {
        //                push = new HiPushSDK(HiDataValue.XGToken, getUid(), HiDataValue.company, pushResult,
        //                        HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122);
        //            } else {
        //                push = new HiPushSDK(HiDataValue.XGToken, getUid(), HiDataValue.company, pushResult,
        //                        HiDataValue.CAMERA_ALARM_ADDRESS_233);
        //            }
        //        }

        onBindPushResult = bindPushResult;
        if (isBind) {
            push.bind();
        } else {
            push.unbind(getPushState());
        }
    }
    

    /**
     * 处理UID前缀为XXX YYYY ZZZ
     *
     * @return 如果是则返回 true
     */
    public boolean handSubXYZ() {
        String subUid = this.getUid().substring(0, 4);
        for (String str : HiDataValue.SUBUID) {
            if (str.equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理UID前缀为WWW TTT UUU
     *
     * @return 如果是则返回 true
     */
    public boolean handSubWTU() {
        String subUid = this.getUid().substring(0, 4);
        for (String str : HiDataValue.SUBUID_WTU) {
            if (str.equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return 是否是FDTAA 设备
     */
    public boolean isFDTAA() {
        if (!TextUtils.isEmpty(this.getUid()) && this.getUid().length() > 4) {
            String subUid = this.getUid().substring(0, 4);
            if ("FDTAA".equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return 是否是DEAA 设备
     */
    public boolean isDEAA() {
        if (!TextUtils.isEmpty(this.getUid()) && this.getUid().length() > 4) {
            String subUid = this.getUid().substring(0, 4);
            if ("DEAA".equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断Camera 是否是鱼眼设备
     */
    public boolean isFishEye() {
        if (mContext == null)
            return false;
        int isFishEye = 0;
        if (this.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
            isFishEye = this.getmold();
            SharePreUtils.putInt("cache", mContext, this.getUid() + "isFishEye", this.getmold());
        } else {
            isFishEye = SharePreUtils.getInt("cache", mContext, this.getUid() + "isFishEye");
        }
        if (isFishEye == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     */
    public void putFishModType(int fishModetype) {
        if (mContext == null)
            return;
        SharePreUtils.putInt("cache", mContext, this.getUid() + "fishmodtype", fishModetype);
    }

    /**
     */
    public int getFishModType() {
        if (mContext == null)
            return 0;
        int type = SharePreUtils.getInt("cache", mContext, this.getUid() + "fishmodtype");
        if (type == -1) {
            return 0;
        } else {
            return type;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MyCamera other = (MyCamera) obj;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        return true;
    }
}
