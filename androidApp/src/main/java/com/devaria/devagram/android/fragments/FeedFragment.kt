package com.devaria.devagram.android.fragments

import PublicoesAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.devaria.devagram.android.R
import com.devaria.devagram.android.utils.Dialog
import com.devaria.devagram.model.Cadastrar.ResponseErro
import com.devaria.devagram.model.Feed.Comentario
import com.devaria.devagram.model.Feed.Publicacao
import com.devaria.devagram.model.Usuario.Usuario
import com.devaria.devagram.services.Feed
import io.ktor.client.call.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "idPerfil"
private const val ARG_PARAM2 = "nomePerfil"
private const val ARG_PARAM3 = "avatarPerfil"

class FeedFragment : Fragment() {
    private val mainScope = MainScope()

    private var idPerfil: String? = null
    private var nomePerfil: String? = null
    private var avatarPerfil: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idPerfil = it.getString(ARG_PARAM1)
            nomePerfil = it.getString(ARG_PARAM2)
            avatarPerfil = it.getString(ARG_PARAM3)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shared = context?.getSharedPreferences("devagram", Context.MODE_PRIVATE)
        val token = shared!!.getString("token", "")
        mainScope.launch {
            kotlin.runCatching {
                Feed().getFeed(token!!, idPerfil)
            }.onSuccess {
                if(it.status.value >= 400){
                    val erroData : ResponseErro = it.body()
                    Log.e("Erro", "Erro: ${erroData.erro}")
                    context?.let { context -> Dialog(context).show("Erro", "Erro: ${erroData.erro}") }
                }else{
                    val publicacoes : ArrayList<Publicacao> = it.body()
                    publicacoes.map { it ->
                        if(it.usuario == null){
                            it.usuario =  Usuario(idPerfil, nomePerfil!!, avatarPerfil )
                        }}
                    val adapter = PublicoesAdapter(view.context, publicacoes)
                    val listView : ListView = view.findViewById(R.id.list_view)
                    listView.adapter = adapter
                }
            }.onFailure {
                Log.e("Erro", "Erro: ${it.localizedMessage}")
                context?.let { context -> Dialog(context).show("Erro", "Erro: ${it.localizedMessage}") }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(idPerfil: String?, nomePerfil: String?, avatarPerfil: String?) =
            FeedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, idPerfil)
                    putString(ARG_PARAM2, nomePerfil)
                    putString(ARG_PARAM3, avatarPerfil)
                }
            }
    }
}