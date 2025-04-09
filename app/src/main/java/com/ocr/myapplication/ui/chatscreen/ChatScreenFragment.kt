package com.ocr.myapplication.ui.chatscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.ocr.myapplication.R
import com.ocr.myapplication.sharedpreference.AppPreferences
import enableDragToTopDismiss

class ChatScreenFragment : Fragment() {

    private lateinit var Back: LinearLayout
    override fun onCreateView (
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appPreferences = AppPreferences(requireContext())
        Back = view.findViewById(R.id.chatscreen_background)
        Back.setBackgroundResource(appPreferences.getChatBackgroundImage())
        enableDragToTopDismiss(view)
    }
}