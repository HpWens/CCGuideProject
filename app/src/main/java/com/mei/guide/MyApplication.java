package com.mei.guide;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mei.guide.load.DataConstants;
import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.cache.converter.SerializableDiskConverter;
import com.zhouyou.http.model.HttpHeaders;
import com.zhouyou.http.model.HttpParams;

import retrofit2.converter.gson.GsonConverterFactory;

public class MyApplication extends Application {

    String TEST_BASE_URL = "http://47.96.4.192:8080/app/";
    String RELEASE_BASE_URL = "http://prd-api.ccspeed.cn/app/";

    @Override
    public void onCreate() {
        super.onCreate();

        initEasyHttp();
    }

    private void initEasyHttp() {
        // 初始化网络框架
        EasyHttp.init(this);
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", "application/json");

        // 设置请求参数
        HttpParams params = new HttpParams();
        EasyHttp.getInstance()
                // isDebug() ? true : false  todo
                .debug("RxEasyHttp", isDebug() ? true : false)
                .setReadTimeOut(10 * 60 * 1000)
                .setWriteTimeOut(10 * 60 * 1000)
                .setConnectTimeout(10 * 60 * 1000)
                .setRetryCount(3) // 默认网络不好自动重试3次
                .setRetryDelay(500) // 每次延时500ms重试
                .setRetryIncreaseDelay(500) // 每次延时叠加500ms
                .setBaseUrl(isDebug() ? TEST_BASE_URL : RELEASE_BASE_URL)
                .setCacheDiskConverter(new SerializableDiskConverter()) // 默认缓存使用序列化转化
                .setCacheMaxSize(50 * 1024 * 1024) // 设置缓存大小为50M
                .setCacheVersion(1) // 缓存版本为1
                .setCertificates() // 信任所有证书
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .addCommonHeaders(headers) // 设置全局公共头
                .addCommonParams(params); // 设置全局公共参数
    }

    public boolean isDebug() {
        boolean debuggable = false;
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(getPackageName(), 0);
            debuggable = (0 != (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            /*debuggable variable will remain false*/
        }
        return debuggable;
    }
}
