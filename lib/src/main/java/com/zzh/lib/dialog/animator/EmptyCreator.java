package com.zzh.lib.dialog.animator;

import android.animation.Animator;
import android.view.View;

import com.zzh.lib.dialog.Dialoger;


public final class EmptyCreator implements Dialoger.AnimatorCreator
{
    @Override
    public Animator createAnimator(boolean show, View view)
    {
        return null;
    }
}
