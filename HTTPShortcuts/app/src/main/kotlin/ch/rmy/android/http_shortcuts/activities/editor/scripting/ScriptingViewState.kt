package ch.rmy.android.http_shortcuts.activities.editor.scripting

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel

data class ScriptingViewState(
    val dialogState: DialogState? = null,
    val shortcuts: List<ShortcutModel>? = null,
    val codeOnPrepare: String = "",
    val codeOnSuccess: String = "",
    val codeOnFailure: String = "",
    val codePrepareMinLines: Int = 6,
    val codePrepareHint: Localizable = Localizable.EMPTY,
    val codePrepareVisible: Boolean = false,
    val postRequestScriptingVisible: Boolean = false,
)
