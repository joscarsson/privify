package se.joscarsson.privify;

import java.util.List;

public interface OnSelectionChangeListener {
    void onSelectionChanged(List<PrivifyFile> selectedFiles);
}
