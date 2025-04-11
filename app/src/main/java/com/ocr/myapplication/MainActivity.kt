package com.ocr.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
import com.ocr.myapplication.ui.comment.CommentFragment
import com.ocr.myapplication.ui.note.NoteFragment
import com.ocr.myapplication.ui.noteadd.NoteAddFragment
import com.ocr.myapplication.ui.noteadd.NoteEditFragment
import com.ocr.myapplication.ui.profile.ProfileFragment
import com.ocr.myapplication.ui.reminder.ReminderFragment
import com.ocr.myapplication.ui.reminderedit.ReminderAddFragment
import com.ocr.myapplication.ui.reminderedit.ReminderEditFragment
import com.ocr.myapplication.ui.repository.RepositoryFragment
import com.ocr.myapplication.ui.respositorydetail.RepositoryDetailFragment
import com.ocr.myapplication.ui.subscription.SubscriptionFragment
import com.ocr.myapplication.viewmodal.NoteViewModel
import com.ocr.myapplication.viewmodal.ReminderViewModel

class MainActivity : AppCompatActivity(),RepositoryFragment.OnDocumentClickListener, NoteFragment.OnNoteItemSelectedListener, ReminderFragment.OnReminderItemSelectedListener {

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
    private lateinit var miniChatText: TextView
    var chatScreenClick: Int = 0

    private lateinit var noteEdit : LinearLayout
    private lateinit var noteDelete : LinearLayout
    private lateinit var reminderEdit: LinearLayout
    private lateinit var reminderDelete: LinearLayout
    private lateinit var miniChatSendButton: ImageView
    private lateinit var miniChatInput: EditText

    private var clickState = BooleanArray(10) { false }

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
        reminderEdit = findViewById(R.id.reminder_edit)
        noteEdit = findViewById(R.id.note_edit)
        val noteAdd = findViewById<LinearLayout>(R.id.note_add)
        val comment = findViewById<LinearLayout>(R.id.comment)
        reminderDelete = findViewById(R.id.reminder_delete)
        noteDelete = findViewById(R.id.note_delete)
        miniChatText = findViewById(R.id.mini_chat_text)
        miniChatSendButton = findViewById(R.id.mini_chat_send_button)
        miniChatInput = findViewById(R.id.mini_chat_input)

        setupClickListener(first, AppFragment(), 0)
        setupClickListener(second, SubscriptionFragment(), 1)
        setupClickListener(third, ProfileFragment(), 2)
        setupClickListener(fourth, ChatScreenFragment(), 3)
        setupClickListener(reminderAdd, ReminderAddFragment(), 4)
        setupClickListener(noteAdd, NoteAddFragment(), 6)
//        setupClickListener(comment, CommentFragment(), 8)

        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                    super.onFragmentViewDestroyed(fm, f)
                    clickState = BooleanArray(10){ false }
                    showMiniTextVisible()
                }
            }, true)

        reminderEdit.setOnClickListener(View.OnClickListener {
            if (!clickState[5]) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_up_to_down_enter,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_down_enter
                    )
                    .replace(R.id.nav_host_fragment_content_main, ReminderEditFragment(selectedReminder))
                    .addToBackStack(null)
                    .commit()
                clickState = BooleanArray(10) { false }
            } else {
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
                if (currentFragment != null) {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_down_exit,
                            R.anim.fade_out,
                            R.anim.fade_in,
                            R.anim.slide_up_enter
                        )
                        .remove(currentFragment)
                        .commit()
                }
            }
            clickState[5] = !clickState[5]
            showMiniTextVisible()
        })
        noteEdit.setOnClickListener(View.OnClickListener {
            if (!clickState[7]) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_up_to_down_enter,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_down_enter
                    )
                    .replace(R.id.nav_host_fragment_content_main, NoteEditFragment(selectedNote))
                    .addToBackStack(null)
                    .commit()
                clickState = BooleanArray(10) { false }
            } else {
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
                if (currentFragment != null) {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_down_exit,
                            R.anim.fade_out,
                            R.anim.fade_in,
                            R.anim.slide_up_enter
                        )
                        .remove(currentFragment)
                        .commit()
                }
            }
            clickState[7] = !clickState[7]
            showMiniTextVisible()
        })
        noteDelete.setOnClickListener(View.OnClickListener {
            val result = noteRepository.deleteNote(selectedNote.id)
            if (result > 0) {
                // Note was successfully deleted

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
        comment.setOnClickListener(View.OnClickListener {
            if (!clickState[8]) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_up_to_down_enter,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_down_enter
                    )
                    .add(R.id.nav_host_fragment_content_main, CommentFragment())
                    .addToBackStack(null)
                    .commit()
                clickState = BooleanArray(10) { false }

            } else {
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
                if (currentFragment != null) {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_down_exit,
                            R.anim.fade_out,
                            R.anim.fade_in,
                            R.anim.slide_up_enter
                        )
                        .remove(currentFragment)
                        .commit()
                }
            }
            clickState[8] = !clickState[8]
            showMiniTextVisible()
        })

        miniChatSendButton.setOnClickListener(View.OnClickListener {
            if (miniChatInput.text.isNotEmpty()) {
                miniChatText.text = miniChatInput.text
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
                    noteEdit.visibility = View.GONE
                    noteAdd.visibility = View.VISIBLE
                    comment.visibility = View.GONE
                    reminderEdit.visibility = View.GONE
                    noteDelete.visibility = View.GONE
                    reminderDelete.visibility = View.GONE
                }
                "Reminder" -> {
                    reminderAdd.visibility = View.VISIBLE
                    noteEdit.visibility = View.GONE
                    noteAdd.visibility = View.GONE
                    comment.visibility = View.GONE
                    reminderEdit.visibility = View.GONE
                    reminderDelete.visibility = View.GONE
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

    private fun showMiniTextVisible(){
        if (clickState[8]||clickState[7]||clickState[6]||clickState[5]||clickState[4])
            miniChatText.visibility = View.GONE
        else miniChatText.visibility = View.VISIBLE
    }

    override fun onDocumentClicked(document: RepositoryFragment.Document) {
        Log.d("Document", "onDocumentClicked: ")
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_content_main, RepositoryDetailFragment(document.imageResId))
            .addToBackStack(null)
            .commit()

    }

    fun clearCheckState(){
        for (i in 0 until 10){
            this.clickState[i] = false
        }
    }

    private fun setupClickListener(linearLayout: LinearLayout, fragment: Fragment, index: Int) {
        linearLayout.setOnClickListener {
            if (index == 3) {
                if (!clickState[index]) {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_up_enter,
                            R.anim.fade_out,
                            R.anim.fade_in,
                            R.anim.slide_down_exit
                        )
                        .replace(R.id.nav_host_fragment_content_main, fragment)
                        .addToBackStack(null)
                        .commit()
                    clickState = BooleanArray(10) { false }
                    miniChatText.visibility = View.GONE
                } else {
                    val currentFragment =
                        supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
                    if (currentFragment != null) {
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_down_enter,
                                R.anim.fade_out,
                                R.anim.fade_in,
                                R.anim.slide_up_exit
                            )
                            .remove(currentFragment)
                            .commit()
                    }
                    miniChatText.visibility = View.VISIBLE
                }
            } else {
                if (index !=2 && index != 4 && index != 6 && index != 8) {
                    if (!clickState[index]) {
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_up_enter,
                                R.anim.fade_out,
                                R.anim.fade_in,
                                R.anim.slide_down_exit
                            )
                            .replace(R.id.nav_host_fragment_content_main, fragment)
                            .addToBackStack(null)
                            .commit()
                        clickState = BooleanArray(10) { false }
                    } else {
                        val currentFragment =
                            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
                        if (currentFragment != null) {
                            supportFragmentManager.beginTransaction()
                                .setCustomAnimations(
                                    R.anim.slide_down_enter,
                                    R.anim.fade_out,
                                    R.anim.fade_in,
                                    R.anim.slide_up_exit
                                )
                                .remove(currentFragment)
                                .commit()
                        }
                    }
                } else {
                    if (!clickState[index]) {
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_up_to_down_enter,
                                R.anim.fade_out,
                                R.anim.fade_in,
                                R.anim.slide_down_enter
                            )
                            .replace(R.id.nav_host_fragment_content_main, fragment)
                            .addToBackStack(null)
                            .commit()
                        clickState = BooleanArray(10) { false }
                    } else {
                        val currentFragment =
                            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
                        if (currentFragment != null) {
                            supportFragmentManager.beginTransaction()
                                .setCustomAnimations(
                                    R.anim.slide_down_exit,
                                    R.anim.fade_out,
                                    R.anim.fade_in,
                                    R.anim.slide_up_enter
                                )
                                .remove(currentFragment)
                                .commit()
                        }
                    }
                }
                miniChatText.visibility = View.VISIBLE
            }
            clickState[index] = !clickState[index]
            showMiniTextVisible()
        }
    }

    private fun handleNoteClick(note: Note) {
        selectedNote = note
        clickState[7] = false
        Log.d("MainActivity", "Note clicked: ID=${note.id}, Topic=${note.topic}, Description=${note.description}")
    }
    private fun handleReminderClick(reminder: Reminder) {
        selectedReminder = reminder
        clickState[5] = false
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

    override fun onNoteItemSelected(boolean: Boolean) {
        if(boolean) {
            noteEdit.visibility = View.VISIBLE
            noteDelete.visibility = View.VISIBLE
        }
        else {
            noteEdit.visibility = View.GONE
            noteDelete.visibility = View.GONE
        }
    }

    override fun onReminderItemSelected(boolean: Boolean) {
        if(boolean) {
            reminderEdit.visibility = View.VISIBLE
            reminderDelete.visibility = View.VISIBLE
        }
        else {
            reminderEdit.visibility = View.GONE
            reminderDelete.visibility = View.GONE
        }
    }
}