package me.kartikarora.transfersh.presenter

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.google.android.gms.ads.AdRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.analytics.FirebaseAnalytics
import me.kartikarora.transfersh.R
import me.kartikarora.transfersh.actions.IntentAction
import me.kartikarora.transfersh.activities.AboutActivity
import me.kartikarora.transfersh.activities.SplashActivity.PREF_URL_FLAG
import me.kartikarora.transfersh.activities.TransferActivity.PREF_GRID_VIEW_FLAG
import me.kartikarora.transfersh.adapters.FileGridAdapter
import me.kartikarora.transfersh.applications.TransferApplication
import me.kartikarora.transfersh.contracts.TransferActivityContract
import me.kartikarora.transfersh.custom.CountingTypedFile
import me.kartikarora.transfersh.helpers.UtilsHelper
import me.kartikarora.transfersh.models.TransferActivityModel
import me.kartikarora.transfersh.network.NetworkResponseListener
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.File
import java.util.*


public class TransferActivityPresenter(view: TransferActivityContract.View)
    : TransferActivityContract.Presenter, LoaderManager.LoaderCallbacks<Cursor> {
    private val mModel: TransferActivityContract.Model
    private lateinit var mFirebaseAnalyticsTracker: FirebaseAnalytics
    private val mView: TransferActivityContract.View = view

    init {
        mModel = TransferActivityModel()
        mModel.injectContext(mView.getContextFromView())
        mView.initView()
    }

    override fun onCreate(application: TransferApplication) {
        mFirebaseAnalyticsTracker = application.defaultTracker
        mView.loadDataFromLoader()

    }

    override fun onResume(intent: Intent) {
        buildAdRequest()
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val dataUri: Uri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
                initiateFileUploadFromUri(Objects.requireNonNull(dataUri))
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val dataUris: ArrayList<Uri> = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
                for (uri in Objects.requireNonNull(dataUris)) {
                    initiateFileUploadFromUri(uri)
                }
            }
            IntentAction.ACTION_REUPLOAD -> {
                val id: Long = intent.getLongExtra("file_id", -1)
                if (id != -1L) {
                    val uri = mModel.getUriFromFileIdToReuploadFile(id)
                    initiateFileUploadFromUri(uri)
                    mView.notifyAdapterOfDataSetChange()
                }
            }
        }
    }

    override fun checkIfServerIsResponsive(serverUrl: String) {
        mView.showPleaseWaitDialog()
        mModel.pingServerForResponse(serverUrl, object : NetworkResponseListener {
            override fun success(response: Response) {
                if (response.status == 200) {
                    val headerList = response.headers
                    for (header in headerList) {
                        if (!TextUtils.isEmpty(header.name) && header.name.equals("server", ignoreCase = true)) {
                            val value = header.value
                            if (value.toLowerCase().contains("transfer.sh")) {
                                mModel.storeStringInPreference(PREF_URL_FLAG, serverUrl)
                                mView.hidePleaseWaitDialog()
                            } else {
                                mView.hidePleaseWaitDialog()
                                mView.showServerUrlChangeAlertDialog(serverUrl)
                            }
                        }
                    }
                }
            }

            override fun failure(error: RetrofitError) {
                mView.hidePleaseWaitDialog()
                mView.showServerUrlChangeAlertDialog(serverUrl)
            }
        })
    }

    override fun initiateFileUploadFromUri(uri: Uri) {
        val baseUrl = mModel.getStringFromPreference((PREF_URL_FLAG))
        val fileToUpload: File = mModel.getFileFromContentResolverUsingUri(uri)
        val name = fileToUpload.name
        mView.setUploadingFileNameInBottomSheet(name)
        val mimeType: String = mModel.getMimeTypeOfFileUsingUriFromContentResolver(uri)
        val multipartTypedOutput = mModel.createMultipartDataFromFileToUpload(fileToUpload, mimeType,
                CountingTypedFile.FileUploadListener {
                    val percentage = mModel.getPercentageFromValue(it, fileToUpload.length())
                    mView.setUploadProgressPercentage(percentage)
                })
        mView.setBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
        mModel.uploadMultipartTypedOutputToRemoteServer(baseUrl, name, multipartTypedOutput,
                object : NetworkResponseListener {
                    override fun success(response: Response) {
                        val shareableUrl = mModel.getShareableUrlFromResponse(response)
                        mModel.saveUploadedFileMetaToDatabase(fileToUpload, shareableUrl, uri)
                        mView.showSnackbarWithAction("$name.toString() $mView.getStringFromResource(R.string.uploaded)",
                                R.string.share, Intent().setAction(Intent.ACTION_SEND)
                                .putExtra(Intent.EXTRA_TEXT, shareableUrl)
                                .setType("text/plain")
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        mView.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
                    }

                    override fun failure(error: RetrofitError) {
                        error.printStackTrace()
                        mView.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
                        mView.setUploadProgressPercentage(0)
                        mView.setUploadingFileNameInBottomSheet("")
                        mView.showSnackbar(R.string.something_went_wrong)
                    }
                })
    }


    override fun initiateServerUrlChange() {
        val serverURL = mModel.getStringFromPreference(PREF_URL_FLAG)
        mView.showServerUrlChangeAlertDialog(serverURL)
    }

    override fun buildAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return mModel.getCursorLoader()
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        val adapter = FileGridAdapter(mView.getActivityFromView(), data, mFirebaseAnalyticsTracker, mModel.getBooleanFromPreference(PREF_GRID_VIEW_FLAG))
        mView.initGrid(adapter)
        if (null != data && data.count == 0) {
            mView.hideGridView()
        } else {
            mView.showGridView()
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mView.resetGrid()
    }

    override fun getLoaderCallbacks(): LoaderManager.LoaderCallbacks<Cursor> {
        return this
    }

    override fun snackBarActionClicked(intent: Intent) {
        mModel.fireIntent(intent)
    }

    override fun optionsItemSelected(itemId: Int) {
        when (itemId) {
            R.id.action_about -> {
                mModel.fireIntent(AboutActivity.createIntent(mView.getContextFromView()))
            }
            R.id.action_view_grid -> {
                mModel.storeBooleanInPreference(PREF_GRID_VIEW_FLAG, true)
                mView.setColumnCountOfGridView(mView.getIntFromResource(R.integer.column_count))
            }
            R.id.action_view_list -> {
                mModel.storeBooleanInPreference(PREF_GRID_VIEW_FLAG, false)
                mView.setColumnCountOfGridView(1)
            }
            R.id.action_set_server_url -> {
                initiateServerUrlChange()
            }
        }
    }

    override fun computeCorrectMenuItem(menu: Menu) {
        val showAsGrid = mModel.getBooleanFromPreference(PREF_GRID_VIEW_FLAG)
        menu.getItem(0).isVisible = !showAsGrid
        menu.getItem(1).isVisible = showAsGrid
    }

    override fun onPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == FileGridAdapter.PERM_REQUEST_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mView.tellAdapterThatPermissionWasGranted()
            }
        }
    }

    override fun trackEvent(vararg params: String) {
        UtilsHelper.getInstance().trackEvent(mFirebaseAnalyticsTracker, *params)
    }
}