package cn.xiayiye.xiayiyelibrary;

import android.app.Application;

import com.fincy.pay.FincyClient;

/**
 * @author : GuoXuan
 * @since : 2019/4/18
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FincyClient.init("522198baca3e44938394c7ec6be2eb71");
//        FincyClient.init("9ab02b08d8cd455bbc5c313e3c99cf1e");
    }
}
