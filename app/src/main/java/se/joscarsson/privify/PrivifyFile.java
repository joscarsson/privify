package se.joscarsson.privify;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import java.io.File;
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
        return this.nativeFile.getName();
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

    String getEncryptedPath() {
        return isEncrypted() ? this.nativeFile.getAbsolutePath() : this.nativeFile.getAbsolutePath() + ".pri";
    }

    String getPath() {
        String path = this.nativeFile.getAbsolutePath();
        return isEncrypted() ? path.substring(0, path.length() - 4) : path;
    }

    @Override
    public int compareTo(@NonNull PrivifyFile o) {
        String thisName = this.nativeFile.getName();
        String otherName = o.nativeFile.getName();
        return thisName.compareToIgnoreCase(otherName);
    }
}
