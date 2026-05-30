package com.reminderpro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.reminderpro.data.Reminder
import com.reminderpro.databinding.ActivityMainBinding
import com.reminderpro.ui.AddReminderDialog
import com.reminderpro.ui.EditReminderDialog
import com.reminderpro.ui.QuickSelectBottomSheet
import com.reminderpro.ui.ReminderAdapter
import com.reminderpro.viewmodel.ReminderViewModel
import com.reminderpro.workers.ReminderScheduler

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ReminderViewModel
    private lateinit var adapter: ReminderAdapter
    private lateinit var scheduler: ReminderScheduler

    // Request permission launcher dla Android 13+
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Snackbar.make(binding.root, "Uprawnienie do powiadomień przyznane", Snackbar.LENGTH_SHORT).show()
            } else {
                showPermissionRationale()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbar)

        // Inicjalizacja
        viewModel = ViewModelProvider(this)[ReminderViewModel::class.java]
        scheduler = ReminderScheduler.getInstance(this)

        setupWindowInsets()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        checkPermissions()
    }

    /**
     * Obsługuje window insets (edge-to-edge wymuszone na Androidzie 15 / targetSdk 35).
     * Dodaje padding paska statusu na górze oraz odsuwa dolne elementy (FAB, przycisk
     * szybkiego wyboru, listę) ponad pasek nawigacji, żeby się pod niego nie chowały.
     */
    private fun setupWindowInsets() {
        // Bazowe (XML-owe) odstępy, do których doliczamy insety systemowe.
        val fabBaseMargin = (binding.fabAddReminder.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
        val quickSelectBaseMargin = (binding.buttonQuickSelect.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
        val listBasePaddingBottom = binding.recyclerViewReminders.paddingBottom
        val appBarBasePaddingTop = binding.appBarLayout.paddingTop

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.appBarLayout.updatePadding(top = appBarBasePaddingTop + bars.top)

            binding.fabAddReminder.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = fabBaseMargin + bars.bottom
            }
            binding.buttonQuickSelect.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = quickSelectBaseMargin + bars.bottom
            }
            binding.recyclerViewReminders.updatePadding(bottom = listBasePaddingBottom + bars.bottom)

            insets
        }
    }

    /**
     * Konfiguracja RecyclerView.
     */
    private fun setupRecyclerView() {
        adapter = ReminderAdapter(
            onToggleEnabled = { reminder, enabled ->
                handleToggleReminder(reminder, enabled)
            },
            onEdit = { reminder ->
                showEditReminderDialog(reminder)
            },
            onDelete = { reminder ->
                showDeleteConfirmation(reminder)
            }
        )

        binding.recyclerViewReminders.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    /**
     * Konfiguracja observerów LiveData.
     */
    private fun setupObservers() {
        viewModel.allReminders.observe(this) { reminders ->
            adapter.submitList(reminders)

            // Pokaż/ukryj empty state
            if (reminders.isEmpty()) {
                binding.recyclerViewReminders.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.VISIBLE
            } else {
                binding.recyclerViewReminders.visibility = View.VISIBLE
                binding.emptyStateLayout.visibility = View.GONE
            }
        }
    }

    /**
     * Konfiguracja listenerów przycisków.
     */
    private fun setupClickListeners() {
        // FAB - dodaj przypomnienie
        binding.fabAddReminder.setOnClickListener {
            showAddReminderDialog()
        }

        // Przycisk szybkiego wyboru
        binding.buttonQuickSelect.setOnClickListener {
            showQuickSelectBottomSheet()
        }
    }

    /**
     * Sprawdza i requestuje niezbędne uprawnienia.
     */
    private fun checkPermissions() {
        // Android 13+ - request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Android 12+ - sprawdź dokładne alarmy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!scheduler.canScheduleExactAlarms()) {
                showExactAlarmPermissionDialog()
            }
        }
    }

    /**
     * Pokazuje dialog dodawania przypomnienia.
     */
    private fun showAddReminderDialog() {
        val dialog = AddReminderDialog { title, message, intervalMinutes, categoryIcon ->
            val reminder = Reminder(
                title = title,
                message = message,
                intervalMinutes = intervalMinutes,
                categoryIcon = categoryIcon
            )

            viewModel.addReminder(reminder) { reminderId ->
                // Schedule przypomnienie
                val newReminder = reminder.copy(id = reminderId)
                scheduler.scheduleReminder(newReminder)

                runOnUiThread {
                    Snackbar.make(binding.root, getString(R.string.reminder_added), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show(supportFragmentManager, "AddReminderDialog")
    }

    /**
     * Pokazuje dialog edycji przypomnienia.
     */
    private fun showEditReminderDialog(reminder: Reminder) {
        val dialog = EditReminderDialog(reminder) { id, title, message, intervalMinutes, categoryIcon ->
            val updatedReminder = reminder.copy(
                title = title,
                message = message,
                intervalMinutes = intervalMinutes,
                categoryIcon = categoryIcon
            )

            viewModel.updateReminder(updatedReminder) {
                // Jeśli przypomnienie jest aktywne, reschedule z nowymi parametrami
                if (updatedReminder.isEnabled) {
                    scheduler.cancelReminder(id)
                    scheduler.scheduleReminder(updatedReminder)
                }

                runOnUiThread {
                    Snackbar.make(binding.root, getString(R.string.reminder_updated), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show(supportFragmentManager, "EditReminderDialog")
    }

    /**
     * Pokazuje bottom sheet z szybkim wyborem.
     */
    private fun showQuickSelectBottomSheet() {
        val bottomSheet = QuickSelectBottomSheet { quickReminder ->
            viewModel.addReminder(quickReminder) { reminderId ->
                // Schedule przypomnienie
                val newReminder = quickReminder.copy(id = reminderId)
                scheduler.scheduleReminder(newReminder)

                runOnUiThread {
                    Snackbar.make(binding.root, getString(R.string.reminder_added), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        bottomSheet.show(supportFragmentManager, "QuickSelectBottomSheet")
    }

    /**
     * Obsługuje przełączanie przypomnienia (włącz/wyłącz).
     */
    private fun handleToggleReminder(reminder: Reminder, enabled: Boolean) {
        viewModel.toggleReminderEnabled(reminder.id, enabled) {
            if (enabled) {
                // Schedule przypomnienie
                val updatedReminder = reminder.copy(isEnabled = true)
                scheduler.scheduleReminder(updatedReminder)
            } else {
                // Anuluj przypomnienie
                scheduler.cancelReminder(reminder.id)
            }
        }
    }

    /**
     * Pokazuje dialog potwierdzenia usunięcia.
     */
    private fun showDeleteConfirmation(reminder: Reminder) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.delete_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                deleteReminder(reminder)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    /**
     * Usuwa przypomnienie.
     */
    private fun deleteReminder(reminder: Reminder) {
        viewModel.deleteReminder(reminder) {
            // Anuluj schedulowane alarmy
            scheduler.cancelReminder(reminder.id)

            runOnUiThread {
                Snackbar.make(binding.root, getString(R.string.reminder_deleted), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Pokazuje wyjaśnienie potrzeby uprawnień.
     */
    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_notification_title))
            .setMessage(getString(R.string.permission_notification_message))
            .setPositiveButton(getString(R.string.grant_permission)) { _, _ ->
                // Otwórz ustawienia aplikacji
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    /**
     * Pokazuje dialog o dokładnych alarmach (Android 12+).
     */
    private fun showExactAlarmPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_alarm_title))
            .setMessage(getString(R.string.permission_alarm_message))
            .setPositiveButton(getString(R.string.grant_permission)) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
