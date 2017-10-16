package se.joscarsson.privify;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileListAdapter extends BaseAdapter {
    private Context context;
    private List<PrivifyFile> files;
    private Set<PrivifyFile> selectedFiles;
    private PrivifyFile currentDirectory;

    FileListAdapter(Context context) {
        this.context = context;
        this.selectedFiles = new HashSet<>();
    }

    void openRootDirectory() {
        this.openDirectory(new PrivifyFile("/sdcard"));
    }

    boolean up() {
        return this.openDirectory(this.currentDirectory.getParent());
    }

    List<PrivifyFile> getSelectedFiles() {
        List<PrivifyFile> files = new ArrayList<>(this.selectedFiles);
        Collections.sort(files);
        return files;
    }

    boolean openDirectory(PrivifyFile directory) {
        if (directory.isRoot()) return false;
        this.currentDirectory = directory;
        this.selectedFiles.clear();
        this.notifyDataSetChanged();
        return true;
    }

    @Override
    public void notifyDataSetChanged() {
        this.files = this.currentDirectory.getFiles();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (this.files == null) return 0;
        return this.files.size();
    }

    @Override
    public Object getItem(int position) {
        return this.files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (convertView == null) {
            row = LayoutInflater.from(context).inflate(R.layout.file_row, parent, false);
        }

        final PrivifyFile file = this.files.get(position);
        row.setTag(file);

        CheckBox actionCheckBox = row.findViewById(R.id.actionCheckBox);
        actionCheckBox.setEnabled(true);
        actionCheckBox.setChecked(this.selectedFiles.contains(file));

        TextView filenameTextView = row.findViewById(R.id.filenameTextView);
        filenameTextView.setText(file.getName());

        ImageView iconImageView = row.findViewById(R.id.iconImageView);

        if (file.isDirectory()) {
            actionCheckBox.setEnabled(false);
            iconImageView.setImageResource(R.drawable.ic_folder_open_black);
        } else {
            iconImageView.setImageResource(file.isEncrypted() ? R.drawable.ic_lock_black : R.drawable.ic_lock_open_black);
        }

        actionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                if (isChecked) {
                    FileListAdapter.this.selectedFiles.add(file);
                } else {
                    FileListAdapter.this.selectedFiles.remove(file);
                }
            }
        });

        parent.jumpDrawablesToCurrentState();

        return row;
    }
}
