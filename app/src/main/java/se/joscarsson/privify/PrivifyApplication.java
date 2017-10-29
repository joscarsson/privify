package se.joscarsson.privify;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

public class PrivifyApplication extends Application implements Application.ActivityLifecycleCallbacks {
    static final String INTENT_LOCK_ACTION = BaseActivity.class.getName() + ".lock";

    private PendingIntent lockIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        cancelLock();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        scheduleLock();
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        cancelLock();
    }

    private void scheduleLock() {
        if (!Settings.isAutoLockEnabled(this)) return;

        Intent intent = new Intent(INTENT_LOCK_ACTION);
        this.lockIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtMillis = System.currentTimeMillis() + Settings.getAutoLockDelayMinutes(this) * 60 * 1000;
        triggerAtMillis += 1000; // Always delay at least 1 second to prevent locking when switching between activites in the app.
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, this.lockIntent);
    }

    private void cancelLock() {
        if (this.lockIntent == null) return;
        this.lockIntent.cancel();
        this.lockIntent = null;
    }
}
