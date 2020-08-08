package com.j990117178.jfw.secret;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.fincy.pay.login.BaseLoginResultActivity;
import com.fincy.pay.login.LoginResultCode;

/**
 * @author wuxi
 * @since 2020/1/14
 */
public class SecretLoginActivity extends BaseLoginResultActivity {
    @Override
    public void onResult(String resultCode, String authCode) {
        Log.d("SecretLoginActivity", "onResult: resultCode: " + resultCode + "     authCode: " + authCode);
        switch (resultCode) {
            case LoginResultCode.UNKNOWN_FAIL: {
                toast("未知错误");
                break;
            }
            case LoginResultCode.UNINSTALLED: {
                toast("未安装Fincy");
                break;
            }
            case LoginResultCode.USER_CANCEL: {
                toast("用户取消登录");
                break;
            }
            case LoginResultCode.LOGIN_SUCCESS: {
                toast("授权登录：" + authCode);
                break;
            }
            default:
                break;
        }
        finish();
    }

    private void toast(String text) {
        Toast.makeText(SecretLoginActivity.this.getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }
}
