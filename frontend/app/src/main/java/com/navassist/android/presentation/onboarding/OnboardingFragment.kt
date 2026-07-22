package com.navassist.android.presentation.onboarding

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.navassist.android.R
import com.navassist.android.data.local.datastore.SettingsDataStore
import com.navassist.android.databinding.FragmentOnboardingBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingFragment : BaseFragment<FragmentOnboardingBinding>(FragmentOnboardingBinding::inflate) {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private lateinit var adapter: OnboardingAdapter
    private val dotsList by lazy {
        listOf(
            binding.dot0,
            binding.dot1,
            binding.dot2,
            binding.dot3,
            binding.dot4,
            binding.dot5
        )
    }

    override fun setupViews() {
        binding.btnSkip.bringToFront()
        setupViewPager()
        setupClickListeners()
    }

    private fun setupViewPager() {
        adapter = OnboardingAdapter(OnboardingPageModel.PAGES)
        binding.viewPagerOnboarding.adapter = adapter
        binding.viewPagerOnboarding.setPageTransformer(DepthPageTransformer())

        binding.viewPagerOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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
            // Scale bounce touch feedback animation
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
                            val current = binding.viewPagerOnboarding.currentItem
                            if (current < OnboardingPageModel.PAGES.size - 1) {
                                binding.viewPagerOnboarding.setCurrentItem(current + 1, true)
                            } else {
                                navigateToLogin()
                            }
                        }
                        .start()
                }
                .start()
        }

        binding.btnSkip.setOnClickListener {
            navigateToLogin()
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
        val pageModel = OnboardingPageModel.PAGES.getOrNull(position) ?: return
        val targetText = getString(pageModel.ctaTextRes)

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
        val isFinalPage = position == OnboardingPageModel.PAGES.size - 1
        val targetAlpha = if (isFinalPage) 0f else 1f

        binding.btnSkip.animate()
            .alpha(targetAlpha)
            .setDuration(200)
            .withEndAction {
                binding.btnSkip.visibility = if (isFinalPage) View.GONE else View.VISIBLE
            }
            .start()
    }

    private fun navigateToLogin() {
        viewLifecycleOwner.lifecycleScope.launch {
            settingsDataStore.setOnboardingCompleted(true)
            findNavController().navigate(R.id.action_onboarding_to_featureOverview)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
