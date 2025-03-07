package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.databinding.ActivityTriggerShortcutsBinding
import ch.rmy.android.http_shortcuts.extensions.applyTheme

class TriggerShortcutsActivity : BaseActivity() {

    private val currentShortcutId: ShortcutId? by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra(EXTRA_SHORTCUT_ID)
    }

    private val viewModel: TriggerShortcutsViewModel by bindViewModel()

    private lateinit var binding: ActivityTriggerShortcutsBinding
    private lateinit var adapter: ShortcutsAdapter

    private var isDraggingEnabled = false

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize(TriggerShortcutsViewModel.InitData(currentShortcutId))
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityTriggerShortcutsBinding.inflate(layoutInflater))
        setTitle(R.string.label_trigger_shortcuts)

        binding.buttonAddTrigger.applyTheme(themeHelper)

        val manager = LinearLayoutManager(context)
        binding.triggerShortcutsList.layoutManager = manager
        binding.triggerShortcutsList.setHasFixedSize(true)

        adapter = ShortcutsAdapter()
        binding.triggerShortcutsList.adapter = adapter
    }

    private fun initUserInputBindings() {
        adapter.userEvents
            .subscribe { event ->
                when (event) {
                    is ShortcutsAdapter.UserEvent.ShortcutClicked -> {
                        viewModel.onShortcutClicked(event.id)
                    }
                }
            }
            .attachTo(destroyer)

        binding.buttonAddTrigger.setOnClickListener {
            viewModel.onAddButtonClicked()
        }
        initDragOrdering()
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper(
            isEnabledCallback = { isDraggingEnabled },
            getId = { (it as? ShortcutsAdapter.ShortcutViewHolder)?.id },
        )
        dragOrderingHelper.attachTo(binding.triggerShortcutsList)
        dragOrderingHelper.movementSource
            .subscribe { (shortcutId1, shortcutId2) ->
                viewModel.onShortcutMoved(shortcutId1, shortcutId2)
            }
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            adapter.items = viewState.shortcuts
            isDraggingEnabled = viewState.isDraggingEnabled
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    class IntentBuilder : BaseIntentBuilder(TriggerShortcutsActivity::class) {

        fun shortcutId(shortcutId: ShortcutId?) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }
    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "shortcutId"
    }
}
