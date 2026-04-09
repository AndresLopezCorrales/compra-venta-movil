package com.example.appcomprayventa.Anuncios

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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetalleAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleAnuncioBinding
    private var idAnuncio = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idAnuncio = intent.getStringExtra("idAnuncio") ?: ""

        cargarDetalleAnuncio()

        binding.BtnRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.BtnChat.setOnClickListener {
            // Lógica para abrir el chat
            Toast.makeText(this, "Función de chat próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDetalleAnuncio() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val modeloAnuncio = snapshot.getValue(ModeloAnuncio::class.java)
                    if (modeloAnuncio != null) {
                        val titulo = modeloAnuncio.titulo
                        val precio = modeloAnuncio.precio
                        val descripcion = modeloAnuncio.descripcion
                        val marca = modeloAnuncio.marca
                        val categoria = modeloAnuncio.categoria
                        val condicion = modeloAnuncio.condicion
                        val tiempo = modeloAnuncio.tiempo
                        val uidVendedor = modeloAnuncio.uid
                        val urlImagenes = modeloAnuncio.urlImagenes

                        val fecha = Constantes.obtenerFecha(tiempo)

                        binding.TvTitulo.text = titulo
                        binding.TvPrecio.text = "$ $precio"
                        binding.TvDescripcion.text = descripcion
                        binding.TvMarca.text = marca
                        binding.TvCategoria.text = categoria
                        binding.TvCondicion.text = condicion
                        binding.TvFecha.text = "Publicado: $fecha"

                        cargarImagenes(urlImagenes)
                        cargarInfoVendedor(uidVendedor)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
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
                    } catch (e: Exception) {
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}
