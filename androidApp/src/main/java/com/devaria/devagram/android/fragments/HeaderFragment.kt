package com.devaria.devagram.android.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.devaria.devagram.android.R
import com.devaria.devagram.android.activities.ContainerActivity
import com.devaria.devagram.android.activities.LoginActivity
import com.devaria.devagram.android.services.Rotas
import com.devaria.devagram.android.utils.Dialog
import com.devaria.devagram.dto.AddPublicacaoDto
import com.devaria.devagram.dto.EditarUsuarioDto
import com.devaria.devagram.model.Cadastrar.Cadastrar
import com.devaria.devagram.services.Auth
import com.devaria.devagram.services.Feed
import com.devaria.devagram.services.Usuario
import io.ktor.client.call.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match

class HeaderFragment : Fragment() {
    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_header, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val shared = context?.getSharedPreferences("devagram", Context.MODE_PRIVATE)

        definirHeaderAtivo(view)

        val botaoLogout = view.findViewById<ImageView>(R.id.logout)
        val botaoVoltar = view.findViewById<ImageView>(R.id.voltar)
        val botaoCancelar = view.findViewById<TextView>(R.id.cancelar)
        val botaoVoltarPublicacao = view.findViewById<ImageView>(R.id.voltar_nova_publicacao)
        val botaoConcluir = view.findViewById<TextView>(R.id.concluir)
        val botaoCompartilhar = view.findViewById<TextView>(R.id.compartilhar)

        botaoLogout.setOnClickListener {
            shared!!.edit().remove("token").apply()
            val intent = Intent(view.context, LoginActivity::class.java)
            startActivity(intent)
        }

        botaoVoltar.setOnClickListener{
            voltar()
        }

        botaoCancelar.setOnClickListener{
            cancelar()
        }

        botaoVoltarPublicacao.setOnClickListener{
            voltar()
        }

        botaoConcluir.setOnClickListener {
            concluirEdicaoPerfil()
        }

        botaoCompartilhar.setOnClickListener {
            addPublicacao()
        }
    }

    fun concluirEdicaoPerfil(){
        val shared = context?.getSharedPreferences("devagram", Context.MODE_PRIVATE)
        val token = shared!!.getString("token", "")
        val avatar = shared!!.getString("editar_perfil_avatar", "")
        val avatarByteArray = avatar!!.toByteArray()
        val nome = shared!!.getString("editar_perfil_nome", "")

        mainScope.launch {
            kotlin.runCatching {
                if(avatar == ""){
                    Usuario().editarUsuario(EditarUsuarioDto(nome!!), token!!)
                }else{
                    Usuario().editarUsuario(EditarUsuarioDto(nome!!, avatarByteArray), token!!)
                }
            }.onSuccess {
                if(it.status.value >= 400){
                    val erroData : com.devaria.devagram.model.Cadastrar.ResponseErro = it.body()
                    Log.e("Erro", "Erro: ${erroData.erro}")
                    context?.let { it1 -> Dialog(it1).show("Erro", "Erro: ${erroData.erro}") }
                }else{
                    val idUsuarioLogado = shared!!.getString("id_usuario_logado", "")
                    shared.edit().putString("nome_perfil", nome).apply()
                    shared.edit().remove("editar_perfil_nome")
                    shared.edit().remove("editar_perfil_avatar")
                    context?.let { Rotas(it).setRota(PerfilFragment.newInstance(idUsuarioLogado), resources.getString(R.string.rota_perfil)) }
                }
            }.onFailure {
                Log.e("Erro", "Erro: ${it.localizedMessage}")
                context?.let { it1 -> Dialog(it1).show("Erro", "Erro: ${it.localizedMessage}") }
            }
        }
        Log.i("Botao cadastrar", "Clicado")
    }


    fun addPublicacao(){
        val shared = context?.getSharedPreferences("devagram", Context.MODE_PRIVATE)
        val token = shared!!.getString("token", "")
        val foto = shared!!.getString("nova_publicacao_foto", "")
        val fotoByteArray = foto!!.toByteArray()
        val descricao = shared!!.getString("nova_publicacao_descricao", "")
        if(foto == "" || descricao == ""){
            context?.let { it1 -> Dialog(it1).show("Erro", "Foto e descrição sao necessarias") }
        }else{
            mainScope.launch {
                kotlin.runCatching {
                    Feed().addPublicacao(AddPublicacaoDto(descricao!!, fotoByteArray), token!!)
                }.onSuccess {
                    if(it.status.value >= 400){
                        val erroData : com.devaria.devagram.model.Cadastrar.ResponseErro = it.body()
                        Log.e("Erro", "Erro: ${erroData.erro}")
                        context?.let { it1 -> Dialog(it1).show("Erro", "Erro: ${erroData.erro}") }
                    }else{
                        shared.edit().remove("nova_publicacao_foto")
                        shared.edit().remove("nova_publicacao_descricao")
                        context?.let { Rotas(it).setRota(FeedFragment(), resources.getString(R.string.rota_home)) }
                    }
                }.onFailure {
                    Log.e("Erro", "Erro: ${it.localizedMessage}")
                    context?.let { it1 -> Dialog(it1).show("Erro", "Erro: ${it.localizedMessage}") }
                }
            }
        }
    }

    fun voltar(){
        context?.let { Rotas(it).setRota(FeedFragment(), resources.getString(R.string.rota_home)) }
    }

    fun cancelar(){
        val shared = context?.getSharedPreferences("devagram", Context.MODE_PRIVATE)
        val idUsuarioLogado = shared!!.getString("id_usuario_logado", "")
        context?.let { Rotas(it).setRota(PerfilFragment.newInstance(idUsuarioLogado), resources.getString(R.string.rota_perfil)) }
    }

    fun definirHeaderAtivo(view: View){
        val shared = context?.getSharedPreferences("devagram", Context.MODE_PRIVATE)
        val rotaAtual = shared!!.getString("rota_atual", "")
        val nome_perfil = shared!!.getString("nome_perfil", "")

        when(rotaAtual) {
            context?.resources?.getString(R.string.rota_home) -> {
                ocultarHeaders(view)
                view.findViewById<RelativeLayout>(R.id.header_home).visibility = View.VISIBLE
            }
            context?.resources?.getString(R.string.rota_perfil) -> {
                ocultarHeaders(view)
                view.findViewById<RelativeLayout>(R.id.header_perfil_interno).visibility = View.VISIBLE
                val nome = view.findViewById<TextView>(R.id.header_perfil_interno_nome)
                nome.text = nome_perfil
            }
            context?.resources?.getString(R.string.rota_perfil_expecifico) -> {
                ocultarHeaders(view)
                view.findViewById<RelativeLayout>(R.id.header_perfil_expecifico).visibility = View.VISIBLE
                val nome = view.findViewById<TextView>(R.id.header_nome)
                nome.text = nome_perfil
            }
            context?.resources?.getString(R.string.rota_editar_perfil) -> {
                ocultarHeaders(view)
                view.findViewById<RelativeLayout>(R.id.header_editar_perfil).visibility = View.VISIBLE
            }
            context?.resources?.getString(R.string.rota_nova_publicacao) -> {
                ocultarHeaders(view)
                view.findViewById<RelativeLayout>(R.id.header_nova_publicacao).visibility = View.VISIBLE
            }
        }
    }

    fun ocultarHeaders(view: View){
        view.findViewById<RelativeLayout>(R.id.header_perfil_interno).visibility = View.GONE
        view.findViewById<RelativeLayout>(R.id.header_perfil_expecifico).visibility = View.GONE
        view.findViewById<RelativeLayout>(R.id.header_home).visibility = View.GONE
        view.findViewById<RelativeLayout>(R.id.header_nova_publicacao).visibility = View.GONE
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HeaderFragment()
    }
}