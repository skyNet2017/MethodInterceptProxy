package com.mdit.example;

import android.content.Context;
import android.util.Log;

import com.mdit.example.test.Test;
import com.mdit.library.proxy.CallbackFilter;
import com.mdit.library.proxy.Enhancer;
import com.mdit.library.proxy.MethodInterceptor;
import com.mdit.library.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class LogByCgLib {

    protected static Context context;

    public static void init(Context context){
        LogByCgLib.context = context;
    }

    public static <T> T getProxy(final T realObj){
        Enhancer enhancer = new Enhancer(context);
        enhancer.setSuperclass(realObj.getClass());
        enhancer.setCallbackFilter(new CallbackFilter() {
            @Override
            public int accept(Method method) {
                return Modifier.isStatic(method.getModifiers()) ? 0 : 1;
            }
        });
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object object, Object[] args, MethodProxy method) throws Exception {
                StringBuilder sb = new StringBuilder();
                String parms = Arrays.toString(args);
                parms = parms.substring(1,parms.length()-1);

                String objName = object+"";
                if(objName.contains(".")){
                    objName = objName.substring(objName.lastIndexOf(".")+1);
                }
                sb.append(objName)
                        .append(".")
                        .append(method.getMethodName())
                        .append("(")
                        .append(parms)
                        .append(")");

                Object obj = null;
                long start = System.currentTimeMillis();
                try {
                    obj = method.invokeSuper(object, args);
                    sb.append(", result:")
                            .append(obj);
                    doLog(sb,start);
                }catch (Throwable throwable){
                    sb.append(EXCEPTION_DESC)
                            .append(throwable.getClass().getName())
                            .append(":")
                            .append(throwable.getMessage());
                    doLog(sb,start);
                    throw throwable;
                }
                return obj;
            }
        });
        return (T) enhancer.create();

    }

    private static void doLog(StringBuilder sb, long start) {
        long cost = System.currentTimeMillis()-start;
        sb.append(", cost:")
                .append(cost).append("ms").append(" ,thread:").append(Thread.currentThread().getName());
        String str = sb.toString();

        if("main".equals(Thread.currentThread().getName()) && cost> 50){
            Log.i("aspectImpl",str);
        }else if(str.contains(EXCEPTION_DESC)){
            Log.w("aspectImpl",str);
        } else {
            Log.d("aspectImpl",str);
        }
    }

    static String EXCEPTION_DESC = ", throw ";
}
