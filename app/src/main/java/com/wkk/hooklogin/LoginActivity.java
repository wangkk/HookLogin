package com.wkk.hooklogin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by yueban on 2018/8/6.
 */

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onClick(View view) {
        SharedPreferences share = getSharedPreferences("hook",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putBoolean("login", true);
        editor.commit();
        //登录成功跳转目标界面
        String className = getIntent().getStringExtra("extraIntent");
        if (className != null) {
            ComponentName componentName = new ComponentName(LoginActivity.this, className);
            Intent intent = new Intent();
            intent.setComponent(componentName);
            startActivity(intent);
            finish();
        }
    }

}
