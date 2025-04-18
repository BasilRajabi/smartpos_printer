/*
 * Copyright (C) 2017 Haoge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.szzcs.smartpos.utils.update.impl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import com.szzcs.smartpos.utils.update.base.DownloadCallback;
import com.szzcs.smartpos.utils.update.base.DownloadNotifier;
import com.szzcs.smartpos.utils.update.model.Update;
import com.szzcs.smartpos.utils.update.util.ActivityManager;
import com.szzcs.smartpos.utils.update.util.SafeDialogHandle;

import java.io.File;
import java.util.Locale;

/**
 * 默认使用的下载进度通知创建器: 在此创建Dialog弹窗显示并根据下载回调通知进行进度条更新
 *
 * @author haoge
 */
public class DefaultDownloadNotifier extends DownloadNotifier {

    private static String mTitle = "Download apk error";
    private static String mMessage = "Download again?";
    private static String mExit = "Exit";
    private static String mCancel = "Cancel";
    private static String mOK = "OK";

    static {
        if (Locale.getDefault().toString().contains(Locale.CHINA.toString())) {
            mTitle = "下载apk失败";
            mMessage = "是否重新下载？";
            mExit = "退出";
            mCancel = "取消";
            mOK = "确定";
        }
    }

    @Override
    public DownloadCallback create(Update update, Activity activity) {
        final ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(100);
        dialog.setProgress(0);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        SafeDialogHandle.safeShowDialog(dialog);
        return new DownloadCallback() {
            @Override
            public void onDownloadStart() {
            }

            @Override
            public void onDownloadComplete(File file) {
                SafeDialogHandle.safeDismissDialog(dialog);
            }

            @Override
            public void onDownloadProgress(long current, long total) {
                int percent = (int) (current * 1.0f / total * 100);
                dialog.setProgress(percent);
            }

            @Override
            public void onDownloadError(Throwable t) {
                SafeDialogHandle.safeDismissDialog(dialog);
                createRestartDialog();
            }
        };
    }

    private void createRestartDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityManager.get().topActivity())
                .setCancelable(!update.isForced())
                .setTitle(mTitle)
                .setMessage(mMessage)
                .setNeutralButton(update.isForced() ? mExit : mCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (update.isForced()) {
                            ActivityManager.get().exit();
                        } else {
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(mOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartDownload();
                    }
                });

        builder.show();
    }
}
