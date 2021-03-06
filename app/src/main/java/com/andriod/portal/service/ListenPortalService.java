package com.andriod.portal.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import com.andriod.portal.widget.TookitViewController;
import com.andriod.portal.clipboard.ClipboardManagerCompat;
public final class ListenPortalService extends Service implements TookitViewController.ViewExitListerner {
    private static final String KEY_FOR_WEAK_LOCK = "weak-lock";
    private static final String KEY_FOR_CMD = "cmd";
    private static final String KEY_FOR_CONTENT = "content";
    private static final String CMD_TEST = "test";

    private static CharSequence sLastContent = null;
    private ClipboardManagerCompat mClipboardWatcher;
    private TookitViewController mTookitViewController;
    private ClipboardManagerCompat.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener = new ClipboardManagerCompat.OnPrimaryClipChangedListener() {
        public void onPrimaryClipChanged() {
            performClipboardCheck();
        }
    };

    public static void start(Context context) {
        Intent serviceIntent = new Intent(context, ListenPortalService.class);
        context.startService(serviceIntent);
    }

    public static void startForWeakLock(Context context, Intent intent) {

        Intent serviceIntent = new Intent(context, ListenPortalService.class);
        context.startService(serviceIntent);

        intent.putExtra(ListenPortalService.KEY_FOR_WEAK_LOCK, true);
        Intent myIntent = new Intent(context, ListenPortalService.class);

        // using wake lock to start service
        WakefulBroadcastReceiver.startWakefulService(context, myIntent);
    }

    @Override
    public void onCreate() {
        mClipboardWatcher = ClipboardManagerCompat.create(this);
        mClipboardWatcher.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClipboardWatcher.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);

        sLastContent = null;
        if (mTookitViewController != null) {
            mTookitViewController.setmViewExitListener(null);
            mTookitViewController = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent != null) {
            // remove wake lock
            if (intent.getBooleanExtra(KEY_FOR_WEAK_LOCK, false)) {
                BootCompletedReceiver.completeWakefulIntent(intent);
            }
            String cmd = intent.getStringExtra(KEY_FOR_CMD);
            if (!TextUtils.isEmpty(cmd)) {
                if (cmd.equals(CMD_TEST)) {
                    String content = intent.getStringExtra(KEY_FOR_CONTENT);
                    showContent(content);
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void performClipboardCheck() {
        CharSequence content = mClipboardWatcher.getText();
        if (TextUtils.isEmpty(content)) {
            return;
        }
        showContent(content);
    }
    private void showContent(CharSequence content) {
        if (sLastContent != null && sLastContent.equals(content) || content == null) {
            return;
        }
        sLastContent = content;

        if (mTookitViewController != null) {
            mTookitViewController.updateContent(content);
        } else {
            mTookitViewController = new TookitViewController(getApplication(), content);
            mTookitViewController.setmViewExitListener(this);
            mTookitViewController.show();
        }
    }

    @Override
    public void onViewExit() {
        sLastContent = null;
        mTookitViewController = null;
    }
}


