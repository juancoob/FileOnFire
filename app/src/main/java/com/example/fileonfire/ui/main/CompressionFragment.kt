package com.example.fileonfire.ui.main

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ablanco.zoomy.Zoomy
import com.bumptech.glide.Glide
import com.crashlytics.android.Crashlytics
import com.example.fileonfire.R
import com.example.fileonfire.databinding.CompressionFragmentBinding
import com.example.fileonfire.util.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException

/**
 * This class manages the compression from the selected image
 */
class CompressionFragment : Fragment() {

    private var _binding: CompressionFragmentBinding? = null
    private val binding
        get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CompressionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        registerZoomyViews()
        binding.loadConvertUploadImageFab.setOnClickListener {
            chooseImage()
        }
        observeResult()

        if (savedInstanceState != null) {
            restoreImages()
        }
    }

    /**
     * Adds the zoom ability to see images
     */
    private fun registerZoomyViews() {
        val zoomyBuilderOriginalImageView: Zoomy.Builder = Zoomy.Builder(activity)
            .target(binding.originalImageView)
            .enableImmersiveMode(false)
        zoomyBuilderOriginalImageView.register()

        val zoomyBuilderCompressedImageView: Zoomy.Builder = Zoomy.Builder(activity)
            .target(binding.compressedImageView)
            .enableImmersiveMode(false)
        zoomyBuilderCompressedImageView.register()
    }

    /**
     * Opens the image selector
     */
    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            CHOOSE_IMAGE_REQUEST
        )
    }

    /**
     * Observes the MutableLiveData resultFile to show it and upload the file
     */
    private fun observeResult() {
        mainViewModel.resultFile.observe(viewLifecycleOwner, Observer {
            Glide.with(requireContext()).load(it).into(binding.compressedImageView)
            binding.compressedSizeTextView.text = getHumanReadableSize(
                it.length(),
                requireContext().resources,
                R.string.compressed_size
            )
            mainViewModel.resultSize = binding.compressedSizeTextView.text.toString()
            uploadFile(it)
        })
    }

    /**
     * Uploads a file to firebase
     */
    private fun uploadFile(file: File) {
        val storageRef = Firebase.storage.reference
        val storageFileRef = storageRef.child("${DEFAULT_PATH}${file.name}")
        val uploadTask = storageFileRef.putFile(Uri.fromFile(file))
        uploadTask.addOnFailureListener {
            if (this.isVisible) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.image_not_listed),
                    Snackbar.LENGTH_SHORT
                ).show()
                Log.e("TAG", it.toString())
                Crashlytics.logException(it)
            }
        }.addOnSuccessListener {
            if (this.isVisible) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.image_listed),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data == null || data.data == null || context == null) {
                Toast.makeText(context, R.string.failed_open_picture, Toast.LENGTH_SHORT).show()
                return
            }
            try {
                showOriginalImage(data)
                convertOriginalImage()
            } catch (e: IOException) {
                Crashlytics.logException(e)
                Toast.makeText(context, R.string.failed_read_picture, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Shows the selected image and its size
     */
    private fun showOriginalImage(data: Intent) {
        mainViewModel.originalFile = from(requireContext(), data.data!!).also {
            Glide.with(requireContext()).load(it.first).into(binding.originalImageView)
            binding.originalSizeTextView.text = it.second
            mainViewModel.originalSize = it.second
        }.first
    }

    /**
     * Converts the original image
     */
    private fun convertOriginalImage() {
        mainViewModel.originalFile?.let {
            mainViewModel.convertFile(it)
        }
    }

    /**
     * On configuration changes, restore the main image and its compressed version
     */
    private fun restoreImages() {
        mainViewModel.originalFile?.let {
            Glide.with(requireContext()).load(it).into(binding.originalImageView)
            binding.originalSizeTextView.text = mainViewModel.originalSize
            Glide.with(requireContext()).load(mainViewModel.resultFile.value!!)
                .into(binding.compressedImageView)
            binding.compressedSizeTextView.text = mainViewModel.resultSize
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
