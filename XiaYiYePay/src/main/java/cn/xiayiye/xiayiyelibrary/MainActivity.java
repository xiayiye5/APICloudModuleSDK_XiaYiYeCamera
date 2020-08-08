package cn.xiayiye.xiayiyelibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.fincy.pay.FincyClient;
import com.fincy.pay.FincyFactory;
import com.fincy.pay.FincyOrderInfo;
import com.fincy.pay.OtherPayInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xiayiye
 */
public class MainActivity extends Activity {

    private String takeData;
    private FincyClient client;
    private EditText etOrderId;
    String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FincyClient.init("522198baca3e44938394c7ec6be2eb71");
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.tv);
        etOrderId = findViewById(R.id.et_order);
        //拿到传递过来的订单号
        takeData = getIntent().getStringExtra("takeData");
        //将参数设置给tv显示一下即可
        tv.setText(takeData);
        client = FincyFactory.createFincyClient(this);
        //登陆的方法
        FincyClient.init("");
        client.login();
    }

    //吊起支付
    public void startPay(View view) {
        if (etOrderId.getText().toString().equals("")) {
            orderId = "1217004796755566594";
        } else {
            orderId = etOrderId.getText().toString();
        }
        OtherPayInfo info = FincyOrderInfo.pay()
                .orderId(orderId)
                .expireTime(30)
                .build();
        client.pay(info);
    }

    /**
     * 退出页面回传参数
     *
     * @param view view
     */
    public void finishPage(View view) {
        Intent intent = new Intent();
        JSONObject json = new JSONObject();
        try {
            json.put("code", "0000");
            json.put("result", "success");
            json.put("msg", "支付成功！");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        intent.putExtra("resultData", json.toString());
        setResult(888, intent);
        finish();
    }
}
