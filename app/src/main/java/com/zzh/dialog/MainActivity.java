package com.zzh.dialog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.zzh.lib.dialog.TargetDialoger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickView(View v) {

        ADialog dialog = new ADialog(this);
        dialog.target().show(v, TargetDialoger.Position.BottomOutsideRight);
        dialog.show();
    }
}
