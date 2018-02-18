package se.joscarsson.privify;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
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
    static final PrivifyFile ROOT = new PrivifyFile(Environment.getExternalStorageDirectory());

    private File nativeFile;

    private PrivifyFile(File nativeFile) {
        this.nativeFile = nativeFile;
    }

    PrivifyFile(String path) {
        this.nativeFile = new File(path);
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

    String getPath() {
        return this.nativeFile.getAbsolutePath();
    }

    Uri getUri(Context context) {
        String authority = "se.joscarsson.privify.provider." + context.getPackageName();
        return FileProvider.getUriForFile(context, authority, this.nativeFile);
    }

    boolean isUpFromRoot() {
        return this.equals(ROOT.getParent());
    }

    boolean isRoot() {
        return this.equals(ROOT);
    }

    boolean isDirectory() {
        return this.nativeFile.isDirectory();
    }

    boolean isEncrypted() { return this.nativeFile.getName().endsWith(".pri"); }

    boolean exists() {
        return this.nativeFile.exists();
    }

    void delete() {
        delete(false);
    }

    void delete(boolean ignoreError) {
        boolean result = this.nativeFile.delete();
        if (!result && !ignoreError) throw new RuntimeException("Failed to delete file.");
    }

    PrivifyFile asEncrypted(PrivifyFile targetDirectory) {
        String path;

        if (targetDirectory == null) {
            path = this.nativeFile.getAbsolutePath() + ".pri";
        } else {
            path = targetDirectory.getPath() + File.separator + this.nativeFile.getName() + ".pri";
        }

        return new PrivifyFile(new File(path));
    }

    PrivifyFile asPlain() {
        String path = this.nativeFile.getAbsolutePath();
        path = path.substring(0, path.length() - 4);
        return new PrivifyFile(new File(path));
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
        String thisPath = this.nativeFile.getAbsolutePath();
        String otherPath = o.nativeFile.getAbsolutePath();
        return thisPath.compareToIgnoreCase(otherPath);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PrivifyFile) return compareTo((PrivifyFile)obj) == 0;
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
