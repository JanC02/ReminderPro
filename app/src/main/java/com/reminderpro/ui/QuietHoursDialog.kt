package com.reminderpro.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.reminderpro.R
import com.reminderpro.data.QuietHours
import com.reminderpro.data.QuietHoursStore
import com.reminderpro.databinding.DialogQuietHoursBinding

/**
 * Dialog ustawień globalnych godzin ciszy.
 * Zapisuje ustawienia przez [QuietHoursStore] i woła [onSaved], aby właściciel
 * mógł przeplanować aktywne przypomnienia.
 */
class QuietHoursDialog(
    private val onSaved: () -> Unit
) : DialogFragment() {

    private var _binding: DialogQuietHoursBinding? = null
    private val binding get() = _binding!!

    private var startMinutes = QuietHours.DEFAULT_START_MINUTES
    private var endMinutes = QuietHours.DEFAULT_END_MINUTES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_ReminderPro_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogQuietHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Wczytaj bieżące ustawienia
        val current = QuietHoursStore(requireContext()).get()
        startMinutes = current.startMinutes
        endMinutes = current.endMinutes
        binding.switchQuietEnabled.isChecked = current.enabled

        updateTimeButtons()
        updateTimesEnabled(current.enabled)

        binding.switchQuietEnabled.setOnCheckedChangeListener { _, isChecked ->
            updateTimesEnabled(isChecked)
        }

        binding.buttonStartTime.setOnClickListener {
            showTimePicker(
                titleRes = R.string.quiet_hours_picker_start_title,
                initialMinutes = startMinutes
            ) { minutes ->
                startMinutes = minutes
                updateTimeButtons()
            }
        }

        binding.buttonEndTime.setOnClickListener {
            showTimePicker(
                titleRes = R.string.quiet_hours_picker_end_title,
                initialMinutes = endMinutes
            ) { minutes ->
                endMinutes = minutes
                updateTimeButtons()
            }
        }

        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonSave.setOnClickListener { save() }
    }

    /** Włącza/wyłącza przyciski wyboru godzin zależnie od stanu przełącznika. */
    private fun updateTimesEnabled(enabled: Boolean) {
        binding.buttonStartTime.isEnabled = enabled
        binding.buttonEndTime.isEnabled = enabled
    }

    private fun updateTimeButtons() {
        binding.buttonStartTime.text = formatTime(startMinutes)
        binding.buttonEndTime.text = formatTime(endMinutes)
    }

    private fun formatTime(minutes: Int): String =
        getString(R.string.time_format, minutes / 60, minutes % 60)

    private fun showTimePicker(titleRes: Int, initialMinutes: Int, onPicked: (Int) -> Unit) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(initialMinutes / 60)
            .setMinute(initialMinutes % 60)
            .setTitleText(titleRes)
            .build()

        picker.addOnPositiveButtonClickListener {
            onPicked(picker.hour * 60 + picker.minute)
        }
        picker.show(childFragmentManager, "QuietHoursTimePicker")
    }

    private fun save() {
        val quietHours = QuietHours(
            enabled = binding.switchQuietEnabled.isChecked,
            startMinutes = startMinutes,
            endMinutes = endMinutes
        )
        QuietHoursStore(requireContext()).save(quietHours)
        onSaved()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
