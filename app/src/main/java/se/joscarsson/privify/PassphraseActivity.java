package se.joscarsson.privify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class PassphraseActivity extends AppCompatActivity implements TextView.OnEditorActionListener {
    static String passphrase;

    private EditText passphraseEditText;
    private SharedPreferences preferences;

    static void ensurePassphrase(Activity callee) {
        if (PassphraseActivity.passphrase != null) return;
        Intent intent = new Intent(callee, PassphraseActivity.class);
        callee.startActivity(intent);
        callee.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passphrase);

        this.preferences = this.getSharedPreferences("privify", Context.MODE_PRIVATE);

        this.passphraseEditText = findViewById(R.id.passphrase_edit_text);
        this.passphraseEditText.setOnEditorActionListener(this);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != EditorInfo.IME_ACTION_GO) return false;

        String passphrase = passphraseEditText.getText().toString();
        if (passphrase.length() == 0) {
            passphraseEditText.setError("Input passphrase.");
        } else if (storePassphrase(passphrase)) {
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
            this.finish();
        } else {
            passphraseEditText.setError("Wrong passphrase.");
            passphraseEditText.setText("");
        }

        return true;
    }

    private boolean storePassphrase(String passphrase) {
        String currentHash = this.preferences.getString("passphrase", null);
        String salt = this.preferences.getString("salt", null);
        Pair<String, String> newHash = Cryptography.hash(passphrase, salt);

        if (currentHash != null && !currentHash.equals(newHash.first)) {
            return false;
        }

        this.preferences
                .edit()
                .putString("passphrase", newHash.first)
                .putString("salt", newHash.second)
                .commit();

        PassphraseActivity.passphrase = passphrase;

        return true;
    }
}
