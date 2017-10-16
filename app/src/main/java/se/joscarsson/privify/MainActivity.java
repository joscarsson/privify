package se.joscarsson.privify;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayDeque;
import java.util.Deque;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private PassphraseCollector passphraseCollector;
    private EncryptionEngine encryptionEngine;
    private FileListAdapter listAdapter;
    private NotificationHelper notificationHelper;
    private SwipeRefreshLayout refreshLayout;
    private ListView listView;
    private Deque<Pair<Integer, Integer>> scrollPositions;
    private boolean hasPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View view = this.findViewById(R.id.activityMain);
        FloatingActionButton actionButton = this.findViewById(R.id.actionButton);
        this.listView = this.findViewById(R.id.fileListView);
        this.refreshLayout = this.findViewById(R.id.refreshLayout);

        this.scrollPositions = new ArrayDeque<>();
        this.listAdapter = new FileListAdapter(this.getApplicationContext());

        actionButton.setOnClickListener(this);
        this.listView.setOnItemClickListener(this);
        this.listView.setAdapter(this.listAdapter);
        this.listView.setEmptyView(this.findViewById(R.id.emptyTextView));
        this.refreshLayout.setOnRefreshListener(this);

        ensurePermission();

        this.notificationHelper = new NotificationHelper(getApplicationContext());
        UserInterfaceHandler uiHandler = new UserInterfaceHandler(actionButton, this.listAdapter, notificationHelper);

        this.encryptionEngine = new EncryptionEngine(uiHandler);
        this.passphraseCollector = new PassphraseCollector(view);
        passphraseCollector.collect();

        if (this.hasPermission) {
            this.listAdapter.openRootDirectory();
        }
    }

    @Override
    public void onClick(View v) {
        if (this.listAdapter.getSelectedFiles().size() == 0) {
            this.notificationHelper.toast("Select files to process.");
            return;
        }

        this.encryptionEngine.work(this.listAdapter.getSelectedFiles(), passphraseCollector.getPassphrase());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PrivifyFile file = (PrivifyFile)view.getTag();

        if (file.isDirectory()) {
            pushScrollPosition();
            this.listAdapter.openDirectory(file);
        } else if (file.isEncrypted()) {
            this.notificationHelper.toast("File is encrypted, decrypt it before opening.");
        } else {
            openFileInExternalApp(file);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            boolean directoryChanged = this.listAdapter.up();
            if (directoryChanged) {
                popScrollPosition();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRefresh() {
        this.listAdapter.notifyDataSetChanged();
        this.refreshLayout.setRefreshing(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                System.exit(0);
            }
        }

        this.listAdapter.openRootDirectory();
    }

    private void ensurePermission() {
        boolean readExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean writeExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (!readExternalStorage || !writeExternalStorage) {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            this.hasPermission = true;
        }
    }

    private void openFileInExternalApp(PrivifyFile file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setData(file.getUri(getApplicationContext()));

        try {
            getApplicationContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            this.notificationHelper.toast("Found no app capable of opening the selected file.");
        }
    }

    private void pushScrollPosition() {
        int index = this.listView.getFirstVisiblePosition();
        View v = this.listView.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - this.listView.getPaddingTop());
        this.scrollPositions.push(new Pair<>(index, top));
    }

    private void popScrollPosition() {
        Pair<Integer, Integer> pair = scrollPositions.pop();
        this.listView.setSelectionFromTop(pair.first, pair.second);
    }
}
