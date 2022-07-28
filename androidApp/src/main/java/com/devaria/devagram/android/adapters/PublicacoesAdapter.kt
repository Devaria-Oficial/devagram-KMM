import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.devaria.devagram.android.R
import com.devaria.devagram.android.fragments.PerfilFragment
import com.devaria.devagram.android.services.Rotas
import com.devaria.devagram.android.utils.Dialog
import com.devaria.devagram.android.utils.DownloadImagem
import com.devaria.devagram.dto.ComentarioDto
import com.devaria.devagram.model.Cadastrar.ResponseErro
import com.devaria.devagram.model.Feed.Comentario
import com.devaria.devagram.model.Feed.Publicacao
import com.devaria.devagram.services.Feed
import io.ktor.client.call.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class PublicoesAdapter(context: Context?, publicacoes: ArrayList<Publicacao>) :
    ArrayAdapter<Publicacao>(context!!, 0, publicacoes) {
    private val mainScope = MainScope()

    override fun isEnabled(position: Int): Boolean{
        return false
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val shared = context?.getSharedPreferences("devagram", Context.MODE_PRIVATE)
        val token = shared.getString("token", "")
        val idUsuarioLogado = shared.getString("id_usuario_logado","")
        val nomeUsuarioLogado = shared.getString("nome_usuario_logado","")
        var novoComentario : String =  ""

        var curtido: Boolean = false
        var comentado: Boolean = false

        var convertView: View? = convertView
        val publicacao: Publicacao? = getItem(position)
        var qtdLikes : Int = publicacao!!.likes.count()

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_publicacao, parent, false)
        }

        val avatar : ImageView = convertView!!.findViewById(R.id.avatar)
        val nome : TextView = convertView!!.findViewById(R.id.nome)
        val imagem : ImageView = convertView!!.findViewById(R.id.imagem)
        val avatarInput : ImageView = convertView!!.findViewById(R.id.avatar_input)
        val curtir : ImageView = convertView!!.findViewById(R.id.curtir)
        val comentario : ImageView = convertView!!.findViewById(R.id.comentario)
        val qtdCurtidas : TextView = convertView!!.findViewById(R.id.qtd_curtidas)
        val descricao : TextView = convertView!!.findViewById(R.id.texto_descricao)
        val containerComentario : RelativeLayout = convertView!!.findViewById(R.id.input_container)
        val inputComentario: EditText = convertView!!.findViewById(R.id.novo_comentario)


        val adapter = ComentariosAdapter(convertView!!.context, publicacao!!.comentarios)
        val listView : ListView = convertView!!.findViewById(R.id.lista_comentarios)
        listView.adapter = adapter

        if(publicacao!!.likes.contains(idUsuarioLogado)){
            curtido = true
            curtir.setImageDrawable(context.resources.getDrawable(R.drawable.curtir))
        }

        if(publicacao!!.usuario?.avatar != ""){
            DownloadImagem(avatar).execute(publicacao!!.usuario?.avatar)
            DownloadImagem(avatarInput).execute(publicacao!!.usuario?.avatar)
        }

        DownloadImagem(imagem).execute(publicacao!!.foto)

        nome.setText(publicacao?.usuario?.nome)
        qtdCurtidas.setText(qtdLikes.toString() + " pessoas")
        val descricaoFormatada = "<b>${publicacao?.usuario?.nome}</b> ${publicacao!!.descricao}"
        descricao.text = HtmlCompat.fromHtml(descricaoFormatada, HtmlCompat.FROM_HTML_MODE_LEGACY)

        avatar.setOnClickListener{
            shared.edit().putString("id_perfil", publicacao.idUsuario).apply()
            shared.edit().putString("nome_perfil", publicacao.usuario?.nome).apply()
            shared.edit().putString("avatar_perfil", publicacao.usuario?.avatar).apply()
            Rotas(context).setRota(PerfilFragment.newInstance(publicacao.idUsuario), context.resources.getString(R.string.rota_perfil_expecifico ))
        }

        curtir.setOnClickListener {
            mainScope.launch  {
                kotlin.runCatching {
                    Feed().toggleCurtir(publicacao!!._id, token!!)
                }.onSuccess {
                    if(it.status.value >= 400){
                        val erroData : ResponseErro = it.body()
                        Dialog(context).show("Erro", "Erro: ${erroData.erro}")
                    }else{
                        if(!curtido){
                            curtido = true
                            curtir.setImageDrawable(context.resources.getDrawable(R.drawable.curtir))
                            qtdLikes++
                            qtdCurtidas.setText(qtdLikes.toString() + " pessoas")
                        }else{
                            curtido = false
                            curtir.setImageDrawable(context.resources.getDrawable(R.drawable.curtir_inativo))
                            qtdLikes--
                            qtdCurtidas.setText(qtdLikes.toString() + " pessoas")
                        }
                    }
                }.onFailure {
                    Dialog(context).show("Erro", "Erro: ${it.localizedMessage}")
                }
            }
        }

        comentario.setOnClickListener {
            if(!comentado){
                containerComentario.visibility = View.VISIBLE
                comentado = true
                comentario.setImageDrawable(context.resources.getDrawable(R.drawable.comentario))
            }else{
                containerComentario.visibility = View.GONE
                comentado = false
                comentario.setImageDrawable(context.resources.getDrawable(R.drawable.comentario_inativo))
            }
        }

        inputComentario.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                novoComentario = p0.toString()
            }
        })

        inputComentario.setOnEditorActionListener(TextView.OnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                mainScope.launch {
                    kotlin.runCatching {
                        Feed().addComentario(publicacao!!._id, ComentarioDto(novoComentario), token!!)
                    }.onSuccess {
                        if(it.status.value >= 400){
                            val erroData : ResponseErro = it.body()
                            Dialog(context).show("Erro", "Erro: ${erroData.erro}")
                        }else{
                            (context as AppCompatActivity).runOnUiThread {
                                publicacao!!.comentarios.add(Comentario(idUsuarioLogado, nomeUsuarioLogado!!, novoComentario))
                                adapter.notifyDataSetChanged()
                                inputComentario.setText("")
                            }
                        }
                    }.onFailure {
                        Dialog(context).show("Erro", "Erro: ${it.localizedMessage}")
                    }
                }
            }
            false
        })

        return convertView
    }
}