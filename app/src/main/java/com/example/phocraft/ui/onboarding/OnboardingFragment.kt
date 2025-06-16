package com.example.phocraft.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.phocraft.databinding.FragmentSplashBinding
import com.example.phocraft.utils.FRAGMENT_KEY

class OnboardingFragment : Fragment() {
    private val binding by lazy { FragmentSplashBinding.inflate(layoutInflater) }
    private var param1: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            param1 = it.getInt(FRAGMENT_KEY)
        }
        Glide.with(this)
            .load(param1)
            .into(binding.iv)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
            OnboardingFragment().apply {
                arguments = Bundle().apply {
                    putInt(FRAGMENT_KEY, param1)
                }
            }
    }
}