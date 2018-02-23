/**
 * Copyright 2018 Kartik Arora
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.kartikarora.transfersh.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.apache.commons.io.FilenameUtils;

import me.kartikarora.transfersh.BuildConfig;
import me.kartikarora.transfersh.R;
import me.kartikarora.transfersh.adapters.FileGridAdapter;
import me.kartikarora.transfersh.applications.TransferApplication;
import me.kartikarora.transfersh.helpers.UtilsHelper;

/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.activities
 * Project : Transfer.sh
 * Date : 29/6/16
 */
public class DownloadActivity extends AppCompatActivity {
    private static final int PERM_REQUEST_CODE = BuildConfig.VERSION_CODE / 10000;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String name, type, url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        final CoordinatorLayout layout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        Intent intent = getIntent();
        url = intent.getData().toString();
        name = FilenameUtils.getName(url);
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        TransferApplication application = (TransferApplication) getApplication();
        mFirebaseAnalytics = application.getDefaultTracker();

        new AlertDialog.Builder(DownloadActivity.this)
                .setMessage(getString(R.string.download_file) + " " + getString(R.string.app_name) + "?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkForDownload(name, type, url, layout);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .create().show();

        UtilsHelper.getInstance().trackEvent(mFirebaseAnalytics, "Activity : " + this.getClass().getSimpleName(), "Launched");


    }

    private void beginDownload(String name, String type, String url) {
        UtilsHelper.getInstance().trackEvent(mFirebaseAnalytics, "Action", "Download : " + url);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDescription(getString(R.string.app_name));
        request.setTitle(name);
        String dir = "/" + getString(R.string.app_name) + "/" + type + "/" + name;
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, dir);
        manager.enqueue(request);
    }

    private void showRationale(View view) {
        Snackbar.make(view, R.string.permission_message, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showPermissionDialog();
                    }
                }).show();
    }

    private void showPermissionDialog() {
        ActivityCompat.requestPermissions(DownloadActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERM_REQUEST_CODE);
    }

    private void checkForDownload(String name, String type, String url, View view) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            beginDownload(name, type, url);
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(DownloadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                showRationale(view);
            else {
                showPermissionDialog();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FileGridAdapter.PERM_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                beginDownload(name, type, url);
        }
    }
}
