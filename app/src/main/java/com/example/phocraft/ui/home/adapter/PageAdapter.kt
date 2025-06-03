package com.example.phocraft.ui.home.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.phocraft.ui.home.fragment.FavouritesFragment
import com.example.phocraft.ui.home.fragment.RecentsFragment
import com.example.phocraft.ui.home.fragment.SelfiesFragment
import com.example.phocraft.utils.FAVOURITE_PAGE_INDEX
import com.example.phocraft.utils.RECENT_PAGE_INDEX
import com.example.phocraft.utils.SELFIES_PAGE_INDEX

class PageAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        RECENT_PAGE_INDEX to { RecentsFragment() },
        FAVOURITE_PAGE_INDEX to { FavouritesFragment() },
        SELFIES_PAGE_INDEX to { SelfiesFragment() }
    )

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }

    override fun getItemCount(): Int {
        return tabFragmentsCreators.size
    }
}