package com.ocr.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.ocr.myapplication.database.DatabaseHelper
import com.ocr.myapplication.database.Note
import com.ocr.myapplication.database.NoteRepository
import com.ocr.myapplication.database.Reminder
import com.ocr.myapplication.database.ReminderRepository
import com.ocr.myapplication.databinding.ActivityMainBinding
import com.ocr.myapplication.sharedpreference.AppPreferences
import com.ocr.myapplication.ui.LoginModalFragment
import com.ocr.myapplication.ui.app.AppFragment
import com.ocr.myapplication.ui.chatscreen.ChatScreenFragment
import com.ocr.myapplication.ui.note.NoteFragment
import com.ocr.myapplication.ui.noteadd.NoteAddFragment
import com.ocr.myapplication.ui.noteadd.NoteEditFragment
import com.ocr.myapplication.ui.profile.ProfileFragment
import com.ocr.myapplication.ui.reminderedit.ReminderAddFragment
import com.ocr.myapplication.ui.reminderedit.ReminderEditFragment
import com.ocr.myapplication.ui.subscription.SubscriptionFragment
import com.ocr.myapplication.viewmodal.NoteViewModel
import com.ocr.myapplication.viewmodal.ReminderViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var first: LinearLayout
    private lateinit var second: LinearLayout
    private lateinit var third: LinearLayout
    private lateinit var fourth: LinearLayout
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var selectedNote: Note
    private lateinit var selectedReminder : Reminder
    private lateinit var noteRepository : NoteRepository
    private lateinit var reminderRepository: ReminderRepository
    private lateinit var reminderViewModel: ReminderViewModel

    @SuppressLint("CutPasteId", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedNote = Note (
            id= 0,
            topic= "",
            description = "",
            createdAt = "",
            icon = 0
        )
        selectedReminder = Reminder(
            id = 0,
            content = "",
            startAt = "",
            endAt = "",
            icon = 0
        )
        val appPreferences = AppPreferences(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        // Observe note click events
        noteViewModel.noteClickedEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { note ->
                handleNoteClick(note)
            }
        }

        reminderViewModel = ViewModelProvider(this)[ReminderViewModel::class.java]

        reminderViewModel.reminderClickedEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { reminder ->
                handleReminderClick(reminder)
            }
        }

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        noteRepository = NoteRepository(this)
        reminderRepository = ReminderRepository(this)
        // Use binding to find views instead of findViewById where possible
        // For views that are in the main layout
        first = findViewById(R.id.first)
        second = findViewById(R.id.second)
        third = findViewById(R.id.third)
        fourth = findViewById(R.id.fourth)
        val reminderAdd = findViewById<LinearLayout>(R.id.reminder_add)
        val reminderEdit = findViewById<LinearLayout>(R.id.reminder_edit)
        val noteEdit = findViewById<LinearLayout>(R.id.edit)
        val noteAdd = findViewById<LinearLayout>(R.id.note_add)
        val comment = findViewById<LinearLayout>(R.id.comment)
        val reminderDelete = findViewById<LinearLayout>(R.id.reminder_delete)
        val noteDelete = findViewById<LinearLayout>(R.id.note_delete)
//        first.setOnClickListener {
//            supportFragmentManager.beginTransaction()
//                .setCustomAnimations(
//                    R.anim.slide_up_enter,
//                    R.anim.fade_out,
//                    R.anim.fade_in,
//                    R.anim.slide_up_exit
//                )
//                .replace(R.id.nav_host_fragment_content_main, AppFragment())
//                .addToBackStack(null)
//                .commit()
//        }

        first.setOnClickListener {
            val loginFragment = LoginModalFragment()
            loginFragment.show(supportFragmentManager, "loginModal")
        }
        second.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_up_enter,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_up_exit
                )
                .replace(R.id.nav_host_fragment_content_main, SubscriptionFragment())
                .addToBackStack(null)
                .commit()
        }
        third.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_down_enter,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_down_exit
                )
                .replace(R.id.nav_host_fragment_content_main, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }
        fourth.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_up_enter,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_up_exit
                )
                .replace(R.id.nav_host_fragment_content_main, ChatScreenFragment())
                .addToBackStack(null)
                .commit()
        }
        reminderAdd.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_down_enter,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_down_exit
                )
                .add(R.id.nav_host_fragment_content_main, ReminderAddFragment())
                .addToBackStack(null)
                .commit()
        }
        // Fix the noteAdd click listener to properly handle fragment communication
        noteAdd.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_down_enter,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_down_exit
                )
                .add(R.id.nav_host_fragment_content_main, NoteAddFragment())
                .addToBackStack(null)
                .commit()
        }

        noteEdit.setOnClickListener {
            Log.d("NoteEdit", "onCreate: "+selectedNote.description)
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_down_enter,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_down_exit
                )
                .add(R.id.nav_host_fragment_content_main, NoteEditFragment(selectedNote))
                .addToBackStack(null)
                .commit()
        }

        reminderEdit.setOnClickListener {
            Log.d("NoteEdit", "onCreate: "+selectedReminder.content)
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_down_enter,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_down_exit
                )
                .add(R.id.nav_host_fragment_content_main, ReminderEditFragment(selectedReminder))
                .addToBackStack(null)
                .commit()
        }

        noteDelete.setOnClickListener(View.OnClickListener {
            val result = noteRepository.deleteNote(selectedNote.id)
            if (result > 0) {
                // Note was successfully deleted
                Log.d("MainActivity", "Note deleted: ID=${selectedNote.id}")

                // Notify the ViewModel that a note was deleted
                noteViewModel.noteDeleted()

                // Reset the selected note
                selectedNote = Note(
                    id = 0,
                    topic = "",
                    description = "",
                    createdAt = "",
                    icon = 0
                )
            } else {
                Log.e("MainActivity", "Failed to delete note: ID=${selectedNote.id}")
            }
        })

        reminderDelete.setOnClickListener(View.OnClickListener {
            val result = reminderRepository.deleteReminder(selectedReminder.id)
            if (result > 0) {
                // Note was successfully deleted
                Log.d("MainActivity", "Reminder deleted: ID=${selectedNote.id}")

                // Notify the ViewModel that a note was deleted
                reminderViewModel.reminderDeleted()

                // Reset the selected note
                selectedReminder = Reminder(
                    id = 0,
                    content = "",
                    startAt = "",
                    endAt = "",
                    icon = 0
                )
            } else {
                Log.e("MainActivity", "Failed to delete note: ID=${selectedNote.id}")
            }
        })

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.header_nav_home, R.id.header_nav_gallery, R.id.header_nav_notes, R.id.header_nav_repository
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        val icon = ContextCompat.getDrawable(this, appPreferences.getAppIcon())?.apply {
            val sizeInPx = (4 * resources.displayMetrics.density).toInt()
            setBounds(0, 0, sizeInPx, sizeInPx)
        }

        supportActionBar?.setHomeAsUpIndicator(icon)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.label) {
                "Home" -> {
                    reminderAdd.visibility = View.GONE
                    noteEdit.visibility = View.GONE
                    noteAdd.visibility = View.GONE
                    comment.visibility = View.GONE
                    reminderEdit.visibility = View.GONE
                    reminderDelete.visibility = View.GONE
                    noteDelete.visibility = View.GONE
                }
                "Notes" -> {
                    reminderAdd.visibility = View.GONE
                    noteEdit.visibility = View.VISIBLE
                    noteAdd.visibility = View.VISIBLE
                    comment.visibility = View.GONE
                    reminderEdit.visibility = View.GONE
                    noteDelete.visibility = View.VISIBLE
                    reminderDelete.visibility = View.GONE
                }
                "Reminder" -> {
                    reminderAdd.visibility = View.VISIBLE
                    noteEdit.visibility = View.GONE
                    noteAdd.visibility = View.GONE
                    comment.visibility = View.GONE
                    reminderEdit.visibility = View.VISIBLE
                    reminderDelete.visibility = View.VISIBLE
                    noteDelete.visibility = View.GONE
                }
                "Repositories" -> {
                    reminderAdd.visibility = View.GONE
                    noteEdit.visibility = View.GONE
                    noteAdd.visibility = View.GONE
                    comment.visibility = View.VISIBLE
                    reminderEdit.visibility = View.GONE
                    reminderDelete.visibility = View.GONE
                    noteDelete.visibility = View.GONE
                }
            }
        }

        // Set up fragment result listener at the activity level
        supportFragmentManager.setFragmentResultListener("note_added", this) { _, _ ->
            // Notify the ViewModel that a note was added
            noteViewModel.noteAdded()
        }
    }

    private fun handleNoteClick(note: Note) {
        selectedNote = note
        Log.d("MainActivity", "Note clicked: ID=${note.id}, Topic=${note.topic}, Description=${note.description}")
    }
    private fun handleReminderClick(reminder: Reminder) {
        selectedReminder = reminder
        Log.d("MainActivity", "Note clicked: ID=${reminder.id}, Description=${reminder.content}")
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}