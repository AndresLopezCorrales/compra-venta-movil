package com.example.appcomprayventa.Anuncios

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.appcomprayventa.Adaptadores.AdaptadorImagenSeleccionada
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelos.ModeloAnuncio
import com.example.appcomprayventa.Modelos.ModeloImagenSeleccionada
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ActivityCrearAnuncioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class CrearAnuncio : AppCompatActivity() {
    private lateinit var binding: ActivityCrearAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var imagenSelecArrayList: ArrayList<ModeloImagenSeleccionada>
    private lateinit var adaptadorImagenSel: AdaptadorImagenSeleccionada

    private var imageUri: Uri? = null

    // Registro del selector de fotos con un límite dinámico para la galería
    private val pickMultipleMedia = registerForActivityResult(object : ActivityResultContract<Int, List<Uri>>() {
        override fun createIntent(context: Context, input: Int): Intent {
            val intent = Intent(if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                MediaStore.ACTION_PICK_IMAGES
            } else {
                Intent.ACTION_GET_CONTENT
            })
            
            intent.type = "image/*"
            
            if (input > 1) {
                if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                    intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, input)
                } else {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
            }
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
            val list = mutableListOf<Uri>()
            if (resultCode == Activity.RESULT_OK && intent != null) {
                if (intent.clipData != null) {
                    val count = intent.clipData!!.itemCount
                    for (i in 0 until count) {
                        list.add(intent.clipData!!.getItemAt(i).uri)
                    }
                } else if (intent.data != null) {
                    list.add(intent.data!!)
                }
            }
            return list
        }
    }) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEachIndexed { index, uri ->
                if (imagenSelecArrayList.size < 5) {
                    val id = "${System.currentTimeMillis()}_$index"
                    val modelo = ModeloImagenSeleccionada(id, uri, null, false)
                    imagenSelecArrayList.add(modelo)
                }
            }
            adaptadorImagenSel.notifyDataSetChanged()
        }
    }

    // Registro para la cámara
    private val resultadoCamara_ARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            imageUri?.let { uri ->
                if (imagenSelecArrayList.size < 5) {
                    val id = "${System.currentTimeMillis()}"
                    val modelo = ModeloImagenSeleccionada(id, uri, null, false)
                    imagenSelecArrayList.add(modelo)
                    adaptadorImagenSel.notifyDataSetChanged()
                }
            }
        } else {
            Toast.makeText(this, "Captura cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    // Registro para permisos de cámara
    private val concederPermisoCamara = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
        var concedidoTodos = true
        for (seConcede in resultado.values) {
            concedidoTodos = concedidoTodos && seConcede
        }
        if (concedidoTodos) {
            imagenCamara()
        } else {
            Toast.makeText(this, "Los permisos de cámara/almacenamiento son necesarios", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        val adaptadorCat = ArrayAdapter(this, R.layout.item_categoria, Constantes.categorias)
        binding.Categoria.setAdapter(adaptadorCat)

        val adaptadorCon = ArrayAdapter(this, R.layout.item_condicion, Constantes.condiciones)
        binding.Condicion.setAdapter(adaptadorCon)

        imagenSelecArrayList = ArrayList()
        cargarImagenes()

        binding.agregarImg.setOnClickListener {
            if (imagenSelecArrayList.size < 5) {
                seleccionarImagenesDe()
            } else {
                Toast.makeText(this, "Solo puedes seleccionar un máximo de 5 imágenes", Toast.LENGTH_SHORT).show()
            }
        }

        binding.BtnCrearAnuncio.setOnClickListener {
            validarDatos()
        }
    }

    private fun cargarImagenes() {
        adaptadorImagenSel = AdaptadorImagenSeleccionada(this, imagenSelecArrayList)
        binding.RVImagenes.adapter = adaptadorImagenSel
    }

    private fun seleccionarImagenesDe() {
        val popupMenu = PopupMenu(this, binding.agregarImg)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Cámara")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galería")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> { // Cámara
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        concederPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                    } else {
                        concederPermisoCamara.launch(arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ))
                    }
                }
                2 -> { // Galería
                    val maxRestante = 5 - imagenSelecArrayList.size
                    pickMultipleMedia.launch(maxRestante)
                }
            }
            true
        }
    }

    private fun imagenCamara() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Imagen")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Descripcion")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        resultadoCamara_ARL.launch(intent)
    }

    private var marca = ""
    private var categoria = ""
    private var condicion = ""
    private var precio = ""
    private var titulo = ""
    private var descripcion = ""

    private fun validarDatos() {
        marca = binding.EtMarca.text.toString().trim()
        categoria = binding.Categoria.text.toString().trim()
        condicion = binding.Condicion.text.toString().trim()
        precio = binding.EtPrecio.text.toString().trim()
        titulo = binding.EtTitulo.text.toString().trim()
        descripcion = binding.EtDescripcion.text.toString().trim()

        if (marca.isEmpty()) {
            Toast.makeText(this, "Ingrese marca", Toast.LENGTH_SHORT).show()
        } else if (categoria.isEmpty()) {
            Toast.makeText(this, "Seleccione categoría", Toast.LENGTH_SHORT).show()
        } else if (condicion.isEmpty()) {
            Toast.makeText(this, "Seleccione condición", Toast.LENGTH_SHORT).show()
        } else if (precio.isEmpty()) {
            Toast.makeText(this, "Ingrese precio", Toast.LENGTH_SHORT).show()
        } else if (titulo.isEmpty()) {
            Toast.makeText(this, "Ingrese título", Toast.LENGTH_SHORT).show()
        } else if (descripcion.isEmpty()) {
            Toast.makeText(this, "Ingrese descripción", Toast.LENGTH_SHORT).show()
        } else if (imagenSelecArrayList.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos una imagen", Toast.LENGTH_SHORT).show()
        } else {
            subirImagenesStorage()
        }
    }

    private fun subirImagenesStorage() {
        progressDialog.setMessage("Subiendo imágenes...")
        progressDialog.show()

        val tiempo = Constantes.obtenerTiempoDis()
        // Usamos un array de tamaño fijo para mantener el orden exacto de las imágenes
        val urlImagenesArray = arrayOfNulls<String>(imagenSelecArrayList.size)
        var imagenesSubidasCount = 0

        val refDatabase = FirebaseDatabase.getInstance().getReference("Anuncios")
        val idAnuncio = refDatabase.push().key ?: "$tiempo"

        for (i in imagenSelecArrayList.indices) {
            val modelo = imagenSelecArrayList[i]
            val nombreImagen = "imagen_$i"
            val rutaImagen = "Anuncios/$idAnuncio/$nombreImagen"
            val storageRef = FirebaseStorage.getInstance().getReference(rutaImagen)

            storageRef.putFile(modelo.imagenUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    val uriTask = taskSnapshot.storage.downloadUrl
                    uriTask.addOnSuccessListener { uri ->
                        val urlImagenCargada = uri.toString()

                        // Guardamos la URL en la posición correspondiente (i) del array
                        urlImagenesArray[i] = urlImagenCargada
                        imagenesSubidasCount++

                        // Cuando todas las imágenes se hayan subido
                        if (imagenesSubidasCount == imagenSelecArrayList.size) {
                            val urlImagenesList = urlImagenesArray.filterNotNull()
                            guardarAnuncio(urlImagenesList, tiempo, idAnuncio)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun guardarAnuncio(urlImagenes: List<String>, tiempo: Long, idAnuncio: String) {
        progressDialog.setMessage("Guardando anuncio...")

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")

        val modeloAnuncio = ModeloAnuncio(
            idAnuncio,
            firebaseAuth.uid!!,
            marca,
            categoria,
            condicion,
            precio,
            titulo,
            descripcion,
            tiempo,
            urlImagenes
        )

        ref.child(idAnuncio).setValue(modeloAnuncio)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Anuncio publicado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
