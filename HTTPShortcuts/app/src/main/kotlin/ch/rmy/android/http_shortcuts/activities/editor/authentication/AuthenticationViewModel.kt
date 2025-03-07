package ch.rmy.android.http_shortcuts.activities.editor.authentication

import android.app.Application
import android.net.Uri
import android.text.InputType
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.authentication.usecases.CopyCertificateFileUseCase
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.usecases.GetVariablePlaceholderPickerDialogUseCase
import ch.rmy.android.http_shortcuts.usecases.KeepVariablePlaceholderProviderUpdatedUseCase
import javax.inject.Inject

class AuthenticationViewModel(application: Application) : BaseViewModel<Unit, AuthenticationViewState>(application), WithDialog {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var keepVariablePlaceholderProviderUpdated: KeepVariablePlaceholderProviderUpdatedUseCase

    @Inject
    lateinit var copyCertificateFile: CopyCertificateFileUseCase

    @Inject
    lateinit var getVariablePlaceholderPickerDialog: GetVariablePlaceholderPickerDialogUseCase

    init {
        getApplicationComponent().inject(this)
    }

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun onInitializationStarted(data: Unit) {
        finalizeInitialization(silent = true)
    }

    override fun initViewState() = AuthenticationViewState()

    override fun onInitialized() {
        temporaryShortcutRepository.getTemporaryShortcut()
            .subscribe(
                ::initViewStateFromShortcut,
                ::onInitializationError,
            )
            .attachTo(destroyer)

        keepVariablePlaceholderProviderUpdated(::emitCurrentViewState)
            .attachTo(destroyer)
    }

    private fun initViewStateFromShortcut(shortcut: ShortcutModel) {
        updateViewState {
            copy(
                authenticationType = shortcut.authenticationType,
                username = shortcut.username,
                password = shortcut.password,
                token = shortcut.authToken,
                clientCertParams = shortcut.clientCertParams,
                isClientCertButtonEnabled = !shortcut.acceptAllCertificates,
            )
        }
    }

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onAuthenticationTypeChanged(authenticationType: ShortcutAuthenticationType) {
        updateViewState {
            copy(
                authenticationType = authenticationType,
            )
        }
        performOperation(
            temporaryShortcutRepository.setAuthenticationType(authenticationType)
        )
    }

    fun onUsernameChanged(username: String) {
        updateViewState {
            copy(username = username)
        }

        performOperation(
            temporaryShortcutRepository.setUsername(username)
        )
    }

    fun onPasswordChanged(password: String) {
        updateViewState {
            copy(password = password)
        }

        performOperation(
            temporaryShortcutRepository.setPassword(password)
        )
    }

    fun onTokenChanged(token: String) {
        updateViewState {
            copy(token = token)
        }

        performOperation(
            temporaryShortcutRepository.setToken(token)
        )
    }

    fun onClientCertParamsChanged(clientCertParams: ClientCertParams?) {
        updateViewState {
            copy(clientCertParams = clientCertParams)
        }
        performOperation(
            temporaryShortcutRepository.setClientCertParams(clientCertParams)
        )
    }

    fun onClientCertButtonClicked() {
        doWithViewState { viewState ->
            if (viewState.clientCertParams == null) {
                showClientCertDialog()
            } else {
                onClientCertParamsChanged(null)
            }
        }
    }

    private fun showClientCertDialog() {
        dialogState = DialogState.create {
            title(R.string.title_client_cert)
                .item(R.string.label_client_cert_from_os, descriptionRes = R.string.label_client_cert_from_os_subtitle) {
                    promptForClientCertAlias()
                }
                .item(R.string.label_client_cert_from_file, descriptionRes = R.string.label_client_cert_from_file_subtitle) {
                    openCertificateFilePicker()
                }
                .build()
        }
    }

    private fun promptForClientCertAlias() {
        emitEvent(AuthenticationEvent.PromptForClientCertAlias)
    }

    private fun openCertificateFilePicker() {
        emitEvent(AuthenticationEvent.OpenCertificateFilePicker)
    }

    fun onCertificateFileSelected(file: Uri) {
        copyCertificateFile(file)
            .compose(progressMonitor.singleTransformer())
            .subscribe(
                ::promptForPassword,
                ::handleUnexpectedError,
            )
            .attachTo(destroyer)
    }

    private fun promptForPassword(fileName: String) {
        dialogState = DialogState.create {
            title(R.string.title_client_cert_file_password)
                .textInput(
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                ) { password ->
                    onClientCertParamsChanged(
                        ClientCertParams.File(fileName, password)
                    )
                }
                .build()
        }
    }

    fun onBackPressed() {
        waitForOperationsToFinish {
            finish()
        }
    }

    fun onUsernameVariableButtonClicked() {
        showVariableDialog {
            AuthenticationEvent.InsertVariablePlaceholderForUsername(it)
        }
    }

    fun onPasswordVariableButtonClicked() {
        showVariableDialog {
            AuthenticationEvent.InsertVariablePlaceholderForPassword(it)
        }
    }

    fun onTokenVariableButtonClicked() {
        showVariableDialog {
            AuthenticationEvent.InsertVariablePlaceholderForToken(it)
        }
    }

    private fun showVariableDialog(onVariablePicked: (VariablePlaceholder) -> AuthenticationEvent) {
        dialogState = getVariablePlaceholderPickerDialog.invoke(
            onVariableSelected = {
                emitEvent(onVariablePicked(it))
            },
            onEditVariableButtonClicked = {
                openActivity(
                    VariablesActivity.IntentBuilder()
                )
            },
        )
    }
}
