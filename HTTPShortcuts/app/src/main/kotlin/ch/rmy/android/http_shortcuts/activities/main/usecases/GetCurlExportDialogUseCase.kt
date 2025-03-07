package ch.rmy.android.http_shortcuts.activities.main.usecases

import android.content.Context
import android.widget.TextView
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import ch.rmy.curlcommand.CurlCommand
import ch.rmy.curlcommand.CurlConstructor
import com.afollestad.materialdialogs.callbacks.onShow
import javax.inject.Inject

class GetCurlExportDialogUseCase
@Inject
constructor(
    private val clipboardUtil: ClipboardUtil,
) {

    operator fun invoke(title: String, command: CurlCommand): DialogState =
        DialogState.create(DIALOG_ID) {
            val curlCommand = CurlConstructor.toCurlCommandString(command)
            this
                .title(title)
                .view(R.layout.curl_export_dialog)
                .neutral(android.R.string.cancel)
                .negative(R.string.share_button) { shareCurlExport(context, curlCommand) }
                .positive(R.string.button_copy_curl_export) { copyCurlExport(curlCommand) }
                .build()
                .onShow { dialog ->
                    dialog.findViewById<TextView>(R.id.curl_export_textview).text = curlCommand
                }
        }

    private fun shareCurlExport(context: Context, curlCommand: String) {
        ShareUtil.shareText(context, curlCommand)
    }

    private fun copyCurlExport(curlCommand: String) {
        clipboardUtil.copyToClipboard(curlCommand)
    }

    companion object {
        private const val DIALOG_ID = "curl-export"
    }
}
