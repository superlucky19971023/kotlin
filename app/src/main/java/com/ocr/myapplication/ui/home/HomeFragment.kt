package com.ocr.myapplication.ui.home

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ocr.myapplication.R
import com.ocr.myapplication.databinding.FragmentHomeBinding
import com.ocr.myapplication.sharedpreference.AppPreferences
import com.ocr.myapplication.ui.profile.ProfileFragment

class HomeFragment : Fragment() {
    private lateinit var mainContainer: View
    private lateinit var backgroundImage: ImageView
    override fun onCreateView (
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainContainer = inflater.inflate(R.layout.fragment_home, container, false)
        backgroundImage = mainContainer.findViewById(R.id.home_background)

        val appPreference = AppPreferences(requireContext())
        val background = appPreference.getBackgroundImage();
        backgroundImage.setImageResource(background)
        backgroundImage.setOnClickListener(View.OnClickListener {
            parentFragmentManager.beginTransaction()
                .remove(ProfileFragment())
//                .remove(CommentFragment())
                .commit()
        })
        return mainContainer;
    }
}

