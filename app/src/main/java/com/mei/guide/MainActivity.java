package com.mei.guide;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mei.guide.bean.GuideBean;
import com.mei.guide.util.ParseJsonUtils;
import com.mei.guide.util.Result;
import com.mei.guide.web.FullscreenHolder;
import com.mei.guide.web.IWebPageView;
import com.mei.guide.web.MyJavascriptInterface;
import com.mei.guide.web.MyWebChromeClient;
import com.mei.guide.web.MyWebViewClient;
import com.mei.guide.web.WebProgress;
import com.mei.guide.web.WebTools;
import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.callback.SimpleCallBack;
import com.zhouyou.http.exception.ApiException;

public class MainActivity extends AppCompatActivity implements IWebPageView {

    private WebView webView;

    // 进度条
    private WebProgress mProgressBar;

    private MyWebChromeClient mWebChromeClient;

    // 全屏时视频加载view
    private FrameLayout videoFullView;

    private TextView tvHtml;

    private ImageView ivShare;

    private ImageView ivReserve;

    private boolean isCommit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivShare = findViewById(R.id.iv_share);
        ivReserve = findViewById(R.id.iv_reserve);
        webView = findViewById(R.id.web_view);
        tvHtml = findViewById(R.id.tv_html);
        mProgressBar = findViewById(R.id.pb_progress);
        mProgressBar.setColor(ContextCompat.getColor(this, R.color.colorAccent));
        mProgressBar.show();

        initWebView();

        //webView.loadUrl("https://www.jappstore.com/");

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        requestData();

        ivReserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCommit) {
                    new ReserveDialog(MainActivity.this, new ReserveDialog.OnItemListener() {
                        @Override
                        public void onCommit(Dialog dialog) {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "预约成功", Toast.LENGTH_SHORT).show();
                            isCommit = true;
                            ivReserve.setImageResource(R.mipmap.ic_reserved);
                        }
                    }).show();
                }
            }
        });
    }

    private void requestData() {
        EasyHttp.get("gameBook/getLolContentConf")
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                    }

                    @Override
                    public void onSuccess(String s) {
                        final Result<GuideBean> result = ParseJsonUtils.parseDataToResult(s, GuideBean.class);
                        if (result.isOk()) {
                            // tvHtml.setText(Html.fromHtml(result.data.mainContent));
                            tvHtml.setVisibility(View.GONE);
                            webView.loadUrl(result.data.h5Url);

                            ivShare.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!TextUtils.isEmpty(result.data.shareContent)) {
                                        WebTools.copy(v.getContext(), result.data.shareContent);
                                        Toast.makeText(v.getContext(), "已复制到黏贴板，快去发给你的小伙伴吧~", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                });
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initWebView() {
        WebSettings ws = webView.getSettings();
        // 网页内容的宽度是否可大于WebView控件的宽度
        ws.setLoadWithOverviewMode(false);
        // 保存表单数据
        ws.setSaveFormData(true);
        // 是否应该支持使用其屏幕缩放控件和手势缩放
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        // 启动应用缓存
        ws.setAppCacheEnabled(true);
        // 设置缓存模式
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        // setDefaultZoom  api19被弃用
        // 设置此属性，可任意比例缩放。
        ws.setUseWideViewPort(true);
        // 不缩放
        webView.setInitialScale(100);
        // 告诉WebView启用JavaScript执行。默认的是false。
        ws.setJavaScriptEnabled(true);
        //  页面加载好以后，再放开图片
        ws.setBlockNetworkImage(false);
        // 使用localStorage则必须打开
        ws.setDomStorageEnabled(true);
        // 排版适应屏幕
        ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        // WebView是否新窗口打开(加了后可能打不开网页)
//        ws.setSupportMultipleWindows(true);

        // webview从5.0开始默认不允许混合模式,https中不能加载http资源,需要设置开启。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        /** 设置字体默认缩放大小(改变网页字体大小,setTextSize  api14被弃用)*/
        ws.setTextZoom(100);

        mWebChromeClient = new MyWebChromeClient(this);
        webView.setWebChromeClient(mWebChromeClient);
        // 与js交互
        webView.addJavascriptInterface(new MyJavascriptInterface(this), "injectedObject");
        webView.setWebViewClient(new MyWebViewClient(this));
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleLongImage();
                return false;
            }
        });

    }

    /**
     * 长按图片事件处理
     */
    private boolean handleLongImage() {
        final WebView.HitTestResult hitTestResult = webView.getHitTestResult();
        // 如果是图片类型或者是带有图片链接的类型
        if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            // 弹出保存图片的对话框
            new AlertDialog.Builder(this)
                    .setItems(new String[]{"查看大图", "保存图片到相册"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String picUrl = hitTestResult.getExtra();
                            //获取图片
                            switch (which) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                default:
                                    break;
                            }
                        }
                    })
                    .show();
            return true;
        }
        return false;
    }

    @Override
    public void showWebView() {
        webView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hindWebView() {
        webView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void startProgress(int newProgress) {
        mProgressBar.setWebProgress(newProgress);
    }

    @Override
    public void fullViewAddView(View view) {
        FrameLayout decor = (FrameLayout) this.getWindow().getDecorView();
        videoFullView = new FullscreenHolder(this);
        videoFullView.addView(view);
        decor.addView(videoFullView);
    }

    @Override
    public void showVideoFullView() {
        videoFullView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hindVideoFullView() {
        videoFullView.setVisibility(View.GONE);
    }

    @Override
    public void setRequestedOrientation(int screenOrientationPortrait) {

    }

    @Override
    public FrameLayout getVideoFullView() {
        return null;
    }

    @Override
    public View getVideoLoadingProgressView() {
        return videoFullView;
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {

    }

    @Override
    public void startFileChooserForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        mProgressBar.hide();
        loadImageClickJS();
        loadTextClickJS();
        loadCallJS();
        loadWebsiteSourceCodeJS();
    }

    @Override
    public boolean isOpenThirdApp(String url) {
        return WebTools.handleThirdApp(this, url);
    }

    /**
     * 前端注入JS：
     * 这段js函数的功能就是，遍历所有的img节点，并添加onclick函数，函数的功能是在图片点击的时候调用本地java接口并传递url过去
     */
    private void loadImageClickJS() {
//        loadJs("javascript:(function(){" +
//                "var objs = document.getElementsByTagName(\"img\");" +
//                "for(var i=0;i<objs.length;i++)" +
//                "{" +
//                "objs[i].onclick=function(){window.injectedObject.imageClick(this.getAttribute(\"src\"));}" +
//                "}" +
//                "})()");
    }

    /**
     * 前端注入JS：
     * 遍历所有的<li>节点,将节点里的属性传递过去(属性自定义,用于页面跳转)
     */
    private void loadTextClickJS() {
//        loadJs("javascript:(function(){" +
//                "var objs =document.getElementsByTagName(\"li\");" +
//                "for(var i=0;i<objs.length;i++)" +
//                "{" +
//                "objs[i].onclick=function(){" +
//                "window.injectedObject.textClick(this.getAttribute(\"type\"),this.getAttribute(\"item_pk\"));}" +
//                "}" +
//                "})()");
    }

    /**
     * 传应用内的数据给html，方便html处理
     */
    private void loadCallJS() {
        // 无参数调用
        // loadJs("javascript:javacalljs()");
        // 传递参数调用
        //loadJs("javascript:javacalljswithargs('" + "android传入到网页里的数据，有参" + "')");
    }

    /**
     * get website source code
     * 获取网页源码
     */
    private void loadWebsiteSourceCodeJS() {
        // loadJs("javascript:window.injectedObject.showSource(document.getElementsByTagName('html')[0].innerHTML);");
    }


    /**
     * 4.4以上可用 evaluateJavascript 效率高
     */
    private void loadJs(String jsString) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(jsString, null);
        } else {
            webView.loadUrl(jsString);
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

}