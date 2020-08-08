package com.hichip.thecamhi.widget.toast;

import android.support.annotation.NonNull;
import android.widget.Toast;

public interface BadTokenListener {
    void onBadTokenCaught(@NonNull Toast toast);
}
