package com.devaria.devagram.model.Usuario

import kotlinx.serialization.Serializable

@Serializable
class Usuario(
    var _id: String? = "",
    var nome: String,
    var avatar: String? = "",
    var seguidores: Int = 0,
    var seguindo: Int = 0,
    var publicacoes: Int = 0,
    var segueEsseUsuario: Boolean? = false
) {}