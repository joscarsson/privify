package se.joscarsson.privify;

import java.util.List;

public interface OnChangeListener {
    void onSelectionChanged(List<PrivifyFile> selectedFiles);
}
