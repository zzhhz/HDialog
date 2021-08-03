package com.zzh.dialog;

import android.app.Application;

import com.zzh.lib.dialog.load.LoadingDialog;
import com.zzh.lib.dialog.manager.StyleManager;

/**
 * Created by ZZH on 2021/8/3.
 *
 * @Date: 2021/8/3
 * @Email: zzh_hz@126.com
 * @QQ: 1299234582
 * @Author: zzh
 * @Description:
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LoadingDialog.initStyle(StyleManager.getDefault().setLoadStyle(LoadingDialog.STYLE_LINE).failedText("加载失败...").loadText("正在加载").repeatTime(0).intercept(false).speed(LoadingDialog.Speed.SPEED_FOUR));
    }
}
