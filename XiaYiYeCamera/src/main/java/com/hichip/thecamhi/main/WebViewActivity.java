package com.hichip.thecamhi.main;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.hichip.R;
import com.hichip.thecamhi.base.TitleView;

import static android.webkit.WebSettings.LOAD_NO_CACHE;


/**
 * 协议页面
 */
public class WebViewActivity extends HiActivity {
    String mtitle, webUrl;
    private WebView mwebView;
    LinearLayout llEmpty;
    RelativeLayout webParentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        initView();

    }

    private void initView() {
        mtitle = getIntent().getStringExtra("title");
        webUrl = getIntent().getStringExtra("webUrl");
        TitleView title = (TitleView) findViewById(R.id.title_top);
        mwebView = findViewById(R.id.web_agreement);
        llEmpty = findViewById(R.id.ll_empty);
        mwebView.getSettings().setCacheMode(LOAD_NO_CACHE);
        mwebView.clearCache(true);//每次清理缓存
        title.setTitle(mtitle.substring(1, mtitle.length() - 1));
        title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new TitleView.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        WebViewActivity.this.finish();
                        break;
                }

            }
        });

        //支持javascript
        mwebView.getSettings().setJavaScriptEnabled(true);
        // 设置可以支持缩放
        mwebView.getSettings().setSupportZoom(true);
        // 设置出现缩放工具
        mwebView.getSettings().setBuiltInZoomControls(true);
        //扩大比例的缩放
        mwebView.getSettings().setUseWideViewPort(true);
        //自适应屏幕
        mwebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mwebView.getSettings().setLoadWithOverviewMode(true);
        mwebView.getSettings().setDefaultTextEncodingName("utf-8");

        //如果不设置WebViewClient，请求会跳转系统浏览器
        mwebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //该方法在Build.VERSION_CODES.LOLLIPOP以前有效，从Build.VERSION_CODES.LOLLIPOP起，建议使用shouldOverrideUrlLoading(WebView, WebResourceRequest)} instead
                //返回false，意味着请求过程里，不管有多少次的跳转请求（即新的请求地址），均交给webView自己处理，这也是此方法的默认处理
                //返回true，说明你自己想根据url，做新的跳转，比如在判断url符合条件的情况下，我想让webView加载http://ask.csdn.net/questions/178242

                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //返回false，意味着请求过程里，不管有多少次的跳转请求（即新的请求地址），均交给webView自己处理，这也是此方法的默认处理
                //返回true，说明你自己想根据url，做新的跳转，
                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                //6.0以下执行
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return;
                }
                showErrorPage();
            }

            //处理网页加载失败时
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                //6.0以上执行
                showErrorPage();//显示错误页面
            }
        });
        mwebView.loadUrl(webUrl);
        webParentView = (RelativeLayout) mwebView.getParent(); //获取父容器
    }

    /**
     * 显示自定义错误提示页面，用一个View覆盖在WebView
     */
    private void showErrorPage() {
        Log.i("TAG","aaa");
        llEmpty.setVisibility(View.VISIBLE);
        mwebView.setVisibility(View.GONE);
    }


}
