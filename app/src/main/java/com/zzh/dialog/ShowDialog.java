package com.zzh.dialog;

import android.app.Activity;

import com.zzh.lib.dialog.impl.HDialoger;

/**
 * Created by ZZH on 2020/10/29.
 *
 * @Date: 2020/10/29
 * @Email: zzh_hz@126.com
 * @QQ: 1299234582
 * @Author: zzh
 * @Description:
 */
public class ShowDialog extends HDialoger {
    public ShowDialog(Activity activity) {
        super(activity, R.style.dialog_pop);
        setContentView(R.layout.dialog_test);
    }
}
