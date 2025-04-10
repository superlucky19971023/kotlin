package com.ocr.myapplication.ui.reminderedit

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
import com.ocr.myapplication.database.Reminder
import com.ocr.myapplication.database.ReminderRepository
import com.ocr.myapplication.viewmodal.ReminderViewModel
import enableDragToTopDismiss
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReminderEditFragment(reminder: Reminder): Fragment() {
    private var selectedImageView : ImageView? = null
    private var selectedImageViewId : Int = 0
    private lateinit var saveButton: TextView
    private lateinit var description: EditText
    private lateinit var cancelButton: TextView
    private lateinit var reminderRepository: ReminderRepository
    private lateinit var viewModel: ReminderViewModel
    private lateinit var startDate: EditText
    private lateinit var startTime: EditText
    private lateinit var endDate: EditText
    private lateinit var endTime: EditText
    private val calendar = Calendar.getInstance()
    private var currentReminder : Reminder = reminder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reminder_edit, container, false)
    }

    private fun parseDataTimeToMillos(input: String): Long? {
        return try {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .apply { isLenient = false }
                .parse(input)
                ?.time
        } catch (e: Exception) {
            null
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reminderRepository = ReminderRepository(requireContext())
        viewModel = ViewModelProvider(requireActivity())[ReminderViewModel::class.java]

        val themeLinearLayout = view.findViewById<LinearLayout>(R.id.theme_selector);
        saveButton = view.findViewById(R.id.reminder_save_button);
        cancelButton = view.findViewById(R.id.reminder_cancel_button)
        startDate = view.findViewById(R.id.start_at_date)
        startTime = view.findViewById(R.id.start_at_time)
        endDate = view.findViewById(R.id.end_at_date)
        endTime = view.findViewById(R.id.end_at_time)
        description = view.findViewById(R.id.reminder_add_descripton)
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val time = SimpleDateFormat("HH:mm", Locale.getDefault())

// Setting text for start date and time TextViews
        startDate.setText(date.format(Date(this.currentReminder.startAt.toLong())))
        startTime.setText(time.format(Date(this.currentReminder.startAt.toLong())))

// Setting text for end date and time TextViews
        endDate.setText(date.format(Date(this.currentReminder.endAt.toLong())))
        endTime.setText(time.format(Date(this.currentReminder.endAt.toLong())))
        description.setText(this.currentReminder.content)

        cancelButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }

        saveButton.setOnClickListener(View.OnClickListener {
            val descriptionInfo = description.text
            if (selectedImageViewId != 0 && descriptionInfo != null && descriptionInfo.isNotEmpty()) {
                val newReminder = Reminder(
                    content = descriptionInfo.toString(),
                    icon = selectedImageViewId,
                    startAt = System.currentTimeMillis().toString(),
                    endAt = System.currentTimeMillis().toString(),
                    id = this.currentReminder.id
                )

                reminderRepository.updateReminder(newReminder)
                parentFragmentManager.setFragmentResult("reminder_added", Bundle())
                viewModel.reminderAdded()
                parentFragmentManager.beginTransaction().remove(this).commit()
            } else {
                if (descriptionInfo == null) {
                    description.error = "Description is required"
                }
            }
        })
        enableDragToTopDismiss(view)

        val appIcons = listOf(
            R.drawable.icon_1,
            R.drawable.icon_3,
            R.drawable.icon_1,
            R.drawable.icon_4,
            R.drawable.icon_5,
            R.drawable.icon_6,
        )

        for (icon in appIcons) {
            val imageView = createImageView(icon)
            themeLinearLayout.addView(imageView)
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