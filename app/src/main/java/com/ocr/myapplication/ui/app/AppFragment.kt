package com.ocr.myapplication.ui.app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.ocr.myapplication.MainActivity
import com.ocr.myapplication.R
import com.ocr.myapplication.animation.enableDragToDismiss
import com.ocr.myapplication.sharedpreference.AppPreferences
import com.ocr.myapplication.ui.home.HomeFragment

class AppFragment: Fragment() {
    private var selectedImageViews = arrayOfNulls<ImageView>(5)
    private var selectedImageViewIds = arrayOfNulls<Int>(5)
    private lateinit var app_save_button: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_apptab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val themeLinearLayout = view.findViewById<LinearLayout>(R.id.theme_selector);
        val chatScreenLayout = view.findViewById<LinearLayout>(R.id.chat_screen_selector);
        val repositoryLayout = view.findViewById<LinearLayout>(R.id.repository_selector);
        val appIconLayout = view.findViewById<LinearLayout>(R.id.app_icon_selector);
        val reminderLayout = view.findViewById<LinearLayout>(R.id.reminder_selector);
        app_save_button = view.findViewById(R.id.app_save_button);

        app_save_button.setOnClickListener(View.OnClickListener {
            val appSharedPreferences = AppPreferences(requireContext())
            selectedImageViewIds[0]?.let { lt1 -> appSharedPreferences.setBackgroundImage(lt1) }
            selectedImageViewIds[1]?.let { it1 -> appSharedPreferences.setChatBackgroundImage(it1) }
            selectedImageViewIds[2]?.let { lt1 -> appSharedPreferences.setOtherBackgroundImage(lt1) }
            startActivity(Intent(requireContext(), MainActivity::class.java))
        })

        val themes = listOf(
            R.drawable.theme_1,
            R.drawable.theme_2,
            R.drawable.theme_3,
            R.drawable.theme_4,
            R.drawable.theme_5,
            R.drawable.theme_6,
            R.drawable.theme_7,
            R.drawable.theme_8,
            R.drawable.theme_9
        )
        for (theme in themes) {
            val imageView = createImageView(theme, 0);
            themeLinearLayout.addView(imageView)
        }
        val chatscreens = listOf(
            R.drawable.color_screen1,
            R.drawable.color_screen2,
            R.drawable.color_screen3,
            R.drawable.color_screen4,
            R.drawable.color_screen6,
            R.drawable.color_screen7,
            R.drawable.color_screen8,
            R.drawable.color_screen9,
            R.drawable.chatscreen
        )
//
        for (chat_screen in chatscreens) {
            val imageView = createImageView(chat_screen, 1)
            chatScreenLayout.addView(imageView)
        }

        val repositories = listOf(
            R.drawable.color_screen1,
            R.drawable.color_screen2,
            R.drawable.color_screen3,
            R.drawable.color_screen4,
            R.drawable.color_screen6,
            R.drawable.color_screen7,
            R.drawable.color_screen8,
            R.drawable.color_screen9,
            R.drawable.color_screen9,
        )

        for (repository in repositories) {
            val imageView = createImageView(repository, 2)
            repositoryLayout.addView(imageView)
        }

        val app_icons = listOf(
            R.drawable.icon_1,
            R.drawable.icon_3,
            R.drawable.icon_1,
            R.drawable.icon_4,
            R.drawable.icon_5,
            R.drawable.icon_6,
        )

        for (repository in app_icons) {
            val imageView = createImageView(repository, 3)
            appIconLayout.addView(imageView)
        }
        val reminders = listOf(
            R.drawable.sound_1,
            R.drawable.sound_2,
            R.drawable.sound_3,
            R.drawable.sound_4,
            R.drawable.sound_5,
            R.drawable.sound_6,
        )

        for (repository in reminders) {
            val imageView = createImageView(repository, 4)
            reminderLayout.addView(imageView)
        }

        enableDragToDismiss(view)
    }

    @SuppressLint("ResourceAsColor")
    private fun createImageView(imageId: Int, index: Int): ImageView {
        val imageView = ImageView(requireContext())

        // Set layout parameters with margins
        when (index) {
            0 -> {
                val layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.circle_size),
                    resources.getDimensionPixelSize(R.dimen.circle_size)
                )
                layoutParams.marginEnd = resources.getDimensionPixelSize(R.dimen.circle_margin)
                imageView.layoutParams = layoutParams

                // Set the image resource
                imageView.setImageResource(imageId)

                // Create circular clip path
                imageView.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setOval(0, 0, view.width, view.height)
                    }
                }
                imageView.clipToOutline = true
            }
            1, 2 -> {
                val layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.chat_screen_width),
                    resources.getDimensionPixelSize(R.dimen.chat_screen_height)
                )
                layoutParams.marginEnd = resources.getDimensionPixelSize(R.dimen.circle_margin)
                imageView.layoutParams = layoutParams

                // Set the image resource
                imageView.setImageResource(imageId)
            }
            4 -> {
                val layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.chat_screen_width),
                    resources.getDimensionPixelSize(R.dimen.chat_screen_height)
                )
                layoutParams.marginEnd = resources.getDimensionPixelSize(R.dimen.circle_margin)
                imageView.layoutParams = layoutParams

                // Set the image resource
                imageView.setImageResource(imageId)
            }
            else -> {
                val layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.circle_size),
                    resources.getDimensionPixelSize(R.dimen.circle_size)
                )
                layoutParams.marginEnd = resources.getDimensionPixelSize(R.dimen.circle_margin)
                imageView.layoutParams = layoutParams

                // Set the image resource
                imageView.setImageResource(imageId)
            }
        }



        // Set scaleType to ensure content fits properly
        imageView.scaleType = ImageView.ScaleType.FIT_XY

        // Set click listener
        imageView.setOnClickListener {
            // Remove border from previously selected view
            selectedImageViews[index]?.let { previouslySelected ->
                val border = GradientDrawable()
                border.shape = GradientDrawable.OVAL
                border.setStroke(
                    resources.getDimensionPixelSize(R.dimen.border_width),
                    Color.BLACK
                )
                previouslySelected.foreground = null
            }

            // Create a new drawable for the border
            val borderDrawable = GradientDrawable()

            if (index == 0 ){
                borderDrawable.shape = GradientDrawable.OVAL
            } else {
                borderDrawable.shape = GradientDrawable.RECTANGLE
                borderDrawable.cornerRadius = 3F
            }
            borderDrawable.setStroke(
                resources.getDimensionPixelSize(R.dimen.border_width),
                resources.getColor(R.color.selected_border_color)
            )

            // Set the border as background
            imageView.foreground = borderDrawable

            // Update the selected view reference
            selectedImageViews[index] = imageView
            selectedImageViewIds[index] = imageId
        }
        return imageView
    }

}