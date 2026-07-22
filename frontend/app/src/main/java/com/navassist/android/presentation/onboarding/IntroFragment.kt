package com.navassist.android.presentation.onboarding

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.navassist.android.R
import com.navassist.android.data.local.datastore.SettingsDataStore
import com.navassist.android.databinding.FragmentIntroBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.onboarding.model.IntroPage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IntroFragment : BaseFragment<FragmentIntroBinding>(FragmentIntroBinding::inflate) {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private lateinit var pagerAdapter: IntroPagerAdapter
    private val dotsList by lazy {
        listOf(
            binding.dot0.root,
            binding.dot1.root,
            binding.dot2.root,
            binding.dot3.root,
            binding.dot4.root
        )
    }

    override fun setupViews() {
        binding.btnSkip.bringToFront()
        setupViewPager()
        setupClickListeners()
    }

    private fun setupViewPager() {
        pagerAdapter = IntroPagerAdapter(this)
        binding.viewPagerIntro.adapter = pagerAdapter
        binding.viewPagerIntro.setPageTransformer(DepthPageTransformer())

        binding.viewPagerIntro.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateCapsuleIndicator(position)
                updateCtaButton(position)
                updateSkipButton(position)
            }
        })
    }

    private fun setupClickListeners() {
        binding.cardCta.setOnClickListener { view ->
            // Scale bounce touch feedback
            view.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80)
                .withEndAction {
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(120)
                        .withEndAction {
                            val current = binding.viewPagerIntro.currentItem
                            if (current < IntroPage.PAGES.size - 1) {
                                binding.viewPagerIntro.setCurrentItem(current + 1, true)
                            } else {
                                completeOnboardingAndNavigate()
                            }
                        }
                        .start()
                }
                .start()
        }

        binding.btnSkip.setOnClickListener {
            completeOnboardingAndNavigate()
        }
    }

    private fun updateCapsuleIndicator(activePosition: Int) {
        dotsList.forEachIndexed { index, dot ->
            val isSelected = index == activePosition
            val targetWidth = if (isSelected) dpToPx(24) else dpToPx(6)
            val targetAlpha = if (isSelected) 1.0f else 0.3f

            val currentWidth = dot.layoutParams.width
            val anim = ValueAnimator.ofInt(currentWidth, targetWidth).apply {
                duration = 250
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animator ->
                    val params = dot.layoutParams
                    params.width = animator.animatedValue as Int
                    dot.layoutParams = params
                }
            }
            anim.start()
            dot.animate().alpha(targetAlpha).setDuration(250).start()
        }
    }

    private fun updateCtaButton(position: Int) {
        val page = IntroPage.PAGES.getOrNull(position) ?: return
        val targetText = getString(page.ctaTextRes)

        if (binding.tvCta.text != targetText) {
            binding.tvCta.animate()
                .alpha(0f)
                .setDuration(120)
                .withEndAction {
                    binding.tvCta.text = targetText
                    binding.tvCta.animate().alpha(1f).setDuration(120).start()
                }
                .start()
        }
    }

    private fun updateSkipButton(position: Int) {
        val isFinalPage = position == IntroPage.PAGES.size - 1
        val targetAlpha = if (isFinalPage) 0f else 1f

        binding.btnSkip.animate()
            .alpha(targetAlpha)
            .setDuration(200)
            .withEndAction {
                binding.btnSkip.visibility = if (isFinalPage) View.GONE else View.VISIBLE
            }
            .start()
    }

    private fun completeOnboardingAndNavigate() {
        viewLifecycleOwner.lifecycleScope.launch {
            settingsDataStore.setOnboardingCompleted(true)
            findNavController().navigate(R.id.action_intro_to_login)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
