package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelos.ModeloComentario
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ItemComentarioBinding

class AdaptadorComentario(
    private val context: Context,
    private val comentarioArrayList: ArrayList<ModeloComentario>
) : RecyclerView.Adapter<AdaptadorComentario.HolderComentario>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComentario {
        val binding = ItemComentarioBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComentario(binding)
    }

    override fun onBindViewHolder(holder: HolderComentario, position: Int) {
        val modelo = comentarioArrayList[position]

        val nombres = modelo.nombres
        val comentario = modelo.comentario
        val tiempo = modelo.tiempo
        val imagenPerfil = modelo.urlImagenPerfil

        val fecha = Constantes.obtenerFecha(tiempo)

        holder.binding.TvNombres.text = nombres
        holder.binding.TvComentario.text = comentario
        holder.binding.TvFecha.text = fecha

        try {
            Glide.with(context)
                .load(imagenPerfil)
                .placeholder(R.drawable.ic_imagen_perfil)
                .into(holder.binding.IvPerfil)
        } catch (e: Exception) {
            holder.binding.IvPerfil.setImageResource(R.drawable.ic_imagen_perfil)
        }
    }

    override fun getItemCount(): Int {
        return comentarioArrayList.size
    }

    inner class HolderComentario(val binding: ItemComentarioBinding) : RecyclerView.ViewHolder(binding.root)
}
