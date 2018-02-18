package se.joscarsson.privify;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FileBrowserActivity {
    private EncryptionEngine encryptionEngine;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.notificationHelper = new NotificationHelper(this);
        UserInterfaceHandler uiHandler = new UserInterfaceHandler(this.actionButton, this.listAdapter, notificationHelper);

        this.encryptionEngine = new EncryptionEngine(uiHandler);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void initialize() {
        handleShareIntent();
        super.initialize();
    }

    private void handleShareIntent() {
        Intent intent = getIntent();

        if (intent == null) return;
        if (!Intent.ACTION_SEND.equals(intent.getAction())) return;
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) return;
        if (intent.hasExtra("se.joscarsson.privify.Consumed")) return;
        if (!ensureShareTargetDirectory()) return;

        this.listAdapter.setCurrentDirectory(Settings.getShareTargetDirectory(this));

        Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        String path = getFilePathFromUri(uri);
        final PrivifyFile file = new PrivifyFile(path);
        this.encryptionEngine.work(new ArrayList<PrivifyFile>() {{ add(file); }}, PassphraseActivity.passphrase, false, Settings.getShareTargetDirectory(this));

        getIntent().putExtra("se.joscarsson.privify.Consumed", true);
    }

    @Override
    protected void onFileClicked(PrivifyFile file) {
        if (file.isEncrypted()) {
            this.notificationHelper.toast("File is encrypted, decrypt it before opening.");
        } else {
            openFileInExternalApp(file);
        }
    }

    @Override
    protected void onActionButtonClicked() {
        if (this.listAdapter.getSelectedFiles().size() == 0) {
            this.notificationHelper.toast("Select files to process.");
            return;
        }

        this.encryptionEngine.work(this.listAdapter.getSelectedFiles(), PassphraseActivity.passphrase, true, null);
    }

    @Override
    protected String getRootTitle() {
        return getString(R.string.app_name);
    }

    @Override
    protected int getMenuItemId() {
        return R.id.storage_menu_item;
    }

    @Override
    public void onSelectionChanged(List<PrivifyFile> selectedFiles) {
        boolean onlyEncrypted = true;

        for (PrivifyFile f : selectedFiles) {
            if (!f.isEncrypted()) {
                onlyEncrypted = false;
                break;
            }
        }

        if (onlyEncrypted && !selectedFiles.isEmpty()) {
            this.actionButton.setImageResource(R.drawable.ic_lock_open_white);
        } else {
            this.actionButton.setImageResource(R.drawable.ic_lock_white);
        }
    }

    private String getFilePathFromUri(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, new String[] { MediaStore.MediaColumns.DATA }, null, null, null)) {
            cursor.moveToFirst();
            return cursor.getString(0);
        }
    }

    private boolean ensureShareTargetDirectory() {
        if (Settings.hasShareTargetDirectory(this)) return true;
        Intent intent = new Intent(this, DirectoryChooserActivity.class);
        startActivity(intent);
        return false;
    }

    private void openFileInExternalApp(PrivifyFile file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setData(file.getUri(this));

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            this.notificationHelper.toast("Found no app capable of opening the selected file.");
        }
    }
}
