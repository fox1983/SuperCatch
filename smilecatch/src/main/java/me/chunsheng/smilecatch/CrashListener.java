package me.chunsheng.smilecatch;

/**
 * Copyright © 2017 edaixi. All Rights Reserved.
 * Author: wei_spring
 * Date: 2017/12/19
 * Email:weichsh@edaixi.com
 * Function: 全局Crash监听，crash后的对应的业务逻辑处理
 */
public interface CrashListener {

    void onCrashListener(Thread thread, Throwable throwable);

}
