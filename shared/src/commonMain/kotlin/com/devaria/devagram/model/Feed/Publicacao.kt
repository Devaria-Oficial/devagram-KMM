package com.devaria.devagram.model.Feed

import com.devaria.devagram.model.Usuario.Usuario
import kotlinx.serialization.Serializable

@Serializable
class Publicacao(
   var _id : String,
   var idUsuario: String,
   var descricao: String,
   var foto: String,
   var data: String,
   var comentarios: ArrayList<Comentario>,
   var likes: ArrayList<String>,
   var usuario: Usuario? = null
) {}