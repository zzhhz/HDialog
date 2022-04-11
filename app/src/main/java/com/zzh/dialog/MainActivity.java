package com.zzh.dialog;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.zzh.lib.dialog.DialogConfirmView;
import com.zzh.lib.dialog.DialogMenuView;
import com.zzh.lib.dialog.Dialoger;
import com.zzh.lib.dialog.animator.AlphaCreator;
import com.zzh.lib.dialog.impl.HDialogConfirmView;
import com.zzh.lib.dialog.impl.HDialogMenuView;
import com.zzh.lib.dialog.load.LoadingDialog;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    static Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    ShowDialog dialog;

    public void onClickView(View v) {
        if (dialog == null) {
            dialog = new ShowDialog(this);
            dialog.setAnimatorCreator(new AlphaCreator());
        }
        if (dialog.isShowing()) {
            dialog.dismiss();
        } else {
            dialog.show();
        }
    }

    public void onClickView1(View v) {
        HDialogConfirmView view = new HDialogConfirmView(this);
        Dialoger dialoger = view.getDialoger();
        dialoger.show();
        view.setCallback(new DialogConfirmView.Callback() {
            @Override
            public void onClickCancel(View v, DialogConfirmView view) {
                super.onClickCancel(v, view);
                Log.e("-----", "cancel");
            }

            @Override
            public void onClickConfirm(View v, DialogConfirmView view) {
                super.onClickConfirm(v, view);
                Log.e("-----", "onClickConfirm");
            }
        });

    }

    public void onClickView2(View v) {
        LoadingDialog dialog = new LoadingDialog(this);
        dialog.setShowTime(3000);
        dialog.show();
        handler.postDelayed(() -> {
//            dialog.loadSuccess();
            dialog.close();
        }, 3000);
    }

    public void onClickView3(View v) {
        HDialogMenuView view = new HDialogMenuView(this);
        view.setItems("一", "二", "伞");
        Dialoger dialoger = view.getDialoger();
        dialoger.show();
        view.setCallback(new DialogMenuView.Callback() {
            @Override
            public void onClickItem(View v, int index, DialogMenuView view) {
                super.onClickItem(v, index, view);
            }

            @Override
            public void onClickCancel(View v, DialogMenuView view) {
                super.onClickCancel(v, view);
            }
        });
    }


}
