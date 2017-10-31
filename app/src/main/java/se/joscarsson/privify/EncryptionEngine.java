package se.joscarsson.privify;

import android.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

class EncryptionEngine {
    private Executor executor = Executors.newSingleThreadExecutor();
    private UserInterfaceHandler uiHandler;

    EncryptionEngine(UserInterfaceHandler uiHandler) {
        this.uiHandler = uiHandler;
    }

    void work(final List<PrivifyFile> files, final String passphrase) {
        uiHandler.sendWorkBegun();

        this.executor.execute(new Runnable() {
            private byte[] buffer = new byte[1024*1024];
            private long processedBytes = 0;
            private long totalBytes = 0;
            private boolean currentIsEncrypted;
            private String currentName;

            @Override
            public void run() {
                try {
                    List<PrivifyFile> expandedFiles = PrivifyFile.expandDirectories(files);

                    for (PrivifyFile file : expandedFiles) {
                        this.totalBytes += file.getSize();
                    }

                    for (PrivifyFile file : expandedFiles) {
                        this.currentName = file.getName();
                        this.currentIsEncrypted = file.isEncrypted();

                        if (this.currentIsEncrypted) {
                            decryptFile(file);
                        } else {
                            encryptFile(file);
                        }
                    }

                    EncryptionEngine.this.uiHandler.sendWorkDone();
                } catch (Exception e) {
                    EncryptionEngine.this.uiHandler.sendWorkError();
                }
            }

            private void encryptFile(PrivifyFile plainFile) throws BadPaddingException {
                PrivifyFile encryptedFile = plainFile.asEncrypted();

                try {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    Pair<Cipher, byte[]> cipherPair = Cryptography.newCipher(passphrase);
                    Cipher cipher = cipherPair.first;
                    byte[] header = cipherPair.second;

                    try {
                        inputStream = plainFile.getInputStream();
                        outputStream = encryptedFile.getOutputStream();

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

                    garbleFile(plainFile);
                    plainFile.delete();
                } catch (Exception e) {
                    encryptedFile.delete(true);
                    throw new RuntimeException(e);
                }
            }

            private void decryptFile(PrivifyFile encryptedFile) throws BadPaddingException {
                PrivifyFile plainFile = encryptedFile.asPlain();

                try {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {
                        inputStream = encryptedFile.getInputStream();
                        Cipher cipher = Cryptography.getCipher(passphrase, inputStream);
                        outputStream = new CipherOutputStream(plainFile.getOutputStream(), cipher);

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

                    encryptedFile.delete();
                } catch (Exception e) {
                    plainFile.delete(true);
                    throw new RuntimeException(e);
                }
            }

            /**
             * Garbles the file by writing zeros to it. As the memory is of type Flash this does
             * not guarantee that data gets fully overwritten (the driver might very well choose
             * to write the garble data to other memory cells), but this at least makes recovery
             * harder.
             */
            private void garbleFile(PrivifyFile file) throws IOException {
                OutputStream outputStream = null;
                long bytesToWrite = file.getSize();
                long bytesWritten = 0;

                if (bytesToWrite > 5*1024*1024) {
                    bytesToWrite = (long)(bytesToWrite * 0.1);
                }

                Arrays.fill(buffer, (byte)0);

                try {
                    outputStream = file.getOutputStream();

                    while (bytesWritten < bytesToWrite) {
                        int len = bytesToWrite > buffer.length ? buffer.length : (int)bytesToWrite;
                        outputStream.write(buffer, 0, len);
                        bytesWritten += len;
                    }
                } finally {
                    if (outputStream != null) outputStream.close();
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
