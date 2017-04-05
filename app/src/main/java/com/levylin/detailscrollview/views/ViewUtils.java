package com.levylin.detailscrollview.views;

import android.view.View;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by LinXin on 2017/4/1.
 */
public class ViewUtils {

    public static int computeVerticalScrollRange(View view) throws Exception {
        if (view instanceof com.tencent.smtt.sdk.WebView) {
            view = ((com.tencent.smtt.sdk.WebView) view).getView();
        }
        Class clazz = view.getClass();
        Method method = getClassMethod(clazz, "computeVerticalScrollRange", new Class[0]);
        method.setAccessible(true);
        Object range = method.invoke(view);
        return (Integer) range;
    }

    public static int computeVerticalScrollExtent(View view) throws Exception {
        if (view instanceof com.tencent.smtt.sdk.WebView) {
            view = ((com.tencent.smtt.sdk.WebView) view).getView();
        }
        Class clazz = view.getClass();
        Method method = getClassMethod(clazz, "computeVerticalScrollExtent", new Class[0]);
        Object range = method.invoke(view);
        return (Integer) range;
    }

    public static int computeVerticalScrollOffset(View view) throws Exception {
        if (view instanceof com.tencent.smtt.sdk.WebView) {
            view = ((com.tencent.smtt.sdk.WebView) view).getView();
        }
        Class clazz = view.getClass();
        Method method = getClassMethod(clazz, "computeVerticalScrollOffset", new Class[0]);
        Object range = method.invoke(view);
        return (Integer) range;
    }

    private static Method getClassMethod(Class cur_class, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = cur_class.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ignored) {
        }
        if (method == null && cur_class.getSuperclass() != null) {
            return getClassMethod(cur_class.getSuperclass(), methodName, parameterTypes);
        }
        if (method != null) {
            method.setAccessible(true);
        }
        return method;
    }
}
