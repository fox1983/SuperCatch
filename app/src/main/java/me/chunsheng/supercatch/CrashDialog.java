package me.chunsheng.supercatch;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

/**
 * Copyright © 2017 edaixi. All Rights Reserved.
 * Author: wei_spring
 * Date: 2017/12/20
 * Email:weichsh@edaixi.com
 * Function: 自定义Crash Dialog
 */
public class CrashDialog extends AlertDialog {
    protected CrashDialog(Context context) {
        super(context);
    }

    protected CrashDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    protected CrashDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void setView(View view) {
        super.setView(view);
    }
}
