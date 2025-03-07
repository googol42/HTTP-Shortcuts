package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import io.reactivex.Completable
import javax.inject.Inject

class ChangeIconAction(private val iconName: String, private val shortcutNameOrId: ShortcutNameOrId?) : BaseAction() {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var widgetManager: WidgetManager

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun execute(executionContext: ExecutionContext): Completable =
        changeIcon(executionContext.context, this.shortcutNameOrId ?: executionContext.shortcutId)

    private fun changeIcon(context: Context, shortcutNameOrId: ShortcutNameOrId): Completable {
        val newIcon = ShortcutIcon.fromName(iconName)
        return shortcutRepository.getShortcutByNameOrId(shortcutNameOrId)
            .flatMapCompletable { shortcut ->
                shortcutRepository.setIcon(shortcut.id, newIcon)
                    .andThen(
                        Completable.fromAction {
                            val launcherShortcutManager = LauncherShortcutManager(context)
                            if (launcherShortcutManager.supportsPinning()) {
                                launcherShortcutManager.updatePinnedShortcut(
                                    shortcutId = shortcut.id,
                                    shortcutName = shortcut.name,
                                    shortcutIcon = newIcon,
                                )
                            }
                        }
                    )
                    .andThen(widgetManager.updateWidgets(context, shortcut.id))
            }
            .onErrorResumeNext { error ->
                if (error is NoSuchElementException) {
                    Completable
                        .error(
                            ActionException {
                                it.getString(R.string.error_shortcut_not_found_for_changing_icon, shortcutNameOrId)
                            }
                        )
                } else {
                    Completable.error(error)
                }
            }
    }
}
