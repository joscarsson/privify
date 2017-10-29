package se.joscarsson.privify;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrivifyFile implements Comparable<PrivifyFile> {
    private File nativeFile;

    PrivifyFile(String path) {
        this.nativeFile = new File(path);
    }

    private PrivifyFile(File nativeFile) {
        this.nativeFile = nativeFile;
    }

    List<PrivifyFile> getFiles() {
        File[] nativeFiles = this.nativeFile.listFiles();
        List<PrivifyFile> files = new ArrayList<>();

        for (File f : nativeFiles) {
            files.add(new PrivifyFile(f));
        }

        Collections.sort(files);
        return files;
    }

    PrivifyFile getParent() {
        return new PrivifyFile(this.nativeFile.getParentFile());
    }

    String getName() {
        String name = this.nativeFile.getName();
        return isEncrypted() ? name.substring(0, name.length() - 4) : name;
    }

    Uri getUri(Context context) {
        String authority = "se.joscarsson.privify.provider." + context.getPackageName();
        return FileProvider.getUriForFile(context, authority, this.nativeFile);
    }

    boolean isRoot() {
        return this.nativeFile.getAbsolutePath().equals("/");
    }

    boolean isDirectory() {
        return this.nativeFile.isDirectory();
    }

    boolean isEncrypted() { return this.nativeFile.getName().endsWith(".pri"); }

    void delete() {
        this.delete(false);
    }

    void delete(boolean ignoreError) {
        boolean result = this.nativeFile.delete();
        if (!result && !ignoreError) throw new RuntimeException("Failed to delete file.");
    }

    String getEncryptedPath() {
        return isEncrypted() ? this.nativeFile.getAbsolutePath() : this.nativeFile.getAbsolutePath() + ".pri";
    }

    String getPath() {
        String path = this.nativeFile.getAbsolutePath();
        return isEncrypted() ? path.substring(0, path.length() - 4) : path;
    }

    FileOutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream(this.nativeFile);
    }

    FileInputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(this.nativeFile);
    }

    long getSize() {
        return this.nativeFile.length();
    }

    @Override
    public int compareTo(@NonNull PrivifyFile o) {
        String thisName = this.nativeFile.getName();
        String otherName = o.nativeFile.getName();
        return thisName.compareToIgnoreCase(otherName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PrivifyFile) return this.compareTo((PrivifyFile)obj) == 0;
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.nativeFile.hashCode();
    }

    static List<PrivifyFile> expandDirectories(List<PrivifyFile> files) {
        List<PrivifyFile> expandedFiles = new ArrayList<>();

        for (final PrivifyFile file : files) {
            if (file.isDirectory()) {
                expandedFiles.addAll(expandDirectories(file.getFiles()));
            } else {
                expandedFiles.add(file);
            }
        }

        return expandedFiles;
    }
}
