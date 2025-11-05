package com.reminderpro.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reminderpro.R
import com.reminderpro.data.Reminder
import com.reminderpro.databinding.BottomSheetQuickSelectBinding

/**
 * Bottom Sheet z predefiniowanymi szablonami przypomnień.
 */
class QuickSelectBottomSheet(
    private val onQuickReminderSelected: (Reminder) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetQuickSelectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetQuickSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupQuickReminders()
    }

    /**
     * Konfiguruje listę szybkich przypomnień.
     */
    private fun setupQuickReminders() {
        val templates = listOf(
            QuickReminderTemplate(
                title = getString(R.string.quick_water_title),
                message = getString(R.string.quick_water_message),
                intervalMinutes = 120, // 2 godziny
                iconRes = R.drawable.ic_water,
                categoryIcon = "water"
            ),
            QuickReminderTemplate(
                title = getString(R.string.quick_stretch_title),
                message = getString(R.string.quick_stretch_message),
                intervalMinutes = 30, // 30 minut
                iconRes = R.drawable.ic_stretch,
                categoryIcon = "stretch"
            ),
            QuickReminderTemplate(
                title = getString(R.string.quick_screen_title),
                message = getString(R.string.quick_screen_message),
                intervalMinutes = 20, // 20 minut
                iconRes = R.drawable.ic_screen,
                categoryIcon = "screen"
            ),
            QuickReminderTemplate(
                title = getString(R.string.quick_vitamins_title),
                message = getString(R.string.quick_vitamins_message),
                intervalMinutes = 1440, // Codziennie (24h)
                iconRes = R.drawable.ic_vitamins,
                categoryIcon = "vitamins"
            ),
            QuickReminderTemplate(
                title = getString(R.string.quick_exercise_title),
                message = getString(R.string.quick_exercise_message),
                intervalMinutes = 240, // 4 godziny
                iconRes = R.drawable.ic_exercise,
                categoryIcon = "exercise"
            ),
            QuickReminderTemplate(
                title = getString(R.string.quick_posture_title),
                message = getString(R.string.quick_posture_message),
                intervalMinutes = 45, // 45 minut
                iconRes = R.drawable.ic_posture,
                categoryIcon = "posture"
            )
        )

        val adapter = QuickReminderAdapter(templates) { template ->
            // Konwertuj template na Reminder
            val reminder = Reminder(
                title = template.title,
                message = template.message,
                intervalMinutes = template.intervalMinutes,
                categoryIcon = template.categoryIcon
            )

            onQuickReminderSelected(reminder)
            dismiss()
        }

        binding.recyclerViewQuickSelect.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            this.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
