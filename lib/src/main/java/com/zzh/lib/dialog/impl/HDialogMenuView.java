package com.zzh.lib.dialog.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zzh.lib.dialog.DialogMenuView;
import com.zzh.lib.dialog.Dialoger;
import com.zzh.lib.dialog.R;

import java.util.Arrays;
import java.util.List;

/**
 * 带取消按钮的菜单
 */
public class HDialogMenuView extends BaseDialogView implements DialogMenuView {
    public TextView tv_title;
    public TextView tv_cancel;
    public ListView lv_content;

    private List<Object> mListModel;

    private Callback mCallback;

    public HDialogMenuView(Context context) {
        this(context, null);
    }

    public HDialogMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setContentView(R.layout.lib_dialogview_view_menu);
        tv_title = findViewById(R.id.tv_title);
        tv_cancel = findViewById(R.id.tv_cancel);
        lv_content = findViewById(R.id.lv_content);

        tv_cancel.setOnClickListener(this);
        setTextTitle(null);

        if (getLayoutParams() == null) {
            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    @Override
    protected void initDialog(Dialoger dialog) {
        super.initDialog(dialog);
        dialog.setPadding(0, 0, 0, 0);
        dialog.setGravity(Gravity.BOTTOM);
        dialog.setCanceledOnTouchOutside(true);
    }

    @Override
    public DialogMenuView setTextTitle(String text) {
        if (TextUtils.isEmpty(text)) {
            tv_title.setVisibility(View.GONE);
        } else {
            tv_title.setVisibility(View.VISIBLE);
            tv_title.setText(text);
        }
        return this;
    }

    @Override
    public DialogMenuView setTextCancel(String text) {
        if (TextUtils.isEmpty(text)) {
            tv_cancel.setVisibility(View.GONE);
        } else {
            tv_cancel.setVisibility(View.VISIBLE);
            tv_cancel.setText(text);
        }
        return this;
    }

    @Override
    public DialogMenuView setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

    @Override
    public DialogMenuView setItems(Object... objects) {
        List<Object> listObject = null;
        if (objects != null) {
            listObject = Arrays.asList(objects);
        }
        setItems(listObject);
        return this;
    }

    @Override
    public DialogMenuView setItems(List<Object> listObject) {
        mListModel = listObject;
        setAdapter(getAdapter());
        return this;
    }

    @Override
    public DialogMenuView setAdapter(BaseAdapter adapter) {
        lv_content.setAdapter(adapter);
        lv_content.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCallback != null)
                    mCallback.onClickItem(view, (int) id, HDialogMenuView.this);
                else
                    dismiss();
            }
        });
        return this;
    }

    protected BaseAdapter getAdapter() {
        return mInternalAdapter;
    }

    private BaseAdapter mInternalAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            if (mListModel != null && !mListModel.isEmpty()) {
                return mListModel.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return getModel(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.lib_dialogview_item_view_menu, parent, false);
            }
            TextView textView = convertView.findViewById(R.id.tv_content);
            Object object = getItem(position);
            if (object != null) {
                textView.setText(String.valueOf(object));
            }
            return convertView;
        }
    };

    private Object getModel(int position) {
        if (mListModel != null && !mListModel.isEmpty()
                && position >= 0
                && position < mListModel.size()) {
            return mListModel.get(position);
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v == tv_cancel) {
            if (mCallback != null)
                mCallback.onClickCancel(v, this);
            else
                dismiss();
        }
    }
}
