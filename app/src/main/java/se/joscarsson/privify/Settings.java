package se.joscarsson.privify;

import android.content.Context;
import android.content.SharedPreferences;

class Settings {
    static final String PREFERENCES_NAME = "privifyAppSettings";

    static boolean isAutoLockEnabled(Context context) {
        return preferences(context).getBoolean("auto_lock_enabled", true);
    }

    static int getAutoLockDelayMinutes(Context context) {
        return Integer.parseInt(preferences(context).getString("auto_lock_delay_minutes", "5"));
    }

    static PrivifyFile getDefaultDirectory(Context context) {
        String path = preferences(context).getString("default_directory", null);
        if (path == null) return PrivifyFile.ROOT;
        PrivifyFile directory = new PrivifyFile(path);
        if (!directory.exists()) return PrivifyFile.ROOT;
        return directory;
    }

    static void setDefaultDirectory(Context context, PrivifyFile directory) {
        preferences(context).edit().putString("default_directory", directory.getPath()).commit();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}
