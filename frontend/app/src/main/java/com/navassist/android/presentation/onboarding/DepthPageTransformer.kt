package com.navassist.android.presentation.onboarding

import android.view.View
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.navassist.android.R
import kotlin.math.abs

class DepthPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val cardHero = page.findViewById<View>(R.id.cardHero)
        val ivHero = page.findViewById<ImageView>(R.id.ivHero)
        val tvTitle = page.findViewById<View>(R.id.tvTitle)
        val tvDescription = page.findViewById<View>(R.id.tvDescription)

        page.apply {
            when {
                position < -1 -> { // [-Infinity, -1) Off-screen to the left
                    alpha = 0f
                }
                position <= 0 -> { // [-1, 0] Sliding out to the left
                    alpha = 1.0f + position
                    translationX = 0f
                    scaleX = 1.0f
                    scaleY = 1.0f

                    cardHero?.translationX = position * (width * 0.3f)
                    ivHero?.translationX = -position * (width * 0.15f)
                    tvTitle?.alpha = 1.0f + position * 1.5f
                    tvDescription?.alpha = 1.0f + position * 1.5f
                }
                position <= 1 -> { // (0, 1] Sliding in from the right
                    alpha = 1.0f - position
                    translationX = page.width * -position

                    val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position))
                    scaleX = scaleFactor
                    scaleY = scaleFactor

                    cardHero?.translationX = position * (width * 0.3f)
                    ivHero?.translationX = position * (width * 0.15f)
                    tvTitle?.alpha = 1.0f - position * 1.5f
                    tvDescription?.alpha = 1.0f - position * 1.5f
                }
                else -> { // (1, +Infinity] Off-screen to the right
                    alpha = 0f
                }
            }
        }
    }

    companion object {
        private const val MIN_SCALE = 0.88f
    }
}
