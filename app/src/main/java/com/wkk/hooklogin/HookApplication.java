package com.wkk.hooklogin;

import android.app.Application;

/**
 * Created by yueban on 2018/8/6.
 */

public class HookApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HookUtil hookUtil = new HookUtil(this);
        hookUtil.hookActivityStart();
        hookUtil.hookHandlerCallBack();
    }
}
