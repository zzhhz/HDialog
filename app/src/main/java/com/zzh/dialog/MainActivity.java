package com.zzh.dialog;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.zzh.lib.dialog.animator.AlphaCreator;
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
        BindTipsDialog dialog = new BindTipsDialog(this);
        dialog.show();
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
}
