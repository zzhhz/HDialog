package com.zzh.dialog;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.zzh.lib.dialog.load.LoadingDialog;
import com.zzh.lib.dialog.manager.StyleManager;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoadingDialog.initStyle(StyleManager.getDefault().speed(LoadingDialog.Speed.SPEED_FOUR));
    }


    public void onClickView(View v) {
        final LoadingDialog dialog = new LoadingDialog(this);
        dialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int anInt = new Random().nextInt(2);
                if (anInt == 0) {
                    dialog.loadFailed();
                } else {
                    dialog.loadSuccess();
                }

            }
        }, 3000);
    }
}
