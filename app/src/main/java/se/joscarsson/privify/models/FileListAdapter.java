package se.joscarsson.privify.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.joscarsson.privify.R;

public class FileListAdapter extends BaseAdapter {
    private Context context;
    private List<PrivifyFile> files;
    private Set<PrivifyFile> selectedFiles;
    private PrivifyFile currentDirectory;
    private OnChangeListener listener;
    private boolean checkboxesEnabled;

    public FileListAdapter(Context context, OnChangeListener listener) {
        this.context = context;
        this.listener = listener;
        this.selectedFiles = new HashSet<>();
        this.checkboxesEnabled = true;
        this.currentDirectory = ConcretePrivifyFile.ROOT;
    }

    public void setCheckboxesEnabled(boolean enabled) {
        this.checkboxesEnabled = enabled;
    }

    public void setCurrentDirectory(PrivifyFile directory) {
        this.currentDirectory = directory;
    }

    public PrivifyFile up() {
        return openDirectory(this.currentDirectory.getParent());
    }

    public List<PrivifyFile> getSelectedFiles() {
        List<PrivifyFile> files = new ArrayList<>(this.selectedFiles);
        Collections.sort(files);
        return files;
    }

    public PrivifyFile openDirectory(PrivifyFile directory) {
        if (directory.isUpFromRoot()) return null;
        this.currentDirectory = directory;
        this.selectedFiles.clear();
        notifyDataSetChanged();
        return directory;
    }

    public PrivifyFile getCurrentDirectory() {
        return this.currentDirectory;
    }

    @Override
    public void notifyDataSetChanged() {
        this.files = this.currentDirectory.getFiles();
        this.selectedFiles.clear();
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View row = convertView;

        if (convertView == null) {
            row = LayoutInflater.from(this.context).inflate(R.layout.file_row, parent, false);
        }

        final PrivifyFile file = this.files.get(position);
        row.setTag(file);

        CheckBox actionCheckBox = row.findViewById(R.id.action_check_box);
        actionCheckBox.setEnabled(this.checkboxesEnabled);
        actionCheckBox.setOnCheckedChangeListener(null);
        actionCheckBox.setChecked(this.selectedFiles.contains(file));
        actionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                if (isChecked) {
                    FileListAdapter.this.selectedFiles.add(file);
                } else {
                    FileListAdapter.this.selectedFiles.remove(file);
                }

                FileListAdapter.this.listener.onSelectionChanged(new ArrayList<>(FileListAdapter.this.selectedFiles));
            }
        });

        TextView filenameTextView = row.findViewById(R.id.filename_text_view);
        filenameTextView.setText(file.getName());

        ImageView iconImageView = row.findViewById(R.id.icon_image_view);
        if (file.isDirectory()) {
            iconImageView.setImageResource(R.drawable.ic_folder_open_black);
        } else {
            iconImageView.setImageResource(file.isEncrypted() ? R.drawable.ic_lock_black : R.drawable.ic_lock_open_black);
        }

        parent.jumpDrawablesToCurrentState();
        return row;
    }
}
