package se.joscarsson.privify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class PassphraseActivity extends AppCompatActivity implements TextView.OnEditorActionListener {
    static String passphrase;

    private EditText passphraseEditText;
    private EditText passphraseRepeatEditText;
    private SharedPreferences preferences;
    private boolean passphraseSet;

    static boolean ensurePassphrase(Activity callee) {
        if (PassphraseActivity.passphrase != null) return true;
        Intent intent = new Intent(callee, PassphraseActivity.class);
        callee.startActivityForResult(intent, 0);
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finishAfterTransition();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passphrase);

        this.preferences = this.getSharedPreferences("privify", Context.MODE_PRIVATE);

        this.passphraseEditText = findViewById(R.id.passphrase_edit_text);
        this.passphraseEditText.setOnEditorActionListener(this);

        this.passphraseRepeatEditText = findViewById(R.id.passphrase_repeat_edit_text);
        this.passphraseRepeatEditText.setOnEditorActionListener(this);

        this.passphraseSet = this.preferences.contains("passphrase");

        if (!this.passphraseSet) {
            this.passphraseEditText.setHint("Choose passphrase");
            this.passphraseEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            this.passphraseRepeatEditText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != EditorInfo.IME_ACTION_DONE) return false;

        String passphrase = this.passphraseEditText.getText().toString();
        if (passphrase.length() == 0) {
            this.passphraseEditText.setError("Input passphrase.");
            return true;
        }

        if (this.passphraseSet && !verifyPassphrase(passphrase)) {
            this.passphraseEditText.setError("Wrong passphrase.");
            this.passphraseEditText.setText("");
            return true;
        } else if (!this.passphraseSet) {
            String confirmPassphrase = this.passphraseRepeatEditText.getText().toString();
            if (!passphrase.equals(confirmPassphrase)) {
                this.passphraseRepeatEditText.setError("Passphrases do not match.");
                return true;
            }

            storePassphrase(passphrase);
        }

        setResult(RESULT_OK);
        finishAfterTransition();
        return true;
    }

    private void storePassphrase(String passphrase) {
        Pair<String, String> newHash = Cryptography.hash(passphrase, null);

        this.preferences
                .edit()
                .putString("passphrase", newHash.first)
                .putString("salt", newHash.second)
                .commit();

        PassphraseActivity.passphrase = passphrase;
    }

    private boolean verifyPassphrase(String passphrase) {
        String currentHash = this.preferences.getString("passphrase", null);
        String salt = this.preferences.getString("salt", null);
        Pair<String, String> newHash = Cryptography.hash(passphrase, salt);

        if (!currentHash.equals(newHash.first)) return false;

        PassphraseActivity.passphrase = passphrase;
        return true;
    }
}
