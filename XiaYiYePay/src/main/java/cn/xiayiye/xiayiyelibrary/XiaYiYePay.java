package cn.xiayiye.xiayiyelibrary;
/*
 * Copyright (c) 2018, smuyyh@gmail.com All Rights Reserved.
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
 * #               佛祖保佑         永无BUG             #
 * #                                                   #
 */


import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.fincy.pay.BaseResp;
import com.fincy.pay.FincyClient;
import com.fincy.pay.FincyFactory;
import com.fincy.pay.FincyOrderInfo;
import com.fincy.pay.OtherPayInfo;
import com.fincy.pay.ResultCode;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author ：下一页5（轻飞扬）
 * 创建时间：2018/5/18.10:46
 * 个人小站：http://wap.yhsh.ai(已挂)
 * 最新小站：http://www.iyhsh.icoc.in
 * 联系作者：企鹅 13343401268(请用手机QQ添加)
 * 博客地址：http://blog.csdn.net/xiayiye5
 */
public class XiaYiYePay extends UZModule {
    private final FincyClient client;
    private static UZModuleContext mJsCallback;

    public XiaYiYePay(UZWebView webView) {
        super(webView);
        client = FincyFactory.createFincyClient(context());
    }

    /**
     * 初始化sdk的方法
     *
     * @param moduleContext 载体
     */
    public void jsmethod_initSdk(UZModuleContext moduleContext) {
        String appInitValue = moduleContext.optString("appInitValue");
        FincyClient.init(appInitValue);
        Toast.makeText(context(), "初始化了支付sdk项目传递参数为：" + appInitValue, Toast.LENGTH_SHORT).show();
    }

    public void jsmethod_starXiaYiYetPay(UZModuleContext moduleContext) {
        mJsCallback = moduleContext;
//        Intent intent = new Intent(context(), MainActivity.class);
//        startActivityForResult(intent, 888);

        String orderId = moduleContext.optString("orderId");
        Toast.makeText(context(), "点击了支付按钮,传递过来的支付订单号为：" + orderId, Toast.LENGTH_SHORT).show();
//        下面是吊起支付的方法
        OtherPayInfo info = FincyOrderInfo.pay()
                .orderId(orderId)
                .expireTime(30)
                .build();
        client.pay(info);
    }

/*    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 888) {
            String result = data.getStringExtra("resultData");
            if (null != result && null != mJsCallback) {
                try {
                    JSONObject ret = new JSONObject(result);
                    mJsCallback.success(ret, true);
                    mJsCallback = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

    public static void getPayResult(BaseResp baseResp) {
        String code = "-1";
        String result = "T Don't Known !";
        String msg = "未知错误！";
        switch (baseResp.getResultCode()) {
            case ResultCode.pay_success: {
                Log.e("打印结果回调", "支付成功！");
                code = "0000";
                result = "success";
                msg = "支付成功！";
            }
            break;
            case ResultCode.user_cancel: {
                Log.e("打印结果回调", "支付取消！");
                code = "0001";
                result = "cancel";
                msg = "支付取消！";
            }
            break;
            case ResultCode.pay_failed: {
                String message = baseResp.getMessage();
                if (TextUtils.isEmpty(message)) {
                    message = "失败";
                    Log.e("打印结果回调", "支付失败！");
                    code = "0002";
                    result = "fail";
                    msg = "支付失败！";
                }
            }
            break;
            case ResultCode.un_installed: {
                Log.e("打印结果回调", "没有安装支付APP！");
                code = "0003";
                result = "uninstall App";
                msg = "没有安装支付APP！";
            }
            break;
            default:
                break;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("code", code);
            json.put("result", result);
            json.put("msg", msg);
            mJsCallback.success(json, true);
            mJsCallback = null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
