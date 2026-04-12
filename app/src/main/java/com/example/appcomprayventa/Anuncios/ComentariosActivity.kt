package com.example.appcomprayventa.Anuncios

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appcomprayventa.Adaptadores.AdaptadorComentario
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelos.ModeloComentario
import com.example.appcomprayventa.databinding.ActivityComentariosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ComentariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComentariosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var idAnuncio = ""

    private lateinit var comentarioArrayList: ArrayList<ModeloComentario>
    private lateinit var adaptadorComentario: AdaptadorComentario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComentariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        idAnuncio = intent.getStringExtra("idAnuncio") ?: ""

        cargarTituloAnuncio()
        cargarComentarios()

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.BtnPostearComentario.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "Inicie sesión para comentar", Toast.LENGTH_SHORT).show()
            } else {
                validarComentario()
            }
        }
    }

    private fun cargarTituloAnuncio() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val titulo = "${snapshot.child("titulo").value}"
                binding.TvTituloAnuncio.text = titulo
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun validarComentario() {
        val comentario = binding.EtComentario.text.toString().trim()

        if (comentario.isEmpty()) {
            Toast.makeText(this, "Escribe un comentario", Toast.LENGTH_SHORT).show()
        } else {
            agregarComentario(comentario)
        }
    }

    private fun agregarComentario(comentario: String) {
        val tiempo = Constantes.obtenerTiempoDis()
        val refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios")
        refUsuarios.child(firebaseAuth.uid!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombres = "${snapshot.child("nombres").value}"
                val urlImagenPerfil = "${snapshot.child("urlImagenPerfil").value}"
                
                val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
                val idComentario = "${ref.push().key}"

                val modeloComentario = ModeloComentario(
                    idComentario,
                    firebaseAuth.uid!!,
                    nombres,
                    urlImagenPerfil,
                    comentario,
                    tiempo
                )

                ref.child(idAnuncio).child("Comentarios").child(idComentario)
                    .setValue(modeloComentario)
                    .addOnSuccessListener {
                        binding.EtComentario.setText("")
                        Toast.makeText(this@ComentariosActivity, "Comentario publicado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@ComentariosActivity, "${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun cargarComentarios() {
        comentarioArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Comentarios")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    comentarioArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelo = ds.getValue(ModeloComentario::class.java)
                        if (modelo != null) {
                            comentarioArrayList.add(modelo)
                        }
                    }
                    adaptadorComentario = AdaptadorComentario(this@ComentariosActivity, comentarioArrayList)
                    binding.RVComentarios.adapter = adaptadorComentario
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
