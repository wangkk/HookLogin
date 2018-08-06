package com.wkk.hooklogin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ProxySubject proxySubject = new ProxySubject();
//        proxySubject.target();

//        Subject realSubject = new RealSubject();
//        ProxyInvocationHandler proxyInvocationHandler = new ProxyInvocationHandler(realSubject);
//        //通过Proxy类创建代理对象
//        //第一个参数：代理对象
//        //第二个参数：为代理对象提供的接口（目标对象实现的接口）
//        //第三个参数：代理对象关联的handler对象
//        Subject subject = (Subject) Proxy.newProxyInstance(proxyInvocationHandler.getClass().getClassLoader(),
//                realSubject.getClass().getInterfaces(), proxyInvocationHandler);
//        subject.target();
    }

    public void onClick(View view) {
        Log.i(TAG, "点击喽");
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    public void onClickCancel(View view) {
        SharedPreferences share = getSharedPreferences("hook",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putBoolean("login", false);
        editor.commit();
    }
    /****************************静态代理************************/
    //静态代理的缺点也比较明显  当接口改变或者增加实现的时候  代理类和真实类都需要进行修改

    /**
     * 接口 制定代理对象和真实对象需要实现的方法
     */
    public interface Subject {
        void target();
    }

    /**
     * 真实类  目标对象
     */
    public class RealSubject implements Subject {

        @Override
        public void target() {
            Log.i(TAG + "-real", "真实对象干事情了");
        }
    }

    /**
     * 代理类
     */
    public class ProxySubject implements Subject {

        Subject realSubject = new RealSubject();

        @Override
        public void target() {
            //这里面可以进行一些操作
            Log.i(TAG + "-before", "干事情之前准备");
            realSubject.target();
            Log.i(TAG + "-end", "干事情之后休息");
        }
    }

    /***************************动态代理*********************/
    //与静态代理一样需要一个制定规则的接口

    /**
     * 动态代理有一个重要的接口InvocationHandler和一个类Proxy
     * InvocationHandler:这个接口只有一个invoke （）方法，在代理对象调用方法时，会通过代理对象关联的handler转发为调用invoke()方法进行处理
     * Proxy：创建代理对象实例的类
     */
    public class ProxyInvocationHandler implements InvocationHandler {

        private Object realSubject;

        /**
         * 目标对象与handler绑定
         */
        public ProxyInvocationHandler(Object realSubject) {
            this.realSubject = realSubject;
        }

        /**
         * @param proxy  代理的目标对象
         * @param method 代理对象调用的方法（目标对象需要执行的方法）
         * @param args   调用方法中的参数数组
         * @return
         * @throws Throwable
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.i(TAG + "-before", "干事情之前动态准备");
            method.invoke(realSubject, args);
            Log.i(TAG + "-after", "干事情之后动态休息");
            return null;
        }
    }
}
