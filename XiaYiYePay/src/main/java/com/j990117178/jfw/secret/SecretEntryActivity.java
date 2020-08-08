package com.j990117178.jfw.secret;

import android.text.TextUtils;
import android.widget.Toast;

import com.fincy.pay.BaseFincyEntryActivity;
import com.fincy.pay.BaseResp;
import com.fincy.pay.ResultCode;

import cn.xiayiye.xiayiyelibrary.XiaYiYePay;

/**
 * @author : GuoXuan
 * @since : 2019/4/10
 */
public class SecretEntryActivity extends BaseFincyEntryActivity {

    @Override
    public void onResult(final BaseResp baseResp) {
        XiaYiYePay.getPayResult(baseResp);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*switch (baseResp.getResultCode()) {
                    case ResultCode.pay_success: {
                        toast("成功").show();
                    }
                    break;
                    case ResultCode.user_cancel: {
                        toast("取消").show();
                    }
                    break;
                    case ResultCode.pay_failed: {
                        String message = baseResp.getMessage();
                        if (TextUtils.isEmpty(message)) {
                            message = "失败";
                        }
                        toast(message).show();
                    }
                    break;
                    case ResultCode.un_installed: {
                        toast("没有安装").show();
                    }
                    break;
                    default:
                        break;
                }*/
                finish();
            }
        });
    }

//    private Toast toast(String text) {
//        return Toast.makeText(SecretEntryActivity.this.getApplicationContext(), text, Toast.LENGTH_LONG);
//    }
}
