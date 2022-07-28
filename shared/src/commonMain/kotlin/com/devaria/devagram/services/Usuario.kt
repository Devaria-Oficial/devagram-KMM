package com.devaria.devagram.services

import com.devaria.devagram.dto.EditarUsuarioDto
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

class Usuario {
    suspend fun getUsuario(token: String): HttpResponse {
        return DevagramApiService().get("usuario", token)
    }

    suspend fun pesquisarUsuario(token: String, id: String): HttpResponse {
        return DevagramApiService().get("pesquisa?id=${id}", token)
    }

    suspend fun toggleSeguir(token: String, id: String): HttpResponse {
        return DevagramApiService().put("seguir?id=${id}", null, token)
    }

    suspend fun editarUsuario(body: EditarUsuarioDto, token: String): HttpResponse {
        if(body.file != null){
            val form = formData{
                append("nome", body.nome)
                append("file", body.file!!, Headers.build {
                    append(HttpHeaders.ContentType, "image/png")
                    append(HttpHeaders.ContentDisposition, "filename=\"avatar.png\"")
                })
            }
            return DevagramApiService().put("usuario",  form, token)
        }else{
            val form = formData{
                append("nome", body.nome)
            }
            return DevagramApiService().put("usuario",  form, token)
        }
    }
}