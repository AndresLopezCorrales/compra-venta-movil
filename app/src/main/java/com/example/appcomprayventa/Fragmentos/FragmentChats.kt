package com.example.appcomprayventa.Fragmentos

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcomprayventa.Adaptadores.AdaptadorUsuario
import com.example.appcomprayventa.Modelos.Usuario
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.FragmentChatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FragmentChats : Fragment() {
    private lateinit var binding : FragmentChatsBinding

    private lateinit var mContext : Context

    private var usuarioAdaptador: AdaptadorUsuario ?= null
    private var usuarioLista : List<Usuario> ?= null

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatsBinding.inflate(layoutInflater, container, false)

        binding.RVUsuarios.setHasFixedSize(true)
        binding.RVUsuarios.layoutManager = LinearLayoutManager(mContext)

        usuarioLista = ArrayList()

        binding.EtBuscarUsuario.doOnTextChanged {usuario, start, before, count ->
            buscarUsuario(usuario.toString())
        } //AQUI ME QUEDÉ

        listarUsuarios()

        return binding.root
    }

    private fun listarUsuarios() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid
        val reference = FirebaseDatabase.getInstance().reference.child("Usuarios").orderByChild("nombres")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (usuarioLista as ArrayList<Usuario>).clear()

                for(sn in snapshot.children){
                    val usuario : Usuario? = sn.getValue(Usuario::class.java)
                    if(!(usuario!!.uid).equals(firebaseUser)){
                        (usuarioLista as ArrayList<Usuario>).add(usuario)
                    }
                }

                //si la lista esta vacia, mostramos el mensaje y ocultamos el recylcer
                if ((usuarioLista as java.util.ArrayList<Usuario>).isEmpty()){
                    binding.tvSinUsuarios.visibility = View.VISIBLE
                    binding.RVUsuarios.visibility = View.GONE

                    //si hay mas usuarios, ocultamos elk mensaje y mostramos la lista
                }else{
                    binding.tvSinUsuarios.visibility = View.GONE
                    binding.RVUsuarios.visibility = View.VISIBLE

                    //Actualizamos el adaptador
                    usuarioAdaptador = AdaptadorUsuario(mContext, usuarioLista!!)
                    binding.RVUsuarios.adapter = usuarioAdaptador
                }




            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError","Error al leer usuarios: ${error.message}")
                Toast.makeText(mContext, "Error al cargar usuarios: ${error.message}", Toast.LENGTH_SHORT).show()

            }

        })


    }

    private fun buscarUsuario(usuario: String) {

        // Obtenemos el UID del usuario actual
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid

        val reference = FirebaseDatabase.getInstance().reference
            .child("Usuarios")
            .orderByChild("nombres")
            .startAt(usuario)
            .endAt(usuario + "\uf8ff")

        reference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                (usuarioLista as ArrayList<Usuario>).clear()

                for (ss in snapshot.children) {
                    val usuarioObj = ss.getValue(Usuario::class.java)

                    // Evitamos agregarnos a nosotros mismos
                    if (usuarioObj != null && usuarioObj.uid != firebaseUser) {
                        (usuarioLista as ArrayList<Usuario>).add(usuarioObj)
                    }
                }

                // Actualizamos el adaptador
                usuarioAdaptador = AdaptadorUsuario(context!!, usuarioLista!!)
                binding.RVUsuarios.adapter = usuarioAdaptador
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error al buscar usuarios: ${error.message}")

                Toast.makeText(
                    mContext,
                    "Error al buscar: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}