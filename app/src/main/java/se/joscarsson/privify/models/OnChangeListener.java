package se.joscarsson.privify.models;

import java.util.List;

import se.joscarsson.privify.models.PrivifyFile;

public interface OnChangeListener {
    void onSelectionChanged(List<PrivifyFile> selectedFiles);
}
