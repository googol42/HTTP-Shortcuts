package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class CopyToClipboardAction(private val text: String) : BaseAction() {

    @Inject
    lateinit var clipboardUtil: ClipboardUtil

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable
            .fromAction {
                text
                    .takeUnlessEmpty()
                    ?.let(clipboardUtil::copyToClipboard)
            }
            .subscribeOn(AndroidSchedulers.mainThread())
}
