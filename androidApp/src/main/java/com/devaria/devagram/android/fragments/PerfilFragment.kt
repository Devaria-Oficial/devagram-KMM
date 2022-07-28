package com.devaria.devagram.android.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.devaria.devagram.android.R
import com.devaria.devagram.android.services.Rotas
import com.devaria.devagram.android.utils.Dialog
import com.devaria.devagram.android.utils.DownloadImagem
import com.devaria.devagram.model.Cadastrar.ResponseErro
import com.devaria.devagram.model.Cadastrar.ResponseSucesso
import com.devaria.devagram.services.Usuario
import io.ktor.client.call.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "id"

class PerfilFragment : Fragment() {
    private val mainScope = MainScope()

    lateinit var avatar: ImageView
    lateinit var qtdSeguidoresText: TextView
    lateinit var qtdPublicacoesText: TextView
    lateinit var qtdSeguindoText: TextView
    lateinit var botaoSeguirEditar: Button

    var qtdSeguidores = 0
    var idUsuarioLogado = ""
    var token = ""
    var avatarUrl = ""
    var nome = ""

    private var id: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val shared = context?.getSharedPreferences("devagram", Context.MODE_PRIVATE)
        super.onViewCreated(view, savedInstanceState)
        avatar = view.findViewById(R.id.avatar)
        qtdPublicacoesText = view.findViewById(R.id.qtd_seguidores)
        qtdSeguidoresText = view.findViewById(R.id.qtd_seguidores)
        qtdSeguindoText = view.findViewById(R.id.qtd_seguindo)
        botaoSeguirEditar = view.findViewById(R.id.seguir_editar)
        popularHeader()
        idUsuarioLogado = shared?.getString("id_usuario_logado", "").toString()
        token = shared?.getString("token", "").toString()

        if(id == null){
            id = idUsuarioLogado
        }

        botaoSeguirEditar.setOnClickListener {
            if(id != idUsuarioLogado){
                seguirOuDeixarDeSeguir()
            }else{
                context?.let { it1 -> Rotas(it1).setRota(EditarPerfilFragment.newInstance(id, nome, avatarUrl), resources.getString(R.string.rota_editar_perfil)) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun popularHeader(){
        mainScope.launch {
            kotlin.runCatching {
                Usuario().pesquisarUsuario(token!!, id!!)
            }.onSuccess {
                if(it.status.value >= 400){
                    val erroData : ResponseErro = it.body()
                    context?.let { it1 -> Dialog(it1).show("Erro", "Erro: ${erroData.erro}") }
                }else{
                    val usuario : com.devaria.devagram.model.Usuario.Usuario = it.body()
                    nome = usuario.nome
                    avatarUrl = usuario.avatar!!

                    if(usuario.avatar != ""){
                        DownloadImagem(avatar).execute(usuario.avatar)
                    }

                    qtdSeguidores = usuario.seguidores
                    qtdPublicacoesText.text = usuario.publicacoes.toString()
                    qtdSeguidoresText.text = qtdSeguidores.toString()
                    qtdSeguindoText.text = usuario.seguindo.toString()

                    definirBotaoDoPerfil(usuario)

                    var ft = (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    ft.replace(R.id.feed, FeedFragment.newInstance(usuario._id, usuario.nome, usuario.avatar))
                    ft.commit()
                }
            }.onFailure {
                context?.let { it1 -> Dialog(it1).show("Erro", "Erro: ${it.localizedMessage}") }
            }
        }
    }


    fun definirBotaoDoPerfil(usuario: com.devaria.devagram.model.Usuario.Usuario){
        if(idUsuarioLogado == id){
            botaoSeguirEditar.setBackgroundResource(R.drawable.botao_outline_perfil_background)
            botaoSeguirEditar.setTextColor(resources.getColor(R.color.corPrimaria2))
            botaoSeguirEditar.setText(R.string.botao_editar)
        }else if(usuario.segueEsseUsuario == true){
            botaoSeguirEditar.setBackgroundResource(R.drawable.botao_outline_perfil_background)
            botaoSeguirEditar.setTextColor(resources.getColor(R.color.corPrimaria2))
            botaoSeguirEditar.setText(R.string.botao_deixar_seguir)
        }else{
            botaoSeguirEditar.setBackgroundResource(R.drawable.botao_perfil_background)
            botaoSeguirEditar.setTextColor(resources.getColor(R.color.branco))
            botaoSeguirEditar.setText(R.string.botao_seguir)
        }
    }

    fun seguirOuDeixarDeSeguir(){
        mainScope.launch {
            kotlin.runCatching {
                Usuario().toggleSeguir(token!!, id!!)
            }.onSuccess {
                if (it.status.value >= 400) {
                    val erroData: ResponseErro = it.body()
                    context?.let { it1 -> Dialog(it1).show("Erro", "Erro: ${erroData.erro}") }
                } else {
                    val response : ResponseSucesso = it.body()

                    if (response.msg.contains("seguido")) {
                        botaoSeguirEditar.setBackgroundResource(R.drawable.botao_outline_perfil_background)
                        botaoSeguirEditar.setTextColor(resources.getColor(R.color.corPrimaria2))
                        botaoSeguirEditar.setText(R.string.botao_deixar_seguir)
                        qtdSeguidores++
                        qtdSeguidoresText.text = qtdSeguidores.toString()
                    } else {
                        botaoSeguirEditar.setBackgroundResource(R.drawable.botao_perfil_background)
                        botaoSeguirEditar.setTextColor(resources.getColor(R.color.branco))
                        botaoSeguirEditar.setText(R.string.botao_seguir)
                        qtdSeguidores--
                        qtdSeguidoresText.text = qtdSeguidores.toString()
                    }
                }
            }.onFailure {
                context?.let { it1 -> Dialog(it1).show("Erro", "Erro: ${it.localizedMessage}") }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(id: String?) =
            PerfilFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, id)
                }
            }
    }
}