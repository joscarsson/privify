package se.joscarsson.privify;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.widget.BaseAdapter;

public class UserInterfaceHandler extends Handler {
    private static final int MESSAGE_WORK_BEGUN = 1;
    private static final int MESSAGE_WORK_DONE = 2;
    private static final int MESSAGE_WORK_ERROR = 3;
    private static final int MESSAGE_PROGRESS_UPDATE = 4;

    private FloatingActionButton button;
    private BaseAdapter adapter;
    private NotificationHelper notificationHelper;

    UserInterfaceHandler(FloatingActionButton button, BaseAdapter adapter, NotificationHelper notificationHelper) {
        super(Looper.getMainLooper());

        this.button = button;
        this.adapter = adapter;
        this.notificationHelper = notificationHelper;
    }

    void sendWorkBegun() {
        sendEmptyMessage(MESSAGE_WORK_BEGUN);

    }

    void sendWorkDone() {
        sendEmptyMessage(MESSAGE_WORK_DONE);
    }

    void sendProgressUpdate(boolean decrypting, String currentName, int progress) {
        Bundle bundle = new Bundle();
        bundle.putString("name", currentName);
        bundle.putBoolean("decrypting", decrypting);
        bundle.putInt("progress", progress);
        Message message = new Message();
        message.setData(bundle);
        message.what = MESSAGE_PROGRESS_UPDATE;
        sendMessage(message);
    }

    void sendWorkError() {
        sendEmptyMessage(MESSAGE_WORK_ERROR);
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == MESSAGE_WORK_BEGUN) {
            this.button.setEnabled(false);
            this.button.setImageResource(R.drawable.ic_hourglass_full_white);
            this.notificationHelper.showEstimating();
        } else if (msg.what == MESSAGE_WORK_DONE) {
            this.adapter.notifyDataSetChanged();
            this.button.setEnabled(true);
            this.button.setImageResource(R.drawable.ic_lock_white);
            this.notificationHelper.hide();
        } else if (msg.what == MESSAGE_WORK_ERROR) {
            this.adapter.notifyDataSetChanged();
            this.button.setEnabled(true);
            this.button.setImageResource(R.drawable.ic_lock_white);
            this.notificationHelper.showError();
        } else if (msg.what == MESSAGE_PROGRESS_UPDATE) {
            int progress = msg.getData().getInt("progress");
            boolean decrypting = msg.getData().getBoolean("decrypting");
            String name = msg.getData().getString("name");
            this.notificationHelper.showProcessing(progress, decrypting, name);
        }
    }
}
