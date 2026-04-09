package com.example.appcomprayventa.Modelos

class ModeloAnuncio {
    var id: String = ""
    var uid: String = ""
    var marca: String = ""
    var categoria: String = ""
    var condicion: String = ""
    var precio: String = ""
    var titulo: String = ""
    var descripcion: String = ""
    var tiempo: Long = 0
    var urlImagenes: List<String> = emptyList()

    constructor()

    constructor(
        id: String,
        uid: String,
        marca: String,
        categoria: String,
        condicion: String,
        precio: String,
        titulo: String,
        descripcion: String,
        tiempo: Long,
        urlImagenes: List<String>
    ) {
        this.id = id
        this.uid = uid
        this.marca = marca
        this.categoria = categoria
        this.condicion = condicion
        this.precio = precio
        this.titulo = titulo
        this.descripcion = descripcion
        this.tiempo = tiempo
        this.urlImagenes = urlImagenes
    }
}
