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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.ContentLoadingProgressBar;
import me.kartikarora.potato.Potato;
import me.kartikarora.transfersh.BuildConfig;
import me.kartikarora.transfersh.R;
import me.kartikarora.transfersh.actions.IntentAction;
import me.kartikarora.transfersh.adapters.FileGridAdapter;
import me.kartikarora.transfersh.applications.TransferApplication;
import me.kartikarora.transfersh.contracts.FilesContract;
import me.kartikarora.transfersh.contracts.TransferActivityContract;
import me.kartikarora.transfersh.helpers.UtilsHelper;
import me.kartikarora.transfersh.network.NetworkResponseListener;
import me.kartikarora.transfersh.presenter.TransferActivityPresenter;

/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.activities
 * Project : Transfer.sh
 * Date : 9/6/16
 */
public class TransferActivity extends AppCompatActivity implements TransferActivityContract.View {

    private static final int FILE_RESULT_CODE = BuildConfig.VERSION_CODE / 10000;
    private static final String PREF_GRID_VIEW_FLAG = "gridFlag";
    private boolean showAsGrid = false;
    private CoordinatorLayout mCoordinatorLayout;
    private TextView mNoFilesTextView;
    private GridView mFileItemsGridView;
    private FileGridAdapter mAdapter;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AdView mAdView;
    private Cursor mData = null;
    private ConstraintLayout mUploadBottomSheet;
    private BottomSheetBehavior<ConstraintLayout> mUploadBottomSheetBehavior;
    private FloatingActionButton mUploadFileButton;
    private TextView mNameTextView;
    private TextView mPercentTextView;
    private ContentLoadingProgressBar mProgressBar;
    private NetworkResponseListener mListener;
    private ProgressDialog pleaseWaitDialog;
    private AlertDialog serverUrlChangeDialog;
    private TransferActivityContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        mPresenter = new TransferActivityPresenter(this);
        mPresenter.requestDataFromLoader();
    }

    @Override
    public void initView() {
        mNoFilesTextView = findViewById(R.id.no_files_text_view);
        mFileItemsGridView = findViewById(R.id.file_grid_view);
        mUploadFileButton = findViewById(R.id.upload_file_fab);
        mCoordinatorLayout = findViewById(R.id.coordinator_layout);
        mAdView = findViewById(R.id.banner_ad_view);
        mUploadBottomSheet = findViewById(R.id.bottom_sheet);
        mNameTextView = mUploadBottomSheet.findViewById(R.id.uploading_file_text_view);
        mPercentTextView = mUploadBottomSheet.findViewById(R.id.uploading_percent_text_view);
        mProgressBar = mUploadBottomSheet.findViewById(R.id.file_upload_progress_bar);
        mUploadBottomSheetBehavior = BottomSheetBehavior.from(mUploadBottomSheet);
        mUploadBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void loadDataFromLoader() {
        getSupportLoaderManager().initLoader(BuildConfig.VERSION_CODE, null, mPresenter.getLoaderCallbacks());
        TransferApplication application = (TransferApplication) getApplication();
        mFirebaseAnalytics = application.getDefaultTracker();

        UtilsHelper.getInstance().trackEvent(mFirebaseAnalytics, "Activity : " + this.getClass().getSimpleName(), "Launched");

        showAsGrid = Potato.potate(TransferActivity.this).Preferences().getSharedPreferenceBoolean(PREF_GRID_VIEW_FLAG);
        mUploadBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (BottomSheetBehavior.STATE_EXPANDED == newState) {
                    mUploadFileButton.animate().scaleX(0).scaleY(0).setDuration(300).start();
                } else if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                    mUploadFileButton.animate().scaleX(1).scaleY(1).setDuration(300).start();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_RESULT_CODE && resultCode == RESULT_OK) {
            mPresenter.initiateFileUploadFromUri(Objects.requireNonNull(data.getData()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.buildAdRequest();
        String action = getIntent().getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            Uri dataUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            mPresenter.initiateFileUploadFromUri(Objects.requireNonNull(dataUri));
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            ArrayList<Uri> dataUris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            for (Uri uri : Objects.requireNonNull(dataUris)) {
                mPresenter.initiateFileUploadFromUri(uri);
            }
        } else if (IntentAction.ACTION_REUPLOAD.equals(action)) {
            long id = getIntent().getLongExtra("file_id", -1);
            if (id != -1) {
                Cursor cursor = getContentResolver().query(FilesContract.BASE_CONTENT_URI, null, FilesContract.FilesEntry._ID + "=?", new String[]{String.valueOf(id)}, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        Uri uri = Uri.parse(getString(cursor.getColumnIndex(FilesContract.FilesEntry.COLUMN_URI)));
                        mPresenter.initiateFileUploadFromUri(uri);
                        getContentResolver().delete(FilesContract.BASE_CONTENT_URI, FilesContract.FilesEntry._ID + "=?",
                                new String[]{String.valueOf(id)});
                        mAdapter.notifyDataSetChanged();
                    }
                    cursor.close();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }


    private void addFileDetailsToDatabaseAndDeleteTemporaryFile(File file, Uri uri, String mimeType, String result) {
        SimpleDateFormat sdf = UtilsHelper.getInstance().getSdf();
        Calendar upCal = Calendar.getInstance();
        upCal.setTime(new Date(file.lastModified()));
        Calendar delCal = Calendar.getInstance();
        delCal.setTime(upCal.getTime());
        delCal.add(Calendar.DATE, 14);
        ContentValues values = new ContentValues();
        values.put(FilesContract.FilesEntry.COLUMN_NAME, file.getName());
        values.put(FilesContract.FilesEntry.COLUMN_TYPE, mimeType);
        values.put(FilesContract.FilesEntry.COLUMN_URL, result);
        values.put(FilesContract.FilesEntry.COLUMN_URI, uri.toString());
        values.put(FilesContract.FilesEntry.COLUMN_SIZE, String.valueOf(file.getTotalSpace()));
        values.put(FilesContract.FilesEntry.COLUMN_DATE_UPLOAD, sdf.format(upCal.getTime()));
        values.put(FilesContract.FilesEntry.COLUMN_DATE_DELETE, sdf.format(delCal.getTime()));
        getContentResolver().insert(FilesContract.BASE_CONTENT_URI, values);
        FileUtils.deleteQuietly(file);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FileGridAdapter.PERM_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                mAdapter.getPermissionRequestResult().onPermitted();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transfer, menu);
        menu.getItem(0).setVisible(!showAsGrid);
        menu.getItem(1).setVisible(showAsGrid);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (item.getItemId() == R.id.action_view_grid) {
            showAsGrid = true;
            Potato.potate(TransferActivity.this).Preferences().putSharedPreference(PREF_GRID_VIEW_FLAG, true);
            mFileItemsGridView.setNumColumns(getResources().getInteger(R.integer.col_count));
        } else if (item.getItemId() == R.id.action_view_list) {
            showAsGrid = false;
            Potato.potate(TransferActivity.this).Preferences().putSharedPreference(PREF_GRID_VIEW_FLAG, false);
            mFileItemsGridView.setNumColumns(1);
        } else if (item.getItemId() == R.id.action_set_server_url) {
            mPresenter.initiateServerUrlChange();
        }
        invalidateOptionsMenu();
        initGrid(mData);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initGrid(Cursor data) {
        if (mUploadFileButton != null) {
            mUploadFileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("*/*");
                    startActivityForResult(intent, FILE_RESULT_CODE);
                }
            });
        }
        mAdapter = new FileGridAdapter(TransferActivity.this, data, mFirebaseAnalytics, showAsGrid);
        if (showAsGrid) {
            mFileItemsGridView.setNumColumns(getResources().getInteger(R.integer.col_count));
        } else {
            mFileItemsGridView.setNumColumns(1);
        }
        mFileItemsGridView.setAdapter(mAdapter);

        if (null != data && data.getCount() == 0) {
            mFileItemsGridView.setVisibility(View.GONE);
            mNoFilesTextView.setVisibility(View.VISIBLE);
        } else {
            mFileItemsGridView.setVisibility(View.VISIBLE);
            mNoFilesTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void resetGrid() {
        mFileItemsGridView.setVisibility(View.VISIBLE);
        mNoFilesTextView.setVisibility(View.GONE);
        mAdapter.swapCursor(null);
        mData = null;
    }

    @Override
    public void showSnackbar(@NotNull String text) {
        Snackbar.make(mCoordinatorLayout, text, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showSnackbar(int textResource) {
        Snackbar.make(mCoordinatorLayout, getString(textResource), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showSnackbarWithAction(@NotNull String name, @NotNull final String shareableUrl) {
        Snackbar.make(mCoordinatorLayout, name + getString(R.string.uploaded), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.share, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UtilsHelper.getInstance().trackEvent(mFirebaseAnalytics, "Action", "Share : " + shareableUrl);
                        startActivity(new Intent()
                                .setAction(Intent.ACTION_SEND)
                                .putExtra(Intent.EXTRA_TEXT, shareableUrl)
                                .setType("text/plain")
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }).show();
    }

    @Override
    public void setBottomSheetState(int state) {
        mUploadBottomSheetBehavior.setState(state);
    }

    @Override
    public void setUploadingFileNameInBottomSheet(@Nullable String text) {
        mNameTextView.setText(text);
    }

    @Override
    public void setUploadProgressPercentage(int percentage) {
        mPercentTextView.setText(percentage + "%");
        mProgressBar.setProgress(percentage);
    }

    @Override
    public void showPleaseWaitDialog() {
        pleaseWaitDialog = new ProgressDialog(this);
        pleaseWaitDialog.setMessage(getString(R.string.please_wait));
        pleaseWaitDialog.show();
    }

    @Override
    public void hidePleaseWaitDialog() {
        if (pleaseWaitDialog.isShowing()) {
            pleaseWaitDialog.cancel();
            pleaseWaitDialog.dismiss();
        }
    }

    @NotNull
    @Override
    public Context getContextFromView() {
        return this.getApplicationContext();
    }

    @Override
    public void showServerUrlChangeAlertDialog(@NotNull String serverUrl) {
        serverUrlChangeDialog = buildServerUrlChangeDialog(serverUrl);
        serverUrlChangeDialog.show();
    }

    @Override
    public void hideServerUrlChangeAlertDialog() {
        serverUrlChangeDialog.cancel();
    }

    private AlertDialog buildServerUrlChangeDialog(String serverUrl) {
        final String[] url = {serverUrl};
        AlertDialog.Builder builder = new AlertDialog.Builder(TransferActivity.this);
        builder.setTitle(getString(R.string.server_error));
        builder.setMessage(getString(R.string.server_error_message, serverUrl));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setHint(url[0]);
        builder.setView(input);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                url[0] = input.getText().toString();
                if (TextUtils.isEmpty(url[0])) {
                    url[0] = "https://transfer.sh";
                }
                mPresenter.checkIfServerIsResponsive(url[0]);
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                mPresenter.checkIfServerIsResponsive(url[0]);
            }
        });
        return builder.create();
    }

    @Override
    public void loadAdRequest(@NotNull AdRequest adRequest) {
        mAdView.loadAd(adRequest);
    }
}