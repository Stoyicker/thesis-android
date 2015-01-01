package org.thesis.android.io.file;

import org.apache.commons.io.FileUtils;

import java.io.File;

public abstract class FileOperations {
    public static Boolean recursivelyDelete(File cacheDir) {
        return FileUtils.deleteQuietly(cacheDir);
    }
}
