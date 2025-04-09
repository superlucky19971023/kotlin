package com.ocr.myapplication.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ocr.myapplication.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LoginModalFragment : BottomSheetDialogFragment() {
    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reminder_example, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dateEditText = view.findViewById(R.id.dateEditText)
        timeEditText = view.findViewById(R.id.timeEditText)

        // Set up date picker
        dateEditText.setOnClickListener {
            showDatePicker()
        }

        // Set up time picker
        timeEditText.setOnClickListener {
            showTimePicker()
        }

    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            updateTimeInView()
        }

        TimePickerDialog(
            requireContext(),
            timeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateInView() {
        val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        dateEditText.setText(format.format(calendar.time))
    }

    private fun updateTimeInView() {
        val format = SimpleDateFormat("HH:mm", Locale.US)
        timeEditText.setText(format.format(calendar.time))
    }
}
