package com.egeniq.interactiveexpandingappbar.binding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BindingFragment<B: ViewDataBinding>: Fragment() {
    protected lateinit var binding: B

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil.inflate<B>(inflater, layout, container, false).also {
            it.lifecycleOwner = viewLifecycleOwner
            binding = it
        }.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    @get:LayoutRes
    protected abstract val layout: Int
}
