package com.ocr.myapplication.ui.reminder

import android.annotation.SuppressLint
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

    override fun onCreateView (
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
        parentFragmentManager.setFragmentResultListener("reminder_added", viewLifecycleOwner) { _, _ ->
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
                Log.d("Set Adapter", "refreshRemindersList: "+data.size)
                adapter = ReminderAdapter(data)
                reminderContainerRecyclerView.adapter = adapter
            }
        } catch (e: Exception) {
            Log.e("NoteFragment", "Error refreshing notes list", e)
        }
    }


    inner class ReminderAdapter(private var reminders: List<Reminder>) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {
        private var selectedPosition = RecyclerView.NO_POSITION

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.reminder_item, parent, false)
            return ReminderViewHolder(view)
        }

        @SuppressLint("NotifyDataSetChanged")
        fun updateData(newReminder: List<Reminder>) {
            reminders = newReminder
            notifyDataSetChanged()
            Log.d("ReminderAdapter", "Data updated with ${reminders.size} items") // Add logging
        }

        override fun getItemCount(): Int {
            return reminders.size
        }

        override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
            val reminder = reminders[position]

            // Set the background based on whether this item is selected
            if (position == selectedPosition) {
                holder.adapterContainer.setBackgroundResource(R.color.selected_item_color)
            } else {
                holder.adapterContainer.setBackgroundResource(R.color.default_note_item_background);
            }

            holder.bind(reminder, position)
        }

        inner class ReminderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            val adapterContainer: CardView = itemView.findViewById(R.id.reminder_item_card)
            private val adapterDescription: TextView = itemView.findViewById(R.id.reminder_item_content)
            private val adapterDate: TextView = itemView.findViewById(R.id.reminder_item_date)
            private val adapterTime: TextView = itemView.findViewById(R.id.reminder_item_time)
            private val adapterIcon: ImageView = itemView.findViewById(R.id.reminder_item_icon)
//            val adapterContent: LinearLayout = itemView.findViewById(R.id.reminder_adapter_content)

            fun bind(reminder: Reminder,position: Int) {
                adapterDescription.text = reminder.content
                adapterIcon.setImageResource(reminder.icon)
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = reminder.startAt.toLong()
                }

                Log.d("ReminderBind", "bind: "+reminder.content)

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val hour = calendar.get(Calendar.HOUR)
                val minute = calendar.get(Calendar.MINUTE)
                val amPm = calendar.get(Calendar.AM_PM)
                val amPmText = if (amPm == Calendar.AM) "AM" else "PM"
                val date = "$month/$day/$year"
                val time = "$hour:$minute $amPmText"
                adapterDate.text = date
                adapterTime.text = time

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
                    notifyItemChanged(selectedPosition)
                    // Send the clicked note to the ViewModel
                    viewModel.onReminderClicked(reminder)
                }
            }
        }
    }
}