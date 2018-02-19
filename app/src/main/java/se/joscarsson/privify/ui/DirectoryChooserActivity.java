package se.joscarsson.privify.ui;

import android.app.Activity;
import android.os.Bundle;

import java.util.List;

import se.joscarsson.privify.models.PrivifyFile;
import se.joscarsson.privify.R;
import se.joscarsson.privify.Settings;

public class DirectoryChooserActivity extends FileBrowserActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.actionButton.setImageResource(R.drawable.ic_done_white);
        this.listAdapter.setCheckboxesEnabled(false);
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finishAfterTransition();
    }

    @Override
    public void onSelectionChanged(List<PrivifyFile> selectedFiles) {
        // Checkboxes are disabled, this will never be called in this activity.
    }

    @Override
    protected void onFileClicked(PrivifyFile file) {
        // Tapping a file should do nothing.
    }

    @Override
    protected void onActionButtonClicked() {
        Settings.setShareTargetDirectory(this, this.listAdapter.getCurrentDirectory());
        setResult(Activity.RESULT_OK);
        finishAfterTransition();
    }

    @Override
    protected String getRootTitle() {
        return "Choose a directory...";
    }

    @Override
    protected int getMenuItemId() {
        return -1;
    }
}
