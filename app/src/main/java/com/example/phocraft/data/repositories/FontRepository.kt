package com.example.phocraft.data.repositories

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import com.example.phocraft.model.FontItem
import com.example.phocraft.utils.FONT_ASSET_PATH
import java.io.IOException

class FontRepository {
    fun loadFontsFromAssets(context: Context): List<FontItem> {
        val fontList = mutableListOf<FontItem>()
        val assetManager = context.assets

        try {

            val fontFileNames = assetManager.list(FONT_ASSET_PATH)

            if (fontFileNames.isNullOrEmpty()) {
                Log.e("FontManager", "Thư mục 'assets/$FONT_ASSET_PATH' trống hoặc không tồn tại.")
                return emptyList()
            }


            for (fileName in fontFileNames) {

                val fontPath = "$FONT_ASSET_PATH/$fileName"


                val typeface = Typeface.createFromAsset(assetManager, fontPath)


                val displayName = prettifyFontName(fileName)

                fontList.add(FontItem(displayName, typeface))
            }

        } catch (e: IOException) {
            Log.e("FontManager", "Lỗi khi đọc file font từ assets", e)
        }

        return fontList
    }

    private fun prettifyFontName(fileName: String): String {
        return fileName
            .substringBeforeLast('.')
            .replace('_', ' ')
            .split(' ')
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}