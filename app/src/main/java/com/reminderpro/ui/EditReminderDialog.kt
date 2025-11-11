package com.reminderpro.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import com.reminderpro.R
import com.reminderpro.data.Reminder
import com.reminderpro.databinding.DialogAddReminderBinding

/**
 * Dialog do edycji istniejącego przypomnienia.
 */
class EditReminderDialog(
    private val reminder: Reminder,
    private val onSave: (id: Long, title: String, message: String, intervalMinutes: Int, categoryIcon: String?) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddReminderBinding? = null
    private val binding get() = _binding!!

    private var currentIntervalMinutes = 120 // Będzie nadpisane danymi z reminder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_ReminderPro)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicjalizuj interwał z przypomnienia
        currentIntervalMinutes = reminder.intervalMinutes

        setupTimeUnitDropdown()
        setupSlider()
        setupManualInput()
        setupButtons()

        // Wypełnij pola danymi z przypomnienia
        loadReminderData()
    }

    /**
     * Wypełnia pola dialogu danymi z edytowanego przypomnienia.
     */
    private fun loadReminderData() {
        binding.editTextTitle.setText(reminder.title)
        binding.editTextMessage.setText(reminder.message)

        // Ustaw interwał
        updateSliderValue(currentIntervalMinutes)
        if (currentIntervalMinutes in 1..1440) {
            binding.sliderInterval.value = currentIntervalMinutes.toFloat()
        }

        // Ustaw najlepszą jednostkę dla ręcznego pola
        when {
            currentIntervalMinutes % 1440 == 0 -> {
                binding.autoCompleteUnit.setText(getString(R.string.days), false)
                binding.editTextValue.setText((currentIntervalMinutes / 1440).toString())
            }
            currentIntervalMinutes % 60 == 0 -> {
                binding.autoCompleteUnit.setText(getString(R.string.hours), false)
                binding.editTextValue.setText((currentIntervalMinutes / 60).toString())
            }
            else -> {
                binding.autoCompleteUnit.setText(getString(R.string.minutes), false)
                binding.editTextValue.setText(currentIntervalMinutes.toString())
            }
        }

        // Zmień tekst przycisku na "Zaktualizuj"
        binding.buttonSave.text = getString(R.string.update)
    }

    /**
     * Konfiguruje dropdown z jednostkami czasu.
     */
    private fun setupTimeUnitDropdown() {
        val units = arrayOf(
            getString(R.string.minutes),
            getString(R.string.hours),
            getString(R.string.days)
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, units)
        binding.autoCompleteUnit.setAdapter(adapter)
    }

    /**
     * Konfiguruje suwak interwału.
     */
    private fun setupSlider() {
        binding.sliderInterval.apply {
            valueFrom = 1f
            valueTo = 1440f // 24 godziny
            stepSize = 1f

            addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    currentIntervalMinutes = value.toInt()
                    updateSliderValue(currentIntervalMinutes)
                    updateManualInputFromSlider(currentIntervalMinutes)
                }
            }
        }
    }

    /**
     * Konfiguruje ręczne wpisywanie wartości.
     */
    private fun setupManualInput() {
        // Listener dla wartości
        binding.editTextValue.addTextChangedListener {
            updateIntervalFromManualInput()
        }

        // Listener dla jednostki
        binding.autoCompleteUnit.addTextChangedListener {
            updateIntervalFromManualInput()
        }
    }

    /**
     * Aktualizuje interwał na podstawie ręcznego wpisania.
     */
    private fun updateIntervalFromManualInput() {
        val valueText = binding.editTextValue.text.toString()
        val unit = binding.autoCompleteUnit.text.toString()

        if (valueText.isNotEmpty()) {
            val value = valueText.toIntOrNull() ?: return

            currentIntervalMinutes = when (unit) {
                getString(R.string.minutes) -> value
                getString(R.string.hours) -> value * 60
                getString(R.string.days) -> value * 60 * 24
                else -> value
            }

            // Aktualizuj suwak bez triggering listenera
            if (currentIntervalMinutes in 1..1440) {
                binding.sliderInterval.value = currentIntervalMinutes.toFloat()
                updateSliderValue(currentIntervalMinutes)
            }
        }
    }

    /**
     * Aktualizuje ręczne pole na podstawie suwaka.
     */
    private fun updateManualInputFromSlider(minutes: Int) {
        val unit = binding.autoCompleteUnit.text.toString()

        val value = when (unit) {
            getString(R.string.minutes) -> minutes
            getString(R.string.hours) -> minutes / 60
            getString(R.string.days) -> minutes / (60 * 24)
            else -> minutes
        }

        binding.editTextValue.setText(value.toString())
    }

    /**
     * Aktualizuje wyświetlaną wartość suwaka.
     */
    private fun updateSliderValue(minutes: Int) {
        val formatted = when {
            minutes < 60 -> "co $minutes min"
            minutes % 1440 == 0 -> {
                val days = minutes / 1440
                "co $days ${if (days == 1) "dzień" else "dni"}"
            }
            minutes % 60 == 0 -> {
                val hours = minutes / 60
                "co $hours ${if (hours == 1) "godzinę" else if (hours < 5) "godziny" else "godzin"}"
            }
            else -> {
                val hours = minutes / 60
                val mins = minutes % 60
                "co ${hours}h ${mins}min"
            }
        }

        binding.textSliderValue.text = formatted
    }

    /**
     * Konfiguruje przyciski akcji.
     */
    private fun setupButtons() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonSave.setOnClickListener {
            saveReminder()
        }
    }

    /**
     * Waliduje i zapisuje zmiany w przypomnieniu.
     */
    private fun saveReminder() {
        val title = binding.editTextTitle.text.toString().trim()
        val message = binding.editTextMessage.text.toString().trim()

        // Walidacja
        if (title.isEmpty()) {
            binding.textInputLayoutTitle.error = getString(R.string.error_empty_title)
            return
        }

        if (message.isEmpty()) {
            binding.textInputLayoutMessage.error = getString(R.string.error_empty_message)
            return
        }

        if (currentIntervalMinutes <= 0) {
            Snackbar.make(binding.root, getString(R.string.error_interval_too_small), Snackbar.LENGTH_SHORT).show()
            return
        }

        // Zapisz zmiany
        onSave(reminder.id, title, message, currentIntervalMinutes, reminder.categoryIcon)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
