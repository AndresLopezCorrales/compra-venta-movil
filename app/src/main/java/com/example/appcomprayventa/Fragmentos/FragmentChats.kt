package com.example.appcomprayventa.Fragmentos

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.appcomprayventa.Adaptadores.AdaptadorUsuario
import com.example.appcomprayventa.Modelos.Usuario
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.FragmentChatsBinding


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
        return binding.root
    }
}