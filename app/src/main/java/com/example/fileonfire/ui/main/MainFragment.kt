package com.example.fileonfire.ui.main

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.crashlytics.android.Crashlytics
import com.example.fileonfire.R
import com.example.fileonfire.databinding.MainFragmentBinding
import com.example.fileonfire.util.CHOOSE_IMAGE_REQUEST
import com.example.fileonfire.util.from
import id.zelory.compressor.loadBitmap
import java.io.File
import java.io.IOException

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var _binding: MainFragmentBinding? = null
    private val binding
        get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private var currentFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding.loadConvertUploadImageButton.setOnClickListener {
            chooseImage()
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
        if(requestCode == CHOOSE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data == null || data.data == null || context == null) {
                Toast.makeText(context, R.string.failed_open_picture, Toast.LENGTH_SHORT).show()
                return
            }
            try {
                currentFile = from(context!!, data.data!!)?.also {
                    binding.originalImageView.setImageBitmap(loadBitmap(it))
                    // todo Add size: binding.originalSizeTextView.setText(String.format(getString(R.string.actual_size), it.))
                }
            } catch (e: IOException) {
                Crashlytics.logException(e)
                Toast.makeText(context, R.string.failed_read_picture, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
