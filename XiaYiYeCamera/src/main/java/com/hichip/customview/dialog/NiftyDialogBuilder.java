package com.hichip.customview.dialog;

import com.hichip.R;
import com.hichip.thecamhi.zxing.utils.Utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
public class NiftyDialogBuilder extends Dialog implements DialogInterface {

private  Context context;
    private Effectstype type=null;
    private RelativeLayout mRelativeLayoutView;
    private View mDialogView;
    private TextView mTitle;
    private TextView mMessage;
    private int mDuration = -1;
    private static  int mOrientation=1;
    private boolean isCancelable=true;
    private volatile static NiftyDialogBuilder instance;
    private TextView tv_1,tv_2;
    private boolean isMain=false;
    private CheckBox checkBox;
    public NiftyDialogBuilder(Context context) {
        super(context);this.context=context;
        init(context);

    }
    public NiftyDialogBuilder(Context context,int theme) {
        super(context, theme);this.context=context;
        init(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = LayoutParams.MATCH_PARENT;
        params.width  = LayoutParams.MATCH_PARENT;
        getWindow().setAttributes((WindowManager.LayoutParams) params);

    }
    public static NiftyDialogBuilder getInstance(Context context) {

        int ort=context.getResources().getConfiguration().orientation;
        if (mOrientation!=ort){
            mOrientation=ort;
            instance=null;
        }

        if (instance == null) {
            synchronized (NiftyDialogBuilder.class) {
                if (instance == null) {
                    instance = new NiftyDialogBuilder(context,R.style.dialog_untran);
                }
            }
        }
        return instance;

    }

    private void init(Context context) {


        mDialogView = View.inflate(context, R.layout.dialog_layout, null);
        mRelativeLayoutView=(RelativeLayout)mDialogView.findViewById(R.id.main);
        mTitle = (TextView) mDialogView.findViewById(R.id.dialog_title);
        mMessage = (TextView) mDialogView.findViewById(R.id.tv_content);
        
        tv_1=(TextView) mDialogView.findViewById(R.id.tv_1);
        tv_2=(TextView) mDialogView.findViewById(R.id.tv_2);
        checkBox=mDialogView.findViewById(R.id.cb_is_backstage);
        setContentView(mDialogView);

        this.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

               // mLinearLayoutView.setVisibility(View.VISIBLE);
                if(type==null){
                    type=Effectstype.Slidetop;
                }
                start(type);


            }
        });
        mRelativeLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCancelable)dismiss();
            }
        });
    }


    public NiftyDialogBuilder withTitle(CharSequence title) {
        mTitle.setText(title);
        return this;
    }

    public NiftyDialogBuilder withTitleColor(String colorString) {
        mTitle.setTextColor(Color.parseColor(colorString));
        return this;
    }

    public NiftyDialogBuilder withMessage(int textResId) {
        mMessage.setText(textResId);
        return this;
    }
    
    public NiftyDialogBuilder withMessageLayoutWrap() {
    	LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	params.gravity=Gravity.CENTER;
		mMessage.setLayoutParams(params);
    	return this;
    }

    public NiftyDialogBuilder withMessage(CharSequence msg) {
        mMessage.setText(msg);
        return this;
    }


    public NiftyDialogBuilder withDuration(int duration) {
        this.mDuration=duration;
        return this;
    }

    public NiftyDialogBuilder withEffect(Effectstype type) {
        this.type=type;
        return this;
    }
    public NiftyDialogBuilder withButton1Text(CharSequence text) {
        tv_1.setText(text);
        return this;
    }
    public NiftyDialogBuilder withButton2Text(CharSequence text) {
    	tv_2.setText(text);
        return this;
    }
    
    public NiftyDialogBuilder setButton1Click(View.OnClickListener click) {
    	tv_1.setOnClickListener(click);
        return this;
    }

    public NiftyDialogBuilder setButton2Click(View.OnClickListener click) {
    	tv_2.setOnClickListener(click);
        return this;
    }

    public NiftyDialogBuilder isCancelableOnTouchOutside(boolean cancelable) {
        this.isCancelable=cancelable;
        this.setCanceledOnTouchOutside(cancelable);
        return this;
    }

    public NiftyDialogBuilder isCancelable(boolean cancelable) {
        this.isCancelable=cancelable;
        this.setCancelable(cancelable);
        return this;
    }

    @Override
    public void show() {
        super.show();
    }

    private void start(Effectstype type){
        BaseEffects animator = type.getAnimator();
        if(mDuration != -1){
            animator.setDuration(Math.abs(mDuration));
        }
        animator.start(mRelativeLayoutView);
    }
    @Override
    public void dismiss() {
    	super.dismiss();
    	if(instance!=null){
    		instance=null;
    	}
    }
    public boolean getIsManin(){
        return isMain;
    }
    public boolean getCb(){
        return checkBox.isChecked();
    }
    public void setIsMain(boolean isMain){
        this.isMain=isMain;
        if(isMain){
            checkBox.setVisibility(View.VISIBLE);
        }
    }
    public void setContentBootom(boolean isMain){
        if(isMain){
            mMessage.setGravity(Gravity.CENTER|Gravity.BOTTOM);
            //mMessage.setHeight(Utils.dip2px(context,30));

        }else {
            mMessage.setGravity(Gravity.CENTER);
        }

    }
}
