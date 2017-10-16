package se.joscarsson.privify;

import android.content.Context;
import android.util.Pair;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

class EncryptionEngine {
    private FileListAdapter adapter;
    private Executor executor = Executors.newSingleThreadExecutor();
    private UserInterfaceHandler uiHandler;

    EncryptionEngine(FileListAdapter adapter, UserInterfaceHandler uiHandler) {
        this.adapter = adapter;
        this.uiHandler = uiHandler;
    }

    void work(final String passphrase) {
        final List<PrivifyFile> files = this.adapter.getSelectedFiles();

        uiHandler.sendWorkBegun();

        this.executor.execute(new Runnable() {
            private byte[] buffer = new byte[1024*1024];
            private long processedBytes = 0;
            private long totalBytes = 0;
            private boolean currentIsEncrypted;
            private String currentName;

            @Override
            public void run() {
                for (PrivifyFile file : files) {
                    this.totalBytes += file.getSize();
                }

                for (PrivifyFile file : files) {
                    this.currentName = file.getName();
                    this.currentIsEncrypted = file.isEncrypted();

                    if (this.currentIsEncrypted) {
                        decryptFile(file);
                    } else {
                        encryptFile(file);
                    }
                }

                EncryptionEngine.this.uiHandler.sendWorkDone();
            }

            private void encryptFile(PrivifyFile file) {
                try {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    Pair<Cipher, byte[]> cipherPair = Cryptography.newCipher(passphrase);
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
                            updateProgress(bytesRead);
                            bytesRead = inputStream.read(buffer);
                        }
                    } finally {
                        if (inputStream != null) inputStream.close();
                        if (outputStream != null) outputStream.close();
                    }

                    file.delete();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            private void decryptFile(PrivifyFile file) {
                try {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {
                        inputStream = new FileInputStream(file.getEncryptedPath());
                        Cipher cipher = Cryptography.getCipher(passphrase, inputStream);
                        outputStream = new CipherOutputStream(new FileOutputStream(file.getPath()), cipher);

                        int bytesRead = inputStream.read(buffer);
                        while (bytesRead != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            updateProgress(bytesRead);
                            bytesRead = inputStream.read(buffer);
                        }
                    } finally {
                        if (inputStream != null) inputStream.close();
                        if (outputStream != null) outputStream.close();
                    }

                    file.delete();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            private void updateProgress(int bytesRead) {
                processedBytes += bytesRead;
                int progress = (int)(processedBytes * 100 / totalBytes);
                EncryptionEngine.this.uiHandler.sendProgressUpdate(this.currentIsEncrypted, this.currentName, progress);
            }
        });
    }
}
