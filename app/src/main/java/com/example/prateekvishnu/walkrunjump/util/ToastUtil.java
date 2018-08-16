package com.example.prateekvishnu.walkrunjump.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by css on 2018/7/24.
 */

public class ToastUtil {
    private static Toast toast;

    public static void toast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();

    }
}
