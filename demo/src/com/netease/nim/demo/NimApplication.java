package com.netease.nim.demo;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.melink.bqmmsdk.sdk.BQMM;
import com.netease.nim.avchatkit.AVChatKit;
import com.netease.nim.avchatkit.config.AVChatOptions;
import com.netease.nim.avchatkit.model.ITeamDataProvider;
import com.netease.nim.avchatkit.model.IUserInfoProvider;
import com.netease.nim.demo.chatroom.ChatRoomSessionHelper;
import com.netease.nim.demo.common.util.LogHelper;
import com.netease.nim.demo.common.util.crash.AppCrashHandler;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.contact.ContactHelper;
import com.netease.nim.demo.event.DemoOnlineStateContentProvider;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.demo.main.activity.WelcomeActivity;
import com.netease.nim.demo.mixpush.DemoMixPushMessageHandler;
import com.netease.nim.demo.mixpush.DemoPushContentProvider;
import com.netease.nim.demo.redpacket.NIMRedPacketClient;
import com.netease.nim.demo.session.NimDemoLocationProvider;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.UIKitOptions;
import com.netease.nim.uikit.business.contact.core.query.PinYin;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.mixpush.NIMPushClient;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.nimlib.sdk.util.NIMUtil;
import com.tencent.bugly.crashreport.CrashReport;

import io.fabric.sdk.android.Fabric;

public class NimApplication extends Application {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DemoCache.setContext(this);

        // 4.6.0 开始，第三方推送配置入口改为 SDKOption#mixPushConfig，旧版配置方式依旧支持。
        NIMClient.init(this, getLoginInfo(), NimSDKOptionConfig.getSDKOptions(this));

        // crash handler
        AppCrashHandler.getInstance(this);

        // 以下逻辑只在主进程初始化时执行
        if (NIMUtil.isMainProcess(this)) {

            // 注册自定义推送消息处理，这个是可选项
            NIMPushClient.registerMixPushMessageHandler(new DemoMixPushMessageHandler());

            // 初始化红包模块，在初始化UIKit模块之前执行
            NIMRedPacketClient.init(this);
            // init pinyin
            PinYin.init(this);
            PinYin.validate();
            // 初始化UIKit模块
            initUIKit();
            // 初始化消息提醒
            NIMClient.toggleNotification(UserPreferences.getNotificationToggle());
            // 云信sdk相关业务初始化
            NIMInitManager.getInstance().init(true);
            // 初始化音视频模块
            initAVChatKit();
        }

        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit);
        CrashReport.initCrashReport(getApplicationContext());

        /*
         * BQMM集成
         * 首先从AndroidManifest.xml中取得appId和appSecret，然后对BQMM SDK进行初始化
         */
        try {
            Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
            BQMM.getInstance().initConfig(this, bundle.getString("bqmm_app_id"), bundle.getString("bqmm_app_secret"));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private LoginInfo getLoginInfo() {
        String account = Preferences.getUserAccount();
        String token = Preferences.getUserToken();

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            DemoCache.setAccount(account.toLowerCase());
            return new LoginInfo(account, token);
        } else {
            return null;
        }
    }

    private void initUIKit() {
        // 初始化
        NimUIKit.init(this, buildUIKitOptions());

        // 设置地理位置提供者。如果需要发送地理位置消息，该参数必须提供。如果不需要，可以忽略。
        NimUIKit.setLocationProvider(new NimDemoLocationProvider());

        // IM 会话窗口的定制初始化。
        SessionHelper.init();

        // 聊天室聊天窗口的定制初始化。
        ChatRoomSessionHelper.init();

        // 通讯录列表定制初始化
        ContactHelper.init();

        // 添加自定义推送文案以及选项，请开发者在各端（Android、IOS、PC、Web）消息发送时保持一致，以免出现通知不一致的情况
        NimUIKit.setCustomPushContentProvider(new DemoPushContentProvider());

        NimUIKit.setOnlineStateContentProvider(new DemoOnlineStateContentProvider());
    }

    private UIKitOptions buildUIKitOptions() {
        UIKitOptions options = new UIKitOptions();
        // 设置app图片/音频/日志等缓存目录
        options.appCacheDir = NimSDKOptionConfig.getAppCacheDir(this) + "/app";
        return options;
    }

    private void initAVChatKit() {
        AVChatOptions avChatOptions = new AVChatOptions(){
            @Override
            public void logout(Context context) {
                MainActivity.logout(context, true);
            }
        };
        avChatOptions.entranceActivity = WelcomeActivity.class;
        avChatOptions.notificationIconRes = R.drawable.ic_stat_notify_msg;
        AVChatKit.init(avChatOptions);

        // 初始化日志系统
        LogHelper.init();
        // 设置用户相关资料提供者
        AVChatKit.setUserInfoProvider(new IUserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String account) {
                return NimUIKit.getUserInfoProvider().getUserInfo(account);
            }

            @Override
            public String getUserDisplayName(String account) {
                return UserInfoHelper.getUserDisplayName(account);
            }
        });
        // 设置群组数据提供者
        AVChatKit.setTeamDataProvider(new ITeamDataProvider() {
            @Override
            public String getDisplayNameWithoutMe(String teamId, String account) {
                return TeamHelper.getDisplayNameWithoutMe(teamId, account);
            }

            @Override
            public String getTeamMemberDisplayName(String teamId, String account) {
                return TeamHelper.getTeamMemberDisplayName(teamId, account);
            }
        });
    }
}
