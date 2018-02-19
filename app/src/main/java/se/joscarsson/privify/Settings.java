package se.joscarsson.privify;

import android.content.Context;
import android.content.SharedPreferences;

import se.joscarsson.privify.models.ConcretePrivifyFile;
import se.joscarsson.privify.models.PrivifyFile;

public class Settings {
    public static final String PREFERENCES_NAME = "privifyAppSettings";

    static boolean isAutoLockEnabled(Context context) {
        return preferences(context).getBoolean("auto_lock_enabled", true);
    }

    static int getAutoLockDelayMinutes(Context context) {
        return Integer.parseInt(preferences(context).getString("auto_lock_delay_minutes", "5"));
    }

    public static PrivifyFile getShareTargetDirectory(Context context) {
        String path = preferences(context).getString("share_target_directory", null);
        if (path == null) return ConcretePrivifyFile.ROOT;
        PrivifyFile directory = new ConcretePrivifyFile(path);
        if (!directory.exists()) return ConcretePrivifyFile.ROOT;
        return directory;
    }

    public static boolean hasShareTargetDirectory(Context context) {
        String path = preferences(context).getString("share_target_directory", null);
        if (path == null) return false;
        PrivifyFile directory = new ConcretePrivifyFile(path);
        return directory.exists();
    }

    public static void setShareTargetDirectory(Context context, PrivifyFile directory) {
        preferences(context).edit().putString("share_target_directory", directory.getPath()).commit();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}
