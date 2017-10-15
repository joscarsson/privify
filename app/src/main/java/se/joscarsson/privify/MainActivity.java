package se.joscarsson.privify;

import android.Manifest;
import android.app.ListActivity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;

import java.io.File;

public class MainActivity extends ListActivity {
    private PassphraseVault vault;
    private EncryptionEngine encryptionEngine;
    private FileListAdapter listAdapter;
    private boolean hasPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ensurePermission();

        View view = this.findViewById(R.id.activityMain);

        FloatingActionButton actionButton = this.findViewById(R.id.actionButton);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.encryptionEngine.work();
            }
        });

        this.vault = new PassphraseVault(view);
//        this.vault.collectPassphrase();
        this.vault.storePassphrase("abc");

        this.listAdapter = new FileListAdapter(this.getApplicationContext());
        setListAdapter(this.listAdapter);

        this.encryptionEngine = new EncryptionEngine(this.listAdapter);

        if (this.hasPermission) {
            this.listAdapter.openRootDirectory();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            boolean directoryChanged = this.listAdapter.up();
            if (directoryChanged) return true;
        }

        return super.onKeyDown(keyCode, event);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            this.hasPermission = true;
        }
    }
}
