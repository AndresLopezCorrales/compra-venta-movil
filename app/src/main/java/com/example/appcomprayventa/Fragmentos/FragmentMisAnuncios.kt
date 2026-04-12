package com.example.appcomprayventa.Fragmentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appcomprayventa.Adaptadores.AdaptadorAnuncio
import com.example.appcomprayventa.Modelos.ModeloAnuncio
import com.example.appcomprayventa.databinding.FragmentMisAnunciosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentMisAnuncios : Fragment() {

    private lateinit var binding: FragmentMisAnunciosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var anuncioArrayList: ArrayList<ModeloAnuncio>
    private lateinit var adaptadorAnuncio: AdaptadorAnuncio

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMisAnunciosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        cargarMisAnuncios()
    }

    private fun cargarMisAnuncios() {
        anuncioArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.orderByChild("uid").equalTo(firebaseAuth.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return // Verificamos que el fragmento siga activo

                    anuncioArrayList.clear()
                    for (ds in snapshot.children) {
                        try {
                            val modeloAnuncio = ds.getValue(ModeloAnuncio::class.java)
                            if (modeloAnuncio != null) {
                                anuncioArrayList.add(modeloAnuncio)
                            }
                        } catch (e: Exception) {
                        }
                    }
                    
                    context?.let {
                        adaptadorAnuncio = AdaptadorAnuncio(it, anuncioArrayList)
                        binding.RVMisAnuncios.adapter = adaptadorAnuncio
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}
