package com.ocr.myapplication.ui.respositorydetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.ocr.myapplication.R
import enableDragToTopDismiss

class RepositoryDetailFragment(private val imageId: Int) : Fragment() {

    private lateinit var detailImage: ImageView
    override fun onCreateView (
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_repository_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detailImage = view.findViewById(R.id.repository_detail_image)
        detailImage.setBackgroundResource(imageId)
        enableDragToTopDismiss(view)
    }
}