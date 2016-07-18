/**
 * Copyright 2016 Kartik Arora
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

package me.kartikarora.transfersh.adapters;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.commons.io.FilenameUtils;

import me.kartikarora.transfersh.BuildConfig;
import me.kartikarora.transfersh.R;
import me.kartikarora.transfersh.contracts.FilesContract;

/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.adapters
 * Project : Transfer.sh
 * Date : 9/6/16
 */
public class FileGridAdapter extends CursorAdapter {

    public static final int PERM_REQUEST_CODE = BuildConfig.VERSION_CODE / 10000;
    private LayoutInflater inflater;
    private AppCompatActivity activity;
    private Context context;
    private PermissionRequestResult permissionRequestResult;
    private Tracker tracker;
    private Boolean gridViewFlag;

    public FileGridAdapter(AppCompatActivity activity, Cursor cursor, Tracker tracker, Boolean gridViewFlag) {
        super(activity.getApplicationContext(), cursor, false);
        this.context = activity.getApplicationContext();
        this.inflater = LayoutInflater.from(activity);
        this.activity = activity;
        this.tracker = tracker;
        this.gridViewFlag = gridViewFlag;
    }

    public PermissionRequestResult getPermissionRequestResult() {
        return permissionRequestResult;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(gridViewFlag ? R.layout.file_grid_item : R.layout.file_list_item, parent, false);
        FileItemViewHolder holder = new FileItemViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        FileItemViewHolder holder = (FileItemViewHolder) view.getTag();
        int nameCol = cursor.getColumnIndex(FilesContract.FilesEntry.COLUMN_NAME);
        int typeCol = cursor.getColumnIndex(FilesContract.FilesEntry.COLUMN_TYPE);
        int sizeCol = cursor.getColumnIndex(FilesContract.FilesEntry.COLUMN_SIZE);
        int urlCol = cursor.getColumnIndex(FilesContract.FilesEntry.COLUMN_URL);
        final String name = cursor.getString(nameCol);
        final String type = cursor.getString(typeCol);
        final String size = cursor.getString(sizeCol);
        final String url = cursor.getString(urlCol);
        holder.fileNameTextView.setText(name);
        String ext = FilenameUtils.getExtension(name);
        int identifier = context.getResources().getIdentifier("t" + ext, "drawable", context.getPackageName());
        try {
            holder.fileTypeImageView.setImageResource(identifier);
            // holder.fileTypeImageView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), identifier, null));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            holder.fileTypeImageView.setImageResource(R.drawable.tblank);
            //holder.fileTypeImageView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.tblank, null));
        }

        holder.fileInfoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "Name: " + name + "\n" +
                        "File type: " + type + "\n" +
                        "URL: " + url;
                new AlertDialog.Builder(activity)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show();
            }
        });

        holder.fileShareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Share : " + url)
                        .build());
                context.startActivity(new Intent()
                        .setAction(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_TEXT, url)
                        .setType("text/plain")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        holder.fileDownloadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkForDownload(name, type, url, view);
            }
        });
    }

    private class FileItemViewHolder {

        private TextView fileNameTextView;
        private ImageView fileTypeImageView;
        private ImageButton fileInfoImageButton;
        private ImageButton fileShareImageButton;
        private ImageButton fileDownloadImageButton;

        public FileItemViewHolder(View view) {
            fileNameTextView = (TextView) view.findViewById(R.id.file_item_name_text_view);
            fileTypeImageView = (ImageView) view.findViewById(R.id.file_item_type_image_view);
            fileInfoImageButton = (ImageButton) view.findViewById(R.id.file_item_info_image_button);
            fileShareImageButton = (ImageButton) view.findViewById(R.id.file_item_share_image_button);
            fileDownloadImageButton = (ImageButton) view.findViewById(R.id.file_item_download_image_button);
        }
    }

    private void beginDownload(String name, String type, String url) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Download : " + url)
                .build());
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDescription(context.getString(R.string.app_name));
        request.setTitle(name);
        String dir = "/" + context.getString(R.string.app_name) + "/" + type + "/" + name;
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
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERM_REQUEST_CODE);
    }

    private void checkForDownload(final String name, final String type, final String url, View view) {
        this.permissionRequestResult = new PermissionRequestResult() {
            @Override
            public void onPermitted() {
                beginDownload(name, type, url);
            }
        };
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            beginDownload(name, type, url);
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                showRationale(view);
            else {
                showPermissionDialog();
            }
        }
    }

    public interface PermissionRequestResult {
        void onPermitted();
    }
}
