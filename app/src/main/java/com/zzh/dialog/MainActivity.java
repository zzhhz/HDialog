package com.zzh.dialog;

import android.os.Bundle;
import android.view.View;

import com.zzh.lib.dialog.animator.AlphaCreator;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

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
}
