package com.devaria.devagram.android.services

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.devaria.devagram.android.R
import com.devaria.devagram.android.fragments.HeaderFragment


class Rotas (context: Context) {
    val context = context

    fun setRota(fragment: Fragment, rota: String){
        val shared = context.getSharedPreferences("devagram", Context.MODE_PRIVATE)
        val rotaAnterior = shared.getString("rota_atual", "")
        shared.edit().putString("rota_anterior", rotaAnterior).apply()
        shared.edit().putString("rota_atual", rota).apply()
        var ft = (context as FragmentActivity).supportFragmentManager.beginTransaction()
        ft.replace(R.id.frame_container, fragment)
        ft.replace(R.id.header_container, HeaderFragment.newInstance())
        ft.commit()
    }
}