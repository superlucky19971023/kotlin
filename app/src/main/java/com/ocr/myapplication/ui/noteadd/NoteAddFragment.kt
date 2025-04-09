package com.ocr.myapplication.ui.noteadd

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ocr.myapplication.R
import com.ocr.myapplication.database.Note
import com.ocr.myapplication.database.NoteRepository
import com.ocr.myapplication.viewmodal.NoteViewModel
import enableDragToTopDismiss

class NoteAddFragment : Fragment() {
    private var selectedImageView : ImageView? = null
    private var selectedImageViewId : Int = 0
    private lateinit var noteRepository: NoteRepository
    private lateinit var viewModel: NoteViewModel

    override fun onCreateView (
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteRepository = NoteRepository(requireContext())

        // Get ViewModel from activity
        viewModel = ViewModelProvider(requireActivity())[NoteViewModel::class.java]

        val noteIconSelector = view.findViewById<LinearLayout>(R.id.note_icon_selector)
        val noteAddCancel = view.findViewById<TextView>(R.id.note_add_cancel)
        val saveButton = view.findViewById<TextView>(R.id.note_save_button)
        val topic = view.findViewById<EditText>(R.id.note_topic)
        val description = view.findViewById<EditText>(R.id.note_description)

        noteAddCancel.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }

        saveButton.setOnClickListener {
            val topicInfo = topic.text
            val descriptionInfo = description.text
            if (selectedImageViewId != 0 && topicInfo != null && descriptionInfo != null &&
                topicInfo.isNotEmpty() && descriptionInfo.isNotEmpty()) {
                val newNote = Note(
                    topic = topicInfo.toString(),
                    description = descriptionInfo.toString(),
                    icon = selectedImageViewId,
                    createdAt = System.currentTimeMillis().toString()
                )
                val insertedId = noteRepository.insertNote(newNote)

                if (insertedId == -1L) {
                    Log.d("NoteAdd", "Failed to add note to database")
                } else {
                    Log.d("NoteAdd", "Successfully added note to database")

                    // Notify using both methods for compatibility
                    // 1. Set fragment result
                    parentFragmentManager.setFragmentResult("note_added", Bundle())

                    // 2. Use ViewModel
                    viewModel.noteAdded()

                    // Return to parent fragment
                    parentFragmentManager.beginTransaction()
                        .remove(this)
                        .commit()
                }
            } else {
                // Show error message if fields are empty
                if (topicInfo == null || topicInfo.isEmpty()) {
                    topic.error = "Topic is required"
                }
                if (descriptionInfo == null || descriptionInfo.isEmpty()) {
                    description.error = "Description is required"
                }
                if (selectedImageViewId == 0) {
                    // You could show a toast or some other UI indicator
                    Log.d("NoteAdd", "Please select an icon")
                }
            }
        }
        enableDragToTopDismiss(view)

        val appIcons = listOf(
            R.drawable.temp1,
            R.drawable.temp2,
            R.drawable.temp3
        )

        for (icon in appIcons) {
            val imageView = createImageView(icon)
            noteIconSelector.addView(imageView)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun createImageView(imageId: Int): ImageView {
        val imageView = ImageView(requireContext())

        val layoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.circle_size),
            resources.getDimensionPixelSize(R.dimen.circle_size)
        )
        layoutParams.marginEnd = resources.getDimensionPixelSize(R.dimen.circle_margin)
        imageView.layoutParams = layoutParams

        // Set the image resource
        imageView.setImageResource(imageId)

        // Set scaleType to ensure content fits properly
        imageView.scaleType = ImageView.ScaleType.FIT_XY

        // Set click listener
        imageView.setOnClickListener {
            // Remove border from previously selected view
            selectedImageView.let { previouslySelected ->
                val border = GradientDrawable()
                border.shape = GradientDrawable.OVAL
                border.setStroke(
                    resources.getDimensionPixelSize(R.dimen.border_width),
                    Color.BLACK
                )
                if (previouslySelected != null) {
                    previouslySelected.foreground = null
                }
            }

            // Create a new drawable for the border
            val borderDrawable = GradientDrawable()

            borderDrawable.shape = GradientDrawable.RECTANGLE
            borderDrawable.cornerRadius = 3F
            borderDrawable.setStroke(
                resources.getDimensionPixelSize(R.dimen.border_width),
                resources.getColor(R.color.selected_border_color)
            )

            // Set the border as background
            imageView.foreground = borderDrawable

            // Update the selected view reference
            selectedImageView = imageView
            selectedImageViewId = imageId
        }
        return imageView
    }
}