package com.devaria.devagram.android.utils

import android.graphics.Bitmap
import android.graphics.Matrix

class Imagem {
    fun editarImage(imagem: Bitmap, newHeigth: Int, newWidth: Int): Bitmap? {
        val width = imagem.width
        val heigth = imagem.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeigth = newHeigth.toFloat() / heigth

        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeigth)

        return Bitmap.createBitmap(imagem, 0, 0, width, heigth, matrix, false)

    }
}