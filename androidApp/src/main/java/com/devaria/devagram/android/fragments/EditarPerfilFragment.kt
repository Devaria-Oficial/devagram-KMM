package com.devaria.devagram.android.fragments

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.devaria.devagram.android.R
import com.devaria.devagram.android.utils.DownloadImagem
import com.devaria.devagram.android.utils.Imagem
import java.io.ByteArrayOutputStream

private const val ARG_PARAM1 = "idPerfil"
private const val ARG_PARAM2 = "nomePerfil"
private const val ARG_PARAM3 = "avatarPerfil"

class EditarPerfilFragment : Fragment() {
    val REQUEST_CODE = 200
    lateinit var foto : ImageView
    lateinit var inputNome : EditText
    lateinit var botaoLimpar : ImageView

    private var idPerfil: String? = null
    private var nomePerfil: String? = null
    private var avatarPerfil: String? = null

    var avatar: ByteArray? = null

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
        return inflater.inflate(R.layout.fragment_editar_perfil, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(idPerfil: String?, nomePerfil: String?, avatarPerfil: String?) =
            EditarPerfilFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, idPerfil)
                    putString(ARG_PARAM2, nomePerfil)
                    putString(ARG_PARAM3, avatarPerfil)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val shared = context?.getSharedPreferences("devagram", Context.MODE_PRIVATE)

        val botaoTirarFoto = view.findViewById<TextView>(R.id.botao_alterar_foto)
        foto = view.findViewById(R.id.avatar)
        inputNome = view.findViewById(R.id.input_editar_nome)
        botaoLimpar = view.findViewById(R.id.limpar)

        if(avatarPerfil != "" && avatarPerfil != null){
            DownloadImagem(foto).execute(avatarPerfil)
        }

        inputNome.setText(nomePerfil)

        requestPermissions()

        botaoTirarFoto.setOnClickListener{
            capturarFoto()
        }

        botaoLimpar.setOnClickListener {
            inputNome.setText("")
        }

        inputNome.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                shared!!.edit().putString("editar_perfil_nome", p0.toString()).apply()
            }
        })

    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermissions() {
        val permission = context?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.CAMERA
            )
        }

        if(permission != PackageManager.PERMISSION_GRANTED ){
            this.requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA),
                REQUEST_CODE
            )
        }
    }

    fun capturarFoto (){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val shared = context?.getSharedPreferences("devagram", Context.MODE_PRIVATE)

        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE && data != null){
            var imagem = Imagem().editarImage(data.extras?.get("data") as Bitmap, 120, 120)
            val stream = ByteArrayOutputStream()
            imagem!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            avatar = stream.toByteArray()
            shared!!.edit().putString("editar_perfil_avatar", avatar.toString()).apply()
            foto.setImageBitmap(imagem)
        }
    }
}