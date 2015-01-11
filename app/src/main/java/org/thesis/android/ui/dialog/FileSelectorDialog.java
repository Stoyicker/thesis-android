package org.thesis.android.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import org.thesis.android.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad). All rights to him.
 */
public class FileSelectorDialog extends DialogFragment implements MaterialDialog.ListCallback {

    private File parentFolder;
    private File[] parentContents;
    private Boolean canGoUp = Boolean.TRUE;
    private IOnFolderSelectionListener mCallback;

    public interface IOnFolderSelectionListener {
        void onFileSelection(File folder);
    }

    public FileSelectorDialog() {
        parentFolder = Environment.getExternalStorageDirectory();
        parentContents = listFiles();
    }

    @Nullable
    private File findFileByName(String name) {
        for (File x : parentContents)
            if (x.getName().contentEquals(name)) {
                return x;
            }
        return null;
    }

    private String[] getContentsArray() {
        String[] results = new String[parentContents.length + (canGoUp ? 1 : 0)];
        if (canGoUp) results[0] = "..";
        for (int i = 0; i < parentContents.length; i++)
            results[canGoUp ? i + 1 : i] = parentContents[i].getName();
        return results;
    }

    private File[] listFiles() {
        File[] contents = parentFolder.listFiles();
        List<File> results = new ArrayList<>();
        Collections.addAll(results, contents);
        Collections.sort(results, new FileSorter());
        return results.toArray(new File[results.size()]);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .title(parentFolder.getAbsolutePath())
                .negativeText(android.R.string.cancel)
                .titleColor(R.color.material_purple_900)
                .negativeColorRes(R.color.material_purple_900)
                .backgroundColor(android.R.color.white)
                .items(getContentsArray())
                .itemsCallback(this)
                .autoDismiss(Boolean.FALSE)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .build();
    }

    @Override
    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence s) {
        final File selectedFile = findFileByName(getContentsArray()[i]);
        if (selectedFile == null && i != 0) {//If i is 0 it's the up indicator
            materialDialog.dismiss();
            return;
        }
        if (selectedFile != null && !selectedFile.isDirectory() && !(canGoUp && i == 0)) {
            materialDialog.dismiss();
            if (mCallback != null)
                mCallback.onFileSelection(selectedFile);
            return;
        }
        if (canGoUp && i == 0) {
            parentFolder = parentFolder.getParentFile();
            canGoUp = parentFolder.getParent() != null;
        } else {
            parentFolder = parentContents[canGoUp ? i - 1 : i];
            canGoUp = Boolean.TRUE;
        }
        parentContents = listFiles();
        MaterialDialog dialog = (MaterialDialog) getDialog();
        dialog.setTitle(parentFolder.getAbsolutePath());
        dialog.setItems(getContentsArray());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (IOnFolderSelectionListener) activity;
    }

    public void show(Activity context) {
        show(context.getFragmentManager(), "FOLDER_SELECTOR");
    }


    private static class FileSorter implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            if (!lhs.isDirectory() && rhs.isDirectory()) {
                return 1;
            }
            if (lhs.isDirectory() && !rhs.isDirectory()) {
                return -1;
            }
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}
