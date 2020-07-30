package com.example.fileonfire.ui.main

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ablanco.zoomy.Zoomy
import com.example.fileonfire.databinding.GalleryImageItemBinding
import com.example.fileonfire.util.GlideApp
import com.google.firebase.storage.StorageReference


/**
 * Manages the Flexbox adapter
 */
class FlexboxAdapter : RecyclerView.Adapter<FlexboxAdapter.FlexboxViewHolder>() {

    var storageReferenceList = arrayListOf<StorageReference>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlexboxViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val galleryImageItemBinding: GalleryImageItemBinding =
            GalleryImageItemBinding.inflate(layoutInflater, parent, false)
        return FlexboxViewHolder(galleryImageItemBinding)
    }

    override fun onBindViewHolder(holder: FlexboxViewHolder, position: Int) {
        holder.populate(storageReferenceList[position])
    }

    override fun getItemCount(): Int {
        return storageReferenceList.size
    }

    fun updateUriList(storageReferenceList: List<StorageReference>) {
        this.storageReferenceList.clear()
        this.storageReferenceList.addAll(storageReferenceList)
        notifyDataSetChanged()
    }

    class FlexboxViewHolder(private var galleryImageItemBinding: GalleryImageItemBinding) :
        RecyclerView.ViewHolder(galleryImageItemBinding.root) {
        fun populate(storageReference: StorageReference) {
            GlideApp.with(itemView.context).load(storageReference)
                .into(galleryImageItemBinding.galleryImageView)
            val zoomyBuilderGalleryImageView: Zoomy.Builder = Zoomy.Builder(itemView.context as Activity)
                .target(galleryImageItemBinding.galleryImageView)
                .enableImmersiveMode(false)
            zoomyBuilderGalleryImageView.register()
        }
    }
}