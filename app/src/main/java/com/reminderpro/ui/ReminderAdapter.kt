package com.reminderpro.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reminderpro.R
import com.reminderpro.data.Reminder
import com.reminderpro.databinding.ItemReminderBinding

/**
 * Adapter dla RecyclerView wyświetlający listę przypomnień.
 */
class ReminderAdapter(
    private val onToggleEnabled: (Reminder, Boolean) -> Unit,
    private val onDelete: (Reminder) -> Unit
) : ListAdapter<Reminder, ReminderAdapter.ReminderViewHolder>(ReminderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReminderViewHolder(binding, onToggleEnabled, onDelete)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder dla pojedynczego przypomnienia.
     */
    class ReminderViewHolder(
        private val binding: ItemReminderBinding,
        private val onToggleEnabled: (Reminder, Boolean) -> Unit,
        private val onDelete: (Reminder) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: Reminder) {
            binding.apply {
                // Tytuł
                textTitle.text = reminder.title

                // Treść
                textMessage.text = reminder.message

                // Interwał
                chipInterval.text = reminder.getFormattedInterval()

                // Przełącznik włącz/wyłącz
                switchEnabled.isChecked = reminder.isEnabled
                switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                    onToggleEnabled(reminder, isChecked)
                }

                // Przycisk usuwania
                buttonDelete.setOnClickListener {
                    onDelete(reminder)
                }

                // Ikona kategorii
                val iconRes = when (reminder.categoryIcon) {
                    "water" -> R.drawable.ic_water
                    "stretch" -> R.drawable.ic_stretch
                    "screen" -> R.drawable.ic_screen
                    "vitamins" -> R.drawable.ic_vitamins
                    "exercise" -> R.drawable.ic_exercise
                    "posture" -> R.drawable.ic_posture
                    else -> R.drawable.ic_notification
                }
                iconCategory.setImageResource(iconRes)
            }
        }
    }

    /**
     * DiffUtil callback dla optymalizacji aktualizacji listy.
     */
    class ReminderDiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem == newItem
        }
    }
}
