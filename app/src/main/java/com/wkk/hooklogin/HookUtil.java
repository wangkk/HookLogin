package com.wkk.hooklogin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by yueban on 2018/8/3.
 * 打劫startActivity() 跳转登录界面
 */

public class HookUtil {

    private static final String TAG = "HookUtil";
    private Context context;

    public HookUtil(Context context) {
        this.context = context;
    }

    /**
     * 获取IActivityManager对象  将其替换掉
     * 这里需要动态代理
     */
    public void hookActivityStart() {
        Log.i(TAG, "开始");
        try {
            //获取ActivityManager类
            Class<?> activityManagerClass = Class.forName("android.app.ActivityManagerNative");
            //获取其IActivityManagerSingleton属性
            Field iActivityManagerSingleton = activityManagerClass.getDeclaredField("gDefault");
            iActivityManagerSingleton.setAccessible(true);
            //获取IActivityManager静态对象
            Object defaultIActivityManager = iActivityManagerSingleton.get(null);
            Log.i(TAG + "1", "获取defaultIActivityManager");
            //获取Singleton类
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            //获取Singleton的mInsatnce属性
            Field mInstance = singletonClass.getDeclaredField("mInstance");
            mInstance.setAccessible(true);
            //获取当前IActivityManager实例对象
            Object iActivityManager = mInstance.get(defaultIActivityManager);
            Log.i(TAG + "2", "获取IActivityManager实例对象");

            //进行动态代理替换真实的IActivityManager对象
            //获取IActivityManager接口
            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");

            HookHandler hookHandler = new HookHandler(iActivityManager);
            Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{iActivityManagerClass}, hookHandler);
            //proxyInstance代理IActivityManager
            mInstance.set(defaultIActivityManager, proxyInstance);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过反射获取ActivtyThread中的mCallback,设置为我们实现的callback
     */
    public void hookHandlerCallBack() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            //当前主线程对象
            Object activitTthread = currentActivityThreadMethod.invoke(null);
            //获取mH字段
            Field mH = activityThreadClass.getDeclaredField("mH");
            mH.setAccessible(true);
            Handler handler = (Handler) mH.get(activitTthread);
            //获取mCallback字段
            Field mCallback = Handler.class.getDeclaredField("mCallback");
            mCallback.setAccessible(true);
            mCallback.set(handler, new HookThreadHandlerCallBack(handler));
            Log.i(TAG, "CallBack赋值完成");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    class HookHandler implements InvocationHandler {

        private Object object;

        public HookHandler(Object object) {
            this.object = object;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //拦截startActivity()方法
            String name = method.getName();
            Log.i(TAG, "调用" + name + "方法了");
            Intent intent = null;
            int index = -1;
            if ("startActivity".contains(name)) {
                Log.i(TAG, "调用startActivity方法了");
                //获取intent参数
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Intent) {
                        intent = (Intent) arg;
                        index = i;
                    }
                }
                //代理Intent替换掉intent
                Intent proxyIntent = new Intent();
                ComponentName componentName = new ComponentName(context, ProxyActivity.class);
                proxyIntent.setComponent(componentName);
                proxyIntent.putExtra("targetIntent", intent);
                args[index] = proxyIntent;
            }

            return method.invoke(object, args);
        }
    }

    private class HookThreadHandlerCallBack implements Handler.Callback {

        private Handler handler;

        public HookThreadHandlerCallBack(Handler handler) {
            this.handler = handler;
        }

        @Override
        public boolean handleMessage(Message msg) {
            //lauchActivity的what是100
            if (msg.what == 100) {
                Object obj = msg.obj;
                try {
                    Field intent = obj.getClass().getDeclaredField("intent");
                    intent.setAccessible(true);
                    Intent proxyIntent = (Intent) intent.get(obj);
                    //获取真正的intent
                    Intent realIntent = proxyIntent.getParcelableExtra("targetIntent");
                    if (realIntent != null) {
                        SharedPreferences share = context.getSharedPreferences("hook", Context.MODE_PRIVATE);
                        if (share.getBoolean("login", false)) {//是否登录
                            proxyIntent.setComponent(realIntent.getComponent());
                        } else {//跳转登录界面
                            ComponentName componentName = new ComponentName(context, LoginActivity.class);
                            proxyIntent.putExtra("extraIntent", realIntent.getComponent()
                                    .getClassName());
                            proxyIntent.setComponent(componentName);
                        }
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                handler.handleMessage(msg);
            }
            return false;
        }
    }
}