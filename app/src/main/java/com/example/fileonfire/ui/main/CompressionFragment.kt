package com.example.fileonfire.ui.main

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.crashlytics.android.Crashlytics
import com.example.fileonfire.R
import com.example.fileonfire.databinding.CompressionFragmentBinding
import com.example.fileonfire.util.CHOOSE_IMAGE_REQUEST
import com.example.fileonfire.util.from
import com.example.fileonfire.util.getHumanReadableSize
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
        binding.loadConvertUploadImageFab.setOnClickListener {
            chooseImage()
        }
        if (savedInstanceState != null) {
            restoreImages()
        }
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            CHOOSE_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data == null || data.data == null || context == null) {
                Toast.makeText(context, R.string.failed_open_picture, Toast.LENGTH_SHORT).show()
                return
            }
            try {
                showCurrentImage(data)
                showCompressedImage()
            } catch (e: IOException) {
                Crashlytics.logException(e)
                Toast.makeText(context, R.string.failed_read_picture, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Shows the selected image and its size
     */
    private fun showCurrentImage(data: Intent) {
        mainViewModel.originalFile = from(requireContext(), data.data!!).also {
            Glide.with(requireContext()).load(it.first).into(binding.originalImageView)
            binding.originalSizeTextView.text = it.second
            mainViewModel.originalSize = it.second
        }.first
    }

    /**
     * Shows the compressed image and its size
     */
    private fun showCompressedImage() {
        mainViewModel.originalFile?.let {
            mainViewModel.convertFile(it)
            mainViewModel.resultFile.observe(viewLifecycleOwner, Observer {
                Glide.with(requireContext()).load(it).into(binding.compressedImageView)
                binding.compressedSizeTextView.text = getHumanReadableSize(
                    it.length(),
                    requireContext().resources,
                    R.string.compressed_size
                )
                mainViewModel.resultSize = binding.compressedSizeTextView.text.toString()
            })
        }
    }

    private fun restoreImages() {
        mainViewModel.originalFile?.let {
            Glide.with(requireContext()).load(it).into(binding.originalImageView)
            binding.originalSizeTextView.text = mainViewModel.originalSize
            Glide.with(requireContext()).load(mainViewModel.resultFile.value!!).into(binding.compressedImageView)
            binding.compressedSizeTextView.text = mainViewModel.resultSize
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
