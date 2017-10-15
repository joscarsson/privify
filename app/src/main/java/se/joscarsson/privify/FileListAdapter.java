package se.joscarsson.privify;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class FileListAdapter extends BaseAdapter {
    private Context context;
    private File[] files;
    private File parent;

    FileListAdapter(Context context) {
        this.context = context;
    }

    void openRootDirectory() {
        this.openDirectory(new File("/sdcard"));
    }

    boolean up() {
        return this.openDirectory(this.parent.getParentFile());
    }

    private boolean openDirectory(File file) {
        if (file.getAbsolutePath().equals("/")) return false;
        this.parent = file;
        this.files = file.listFiles();
        this.notifyDataSetChanged();
        return true;
    }

    @Override
    public int getCount() {
        if (this.files == null) return 0;
        return this.files.length;
    }

    @Override
    public Object getItem(int position) {
        return this.files[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AbsListView listView = (AbsListView)parent;
        View row = convertView;

        if (convertView == null) {
            row = LayoutInflater.from(context).inflate(R.layout.file_row, parent, false);
        }

        File file = this.files[position];

        CheckBox actionCheckBox = row.findViewById(R.id.actionCheckBox);
        actionCheckBox.setChecked(false);
        actionCheckBox.jumpDrawablesToCurrentState();

        TextView filenameTextView = row.findViewById(R.id.filenameTextView);
        filenameTextView.setText(file.getName());
        filenameTextView.setTag(file);

        ImageView iconImageView = row.findViewById(R.id.iconImageView);

        if (file.isDirectory()) {
            iconImageView.setImageResource(R.drawable.ic_folder_open_black);
            filenameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File file = (File)v.getTag();
                    FileListAdapter.this.openDirectory(file);
                }
            });
        } else {
            iconImageView.setImageResource(R.drawable.ic_lock_open_black);
            filenameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String packageName = "se.joscarsson.privify.provider." + context.getApplicationContext().getPackageName();
                    File file = (File)v.getTag();
                    Uri fileUri = FileProvider.getUriForFile(FileListAdapter.this.context, packageName, file);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setData(fileUri);

                    FileListAdapter.this.context.startActivity(intent);
                }
            });
        }

        return row;
    }
}
