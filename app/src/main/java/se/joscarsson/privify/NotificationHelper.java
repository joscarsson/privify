package se.joscarsson.privify;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

class NotificationHelper {
    private NotificationManager manager;
    private NotificationCompat.Builder builder;

    NotificationHelper(Context context) {
        this.manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.builder = new NotificationCompat.Builder(context, "privify_progress")
                .setSmallIcon(R.drawable.ic_lock_white)
                .setOngoing(true);
    }

    void showEstimating() {
        this.builder
                .setProgress(100, 0, true)
                .setContentTitle("Estimating");
        this.manager.notify(1, this.builder.build());
    }

    void showProcessing(int progress, boolean isEncrypted, String name) {
        this.builder
                .setProgress(100, progress, false)
                .setContentTitle(name)
                .setContentText(isEncrypted ? "Decrypting" : "Encrypting");
        this.manager.notify(1, this.builder.build());
    }

    void hide() {
        this.manager.cancel(1);
    }
}
