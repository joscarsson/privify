package se.joscarsson.privify;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

class PassphraseCollector {
    private View view;
    private Context context;
    private SharedPreferences preferences;
    private String passphrase;
    private AlertDialog dialog;

    PassphraseCollector(View view) {
        this.view = view;
        this.context = view.getContext();
        this.preferences = this.context.getSharedPreferences("privify", Context.MODE_PRIVATE);
    }

    void dev() {
        storePassphrase("cde");
    }

    void ensurePassphrase() {
        if (this.passphrase != null) return;
        if (this.dialog != null && this.dialog.isShowing()) return;

        this.view.post(new Runnable() {
            public void run() {
                PassphraseCollector.this.dialog = new AlertDialog.Builder(PassphraseCollector.this.context)
                        .setTitle("Input passphrase")
                        .setCancelable(false)
                        .setView(R.layout.passphrase_popup)
                        .setPositiveButton(android.R.string.ok, null)
                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                System.exit(0);
                            }
                        })
                        .create();

                PassphraseCollector.this.dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EditText passphraseEditText = ((AlertDialog)dialog).findViewById(R.id.passphrase_edit_text);
                                String passphrase = passphraseEditText.getText().toString();
                                if (passphrase.length() == 0) {
                                    passphraseEditText.setError("Input passphrase.");
                                } else if (storePassphrase(passphrase)) {
                                    dialog.dismiss();
                                } else {
                                    passphraseEditText.setError("Wrong passphrase.");
                                }
                            }
                        });
                    }
                });

                PassphraseCollector.this.dialog.show();
            }
        });
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

        this.passphrase = passphrase;

        return true;
    }

    String getPassphrase() {
        return this.passphrase;
    }

    void clearPassphrase() {
        this.passphrase = null;
    }
}
