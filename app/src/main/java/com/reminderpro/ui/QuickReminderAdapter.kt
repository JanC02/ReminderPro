package com.reminderpro.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reminderpro.R
import com.reminderpro.databinding.ItemQuickReminderBinding

/**
 * Model dla szybkiego przypomnienia.
 */
data class QuickReminderTemplate(
    val title: String,
    val message: String,
    val intervalMinutes: Int,
    val iconRes: Int,
    val categoryIcon: String
)

/**
 * Adapter dla panelu szybkiego wyboru przypomnień.
 */
class QuickReminderAdapter(
    private val templates: List<QuickReminderTemplate>,
    private val onTemplateClick: (QuickReminderTemplate) -> Unit
) : RecyclerView.Adapter<QuickReminderAdapter.QuickReminderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickReminderViewHolder {
        val binding = ItemQuickReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuickReminderViewHolder(binding, onTemplateClick)
    }

    override fun onBindViewHolder(holder: QuickReminderViewHolder, position: Int) {
        holder.bind(templates[position])
    }

    override fun getItemCount(): Int = templates.size

    class QuickReminderViewHolder(
        private val binding: ItemQuickReminderBinding,
        private val onTemplateClick: (QuickReminderTemplate) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(template: QuickReminderTemplate) {
            binding.apply {
                iconQuick.setImageResource(template.iconRes)
                textQuickTitle.text = template.title
                textQuickInterval.text = formatInterval(template.intervalMinutes)

                root.setOnClickListener {
                    onTemplateClick(template)
                }
            }
        }

        private fun formatInterval(minutes: Int): String {
            return when {
                minutes < 60 -> "co ${minutes}min"
                minutes % 1440 == 0 -> {
                    val days = minutes / 1440
                    "co ${days}d"
                }
                minutes % 60 == 0 -> {
                    val hours = minutes / 60
                    "co ${hours}h"
                }
                else -> {
                    val hours = minutes / 60
                    val mins = minutes % 60
                    "co ${hours}h ${mins}min"
                }
            }
        }
    }
}
