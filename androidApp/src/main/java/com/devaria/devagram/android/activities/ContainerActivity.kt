package com.devaria.devagram.android.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.devaria.devagram.android.R
import com.devaria.devagram.android.fragments.FeedFragment
import com.devaria.devagram.android.fragments.HeaderFragment
import com.devaria.devagram.android.fragments.NovaPublicacaoFragment
import com.devaria.devagram.android.fragments.PerfilFragment
import com.devaria.devagram.android.services.Rotas
import com.devaria.devagram.android.utils.Dialog
import com.devaria.devagram.model.Cadastrar.ResponseErro
import com.devaria.devagram.services.Usuario
import io.ktor.client.call.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ContainerActivity : AppCompatActivity() {
    private val mainScope = MainScope()
    lateinit var botaoHome : ImageView
    lateinit var botaoPublicao : ImageView
    lateinit var botaoPerfil : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        val shared = getSharedPreferences("devagram", Context.MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        botaoHome = findViewById(R.id.home)
        botaoPublicao = findViewById(R.id.add_publicacao)
        botaoPerfil = findViewById(R.id.perfil)

        inativaTodos()
        botaoHome.setImageDrawable(getResources().getDrawable(R.drawable.home))
        Rotas(this).setRota(FeedFragment(), resources.getString(R.string.rota_home))

        botaoHome.setOnClickListener {
            inativaTodos()
            botaoHome.setImageDrawable(getResources().getDrawable(R.drawable.home))
            Rotas(this).setRota(FeedFragment(), resources.getString(R.string.rota_home))
        }

        botaoPublicao.setOnClickListener {
            inativaTodos()
            botaoPublicao.setImageDrawable(getResources().getDrawable(R.drawable.nova_publicacao))
            Rotas(this).setRota(NovaPublicacaoFragment(), resources.getString(R.string.rota_nova_publicacao))
        }

        botaoPerfil.setOnClickListener {
            inativaTodos()
            botaoPerfil.setImageDrawable(getResources().getDrawable(R.drawable.user))
            val idUsuarioLogado = shared.getString("id_usuario_logado", "")
            val nomeUsuarioLogado = shared.getString("nome_usuario_logado", "")
            shared.edit().putString("id_perfil", idUsuarioLogado).apply()
            shared.edit().putString("nome_perfil", nomeUsuarioLogado).apply()
            Rotas(this).setRota(PerfilFragment.newInstance(idUsuarioLogado), resources.getString(R.string.rota_perfil))
        }

        saveUsuario()
    }

    fun inativaTodos(){
        val shared = getSharedPreferences("devagram", Context.MODE_PRIVATE)
        botaoHome.setImageDrawable(getResources().getDrawable(R.drawable.home_inativa))
        botaoPublicao.setImageDrawable(getResources().getDrawable(R.drawable.nova_publicao_inativa))
        botaoPerfil.setImageDrawable(getResources().getDrawable(R.drawable.user_inativo))
        shared.edit().remove("id_perfil").apply()
        shared.edit().remove("nome_perfil").apply()
        shared.edit().remove("avatar_perfil").apply()
    }

    fun saveUsuario(){
        val shared = getSharedPreferences("devagram", Context.MODE_PRIVATE)
        mainScope.launch {
            kotlin.runCatching {
                val token = shared?.getString("token", "")
                Usuario().getUsuario(token!!)
            }.onSuccess {
                if(it.status.value >= 400){
                    val erroData : ResponseErro = it.body()
                    Dialog(this@ContainerActivity).show("Erro", "Erro: ${erroData.erro}")
                }else{
                    val usuario : com.devaria.devagram.model.Usuario.Usuario = it.body()
                    shared.edit().putString("id_usuario_logado", usuario!!._id).apply()
                    shared.edit().putString("nome_usuario_logado", usuario!!.nome).apply()
                }
            }.onFailure {
                Dialog(this@ContainerActivity).show("Erro", "Erro: ${it.localizedMessage}")
            }
        }
    }

}