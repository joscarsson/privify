package se.joscarsson.privify.models;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConcretePrivifyFile implements PrivifyFile {
    public static final PrivifyFile ROOT = new ConcretePrivifyFile(Environment.getExternalStorageDirectory());

    private File nativeFile;

    private ConcretePrivifyFile(File nativeFile) {
        this.nativeFile = nativeFile;
    }

    public ConcretePrivifyFile(String path) {
        this.nativeFile = new File(path);
    }

    @Override
    public List<PrivifyFile> getFiles() {
        File[] nativeFiles = this.nativeFile.listFiles();
        List<PrivifyFile> files = new ArrayList<>();

        for (File f : nativeFiles) {
            files.add(new ConcretePrivifyFile(f));
        }

        Collections.sort(files);
        return files;
    }

    @Override
    public PrivifyFile getParent() {
        return new ConcretePrivifyFile(this.nativeFile.getParentFile());
    }

    @Override
    public String getName() {
        String name = this.nativeFile.getName();
        return isEncrypted() ? name.substring(0, name.length() - 4) : name;
    }

    @Override
    public String getPath() {
        return this.nativeFile.getAbsolutePath();
    }

    @Override
    public Uri getUri(Context context) {
        String authority = "se.joscarsson.privify.provider." + context.getPackageName();
        return FileProvider.getUriForFile(context, authority, this.nativeFile);
    }

    @Override
    public boolean isUpFromRoot() {
        return this.equals(ROOT.getParent());
    }

    @Override
    public boolean isRoot() {
        return this.equals(ROOT);
    }

    @Override
    public boolean isDirectory() {
        return this.nativeFile.isDirectory();
    }

    @Override
    public boolean isEncrypted() { return this.nativeFile.getName().endsWith(".pri"); }

    @Override
    public boolean exists() {
        return this.nativeFile.exists();
    }

    @Override
    public void delete() {
        delete(false);
    }

    @Override
    public void delete(boolean ignoreError) {
        boolean result = this.nativeFile.delete();
        if (!result && !ignoreError) throw new RuntimeException("Failed to delete file.");
    }

    @Override
    public PrivifyFile asEncrypted(PrivifyFile targetDirectory) {
        String path;

        if (targetDirectory == null) {
            path = this.nativeFile.getAbsolutePath() + ".pri";
        } else {
            path = targetDirectory.getPath() + File.separator + this.nativeFile.getName() + ".pri";
        }

        return new ConcretePrivifyFile(path);
    }

    @Override
    public PrivifyFile asPlain() {
        String path = this.nativeFile.getAbsolutePath();
        path = path.substring(0, path.length() - 4);
        return new ConcretePrivifyFile(new File(path));
    }

    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream(this.nativeFile);
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(this.nativeFile);
    }

    @Override
    public long getSize() {
        return this.nativeFile.length();
    }

    @Override
    public int compareTo(@NonNull PrivifyFile o) {
        return getPath().compareToIgnoreCase(o.getPath());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConcretePrivifyFile) return compareTo((ConcretePrivifyFile)obj) == 0;
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.nativeFile.hashCode();
    }

    public static List<PrivifyFile> expandDirectories(List<PrivifyFile> files) {
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
