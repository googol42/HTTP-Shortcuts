package ch.rmy.android.http_shortcuts.activities.variables.editor.types.toggle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.databinding.ToggleOptionEditorItemBinding
import ch.rmy.android.http_shortcuts.databinding.VariableEditorToggleBinding
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import javax.inject.Inject

class ToggleTypeFragment : BaseFragment<VariableEditorToggleBinding>() {

    @Inject
    lateinit var variableViewUtils: VariableViewUtils

    @Inject
    lateinit var adapter: ToggleVariableOptionsAdapter

    private val viewModel: ToggleTypeViewModel by bindViewModel()

    private var isDraggingEnabled = false

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize()
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorToggleBinding.inflate(inflater, container, false)

    override fun setupViews() {
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding.toggleOptionsList.layoutManager = LinearLayoutManager(context)
        binding.toggleOptionsList.adapter = adapter
    }

    private fun initUserInputBindings() {
        initDragOrdering()

        adapter.userEvents.observe(this) { event ->
            when (event) {
                is ToggleVariableOptionsAdapter.UserEvent.OptionClicked -> viewModel.onOptionClicked(event.id)
            }
        }

        binding.toggleOptionsAddButton.setOnClickListener {
            viewModel.onAddButtonClicked()
        }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper(
            isEnabledCallback = { isDraggingEnabled },
            getId = { (it as? ToggleVariableOptionsAdapter.SelectOptionViewHolder)?.optionId },
        )
        dragOrderingHelper.movementSource
            .subscribe { (optionId1, optionId2) ->
                viewModel.onOptionMoved(optionId1, optionId2)
            }
            .attachTo(destroyer)
        dragOrderingHelper.attachTo(binding.toggleOptionsList)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            adapter.items = viewState.options
            isDraggingEnabled = viewState.isDraggingEnabled
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ToggleTypeEvent.ShowAddDialog -> showAddDialog()
            is ToggleTypeEvent.ShowEditDialog -> showEditDialog(event.optionId, event.value)
            else -> super.handleEvent(event)
        }
    }

    private fun showAddDialog() {
        val binding = ToggleOptionEditorItemBinding.inflate(layoutInflater)
        variableViewUtils.bindVariableViews(binding.toggleOptionValue, binding.variableButtonValue)
        DialogBuilder(requireContext())
            .title(R.string.title_add_toggle_option)
            .view(binding.root)
            .positive(R.string.dialog_ok) {
                viewModel.onAddDialogConfirmed(binding.toggleOptionValue.rawString)
            }
            .negative(R.string.dialog_cancel)
            .showIfPossible()
    }

    private fun showEditDialog(optionId: String, value: String) {
        val binding = ToggleOptionEditorItemBinding.inflate(layoutInflater)
        variableViewUtils.bindVariableViews(binding.toggleOptionValue, binding.variableButtonValue)
        binding.toggleOptionValue.rawString = value
        DialogBuilder(requireContext())
            .title(R.string.title_edit_toggle_option)
            .view(binding.root)
            .positive(R.string.dialog_ok) {
                viewModel.onEditDialogConfirmed(optionId, value = binding.toggleOptionValue.rawString)
            }
            .negative(R.string.dialog_cancel)
            .neutral(R.string.dialog_remove) {
                viewModel.onDeleteOptionSelected(optionId)
            }
            .showIfPossible()
    }
}
