package com.hichip;
/*
 * Copyright (c) 2020, smuyyh@gmail.com All Rights Reserved.
 * #                                                   #
 * #                       _oo0oo_                     #
 * #                      o8888888o                    #
 * #                      88" . "88                    #
 * #                      (| -_- |)                    #
 * #                      0\  =  /0                    #
 * #                    ___/`---'\___                  #
 * #                  .' \\|     |# '.                 #
 * #                 / \\|||  :  |||# \                #
 * #                / _||||| -:- |||||- \              #
 * #               |   | \\\  -  #/ |   |              #
 * #               | \_|  ''\---/''  |_/ |             #
 * #               \  .-\__  '-'  ___/-. /             #
 * #             ___'. .'  /--.--\  `. .'___           #
 * #          ."" '<  `.___\_<|>_/___.' >' "".         #
 * #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 * #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 * #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 * #                       `=---='                     #
 * #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 * #                                                   #
 * #               佛祖保佑         永无BUG            #
 * #                                                   #
 */

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.control.HiCamera;
import com.hichip.thecamhi.activity.AddCameraActivity;
import com.hichip.thecamhi.activity.LiveViewActivity;
import com.hichip.thecamhi.base.HiTools;
import com.hichip.thecamhi.bean.HiDataValue;
import com.hichip.thecamhi.bean.MyCamera;
import com.hichip.thecamhi.main.AboutActivity;
import com.hichip.thecamhi.main.CameraListActivity;
import com.hichip.thecamhi.main.MainActivity;
import com.hichip.thecamhi.main.PictureActivity;
import com.hichip.thecamhi.main.VideoActivity;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author 下一页5（轻飞扬）
 * 创建时间：2020/7/20 21:34
 * 个人小站：http://yhsh.wap.ai(已挂)
 * 最新小站：http://www.iyhsh.icoc.in
 * 联系作者：企鹅 13343401268
 * 博客地址：http://blog.csdn.net/xiayiye5
 * 项目名称：APICloudModuleSDK
 * 文件包名：com.hichip
 * 文件说明：
 */
public class XiaYiYeCamera extends UZModule implements ICameraIOSessionCallback {
    private CameraBroadcastReceiver receiver = new CameraBroadcastReceiver();

    private UZModuleContext mJsCallback;
    private SharedPreferences cameraDataSP = context().getSharedPreferences("cameraData", Context.MODE_PRIVATE);

    public XiaYiYeCamera(UZWebView webView) {
        super(webView);
    }

    /**
     * 获取摄像机列表的方法
     *
     * @param moduleContext 载体
     */
    public void jsmethod_getLocalCameraList(UZModuleContext moduleContext) {
        HiDataValue.ANDROID_VERSION = HiTools.getAndroidVersion();
        if (HiDataValue.ANDROID_VERSION >= 23) {
            HiTools.checkPermissionAll(activity());
        }
        String getCameraListData = moduleContext.optString("appParam");
        //获取本地摄像机列表数据
        String cameraListData = cameraDataSP.getString("cameraListData", "");
        if (TextUtils.isEmpty(cameraListData)) {
            Toast.makeText(context(), "暂无数据,请先添加摄像机", Toast.LENGTH_SHORT).show();
            return;
        }
        //回传到js页面
        try {
            JSONObject jsonObject = new JSONObject(cameraListData);
            moduleContext.success(jsonObject, true);
            JSONArray camera = jsonObject.getJSONArray("camera");
            for (int i = 0; i < camera.length(); i++) {
                JSONObject jsonArrayData = camera.getJSONObject(i);
                String uid = jsonArrayData.getString("uid");
                String username = jsonArrayData.getString("username");
                String password = jsonArrayData.getString("password");
                MyCamera myCamera = new MyCamera(context(), username, uid, username, password);
                //将数据设置给 HiDataValue.CameraList
                HiDataValue.CameraList.add(myCamera);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加摄像机的方法
     *
     * @param moduleContext 载体
     */
    public void jsmethod_addCamera(UZModuleContext moduleContext) {
        mJsCallback = moduleContext;
        String appInitValue = moduleContext.optString("jsData");
        Toast.makeText(context(), "js传递过来的参数为：" + appInitValue, Toast.LENGTH_SHORT).show();
        //打开添加摄像机的方法
        startActivity(new Intent(context(), AddCameraActivity.class));
//        HiDataValue.CameraList.add(new MyCamera(context(), "xiayiye", "admin", "admin", "admin"));
        IntentFilter filter = new IntentFilter();
        filter.addAction(HiDataValue.ACTION_CAMERA_INIT_END);
        context().registerReceiver(receiver, filter);
    }

    /**
     * 连接摄像机
     *
     * @param moduleContext 上下文
     */
    public void jsmethod_connectCamera(UZModuleContext moduleContext) {
        if (HiDataValue.CameraList.size() == 0) {
            Toast.makeText(context(), "请先添加摄像机！", Toast.LENGTH_SHORT).show();
            return;
        }
        //position代表摄像机列表中的位置
        String position = moduleContext.optString("position");
        MyCamera camera = HiDataValue.CameraList.get(Integer.valueOf(position));
        while (true) {
            camera.connect();
            int state = camera.getConnectState();
            if (state == 4) {
                Toast.makeText(context(), "相机连接成功!", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    /**
     * 打开在摄相机的方法
     *
     * @param moduleContext 上下文
     */
    public void jsmethod_openCamera(UZModuleContext moduleContext) {
        if (HiDataValue.CameraList.size() == 0) {
            Toast.makeText(context(), "请先添加摄像机！", Toast.LENGTH_SHORT).show();
            return;
        }
        //position代表摄像机列表中的位置
        String position = moduleContext.optString("position");
        MyCamera camera = HiDataValue.CameraList.get(Integer.valueOf(position));
        Toast.makeText(context(), "js传递过来的相机position为：" + position, Toast.LENGTH_SHORT).show();
        while (true) {
            int state = camera.getConnectState();
            if (state == 4) {
                //打开添加摄像机的方法
                Intent intent = new Intent(context(), LiveViewActivity.class);
                Bundle extras = new Bundle();
                extras.putString(HiDataValue.EXTRAS_KEY_UID, HiDataValue.CameraList.get(Integer.valueOf(position)).uid);
                intent.putExtras(extras);
                startActivity(intent);
                break;
            }
        }

        /*switch (state) {
            case 0:// DISCONNECTED
                Log.e("打印状态", "断开");
                break;
            case -8:
            case 1:// CONNECTING
                Log.e("打印状态", "连接中");
                break;
            case 2:// CONNECTED
                Log.e("打印状态", "已连接");
                break;
            case 3:// WRONG_PASSWORD
                Log.e("打印状态", "密码错误");
                break;
            case 4:// STATE_LOGIN
                Log.e("打印状态", "登陆中");
                break;
            default:
        }*/
    }

    /**
     * 打开照片的方法
     *
     * @param moduleContext 上席文
     */
    public void jsmethod_openPicture(UZModuleContext moduleContext) {
        String appInitValue = moduleContext.optString("cameraId");
        Toast.makeText(context(), "js传递过来的参数为：" + appInitValue, Toast.LENGTH_SHORT).show();
        //打开照片的方法
        startActivity(new Intent(context(), PictureActivity.class));
    }

    /**
     * 打开保存的视频的方法
     *
     * @param moduleContext 上下文
     */
    public void jsmethod_openSaveVideo(UZModuleContext moduleContext) {
        String appInitValue = moduleContext.optString("cameraId");
        Toast.makeText(context(), "js传递过来的参数为：" + appInitValue, Toast.LENGTH_SHORT).show();
        //打开照片的方法
        startActivity(new Intent(context(), VideoActivity.class));
    }

    /**
     * 打开关于的页面方法
     *
     * @param moduleContext 上下文
     */
    public void jsmethod_openAboutPage(UZModuleContext moduleContext) {
        String appInitValue = moduleContext.optString("cameraId");
        Toast.makeText(context(), "js传递过来的参数为：" + appInitValue, Toast.LENGTH_SHORT).show();
        //打开照片的方法
        startActivity(new Intent(context(), AboutActivity.class));
    }

    /**
     * 打开摄像机列表的方法
     *
     * @param moduleContext 上下文
     */
    public void jsmethod_openCameraList(UZModuleContext moduleContext) {
        String appInitValue = moduleContext.optString("cameraId");
        Toast.makeText(context(), "js传递过来的参数为：" + appInitValue, Toast.LENGTH_SHORT).show();
        //打开照片的方法
        startActivity(new Intent(context(), CameraListActivity.class));
    }

    @Override
    public void receiveSessionState(HiCamera hiCamera, int i) {
        //添加成功回调数据
    }

    @Override
    public void receiveIOCtrlData(HiCamera hiCamera, int i, byte[] bytes, int i1) {
    }

    private class CameraBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("==ConnectState=", "CameraBroadcastReceiver");
            if (intent.getAction().equals(HiDataValue.ACTION_CAMERA_INIT_END)) {
                if (HiDataValue.ANDROID_VERSION >= 23 && !HiTools.checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    return;
                }
                new HiThreadConnect().start();
            }
        }
    }

    public class HiThreadConnect extends Thread {
        private int connnum = 0;

        @Override
        public synchronized void run() {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (MyCamera myCamera : HiDataValue.CameraList) {
                String username = myCamera.getUsername();
                String uid = myCamera.getUid();
                String password = myCamera.getPassword();
                JSONObject one = new JSONObject();
                try {
                    one.put("username", username);
                    one.put("uid", uid);
                    one.put("password", password);
                    jsonArray.put(one);
                    jsonObject.put("camera", jsonArray);
                    Log.e("打印Json", jsonObject.toString());
                    //保存摄像机列表到本地
                    cameraDataSP.edit().putString("cameraListData", jsonObject.toString()).apply();
                    if (null != mJsCallback) {
                        mJsCallback.success(jsonObject, true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            for (connnum = 0; connnum < HiDataValue.CameraList.size(); connnum++) {
                MyCamera camera = HiDataValue.CameraList.get(connnum);
                Log.e("==ConnectState=", camera.getConnectState() + "");
                if (camera != null) {
                    if (camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED) {
                        camera.registerIOSessionListener(XiaYiYeCamera.this);
                        camera.connect();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }
}
