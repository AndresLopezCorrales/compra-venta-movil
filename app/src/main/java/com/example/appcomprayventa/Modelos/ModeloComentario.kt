package com.example.appcomprayventa.Modelos

class ModeloComentario {
    var id = ""
    var uid = ""
    var nombres = ""
    var urlImagenPerfil = ""
    var comentario = ""
    var tiempo: Long = 0

    constructor()

    constructor(id: String, uid: String, nombres: String, urlImagenPerfil: String, comentario: String, tiempo: Long) {
        this.id = id
        this.uid = uid
        this.nombres = nombres
        this.urlImagenPerfil = urlImagenPerfil
        this.comentario = comentario
        this.tiempo = tiempo
    }
}
