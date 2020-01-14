package com.zzh.dialog;

import android.app.Activity;

import com.zzh.lib.dialog.impl.HDialoger;

/**
 * Created by ZZH on 2020-01-13.
 *
 * @Date: 2020-01-13
 * @Email: zzh_hz@126.com
 * @QQ: 1299234582
 * @Author: zzh
 * @Description:
 */
public class ADialog extends HDialoger {

    public ADialog(Activity activity) {
        super(activity);
        setContentView(R.layout.dialog);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void show() {
        super.show();
        /*Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.height =  300;

        window.setAttributes(attributes);*/

    }
}
