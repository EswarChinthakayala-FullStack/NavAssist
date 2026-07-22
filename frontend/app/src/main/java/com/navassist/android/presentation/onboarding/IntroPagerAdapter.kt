package com.navassist.android.presentation.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.navassist.android.presentation.onboarding.model.IntroPage

class IntroPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = IntroPage.PAGES.size

    override fun createFragment(position: Int): Fragment {
        return IntroPageFragment.newInstance(position)
    }
}
