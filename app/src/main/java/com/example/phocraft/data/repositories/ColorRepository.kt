package com.example.phocraft.data.repositories

import com.example.phocraft.R

class ColorRepository {
    fun getListColorId(): List<Int> {
        return listOf<Int>(
            R.color.white,
            R.color.light_blue,
            R.color.soft_pink,
            R.color.mint_green,
            R.color.sun_yellow,
            R.color.sky_blue,
            R.color.peach_orange,
            R.color.lavender_purple,
            R.color.forest_green,
            R.color.coral_red,
            R.color.charcoal_gray
        )
    }
}