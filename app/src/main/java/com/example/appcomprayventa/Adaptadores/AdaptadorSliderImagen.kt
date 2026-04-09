package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ItemImagenSliderBinding

class AdaptadorSliderImagen(
    private val context: Context,
    private val urlImagenList: List<String>
) : RecyclerView.Adapter<AdaptadorSliderImagen.HolderSliderImagen>() {

    private lateinit var binding: ItemImagenSliderBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderSliderImagen {
        binding = ItemImagenSliderBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderSliderImagen(binding.root)
    }

    override fun onBindViewHolder(holder: HolderSliderImagen, position: Int) {
        val urlImagen = urlImagenList[position]

        try {
            Glide.with(context)
                .load(urlImagen)
                .placeholder(R.drawable.ic_imagen_perfil)
                .into(holder.imagenIv)
        } catch (e: Exception) {
        }
    }

    override fun getItemCount(): Int {
        return urlImagenList.size
    }

    inner class HolderSliderImagen(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imagenIv = binding.imagenSlider
    }
}
