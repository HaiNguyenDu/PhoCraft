package com.example.phocraft.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.phocraft.databinding.FragmentFavouritesBinding
import com.example.phocraft.enum.ImageCategory
import com.example.phocraft.ui.home.HomeViewModel
import com.example.phocraft.ui.home.adapter.ImageAdapter

class FavouritesFragment : Fragment() {
    private val binding by lazy { FragmentFavouritesBinding.inflate(layoutInflater) }
    private lateinit var viewModel: HomeViewModel
    private lateinit var imageAdapter: ImageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()
        observeLiveData()
    }

    private fun setUpUi() {
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        binding.rcv.apply {
            imageAdapter = ImageAdapter(context, emptyList()) { uri ->
            }
            layoutManager = GridLayoutManager(context, 3)
            adapter = imageAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeLiveData() {
        viewModel.favoriteImages.observe(viewLifecycleOwner, Observer { listImage ->
            imageAdapter.submitList(listImage)
        })
        viewModel.loadImages(ImageCategory.FAVORITE)
    }

}