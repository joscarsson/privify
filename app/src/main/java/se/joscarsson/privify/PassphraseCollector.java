package se.joscarsson.privify;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

class PassphraseCollector {
    private View view;
    private Context context;
    private SharedPreferences preferences;
    private String passphrase;

    PassphraseCollector(View view) {
        this.view = view;
        this.context = view.getContext();
        this.preferences = this.context.getSharedPreferences("privify", Context.MODE_PRIVATE);
    }

    void dev() {
        storePassphrase("cde");
    }

    void collect() {
        this.view.post(new Runnable() {
            public void run() {
                AlertDialog popupDialog = new AlertDialog.Builder(PassphraseCollector.this.context)
                        .setTitle("Input passphrase")
                        .setCancelable(false)
                        .setView(R.layout.passphrase_popup)
                        .setPositiveButton(android.R.string.ok, null)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                System.exit(0);
                            }
                        })
                        .create();

                popupDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EditText passphraseEditText = ((AlertDialog)dialog).findViewById(R.id.passphraseEditText);
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

                popupDialog.show();
            }
        });
    }

    private boolean storePassphrase(String passphrase) {
        String currentHash = this.preferences.getString("passphrase", null);
        String newHash = Cryptography.hash(passphrase);

        if (currentHash != null && !currentHash.equals(newHash)) {
            return false;
        }

        this.preferences
                .edit()
                .putString("passphrase", newHash)
                .commit();

        this.passphrase = passphrase;

        return true;
    }

    String getPassphrase() {
        return this.passphrase;
    }
}
