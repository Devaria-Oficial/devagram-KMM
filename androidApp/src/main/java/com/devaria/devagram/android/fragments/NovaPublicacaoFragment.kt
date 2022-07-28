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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.devaria.devagram.android.R
import com.devaria.devagram.android.utils.Imagem
import java.io.*


class NovaPublicacaoFragment : Fragment() {
    val REQUEST_CODE = 200
    lateinit var foto : ImageView
    var fotoBinario: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nova_publicacao, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val shared = context?.getSharedPreferences("devagram", Context.MODE_PRIVATE)
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()

        foto = view.findViewById(R.id.imagem)
        val inputDescricao = view.findViewById<EditText>(R.id.input_descricao)

        inputDescricao.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                shared!!.edit().putString("nova_publicacao_descricao", p0.toString()).apply()
            }
        })
    }

    companion object {
        fun newInstance() = NovaPublicacaoFragment()
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

        capturarFoto()
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
            fotoBinario = stream.toByteArray()
            shared!!.edit().putString("nova_publicacao_foto", fotoBinario.toString()).apply()
            foto.setImageBitmap(imagem)
        }
    }
}