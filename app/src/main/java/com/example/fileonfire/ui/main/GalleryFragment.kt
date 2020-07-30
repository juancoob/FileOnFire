package com.example.fileonfire.ui.main

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fileonfire.R
import com.example.fileonfire.databinding.GalleryFragmentBinding
import com.example.fileonfire.util.DEFAULT_PATH
import com.example.fileonfire.util.GALLERY_STATE
import com.google.android.flexbox.FlexDirection.ROW
import com.google.android.flexbox.FlexWrap.WRAP
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


/**
 * Shows the gallery with the compressed images
 */
class GalleryFragment : Fragment() {

    private var _binding: GalleryFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var layoutManager: FlexboxLayoutManager
    private var galleryState: Parcelable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = GalleryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initRecyclerView()
        savedInstanceState?.let {
            it.containsKey(GALLERY_STATE).run {
                galleryState = it.getParcelable(GALLERY_STATE)
            }
        }
    }

    /**
     * Init the recyclerview by using a Flexbox layout
     */
    private fun initRecyclerView() {
        layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = ROW
        layoutManager.flexWrap = WRAP
        binding.galleryRecyclerView.layoutManager = layoutManager
        binding.galleryRecyclerView.adapter = FlexboxAdapter()
    }

    override fun onResume() {
        super.onResume()
        downloadImages()
    }

    /**
     * Downloads the available images, and calls to restore the recyclerview position if it was restored
     */
    private fun downloadImages() {
        val listRef = Firebase.storage.reference.child(DEFAULT_PATH)
        listRef.listAll().addOnSuccessListener { listResult ->
            (binding.galleryRecyclerView.adapter as FlexboxAdapter).updateUriList(listResult.items)
            restoreRecyclerView()
        }.addOnFailureListener() {
            Snackbar.make(
                requireView(),
                getString(R.string.something_happened),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    /**
     * If the recyclerview was restored on configuration changes, loads the previous position
     */
    private fun restoreRecyclerView() {
        if (galleryState != null) {
            layoutManager.onRestoreInstanceState(galleryState)
            galleryState = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(GALLERY_STATE, layoutManager.onSaveInstanceState())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}