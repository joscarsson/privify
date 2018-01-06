package se.joscarsson.privify;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static se.joscarsson.privify.PrivifyApplication.INTENT_LOCK_ACTION;

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, OnSelectionChangeListener {
    private PassphraseCollector passphraseCollector;
    private EncryptionEngine encryptionEngine;
    private FileListAdapter listAdapter;
    private NotificationHelper notificationHelper;
    private SwipeRefreshLayout refreshLayout;
    private ListView listView;
    private FloatingActionButton actionButton;
    private Deque<Pair<Integer, Integer>> scrollPositions;
    private BroadcastReceiver lockBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainActivity.this.passphraseCollector.clearPassphrase();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View view = this.findViewById(R.id.activity_main);

        this.actionButton = this.findViewById(R.id.action_button);
        this.listView = this.findViewById(R.id.file_list_view);
        this.refreshLayout = this.findViewById(R.id.refresh_layout);

        this.scrollPositions = new ArrayDeque<>();
        this.listAdapter = new FileListAdapter(this, this);

        actionButton.setOnClickListener(this);
        this.listView.setOnItemClickListener(this);
        this.listView.setAdapter(this.listAdapter);
        this.listView.setEmptyView(this.findViewById(R.id.empty_text_view));
        this.refreshLayout.setOnRefreshListener(this);

        boolean hasPermission = ensurePermission();

        this.notificationHelper = new NotificationHelper(this);
        UserInterfaceHandler uiHandler = new UserInterfaceHandler(actionButton, this.listAdapter, notificationHelper);

        this.encryptionEngine = new EncryptionEngine(uiHandler);
        this.passphraseCollector = new PassphraseCollector(view);

        if (hasPermission) {
            this.listAdapter.openRootDirectory();
        }

        registerReceiver(this.lockBroadcastReceiver, new IntentFilter(INTENT_LOCK_ACTION));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(this.lockBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.listAdapter.notifyDataSetChanged();
//        this.passphraseCollector.dev();
        this.passphraseCollector.ensurePassphrase();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                System.exit(0);
            }
        }

        this.listAdapter.openRootDirectory();
    }

    @Override
    protected int getMenuItemId() {
        return R.id.storage_menu_item;
    }

    private boolean ensurePermission() {
        boolean readExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean writeExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (!readExternalStorage || !writeExternalStorage) {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return false;
        } else {
            return true;
        }
    }

    private void openFileInExternalApp(PrivifyFile file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setData(file.getUri(this));

        try {
            this.startActivity(intent);
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
