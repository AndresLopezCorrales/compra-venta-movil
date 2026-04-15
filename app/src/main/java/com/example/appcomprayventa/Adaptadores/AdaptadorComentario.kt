package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelos.ModeloComentario
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ItemComentarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdaptadorComentario(
    private val context: Context,
    private val comentarioArrayList: ArrayList<ModeloComentario>,
    private val idAnuncio: String,
    private val esRespuesta: Boolean = false,
    private val idComentarioPadre: String? = null
) : RecyclerView.Adapter<AdaptadorComentario.HolderComentario>() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComentario {
        val binding = ItemComentarioBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComentario(binding)
    }

    override fun onBindViewHolder(holder: HolderComentario, position: Int) {
        val modelo = comentarioArrayList[position]
        val idComentario = modelo.id
        val nombres = modelo.nombres
        val comentario = modelo.comentario
        val tiempo = modelo.tiempo
        val imagenPerfil = modelo.urlImagenPerfil

        holder.binding.TvNombres.text = "@$nombres"
        holder.binding.TvComentario.text = comentario
        holder.binding.TvFecha.text = Constantes.obtenerFecha(tiempo)

        try {
            Glide.with(context)
                .load(imagenPerfil)
                .placeholder(R.drawable.ic_imagen_perfil)
                .into(holder.binding.IvPerfil)
        } catch (e: Exception) {
            holder.binding.IvPerfil.setImageResource(R.drawable.ic_imagen_perfil)
        }

        // Determinar ruta de votos
        val refVotos = if (!esRespuesta) {
            FirebaseDatabase.getInstance().getReference("Anuncios").child(idAnuncio).child("Comentarios").child(idComentario).child("Votos")
        } else {
            FirebaseDatabase.getInstance().getReference("Anuncios").child(idAnuncio).child("Comentarios").child(idComentarioPadre!!).child("Respuestas").child(idComentario).child("Votos")
        }

        cargarVotos(refVotos, holder)

        holder.binding.BtnLikeComentario.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(context, "Inicie sesión para votar", Toast.LENGTH_SHORT).show()
            } else {
                votar(refVotos, 1)
            }
        }

        holder.binding.BtnDislikeComentario.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(context, "Inicie sesión para votar", Toast.LENGTH_SHORT).show()
            } else {
                votar(refVotos, -1)
            }
        }

        if (esRespuesta) {
            holder.binding.TvResponder.visibility = View.GONE
            holder.binding.LLVerRespuestas.visibility = View.GONE
            holder.binding.RVRespuestas.visibility = View.GONE
            holder.binding.LLRespuesta.visibility = View.GONE
            holder.binding.lineaHilo.visibility = View.GONE
        } else {
            holder.binding.TvResponder.visibility = View.VISIBLE

            holder.binding.TvResponder.setOnClickListener {
                if (firebaseAuth.currentUser == null) {
                    Toast.makeText(context, "Inicie sesión para responder", Toast.LENGTH_SHORT).show()
                } else {
                    holder.binding.LLRespuesta.visibility = View.VISIBLE
                }
            }

            holder.binding.BtnCancelarRespuesta.setOnClickListener {
                holder.binding.LLRespuesta.visibility = View.GONE
                holder.binding.EtRespuesta.setText("")
            }

            holder.binding.BtnEnviarRespuesta.setOnClickListener {
                val respuesta = holder.binding.EtRespuesta.text.toString().trim()
                if (respuesta.isEmpty()) {
                    Toast.makeText(context, "Escribe una respuesta", Toast.LENGTH_SHORT).show()
                } else {
                    val palabras = respuesta.split("\\s+".toRegex()).filter { it.isNotEmpty() }
                    if (palabras.size > 500) {
                        Toast.makeText(context, "Límite de 500 palabras excedido", Toast.LENGTH_SHORT).show()
                    } else {
                        enviarRespuesta(respuesta, idComentario, holder)
                    }
                }
            }

            cargarRespuestas(idComentario, holder)

            holder.binding.LLVerRespuestas.setOnClickListener {
                if (holder.binding.RVRespuestas.visibility == View.GONE) {
                    holder.binding.RVRespuestas.visibility = View.VISIBLE
                    holder.binding.IvVerRespuestas.rotation = 90f
                    holder.binding.lineaHilo.visibility = View.VISIBLE
                } else {
                    holder.binding.RVRespuestas.visibility = View.GONE
                    holder.binding.IvVerRespuestas.rotation = 0f
                    holder.binding.lineaHilo.visibility = View.GONE
                }
            }
        }
    }

    private fun cargarVotos(refVotos: com.google.firebase.database.DatabaseReference, holder: HolderComentario) {
        refVotos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likes = snapshot.child("Likes").childrenCount
                val dislikes = snapshot.child("Dislikes").childrenCount
                holder.binding.TvLikesCount.text = "$likes"
                holder.binding.TvDislikesCount.text = "$dislikes"

                if (firebaseAuth.currentUser != null) {
                    val uid = firebaseAuth.uid!!
                    if (snapshot.child("Likes").hasChild(uid)) {
                        holder.binding.BtnLikeComentario.setImageResource(R.drawable.ic_like_filled)
                        holder.binding.BtnDislikeComentario.setImageResource(R.drawable.ic_dislike)
                    } else if (snapshot.child("Dislikes").hasChild(uid)) {
                        holder.binding.BtnLikeComentario.setImageResource(R.drawable.ic_like)
                        holder.binding.BtnDislikeComentario.setImageResource(R.drawable.ic_dislike_filled)
                    } else {
                        holder.binding.BtnLikeComentario.setImageResource(R.drawable.ic_like)
                        holder.binding.BtnDislikeComentario.setImageResource(R.drawable.ic_dislike)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun votar(refVotos: com.google.firebase.database.DatabaseReference, tipo: Int) {
        val uid = firebaseAuth.uid!!
        val nodoPropio = if (tipo == 1) "Likes" else "Dislikes"
        val nodoOpuesto = if (tipo == 1) "Dislikes" else "Likes"

        refVotos.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(nodoPropio).hasChild(uid)) {
                    refVotos.child(nodoPropio).child(uid).removeValue()
                } else {
                    refVotos.child(nodoPropio).child(uid).setValue(true)
                    refVotos.child(nodoOpuesto).child(uid).removeValue()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun enviarRespuesta(respuesta: String, idComentario: String, holder: HolderComentario) {
        val tiempo = Constantes.obtenerTiempoDis()
        val refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios")
        refUsuarios.child(firebaseAuth.uid!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombres = "${snapshot.child("nombres").value}"
                val urlImagenPerfil = "${snapshot.child("urlImagenPerfil").value}"

                val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
                val idRespuesta = ref.push().key ?: ""

                val modeloRespuesta = ModeloComentario(
                    idRespuesta,
                    firebaseAuth.uid!!,
                    nombres,
                    urlImagenPerfil,
                    respuesta,
                    tiempo
                )

                ref.child(idAnuncio).child("Comentarios").child(idComentario).child("Respuestas").child(idRespuesta)
                    .setValue(modeloRespuesta)
                    .addOnSuccessListener {
                        holder.binding.EtRespuesta.setText("")
                        holder.binding.LLRespuesta.visibility = View.GONE
                        Toast.makeText(context, "Respuesta enviada", Toast.LENGTH_SHORT).show()
                    }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun cargarRespuestas(idComentario: String, holder: HolderComentario) {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Comentarios").child(idComentario).child("Respuestas")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val respuestaArrayList = ArrayList<ModeloComentario>()
                    for (ds in snapshot.children) {
                        val modelo = ds.getValue(ModeloComentario::class.java)
                        if (modelo != null) {
                            respuestaArrayList.add(modelo)
                        }
                    }
                    if (respuestaArrayList.isNotEmpty()) {
                        holder.binding.LLVerRespuestas.visibility = View.VISIBLE
                        holder.binding.TvCantidadRespuestas.text = if (respuestaArrayList.size == 1) "1 respuesta" else "${respuestaArrayList.size} respuestas"
                        
                        val adaptadorRespuestas = AdaptadorComentario(context, respuestaArrayList, idAnuncio, true, idComentario)
                        holder.binding.RVRespuestas.adapter = adaptadorRespuestas
                    } else {
                        holder.binding.LLVerRespuestas.visibility = View.GONE
                        holder.binding.RVRespuestas.visibility = View.GONE
                        holder.binding.lineaHilo.visibility = View.GONE
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun getItemCount(): Int = comentarioArrayList.size

    inner class HolderComentario(val binding: ItemComentarioBinding) : RecyclerView.ViewHolder(binding.root)
}
