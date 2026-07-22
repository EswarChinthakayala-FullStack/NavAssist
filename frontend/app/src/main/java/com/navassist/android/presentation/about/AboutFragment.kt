package com.navassist.android.presentation.about

import android.os.Bundle
import com.navassist.android.databinding.FragmentAboutBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : BaseFragment<FragmentAboutBinding>(FragmentAboutBinding::inflate) {

    override fun setupViews() {}

    override fun observeViewModel() {}
}
