package me.kartikarora.transfersh.contracts

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.google.android.gms.ads.AdRequest
import me.kartikarora.transfersh.adapters.FileGridAdapter
import me.kartikarora.transfersh.applications.TransferApplication
import me.kartikarora.transfersh.custom.CountingTypedFile
import me.kartikarora.transfersh.network.NetworkResponseListener
import retrofit.client.Response
import retrofit.mime.MultipartTypedOutput
import java.io.File


class TransferActivityContract {

    interface View {
        fun getStringFromResource(resource: Int): String
        fun getIntFromResource(resource: Int): Int
        fun initView()
        fun loadDataFromLoader()
        fun initGrid(adapter: FileGridAdapter)
        fun resetGrid()
        fun getContextFromView(): Context
        fun showPleaseWaitDialog()
        fun hidePleaseWaitDialog()
        fun showServerUrlChangeAlertDialog(serverUrl: String)
        fun hideServerUrlChangeAlertDialog()
        fun showSnackbar(text: String)
        fun showSnackbar(textResource: Int)
        fun showSnackbarWithAction(text: String, actionString: Int, intent: Intent)
        fun setBottomSheetState(state: Int)
        fun setUploadingFileNameInBottomSheet(text: String?)
        fun setUploadProgressPercentage(percentage: Int)
        fun loadAdRequest(adRequest: AdRequest)
        fun setColumnCountOfGridView(columnCount: Int)
        fun getActivityFromView(): AppCompatActivity
        fun tellAdapterThatPermissionWasGranted()
        fun notifyAdapterOfDataSetChange()
        fun showGridView()
        fun hideGridView()

    }

    interface Model {
        fun pingServerForResponse(baseUrl: String, listener: NetworkResponseListener)
        fun getFileFromContentResolverUsingUri(uri: Uri): File
        fun getMimeTypeOfFileUsingUriFromContentResolver(uri: Uri): String
        fun createMultipartDataFromFileToUpload(fileToUpload: File, mimeType: String,
                                                listener: CountingTypedFile.FileUploadListener): MultipartTypedOutput

        fun uploadMultipartTypedOutputToRemoteServer(baseUrl: String, name: String,
                                                     multipartTypedOutput: MultipartTypedOutput,
                                                     listener: NetworkResponseListener)

        fun storeStringInPreference(key: String, value: String)
        fun getStringFromPreference(key: String): String
        fun storeBooleanInPreference(key: String, value: Boolean)
        fun getBooleanFromPreference(key: String): Boolean
        fun getCursorLoader(): Loader<Cursor>
        fun saveUploadedFileMetaToDatabase(uploadedFile: File, shareableUrl: String, uriInContentResolver: Uri)
        fun fireIntent(intent: Intent)
        fun getUriFromFileIdToReuploadFile(id: Long): Uri
        fun getShareableUrlFromResponse(response: Response): String
        fun getPercentageFromValue(value: Long, max: Long): Int
    }

    interface Presenter {
        fun onCreate(application: TransferApplication)
        fun onResume(intent: Intent)
        fun checkIfServerIsResponsive(serverUrl: String)
        fun initiateFileUploadFromUri(uri: Uri)
        fun initiateServerUrlChange()
        fun buildAdRequest(): AdRequest
        fun getLoaderCallbacks(): LoaderManager.LoaderCallbacks<Cursor>
        fun snackBarActionClicked(intent: Intent)
        fun optionsItemSelected(itemId: Int)
        fun computeCorrectMenuItem(menu: Menu)
        fun onPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
        fun trackEvent(vararg params: String)
    }
}