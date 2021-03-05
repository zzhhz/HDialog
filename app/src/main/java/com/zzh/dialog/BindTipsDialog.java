package com.zzh.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.zzh.lib.dialog.impl.HDialoger;


/**
 * Created by ZZH on 2020-02-09.
 *
 * @Date: 2020-02-09
 * @Email: zzh_hz@126.com
 * @QQ: 1299234582
 * @Author: zzh
 * @Description: 没有绑定异构账号提示框
 */
public class BindTipsDialog extends HDialoger {
    protected TextView tv_tips;


    public BindTipsDialog(Activity activity) {
        super(activity, R.style.dialog_pop_full);
        setContentView(R.layout.dialog_mvp_bind_tips);
        setCanceledOnTouchOutside(true);
    }

    public void onClickView(View v) {
    }
}
