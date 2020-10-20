package com.zzh.lib.dialog.load;

import android.view.View;

/**
 * Created by ZZH on 2020/10/20.
 *
 * @Date: 2020/10/20
 * @Email: zzh_hz@126.com
 * @QQ: 1299234582
 * @Author: zzh
 * @Description:
 */
public interface FinishDrawListener {
    /**
     * 分发绘制完成事件
     *
     * @param v 绘制完成的View
     */
    void dispatchFinishEvent(View v);
}
