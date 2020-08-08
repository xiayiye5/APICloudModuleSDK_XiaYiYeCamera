
package com.hichip.hichip.progressload;

import com.hichip.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;


public class DialogUtils {
    public static void showToast(Context context, String msg) {

        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void showToast(Context context, int msgId) {

        Toast.makeText(context, msgId, Toast.LENGTH_LONG).show();
    }
    public static void showToastShort(Context context, int msgId) {

        Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
    }

    public static void showToastShort(Context context, String msg) {

        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
    public static Dialog createLoadingDialog(Context context,boolean isOutSide) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.layout_loading_dialog, null); 
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view); 
        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog); 
        loadingDialog.setCancelable(true); 
        loadingDialog.setCanceledOnTouchOutside(isOutSide);
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return loadingDialog;
    }
}
