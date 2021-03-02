package com.zzh.lib.dialog.impl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.zzh.lib.dialog.Dialoger;
import com.zzh.lib.dialog.R;
import com.zzh.lib.dialog.TargetDialoger;
import com.zzh.lib.dialog.animator.AlphaCreator;
import com.zzh.lib.dialog.animator.ObjectAnimatorCreator;
import com.zzh.lib.dialog.animator.SlideBottomTopCreator;
import com.zzh.lib.dialog.animator.SlideLeftRightCreator;
import com.zzh.lib.dialog.animator.SlideRightLeftCreator;
import com.zzh.lib.dialog.animator.SlideTopBottomCreator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HDialoger implements Dialoger {
    private final Activity mActivity;
    private final int mThemeResId;

    private final View mDialogerView;
    private final View mBackgroundView;
    private final LinearLayout mContainerView;
    private View mContentView;

    private boolean mCancelable = true;
    private boolean mCanceledOnTouchOutside = true;
    private int mGravity = Gravity.NO_GRAVITY;

    private State mState = State.Dismissed;

    private Boolean mShowStatusBar = null;

    private OnDismissListener mOnDismissListener;
    private OnShowListener mOnShowListener;
    private List<LifecycleCallback> mLifecycleCallbacks;

    private boolean mLockDialoger;

    private HVisibilityAnimatorHandler mAnimatorHandler;
    private AnimatorCreator mAnimatorCreator;
    private AnimatorCreator mBackgroundViewAnimatorCreator;

    private boolean mTryStartShowAnimator;
    private boolean mIsAnimatorCreatorModifiedInternal;

    private boolean mIsDebug;
    private boolean mIsBackgroundDim;

    public HDialoger(Activity activity) {
        this(activity, R.style.lib_dialoger_default);
    }

    public HDialoger(Activity activity, int themeResId) {
        if (activity == null)
            throw new NullPointerException("activity is null");

        mActivity = activity;
        mThemeResId = themeResId;

        final InternalDialogerView dialogerView = new InternalDialogerView(activity);
        mDialogerView = dialogerView;
        mContainerView = dialogerView.mContainerView;
        mBackgroundView = dialogerView.mBackgroundView;

        final int defaultPadding = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.1f);
        setPadding(defaultPadding, 0, defaultPadding, 0);

        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.parseColor("#66000000"));
    }

    @Override
    public void setDebug(boolean debug) {
        mIsDebug = debug;
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public Activity getOwnerActivity() {
        return mActivity;
    }

    @Override
    public Window getWindow() {
        return getDialog().getWindow();
    }

    @Override
    public View getContentView() {
        return mContentView;
    }

    @Override
    public void setContentView(int layoutId) {
        final View view = LayoutInflater.from(mActivity).inflate(layoutId, mContainerView, false);
        setContentView(view);
    }

    @Override
    public void setContentView(View view) {
        setDialogerView(view);
    }

    private void setDialogerView(View view) {
        final View old = mContentView;
        if (old != view) {
            mContentView = view;

            if (old != null)
                mContainerView.removeView(old);

            if (view != null) {
                final ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                final ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params != null) {
                    p.width = params.width;
                    p.height = params.height;
                }

                mContainerView.addView(view, p);
            }

            if (mIsDebug)
                Log.i(Dialoger.class.getSimpleName(), "onContentViewChanged:" + old + " , " + view);

            onContentViewChanged(old, view);
        }
    }

    protected void onContentViewChanged(View oldView, View contentView) {
    }

    @Override
    public void setBackgroundColor(int color) {
        if (color <= 0)
            mBackgroundView.setBackgroundDrawable(null);
        else
            mBackgroundView.setBackgroundColor(color);
    }

    public void setBackgroundDim(boolean backgroundDim) {
        if (mIsBackgroundDim != backgroundDim) {
            mIsBackgroundDim = backgroundDim;
            if (backgroundDim) {
                final int color = mActivity.getResources().getColor(R.color.zh_dialog_background_dim);
                mBackgroundView.setBackgroundColor(color);
            } else {
                mBackgroundView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    @Override
    public void setShowStatusBar(Boolean show) {
        mShowStatusBar = show;
    }

    @Override
    public <T extends View> T findViewById(int id) {
        if (mContentView == null)
            return null;

        return mContentView.findViewById(id);
    }

    @Override
    public void setCancelable(boolean cancel) {
        mCancelable = cancel;
    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        if (cancel && !mCancelable)
            mCancelable = true;

        mCanceledOnTouchOutside = cancel;
    }

    @Override
    public void setOnDismissListener(OnDismissListener listener) {
        mOnDismissListener = listener;
    }

    @Override
    public void setOnShowListener(OnShowListener listener) {
        mOnShowListener = listener;
    }

    @Override
    public void addLifecycleCallback(LifecycleCallback callback) {
        if (callback == null)
            return;

        if (mLifecycleCallbacks == null)
            mLifecycleCallbacks = new CopyOnWriteArrayList<>();

        if (mLifecycleCallbacks.contains(callback))
            return;

        mLifecycleCallbacks.add(callback);
    }

    @Override
    public void removeLifecycleCallback(LifecycleCallback callback) {
        if (callback == null || mLifecycleCallbacks == null)
            return;

        mLifecycleCallbacks.remove(callback);

        if (mLifecycleCallbacks.isEmpty())
            mLifecycleCallbacks = null;
    }

    @Override
    public void setAnimatorCreator(AnimatorCreator creator) {
        mAnimatorCreator = creator;
        mIsAnimatorCreatorModifiedInternal = false;
    }

    @Override
    public AnimatorCreator getAnimatorCreator() {
        return mAnimatorCreator;
    }

    @Override
    public void setGravity(int gravity) {
        mContainerView.setGravity(gravity);
    }

    @Override
    public int getGravity() {
        return mGravity;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        mContainerView.setPadding(left, top, right, bottom);
    }

    @Override
    public int getPaddingLeft() {
        return mContainerView.getPaddingLeft();
    }

    @Override
    public int getPaddingTop() {
        return mContainerView.getPaddingTop();
    }

    @Override
    public int getPaddingRight() {
        return mContainerView.getPaddingRight();
    }

    @Override
    public int getPaddingBottom() {
        return mContainerView.getPaddingBottom();
    }

    @Override
    public boolean isShowing() {
        return mState == State.Shown;
    }

    @Override
    public void show() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mShowRunnable.run();
        } else {
            getDialogerHandler().removeCallbacks(mShowRunnable);
            getDialogerHandler().post(mShowRunnable);
        }
    }

    @Override
    public void dismiss() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mDismissRunnable.run();
        } else {
            getDialogerHandler().removeCallbacks(mDismissRunnable);
            getDialogerHandler().post(mDismissRunnable);
        }
    }

    private final Runnable mShowRunnable = new Runnable() {
        @Override
        public void run() {
            if (mState.isShowPart())
                return;

            if (mActivity.isFinishing())
                return;

            if (mIsDebug)
                Log.i(Dialoger.class.getSimpleName(), "try show");

            setState(State.TryShow);

            if (getAnimatorHandler().isHideAnimatorStarted()) {
                if (mIsDebug)
                    Log.i(Dialoger.class.getSimpleName(), "cancel HideAnimator before show");

                getAnimatorHandler().cancelHideAnimator();
            }
            getDialog().show();
            setState(State.Shown);
        }
    };

    private final Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            if (mState.isDismissPart())
                return;

            if (mIsDebug)
                Log.i(Dialoger.class.getSimpleName(), "try dismiss");

            setState(State.TryDismiss);

            if (getAnimatorHandler().isShowAnimatorStarted()) {
                if (mIsDebug)
                    Log.i(Dialoger.class.getSimpleName(), "cancel ShowAnimator before dismiss");

                getAnimatorHandler().cancelShowAnimator();
            }

            setLockDialoger(true);

            if (mActivity.isFinishing()) {
                removeDialogerView(false);
                return;
            }

            getAnimatorHandler().setHideAnimator(createAnimator(false));
            if (getAnimatorHandler().startHideAnimator()) {
                // 等待动画结束后让窗口消失
            } else {
                removeDialogerView(false);
            }
        }
    };

    private void setLockDialoger(boolean lock) {
        if (mLockDialoger != lock) {
            mLockDialoger = lock;
            if (mIsDebug)
                Log.i(Dialoger.class.getSimpleName(), "setLockDialoger:" + lock);
        }
    }

    private void setTryStartShowAnimator(boolean tryShow) {
        if (mTryStartShowAnimator != tryShow) {
            mTryStartShowAnimator = tryShow;
            if (mIsDebug)
                Log.i(Dialoger.class.getSimpleName(), "setTryStartShowAnimator:" + tryShow);
        }
    }

    private void startShowAnimator() {
        if (mTryStartShowAnimator) {
            if (mIsDebug)
                Log.i(Dialoger.class.getSimpleName(), "startShowAnimator");

            setTryStartShowAnimator(false);
            getAnimatorHandler().setShowAnimator(createAnimator(true));
            getAnimatorHandler().startShowAnimator();
        }
    }

    private void setState(State state) {
        if (state == null)
            throw new IllegalArgumentException("state is null");

        if (mState != state) {
            if (mIsDebug)
                Log.e(Dialoger.class.getSimpleName(), "setState:" + state);

            mState = state;

            if (state.isDismissPart()) {
                setTryStartShowAnimator(false);
            }
        }
    }

    @Override
    public void startDismissRunnable(long delay) {
        stopDismissRunnable();
        getDialogerHandler().postDelayed(mDelayedDismissRunnable, delay);
    }

    @Override
    public void stopDismissRunnable() {
        getDialogerHandler().removeCallbacks(mDelayedDismissRunnable);
    }

    private Handler mDialogerHandler;

    private Handler getDialogerHandler() {
        if (mDialogerHandler == null)
            mDialogerHandler = new Handler(Looper.getMainLooper());
        return mDialogerHandler;
    }

    private final Runnable mDelayedDismissRunnable = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mCancelable) {
            if (mIsDebug)
                Log.i(Dialoger.class.getSimpleName(), "onBackPressed try dismiss ");

            dismiss();
        }
    }

    private void setDefaultConfigBeforeShow() {
        if (mAnimatorCreator == null) {
            switch (mGravity) {
                case Gravity.CENTER:
                    setAnimatorCreator(new AlphaCreator());
                    mIsAnimatorCreatorModifiedInternal = true;
                    break;
                case Gravity.LEFT:
                case Gravity.LEFT | Gravity.CENTER:
                    setAnimatorCreator(new SlideRightLeftCreator());
                    mIsAnimatorCreatorModifiedInternal = true;
                    break;
                case Gravity.TOP:
                case Gravity.TOP | Gravity.CENTER:
                    setAnimatorCreator(new SlideBottomTopCreator());
                    mIsAnimatorCreatorModifiedInternal = true;
                    break;
                case Gravity.RIGHT:
                case Gravity.RIGHT | Gravity.CENTER:
                    setAnimatorCreator(new SlideLeftRightCreator());
                    mIsAnimatorCreatorModifiedInternal = true;
                    break;
                case Gravity.BOTTOM:
                case Gravity.BOTTOM | Gravity.CENTER:
                    setAnimatorCreator(new SlideTopBottomCreator());
                    mIsAnimatorCreatorModifiedInternal = true;
                    break;
            }
        }
    }

    private HVisibilityAnimatorHandler getAnimatorHandler() {
        if (mAnimatorHandler == null) {
            mAnimatorHandler = new HVisibilityAnimatorHandler();
            mAnimatorHandler.setShowAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if (mIsDebug)
                        Log.i(Dialoger.class.getSimpleName(), "show onAnimationStart ");
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    if (mIsDebug)
                        Log.i(Dialoger.class.getSimpleName(), "show onAnimationCancel ");
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (mIsDebug)
                        Log.i(Dialoger.class.getSimpleName(), "show onAnimationEnd ");
                }
            });
            mAnimatorHandler.setHideAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if (mIsDebug)
                        Log.i(Dialoger.class.getSimpleName(), "dismiss onAnimationStart ");
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    if (mIsDebug)
                        Log.i(Dialoger.class.getSimpleName(), "dismiss onAnimationCancel ");
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (mIsDebug)
                        Log.i(Dialoger.class.getSimpleName(), "dismiss onAnimationEnd ");

                    removeDialogerView(true);
                }
            });
        }
        return mAnimatorHandler;
    }

    private AnimatorCreator getBackgroundViewAnimatorCreator() {
        if (mBackgroundViewAnimatorCreator == null) {
            mBackgroundViewAnimatorCreator = new ObjectAnimatorCreator() {
                @Override
                protected String getPropertyName() {
                    return View.ALPHA.getName();
                }

                @Override
                protected float getValueHidden(View view) {
                    return 0.0f;
                }

                @Override
                protected float getValueShown(View view) {
                    return 1.0f;
                }

                @Override
                protected float getValueCurrent(View view) {
                    return view.getAlpha();
                }

                @Override
                protected void onAnimationStart(boolean show, View view) {
                    super.onAnimationStart(show, view);
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onAnimationEnd(boolean show, View view) {
                    super.onAnimationEnd(show, view);
                    if (!show)
                        view.setVisibility(View.INVISIBLE);
                }
            };
        }
        return mBackgroundViewAnimatorCreator;
    }

    private Animator createAnimator(boolean show) {
        Animator animator = null;

        final Animator animatorBackground = (mBackgroundView.getBackground() == null) ?
                null : getBackgroundViewAnimatorCreator().createAnimator(show, mBackgroundView);

        final Animator animatorContent = (mAnimatorCreator == null || mContentView == null) ?
                null : mAnimatorCreator.createAnimator(show, mContentView);

        if (animatorBackground != null && animatorContent != null) {
            final long duration = getAnimatorDuration(animatorContent);
            if (duration < 0)
                throw new RuntimeException("Illegal duration:" + duration);
            animatorBackground.setDuration(duration);

            final AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(animatorBackground).with(animatorContent);
            animator = animatorSet;
        } else if (animatorBackground != null) {
            animator = animatorBackground;
        } else if (animatorContent != null) {
            animator = animatorContent;
        }

        if (mIsDebug)
            Log.i(Dialoger.class.getSimpleName(), "createAnimator " + (show ? "show" : "dismiss") + " animator " + (animator == null ? "null" : "not null"));

        return animator;
    }

    private void removeDialogerView(boolean removeByHideAnimator) {
        if (mIsDebug)
            Log.e(Dialoger.class.getSimpleName(), "removeDialogerView by hideAnimator:" + removeByHideAnimator);

        getDialog().dismiss();
        setState(State.Dismissed);
    }

    protected void onStart() {
    }

    protected void onStop() {
    }

    private SimpleTargetDialoger mTargetDialoger;

    @Override
    public TargetDialoger target() {
        if (mTargetDialoger == null)
            mTargetDialoger = new SimpleTargetDialoger(this);
        return mTargetDialoger;
    }

    private final class InternalDialogerView extends FrameLayout {
        private final View mBackgroundView;
        private final LinearLayout mContainerView;

        public InternalDialogerView(Context context) {
            super(context);

            mBackgroundView = new InternalBackgroundView(context);
            addView(mBackgroundView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            mContainerView = new InternalContainerView(context);
            addView(mContainerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        @Override
        public void onViewAdded(View child) {
            super.onViewAdded(child);
            if (child != mBackgroundView && child != mContainerView)
                throw new RuntimeException("you can not add view to dialoger view");
        }

        @Override
        public void onViewRemoved(View child) {
            super.onViewRemoved(child);
            if (child == mBackgroundView || child == mContainerView)
                throw new RuntimeException("you can not remove dialoger child");
        }

        @Override
        public void setVisibility(int visibility) {
            if (visibility == GONE || visibility == INVISIBLE)
                throw new IllegalArgumentException("you can not hide dialoger");
            super.setVisibility(visibility);
        }

        private boolean isViewUnder(View view, int x, int y) {
            if (view == null)
                return false;

            return x >= view.getLeft() && x < view.getRight()
                    && y >= view.getTop() && y < view.getBottom();
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            if (mLockDialoger)
                return true;
            return super.onInterceptTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (mLockDialoger)
                return false;

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!isViewUnder(mContentView, (int) event.getX(), (int) event.getY())) {
                    if (mCanceledOnTouchOutside && mCancelable) {
                        if (mIsDebug)
                            Log.i(Dialoger.class.getSimpleName(), "touch outside try dismiss ");

                        dismiss();
                        return true;
                    }
                }
            }

            if (HDialoger.this.onTouchEvent(event))
                return true;

            super.onTouchEvent(event);
            return true;
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (mIsDebug)
                Log.i(Dialoger.class.getSimpleName(), "onAttachedToWindow");
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mIsDebug)
                Log.i(Dialoger.class.getSimpleName(), "onDetachedFromWindow");
        }
    }

    private final class InternalContainerView extends LinearLayout {
        public InternalContainerView(Context context) {
            super(context);
        }

        @Override
        public void setGravity(int gravity) {
            if (mGravity != gravity) {
                mGravity = gravity;
                super.setGravity(gravity);
            }
        }

        @Override
        public void setPadding(int left, int top, int right, int bottom) {
            if (left < 0)
                left = getPaddingLeft();
            if (top < 0)
                top = getPaddingTop();
            if (right < 0)
                right = getPaddingRight();
            if (bottom < 0)
                bottom = getPaddingBottom();

            if (left != getPaddingLeft() || top != getPaddingTop()
                    || right != getPaddingRight() || bottom != getPaddingBottom()) {
                super.setPadding(left, top, right, bottom);
            }
        }

        @Override
        public void setVisibility(int visibility) {
            if (visibility == GONE || visibility == INVISIBLE)
                throw new IllegalArgumentException("you can not hide container");
            super.setVisibility(visibility);
        }

        @Override
        public void onViewAdded(View child) {
            super.onViewAdded(child);
            if (child != mContentView)
                throw new RuntimeException("you can not add view to container");

            if (mIsDebug)
                Log.i(Dialoger.class.getSimpleName(), "onContentViewAdded:" + child);
        }

        @Override
        public void onViewRemoved(View child) {
            super.onViewRemoved(child);
            if (child == mContentView) {
                // 外部直接移除内容view的话，关闭窗口
                dismiss();
            }

            if (mIsDebug)
                Log.i(Dialoger.class.getSimpleName(), "onContentViewRemoved:" + child);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (changed)
                HDialoger.this.checkMatchLayoutParams(this);

            startShowAnimator();
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            setTryStartShowAnimator(true);

            if (getWidth() > 0 && getHeight() > 0)
                startShowAnimator();
        }
    }

    private final class InternalBackgroundView extends View {
        public InternalBackgroundView(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            if (changed)
                HDialoger.this.checkMatchLayoutParams(this);
        }
    }

    private void checkMatchLayoutParams(View view) {
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        if (params.width != ViewGroup.LayoutParams.MATCH_PARENT
                || params.height != ViewGroup.LayoutParams.MATCH_PARENT) {
            throw new RuntimeException("you can not change view's width or height");
        }

        if (params.leftMargin != 0 || params.rightMargin != 0
                || params.topMargin != 0 || params.bottomMargin != 0) {
            throw new RuntimeException("you can not set margin to view");
        }
    }

    private Dialog mDialog;

    protected final Dialog getDialog() {
        if (mDialog == null) {
            mDialog = new InternalDialog(mActivity, mThemeResId);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setCancelable(false);
            mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    if (mOnShowListener != null)
                        mOnShowListener.onShow(HDialoger.this);
                }
            });
            mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mOnDismissListener != null)
                        mOnDismissListener.onDismiss(HDialoger.this);
                }
            });

            mDialog.setContentView(mDialogerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return mDialog;
    }

    private boolean shouldTransparentStatusBarForBackgroundDim() {
        if (mIsBackgroundDim) {
            if (!isContentHeightMatchParent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 内容View的高度是否为{@link ViewGroup.LayoutParams#MATCH_PARENT}
     *
     * @return
     */
    private boolean isContentHeightMatchParent() {
        if (mContentView == null)
            return false;

        final ViewGroup.LayoutParams params = mContentView.getLayoutParams();
        if (params == null)
            return false;

        if (params.height == ViewGroup.LayoutParams.WRAP_CONTENT)
            return false;

        if (params.height == ViewGroup.LayoutParams.MATCH_PARENT)
            return true;

        if (params.height == getDisplayHeight(getContext()))
            return true;

        return false;
    }

    private static int getDisplayHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    private final class InternalDialog extends Dialog {
        public InternalDialog(Context context, int themeResId) {
            super(context, themeResId);
        }

        private void setDefaultParams() {
            final int targetWidth = ViewGroup.LayoutParams.MATCH_PARENT;
            int targetHeight = ViewGroup.LayoutParams.WRAP_CONTENT;

            if (shouldTransparentStatusBarForBackgroundDim())
                HStatusBarUtils.setTransparent(this);

            boolean setHeightPixels = false;
            if (HStatusBarUtils.isContentExtension(HDialoger.this.getWindow()))
                setHeightPixels = true;

            if (setHeightPixels)
                targetHeight = getDisplayHeight(getContext());

            final WindowManager.LayoutParams params = getWindow().getAttributes();
            if (params.width != targetWidth || params.height != targetHeight
                    || params.horizontalMargin != 0 || params.verticalMargin != 0) {
                params.width = targetWidth;
                params.height = targetHeight;
                params.horizontalMargin = 0;
                params.verticalMargin = 0;
                getWindow().setAttributes(params);
            }

            final View view = getWindow().getDecorView();
            if (view.getPaddingLeft() != 0 || view.getPaddingTop() != 0
                    || view.getPaddingRight() != 0 || view.getPaddingBottom() != 0) {
                view.setPadding(0, 0, 0, 0);
            }
        }

        @Override
        protected void onStart() {
            super.onStart();
            if (mIsDebug)
                Log.i(Dialoger.class.getSimpleName(), "onStart");

            setState(State.OnStart);
            getActivityLifecycleCallbacks().register(true);

            HDialoger.this.onStart();
            if (mLifecycleCallbacks != null) {
                for (LifecycleCallback item : mLifecycleCallbacks) {
                    item.onStart(HDialoger.this);
                }
            }

            setLockDialoger(false);

            setDefaultParams();
            setDefaultConfigBeforeShow();
        }

        @Override
        protected void onStop() {
            super.onStop();
            if (mIsDebug)
                Log.i(Dialoger.class.getSimpleName(), "onStop");

            setState(State.OnStop);
            getActivityLifecycleCallbacks().register(false);

            stopDismissRunnable();

            HDialoger.this.onStop();
            if (mLifecycleCallbacks != null) {
                for (LifecycleCallback item : mLifecycleCallbacks) {
                    item.onStop(HDialoger.this);
                }
            }

            if (mIsAnimatorCreatorModifiedInternal)
                setAnimatorCreator(null);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (mLockDialoger)
                return false;

            if (HDialoger.this.onKeyDown(keyCode, event))
                return true;

            return super.onKeyDown(keyCode, event);
        }

        @Override
        public void onBackPressed() {
            HDialoger.this.onBackPressed();
        }
    }

    private InternalActivityLifecycleCallbacks mActivityLifecycleCallbacks;

    private InternalActivityLifecycleCallbacks getActivityLifecycleCallbacks() {
        if (mActivityLifecycleCallbacks == null)
            mActivityLifecycleCallbacks = new InternalActivityLifecycleCallbacks();
        return mActivityLifecycleCallbacks;
    }

    private final class InternalActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        public void register(boolean register) {
            final Application application = mActivity.getApplication();
            application.unregisterActivityLifecycleCallbacks(this);
            if (register)
                application.registerActivityLifecycleCallbacks(this);
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (activity == mActivity) {
                if (mIsDebug)
                    Log.e(Dialoger.class.getSimpleName(), "onActivityDestroyed try remove dialoger");

                if (getAnimatorHandler().isShowAnimatorStarted())
                    getAnimatorHandler().cancelShowAnimator();

                if (getAnimatorHandler().isHideAnimatorStarted())
                    getAnimatorHandler().cancelHideAnimator();

                removeDialogerView(false);
            }
        }
    }

    private enum State {
        TryShow,
        OnStart,
        Shown,

        TryDismiss,
        OnStop,
        Dismissed;

        public boolean isShowPart() {
            return this == Shown || this == OnStart || this == TryShow;
        }

        public boolean isDismissPart() {
            return this == Dismissed || this == OnStop || this == TryDismiss;
        }
    }

    private static long getAnimatorDuration(Animator animator) {
        long duration = animator.getDuration();
        if (duration < 0) {
            if (animator instanceof AnimatorSet) {
                final List<Animator> list = ((AnimatorSet) animator).getChildAnimations();
                for (Animator item : list) {
                    final long durationItem = getAnimatorDuration(item);
                    if (durationItem > duration)
                        duration = durationItem;
                }
            }
        }
        return duration;
    }

    private static boolean isFullScreen(Window window) {
        if (window == null)
            return false;

        final WindowManager.LayoutParams params = window.getAttributes();
        if (params == null)
            return false;

        return (params.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }
}
