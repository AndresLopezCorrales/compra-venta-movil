package com.example.appcomprayventa.Anuncios

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.appcomprayventa.Adaptadores.AdaptadorSliderImagen
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelos.ModeloAnuncio
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ActivityDetalleAnuncioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetalleAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var idAnuncio = ""
    private var estadoVoto = 0 // 0: nada, 1: like, -1: dislike

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        idAnuncio = intent.getStringExtra("idAnuncio") ?: ""

        cargarDetalleAnuncio()
        cargarVotos()

        binding.BtnRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.BtnLike.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "Inicie sesión para votar", Toast.LENGTH_SHORT).show()
            } else {
                votar(1)
            }
        }

        binding.BtnDislike.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "Inicie sesión para votar", Toast.LENGTH_SHORT).show()
            } else {
                votar(-1)
            }
        }

        binding.FABComentarios.setOnClickListener {
            val intent = Intent(this, ComentariosActivity::class.java)
            intent.putExtra("idAnuncio", idAnuncio)
            startActivity(intent)
        }

        binding.BtnChat.setOnClickListener {
            Toast.makeText(this, "Función de chat próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarVotos() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").child(idAnuncio).child("Votos")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Conteos de Likes y Dislikes usando childrenCount
                val likes = snapshot.child("Likes").childrenCount
                val dislikes = snapshot.child("Dislikes").childrenCount
                binding.TvLikesCount.text = "$likes"
                binding.TvDislikesCount.text = "$dislikes"

                // Determinar el estado del voto del usuario actual
                if (firebaseAuth.currentUser != null) {
                    val uid = firebaseAuth.uid!!
                    if (snapshot.child("Likes").hasChild(uid)) {
                        estadoVoto = 1
                    } else if (snapshot.child("Dislikes").hasChild(uid)) {
                        estadoVoto = -1
                    } else {
                        estadoVoto = 0
                    }
                    actualizarUIBotonesVoto()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun actualizarUIBotonesVoto() {
        when (estadoVoto) {
            1 -> {
                binding.BtnLike.setImageResource(R.drawable.ic_like_filled)
                binding.BtnDislike.setImageResource(R.drawable.ic_dislike)
            }
            -1 -> {
                binding.BtnLike.setImageResource(R.drawable.ic_like)
                binding.BtnDislike.setImageResource(R.drawable.ic_dislike_filled)
            }
            else -> {
                binding.BtnLike.setImageResource(R.drawable.ic_like)
                binding.BtnDislike.setImageResource(R.drawable.ic_dislike)
            }
        }
    }

    private fun votar(tipo: Int) {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").child(idAnuncio).child("Votos")
        val uid = firebaseAuth.uid!!

        val nodoPropio = if (tipo == 1) "Likes" else "Dislikes"
        val nodoOpuesto = if (tipo == 1) "Dislikes" else "Likes"

        if (estadoVoto == tipo) {
            // Si ya votó lo mismo, lo quita (Toggle)
            ref.child(nodoPropio).child(uid).removeValue()
        } else {
            // Agrega el voto al nodo correspondiente y lo quita del opuesto
            ref.child(nodoPropio).child(uid).setValue(true)
            ref.child(nodoOpuesto).child(uid).removeValue()
        }
    }

    private fun cargarDetalleAnuncio() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val modeloAnuncio = snapshot.getValue(ModeloAnuncio::class.java)
                    if (modeloAnuncio != null) {
                        binding.TvTitulo.text = modeloAnuncio.titulo
                        binding.TvPrecio.text = "$ ${modeloAnuncio.precio}"
                        binding.TvDescripcion.text = modeloAnuncio.descripcion
                        binding.TvMarca.text = modeloAnuncio.marca
                        binding.TvCategoria.text = modeloAnuncio.categoria
                        binding.TvCondicion.text = modeloAnuncio.condicion
                        binding.TvFecha.text = "Publicado: ${Constantes.obtenerFecha(modeloAnuncio.tiempo)}"

                        cargarImagenes(modeloAnuncio.urlImagenes)
                        cargarInfoVendedor(modeloAnuncio.uid)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun cargarImagenes(urlImagenes: List<String>) {
        val adaptadorSlider = AdaptadorSliderImagen(this, urlImagenes)
        binding.viewPagerImagenes.adapter = adaptadorSlider
        binding.TvContadorImg.text = "1/${urlImagenes.size}"
        binding.viewPagerImagenes.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.TvContadorImg.text = "${position + 1}/${urlImagenes.size}"
            }
        })
    }

    private fun cargarInfoVendedor(uidVendedor: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidVendedor)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"
                    binding.TvVendedor.text = nombres
                    try {
                        Glide.with(applicationContext)
                            .load(imagen)
                            .placeholder(R.drawable.ic_imagen_perfil)
                            .into(binding.IvVendedor)
                    } catch (e: Exception) {}
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}