package se.joscarsson.privify;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;

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
        this.listAdapter.setCurrentDirectory(Settings.getDefaultDirectory(this));
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

        this.encryptionEngine.work(this.listAdapter.getSelectedFiles(), PassphraseActivity.passphrase);
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
