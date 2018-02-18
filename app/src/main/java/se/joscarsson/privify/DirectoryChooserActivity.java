package se.joscarsson.privify;

import android.os.Bundle;

import java.util.List;

public class DirectoryChooserActivity extends FileBrowserActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.actionButton.setImageResource(R.drawable.ic_done_white);
        this.listAdapter.setCheckboxesEnabled(false);
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
        finish();
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
