package com.ocr.myapplication.ui.note

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ocr.myapplication.R
import com.ocr.myapplication.database.Note
import com.ocr.myapplication.database.NoteRepository
import com.ocr.myapplication.viewmodal.NoteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class NoteFragment : Fragment() {

    private lateinit var noteRepository : NoteRepository
    private lateinit var noteDataContainer : RecyclerView
    private lateinit var adapter: NoteAdapter
    private lateinit var viewModel: NoteViewModel
    private lateinit var editPanel: LinearLayout
    private lateinit var searchView: EditText
    private var searchText: String = ""
    private lateinit var searchJob:Job

    override fun onCreateView (
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteRepository = NoteRepository(requireContext())
        noteDataContainer = view.findViewById(R.id.note_list)
        noteDataContainer.layoutManager = LinearLayoutManager(requireContext())

        searchView = view.findViewById(R.id.note_search_input)
        searchView.addTextChangedListener(object: TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("asdf", "before" + s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                Log.d("asdf", "after" + s.toString())
                searchText = s.toString()
                refreshNotesList()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("asdf", "onChanged" + s.toString())
            }
        })

        // Get ViewModel from activity
        viewModel = ViewModelProvider(requireActivity())[NoteViewModel::class.java]

        // Observe note added event from ViewModel
        viewModel.noteAddedEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { isAdded ->
                if (isAdded) {
                    refreshNotesList()
                }
            }
        }

        // Observe note deleted event from ViewModel
        viewModel.noteDeletedEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { isDeleted ->
                if (isDeleted) {
                    Log.d("NoteFragment", "Note deleted event received")
                    refreshNotesList()
                }
            }
        }

        // Also keep the fragment result listener for backward compatibility
        parentFragmentManager.setFragmentResultListener("note_added", viewLifecycleOwner) { _, _ ->
            refreshNotesList()
        }

        refreshNotesList()
    }


    private fun refreshNotesList() {
        try {
            // Use searchNotes if searchText is not empty, otherwise get all notes
            val data = if (searchText.isNotEmpty()) {
                noteRepository.searchNotes(searchText)
            } else {
                noteRepository.getAllNotes()
            }

            if (::adapter.isInitialized) {
                adapter.updateData(data)
            } else {
                adapter = NoteAdapter(data)
                noteDataContainer.adapter = adapter
            }
        } catch (e: Exception) {
            Log.e("NoteFragment", "Error refreshing notes listx", e)
        }
    }

    inner class NoteAdapter(private var notes: List<Note>) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

        private var selectedPosition = RecyclerView.NO_POSITION

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note_adapter, parent, false)
            return NoteViewHolder(view)
        }

        @SuppressLint("NotifyDataSetChanged")
        fun updateData(newNotes: List<Note>) {
            notes = newNotes
            notifyDataSetChanged()
        }


        override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
            val note = notes[position]

            // Set the background based on whether this item is selected
            if (position == selectedPosition) {
                holder.adapterContent.setBackgroundResource(R.color.selected_item_color)
            } else {
                holder.adapterContent.setBackgroundResource(R.color.default_note_item_background);
            }

            holder.bind(note, position)
        }

        override fun getItemCount() = notes.size

        inner class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            private val adapterContainer: CardView = itemView.findViewById(R.id.note_adapter)
            private val adapterTopic: TextView = itemView.findViewById(R.id.note_adapter_topic)
            private val adapterDescription: TextView = itemView.findViewById(R.id.note_adapter_description)
            private val adapterDate: TextView = itemView.findViewById(R.id.note_adapter_date)
            private val adapterTime: TextView = itemView.findViewById(R.id.note_adapter_time)
            private val adapterIcon: ImageView = itemView.findViewById(R.id.note_adapter_icon)
            val adapterContent: LinearLayout = itemView.findViewById(R.id.note_adapter_content)

            fun bind(note: Note,position: Int) {
                adapterTopic.text = note.topic
                adapterDescription.text = note.description
                adapterIcon.setImageResource(note.icon)
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = note.createdAt.toLong()
                }

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
                    adapterContent.setBackgroundResource(R.color.selected_item_color)

                    val oldPosition = selectedPosition

                    // Update the selected position
                    selectedPosition = position

                    // Notify adapter to update the old and new positions
                    if (oldPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(oldPosition)
                    }
                    notifyItemChanged(selectedPosition)
                    // Send the clicked note to the ViewModel
                    viewModel.onNoteClicked(note)
                }
            }
        }
    }
}