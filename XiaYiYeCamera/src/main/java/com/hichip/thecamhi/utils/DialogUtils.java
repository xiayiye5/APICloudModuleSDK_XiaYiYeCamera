package com.hichip.thecamhi.utils;


import com.hichip.R;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;



/**
 * created by mango
 * on date  2018/7/16 13:49
 */
public final class DialogUtils {

    private Context context;
    private int themeResId;
    private View layout;
    private boolean cancelable = true;
    private CharSequence title, message, cancelText, sureText;//����message�������ı�����д��Gone��
    private View.OnClickListener sureClickListener, cancelClickListener;

    public DialogUtils(Context context) {
        this(context, R.style.CustomDialog);
    }

    private DialogUtils(Context context, int themeResId) {
        this(context, themeResId, ((LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.my_dialog_layout, null));
    }

    private DialogUtils(Context context, int themeResId, View layout) {
        this.context = context;
        this.themeResId = themeResId;
        this.layout = layout;
    }


    public DialogUtils setCancelable(Boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public DialogUtils title(CharSequence title) {
        this.title = title;
        return this;
    }

    public DialogUtils message(CharSequence message) {
        this.message = message;
        return this;
    }

    public DialogUtils cancelText(CharSequence str) {
        this.cancelText = str;
        return this;
    }


    public DialogUtils sureText(CharSequence str) {
        this.sureText = str;
        return this;
    }

    public DialogUtils setSureOnClickListener(View.OnClickListener listener) {
        this.sureClickListener = listener;
        return this;
    }

    public DialogUtils setCancelOnClickListener(View.OnClickListener listener) {
        this.cancelClickListener = listener;
        return this;
    }

    public Dialog build() {
        final Dialog dialog = new Dialog(context, themeResId);
        dialog.setCancelable(cancelable);
        dialog.addContentView(layout, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));

        setText(title, R.id.title);
        setText(message, R.id.message);
        setText(cancelText, R.id.cancel);
        setText(sureText, R.id.sure);
        if (isValid(cancelText) || isValid(sureText)) {
            layout.findViewById(R.id.line2).setVisibility(View.VISIBLE);
        }
        if (isValid(cancelText) && isValid(sureText)) {
            layout.findViewById(R.id.line).setVisibility(View.VISIBLE);
        }

        final TextView textView = (TextView)layout.findViewById(R.id.message);
        textView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if(textView.getLineCount() ==1){
                    textView.setGravity(Gravity.CENTER);
                }
                return true;
            }
        });

        if (sureClickListener != null) {
            layout.findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sureClickListener.onClick(view);
                    dialog.dismiss();
                }
            });
        }
        if (cancelClickListener != null) {
            layout.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelClickListener.onClick(view);
                    dialog.dismiss();
                }
            });
        }

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.80);
        dialog.getWindow().setAttributes(params);
        return dialog;
    }

    private void setText(CharSequence text, int id) {
        if (isValid(text)) {
            TextView textView = (TextView) layout.findViewById(id);
            textView.setText(text);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private boolean isValid(CharSequence text) {
        return text != null && !"".equals(text.toString().trim());
    }
}

