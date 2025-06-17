package com.example.phocraft.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.phocraft.R

class OnboardingPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        OnboardingFragment.newInstance(R.drawable.splash_1),
        OnboardingFragment.newInstance(R.drawable.splash_2),
        OnboardingFragment.newInstance(R.drawable.splash_3),
        OnboardingFragment.newInstance(R.drawable.splash_4)
    )


    override fun createFragment(position: Int): Fragment = fragments[position]

    override fun getItemCount(): Int = fragments.size

}