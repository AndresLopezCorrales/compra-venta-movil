package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcomprayventa.Anuncios.DetalleAnuncio
import com.example.appcomprayventa.Modelos.ModeloAnuncio
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ItemAnuncioBinding

class AdaptadorAnuncio(
    private val context: Context,
    private val anuncioArrayList: ArrayList<ModeloAnuncio>
) : RecyclerView.Adapter<AdaptadorAnuncio.HolderAnuncio>() {

    private lateinit var binding: ItemAnuncioBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAnuncio {
        binding = ItemAnuncioBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAnuncio(binding.root)
    }

    override fun onBindViewHolder(holder: HolderAnuncio, position: Int) {
        val modelo = anuncioArrayList[position]

        val titulo = modelo.titulo
        val precio = modelo.precio
        val urlImagenes = modelo.urlImagenes

        holder.tituloTv.text = titulo
        holder.precioTv.text = "$ $precio"

        if (urlImagenes.isNotEmpty()) {
            try {
                Glide.with(context)
                    .load(urlImagenes[0])
                    .placeholder(R.drawable.ic_imagen_perfil)
                    .into(holder.imagenIv)
            } catch (e: Exception) {
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetalleAnuncio::class.java)
            intent.putExtra("idAnuncio", modelo.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return anuncioArrayList.size
    }

    inner class HolderAnuncio(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imagenIv = binding.imagenAnuncio
        var tituloTv = binding.tituloAnuncio
        var precioTv = binding.precioAnuncio
    }
}
