package se.joscarsson.privify;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

class EncryptionEngine {
    private String passphrase;
    private FileListAdapter adapter;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            EncryptionEngine.this.adapter.notifyDataSetChanged();
        }
    };

    EncryptionEngine(FileListAdapter adapter, String passphrase) {
        this.adapter = adapter;
        this.passphrase = passphrase;
    }

    void work() {
        final List<PrivifyFile> files = this.adapter.getSelectedFiles();

        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024*1024];

                for (PrivifyFile file : files) {
                    if (file.isEncrypted()) {
                        decryptFile(file, buffer);
                    } else {
                        encryptFile(file, buffer);
                    }
                }

                EncryptionEngine.this.handler.sendEmptyMessage(0);
            }

            private void encryptFile(PrivifyFile file, byte[] buffer) {
                try {
                    FileInputStream inputStream = null;
                    OutputStream outputStream = null;

                    Pair<Cipher, byte[]> cipherPair = Cryptography.newCipher(EncryptionEngine.this.passphrase);
                    Cipher cipher = cipherPair.first;
                    byte[] header = cipherPair.second;

                    try {
                        inputStream = new FileInputStream(file.getPath());
                        outputStream = new FileOutputStream(file.getEncryptedPath());

                        outputStream.write(header);
                        outputStream = new CipherOutputStream(outputStream, cipher);

                        int bytesRead = inputStream.read(buffer);
                        while (bytesRead != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            bytesRead = inputStream.read(buffer);
                        }
                    } finally {
                        if (inputStream != null) inputStream.close();
                        if (outputStream != null) outputStream.close();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            private void decryptFile(PrivifyFile file, byte[] buffer) {
                try {
                    InputStream inputStream = null;
                    FileOutputStream outputStream = null;

                    try {
                        inputStream = new FileInputStream(file.getEncryptedPath());
                        outputStream = new FileOutputStream(file.getPath() + ".test.pdf");

                        Cipher cipher = Cryptography.getCipher(EncryptionEngine.this.passphrase, inputStream);
                        inputStream = new CipherInputStream(inputStream, cipher);

                        int bytesRead = inputStream.read(buffer);
                        while (bytesRead != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            bytesRead = inputStream.read(buffer);
                        }
                    } finally {
                        if (inputStream != null) inputStream.close();
                        if (outputStream != null) outputStream.close();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
