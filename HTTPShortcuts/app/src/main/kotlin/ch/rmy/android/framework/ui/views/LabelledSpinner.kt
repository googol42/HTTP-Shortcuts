package ch.rmy.android.framework.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.use
import ch.rmy.android.framework.extensions.indexOfFirstOrNull
import ch.rmy.android.framework.extensions.layoutInflater
import ch.rmy.android.http_shortcuts.databinding.LabelledSpinnerBinding
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class LabelledSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    LinearLayoutCompat(context, attrs, defStyleAttr) {

    private val binding = LabelledSpinnerBinding.inflate(layoutInflater, this)

    private val selectionChangeSubject = PublishSubject.create<String>()

    val selectionChanges: Observable<String>
        get() = selectionChangeSubject

    var items: List<Item> = emptyList()
        set(value) {
            field = value
            binding.spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, value.map { it.value ?: it.key })
        }

    fun setItemsFromPairs(items: List<Pair<String, String>>) {
        this.items = items.map { (key, value) -> Item(key, value) }
    }

    fun setItemsFromPairs(vararg items: Pair<String, String>) {
        setItemsFromPairs(items.asList())
    }

    init {
        orientation = VERTICAL
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                selectedItem = items[position].key
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        if (attrs != null) {
            context.obtainStyledAttributes(attrs, ATTRIBUTE_IDS).use { a ->
                binding.label.text = a.getText(ATTRIBUTE_IDS.indexOf(android.R.attr.text)) ?: ""
            }
        }
    }

    var selectedItem: String = ""
        set(value) {
            val index = items
                .indexOfFirstOrNull { it.key == value }
                ?: return
            val before = field
            field = value
            binding.spinner.setSelection(index)
            if (before != value && before.isNotEmpty()) {
                selectionChangeSubject.onNext(value)
            }
        }

    data class Item(val key: String, val value: String? = null)

    companion object {

        private val ATTRIBUTE_IDS = intArrayOf(android.R.attr.text)
    }
}
