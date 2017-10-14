package se.joscarsson.privify;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    private PassphraseVault vault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View view = this.findViewById(R.id.activityMain);
        this.vault = new PassphraseVault(view);
        this.vault.collectPassphrase();
    }
}
