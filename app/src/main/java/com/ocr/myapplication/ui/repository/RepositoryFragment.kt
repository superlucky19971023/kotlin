package com.ocr.myapplication.ui.repository

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.ocr.myapplication.R
import com.ocr.myapplication.sharedpreference.AppPreferences

class RepositoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DocumentAdapter
    private lateinit var background: ConstraintLayout
    private var documentClickListener: OnDocumentClickListener? = null

    // Interface for document click events
    interface OnDocumentClickListener {
        fun onDocumentClicked(document: Document)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Try to set the listener automatically if the parent implements it
        if (context is OnDocumentClickListener) {
            documentClickListener = context
        } else if (parentFragment is OnDocumentClickListener) {
            documentClickListener = parentFragment as OnDocumentClickListener
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_repository, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.documentsRecyclerView)
        background = view.findViewById(R.id.repository_background)

        val appPreferences = AppPreferences(requireContext())
        background.setBackgroundResource(appPreferences.getOtherBackgroundImage())

        // Set up staggered grid layout with 3 columns
        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager

        // Sample document data
        val documents = listOf(
            Document("Company Letterhead", R.drawable.document1, 1, 1),
            Document("Business Letter", R.drawable.document2, 1, 1),
            Document("Legal Document", R.drawable.document3, 1, 1),
            Document("New Form", R.drawable.document4, 1, 1),
            Document("Contract", R.drawable.document5, 2, 1),
            Document("Certificate", R.drawable.document6, 1, 1),
            Document("Warranty Deed", R.drawable.document7, 1, 2),
            Document("Agreement", R.drawable.document8, 1, 1),
            Document("Registration Form", R.drawable.document9, 1, 1)
        )

        adapter = DocumentAdapter(documents)
        recyclerView.adapter = adapter
    }

    // Method to manually set the click listener
    fun setOnDocumentClickListener(listener: OnDocumentClickListener) {
        this.documentClickListener = listener
    }

    // Document data class
    data class Document(
        val title: String,
        val imageResId: Int,
        val spanSize: Int = 1,  // How many columns this item spans
        val heightRatio: Int = 1 // Height ratio multiplier
    )

    // Adapter for the document grid
    inner class DocumentAdapter(private val documents: List<Document>) :
        RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_document, parent, false)
            return DocumentViewHolder(view)
        }

        override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
            val document = documents[position]
            holder.bind(document)
        }

        override fun getItemCount() = documents.size

        inner class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val documentPreview: ImageView = itemView.findViewById(R.id.documentPreview)
            private val documentTitle: TextView = itemView.findViewById(R.id.documentTitle)

            fun bind(document: Document) {
                // Load image with Glide
                Glide.with(itemView.context)
                    .load(document.imageResId)
                    .into(documentPreview)

                // Set title if needed
                documentTitle.text = document.title

                // Adjust height based on ratio
                val params = itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
                params.isFullSpan = document.spanSize > 2
                itemView.layoutParams = params

                // Set click listener
                itemView.setOnClickListener {
                    // Use the callback to notify about the click
                    documentClickListener?.onDocumentClicked(document)
                }
            }
        }
    }

    companion object {
        // Factory method to create a new instance of the fragment
        fun newInstance(): RepositoryFragment {
            return RepositoryFragment()
        }
    }
}