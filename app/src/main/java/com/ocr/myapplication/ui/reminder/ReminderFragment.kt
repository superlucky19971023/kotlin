package com.ocr.myapplication.ui.reminder

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ocr.myapplication.R
import com.ocr.myapplication.database.Note
import com.ocr.myapplication.database.NoteRepository
import com.ocr.myapplication.database.Reminder
import com.ocr.myapplication.database.ReminderRepository
import com.ocr.myapplication.sharedpreference.AppPreferences
import com.ocr.myapplication.ui.note.NoteFragment
import com.ocr.myapplication.ui.note.NoteFragment.OnNoteItemSelectedListener
import com.ocr.myapplication.ui.reminderedit.ReminderEditFragment
import com.ocr.myapplication.viewmodal.ReminderViewModel
import java.util.Calendar

class ReminderFragment : Fragment() {
    private lateinit var backgroundReminder: ConstraintLayout;
    private lateinit var reminderContainerRecyclerView: RecyclerView
    private lateinit var adapter: ReminderAdapter
    private lateinit var viewModel: ReminderViewModel
    private lateinit var editPanel: LinearLayout
    private lateinit var reminderRepository: ReminderRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder, container, false)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appPreferences = AppPreferences(requireContext());
        reminderRepository = ReminderRepository(requireContext())
        reminderContainerRecyclerView = view.findViewById(R.id.reminder_container_list)
        reminderContainerRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        backgroundReminder = view.findViewById(R.id.reminder_background);
        backgroundReminder.setBackgroundResource(appPreferences.getOtherBackgroundImage())

        viewModel = ViewModelProvider(requireActivity())[ReminderViewModel::class.java]

        viewModel.reminderAddedEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { isAdded ->
                if (isAdded) {
                    refreshRemindersList()
                }
            }
        }

//         Observe note deleted event from ViewModel
        viewModel.reminderDeletedEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { isDeleted ->
                if (isDeleted) {
                    Log.d("NoteFragment", "Note deleted event received")
                    refreshRemindersList()
                }
            }
        }
        parentFragmentManager.setFragmentResultListener(
            "reminder_added",
            viewLifecycleOwner
        ) { _, _ ->
            refreshRemindersList()
        }

        refreshRemindersList()
    }

    private fun refreshRemindersList() {
        try {
            val data = reminderRepository.getAllReminders()

            if (::adapter.isInitialized) {
                adapter.updateData(data)
            } else {
                adapter = ReminderAdapter(data)
                reminderContainerRecyclerView.adapter = adapter
            }
        } catch (e: Exception) {
            Log.e("NoteFragment", "Error refreshing notes list", e)
        }
    }


    inner class ReminderAdapter(private var reminders: List<Reminder>) :
        RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {
        private var selectedPosition = RecyclerView.NO_POSITION
        private var prevPosition = RecyclerView.NO_POSITION

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.reminder_item, parent, false)
            return ReminderViewHolder(view)
        }

        @SuppressLint("NotifyDataSetChanged")
        fun updateData(newReminder: List<Reminder>) {
            reminders = newReminder
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return reminders.size
        }

        override fun onBindViewHolder(holder: ReminderViewHolder, @SuppressLint("RecyclerView") index: Int) {
            val reminder = reminders[index]

            // Set the background based on whether this item is selected
            if (index == selectedPosition) {
                if (prevPosition == selectedPosition) {
                    holder.adapterContainer.setBackgroundResource(R.color.default_note_item_background)
                    prevPosition = RecyclerView.NO_POSITION
                    selectedPosition = RecyclerView.NO_POSITION

                    reminderSeletedListener?.onReminderItemSelected(false)
                } else {
                    holder.adapterContainer.setBackgroundResource(R.color.selected_item_color)
                    prevPosition = index
                    reminderSeletedListener?.onReminderItemSelected(true)
                }
            } else {
                holder.adapterContainer.setBackgroundResource(R.color.default_note_item_background);
            }

            holder.bind(reminder, index)
        }

        inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val adapterContainer: LinearLayout = itemView.findViewById(R.id.reminder_item_card)
            private val adapterDescription: TextView =
                itemView.findViewById(R.id.reminder_item_content)
            private val adapterStart: TextView = itemView.findViewById(R.id.reminder_start_time)
            private val adapterEnd: TextView = itemView.findViewById(R.id.reminder_end_time)
            private val adapterIcon: ImageView = itemView.findViewById(R.id.reminder_item_icon)

            fun bind(reminder: Reminder, position: Int) {
                adapterDescription.text = reminder.content
                adapterIcon.setImageResource(reminder.icon)
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = reminder.startAt.toLong()
                }

                Log.d("ReminderBind", "bind: " + reminder.content)

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val hour = calendar.get(Calendar.HOUR)
                val minute = calendar.get(Calendar.MINUTE)
                val amPm = calendar.get(Calendar.AM_PM)
                val amPmText = if (amPm == Calendar.AM) "AM" else "PM"
                val startTime = "$month/$day/$year $hour:$minute $amPmText"
                adapterStart.text = startTime

                val calendar1 = Calendar.getInstance().apply {
                    timeInMillis = reminder.startAt.toLong()
                }


                val year1 = calendar1.get(Calendar.YEAR)
                val month1 = calendar1.get(Calendar.MONTH) + 1
                val day1 = calendar1.get(Calendar.DAY_OF_MONTH)
                val hour1 = calendar1.get(Calendar.HOUR)
                val minute1 = calendar1.get(Calendar.MINUTE)
                val amPm1 = calendar1.get(Calendar.AM_PM)
                val amPmText1 = if (amPm1 == Calendar.AM) "AM" else "PM"
                val endTime = "$month1/$day1/$year1 $hour1:$minute1 $amPmText1"
                adapterEnd.text = endTime

                adapterContainer.setOnClickListener {
                    // Change background color to indicate selection
                    adapterContainer.setBackgroundResource(R.color.selected_item_color)
                    val oldPosition = selectedPosition
                    // Update the selected position
                    selectedPosition = position
                    // Notify adapter to update the old and new positions
                    if (oldPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(oldPosition)
                    }

                    // Send the clicked note to the ViewModel
                    viewModel.onReminderClicked(reminder)
                    notifyItemChanged(selectedPosition)
                }
            }
        }
    }


    private var reminderSeletedListener: OnReminderItemSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Try to set the listener automatically if the parent implements it
        if (context is OnReminderItemSelectedListener) {
            reminderSeletedListener = context
        } else if (parentFragment is OnReminderItemSelectedListener) {
            reminderSeletedListener = parentFragment as OnReminderItemSelectedListener
        }
    }

    interface OnReminderItemSelectedListener {
        fun onReminderItemSelected(boolean: Boolean)
    }

    companion object {
        // Factory method to create a new instance of the fragment
        fun newInstance(): ReminderFragment {
            return ReminderFragment()
        }
    }
}