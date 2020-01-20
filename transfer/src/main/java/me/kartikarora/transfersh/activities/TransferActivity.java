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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.ContentLoadingProgressBar;
import me.kartikarora.transfersh.BuildConfig;
import me.kartikarora.transfersh.R;
import me.kartikarora.transfersh.adapters.FileGridAdapter;
import me.kartikarora.transfersh.applications.TransferApplication;
import me.kartikarora.transfersh.contracts.TransferActivityContract;
import me.kartikarora.transfersh.presenter.TransferActivityPresenter;

/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.activities
 * Project : Transfer.sh
 * Date : 9/6/16
 */
public class TransferActivity extends AppCompatActivity implements TransferActivityContract.View {

    private static final int FILE_RESULT_CODE = BuildConfig.VERSION_CODE / 10000;
    public static final String PREF_GRID_VIEW_FLAG = "gridFlag";
    private CoordinatorLayout mCoordinatorLayout;
    private TextView mNoFilesTextView;
    private GridView mFileItemsGridView;
    private FileGridAdapter mAdapter;
    private AdView mAdView;
    private BottomSheetBehavior<ConstraintLayout> mUploadBottomSheetBehavior;
    private FloatingActionButton mUploadFileButton;
    private TextView mNameTextView;
    private TextView mPercentTextView;
    private ContentLoadingProgressBar mProgressBar;
    private ProgressDialog pleaseWaitDialog;
    private AlertDialog serverUrlChangeDialog;
    private TransferActivityContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        mPresenter = new TransferActivityPresenter(this);
        mPresenter.onCreate((TransferApplication) getApplication());
    }

    @Override
    public void initView() {
        mNoFilesTextView = findViewById(R.id.no_files_text_view);
        mFileItemsGridView = findViewById(R.id.file_grid_view);
        mUploadFileButton = findViewById(R.id.upload_file_fab);
        mCoordinatorLayout = findViewById(R.id.coordinator_layout);
        mAdView = findViewById(R.id.banner_ad_view);
        ConstraintLayout uploadBottomSheet = findViewById(R.id.bottom_sheet);
        mNameTextView = uploadBottomSheet.findViewById(R.id.uploading_file_text_view);
        mPercentTextView = uploadBottomSheet.findViewById(R.id.uploading_percent_text_view);
        mProgressBar = uploadBottomSheet.findViewById(R.id.file_upload_progress_bar);
        mUploadBottomSheetBehavior = BottomSheetBehavior.from(uploadBottomSheet);
        mUploadBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void loadDataFromLoader() {
        getSupportLoaderManager().initLoader(BuildConfig.VERSION_CODE, null, mPresenter.getLoaderCallbacks());

        mPresenter.trackEvent("Activity : " + this.getClass().getSimpleName(), "Launched");

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
                // NOOP
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
        mPresenter.onResume(getIntent());
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPresenter.onPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void tellAdapterThatPermissionWasGranted() {
        mAdapter.getPermissionRequestResult().onPermitted();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transfer, menu);
        mPresenter.computeCorrectMenuItem(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPresenter.optionsItemSelected(item.getItemId());
        invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initGrid(@NotNull FileGridAdapter adapter) {
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
        mAdapter = adapter;
        mFileItemsGridView.setAdapter(mAdapter);
    }

    @Override
    public void showGridView() {
        mFileItemsGridView.setVisibility(View.VISIBLE);
        mNoFilesTextView.setVisibility(View.GONE);
    }

    @Override
    public void hideGridView() {
        mFileItemsGridView.setVisibility(View.GONE);
        mNoFilesTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void resetGrid() {
        mFileItemsGridView.setVisibility(View.VISIBLE);
        mNoFilesTextView.setVisibility(View.GONE);
        mAdapter.swapCursor(null);
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

    @Override
    public void loadAdRequest(@NotNull AdRequest adRequest) {
        mAdView.loadAd(adRequest);
    }

    @NotNull
    @Override
    public String getStringFromResource(int resource) {
        return getString(resource);
    }

    @Override
    public void showSnackbarWithAction(@NotNull String text, int actionString, final Intent intent) {
        Snackbar.make(mCoordinatorLayout, text, Snackbar.LENGTH_INDEFINITE)
                .setAction(actionString, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPresenter.snackBarActionClicked(intent);
                    }
                }).show();
    }

    @Override
    public int getIntFromResource(int resource) {
        return getResources().getInteger(R.integer.column_count);
    }

    @Override
    public void setColumnCountOfGridView(int columnCount) {
        mFileItemsGridView.setNumColumns(columnCount);
    }

    @NotNull
    @Override
    public AppCompatActivity getActivityFromView() {
        return this;
    }

    @Override
    public void notifyAdapterOfDataSetChange() {
        mAdapter.notifyDataSetChanged();
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

    public int getColumnCount() {
        return mFileItemsGridView.getNumColumns();
    }
}