package com.zzh.dialog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;

import com.zzh.lib.dialog.Dialoger;
import com.zzh.lib.dialog.impl.HDialogConfirmView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickView(View v) {
        HDialogConfirmView view = new HDialogConfirmView(this);
        view.setTextConfirm("确定");
        view.setTextCancel("取消");
        view.setTextContent("ASAAAAAAAAAAAAAAAAAAAA");
        Dialoger dialoger = view.getDialoger();
        dialoger.setGravity(Gravity.BOTTOM);
        dialoger.show();

    }

}
