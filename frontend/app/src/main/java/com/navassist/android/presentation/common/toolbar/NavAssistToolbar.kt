package com.navassist.android.presentation.common.toolbar

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import com.google.android.material.appbar.AppBarLayout
import com.navassist.android.R
import com.navassist.android.databinding.LayoutGlobalToolbarBinding

class NavAssistToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutGlobalToolbarBinding =
        LayoutGlobalToolbarBinding.inflate(LayoutInflater.from(context), this, true)

    private var currentConfig: ToolbarConfiguration? = null
    private var currentNavController: NavController? = null

    init {
        setupInsets()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }
    }

    fun configure(config: ToolbarConfiguration, navController: NavController? = null) {
        this.currentConfig = config
        this.currentNavController = navController

        if (config.mode == ToolbarMode.HIDDEN) {
            visibility = View.GONE
            return
        }
        visibility = View.VISIBLE

        // 1. Title & Subtitle Setup
        binding.tvCompactTitle.text = config.title
        binding.tvLargeTitle.text = config.title

        if (!config.subtitle.isNullOrBlank()) {
            binding.tvCompactSubtitle.text = config.subtitle
            binding.tvLargeSubtitle.text = config.subtitle
            binding.tvCompactSubtitle.visibility = View.VISIBLE
            binding.tvLargeSubtitle.visibility = View.VISIBLE
        } else {
            binding.tvCompactSubtitle.visibility = View.GONE
            binding.tvLargeSubtitle.visibility = View.GONE
        }

        // 2. Back Button Setup
        if (config.showBackButton && navController != null) {
            binding.btnBackContainer.visibility = View.VISIBLE
            binding.btnBackContainer.setOnClickListener { view ->
                view.animate()
                    .scaleX(0.92f)
                    .scaleY(0.92f)
                    .setDuration(70)
                    .withEndAction {
                        view.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(90)
                            .withEndAction {
                                config.onBackClick?.invoke() ?: navController.navigateUp()
                            }
                            .start()
                    }
                    .start()
            }
        } else {
            binding.btnBackContainer.visibility = View.GONE
        }

        // 3. Actions Container Setup
        setupActions(config.actions)

        // 4. Mode Configuration
        when (config.mode) {
            ToolbarMode.LARGE_TITLE -> {
                binding.layoutLargeTitleContainer.visibility = View.VISIBLE
                binding.tvCompactTitle.alpha = 0f
                binding.tvCompactSubtitle.alpha = 0f
                binding.appBarLayout.setBackgroundColor(Color.parseColor("#0A0A0A"))
                binding.vToolbarDivider.visibility = View.VISIBLE

                // Set up CollapsingToolbar scroll listener for title scaling & fading
                val layoutParams = binding.collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
                layoutParams.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                        AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or
                        AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
                binding.collapsingToolbar.layoutParams = layoutParams

                binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBar, verticalOffset ->
                    val totalScroll = appBar.totalScrollRange.toFloat()
                    if (totalScroll > 0) {
                        val percentage = Math.abs(verticalOffset).toFloat() / totalScroll
                        // Fade in compact title as large title collapses
                        binding.tvCompactTitle.alpha = percentage
                        binding.tvCompactSubtitle.alpha = percentage
                        // Fade out large title
                        binding.layoutLargeTitleContainer.alpha = 1f - (percentage * 1.2f).coerceAtMost(1f)
                    }
                })
            }
            ToolbarMode.NAVIGATION -> {
                binding.layoutLargeTitleContainer.visibility = View.GONE
                binding.tvCompactTitle.alpha = 1f
                binding.tvCompactSubtitle.alpha = 1f
                binding.appBarLayout.setBackgroundColor(Color.parseColor("#09090B"))
                binding.vToolbarDivider.visibility = View.VISIBLE

                val layoutParams = binding.collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
                layoutParams.scrollFlags = 0
                binding.collapsingToolbar.layoutParams = layoutParams
            }
            ToolbarMode.TRANSPARENT_OVERLAY -> {
                binding.layoutLargeTitleContainer.visibility = View.GONE
                binding.tvCompactTitle.alpha = 1f
                binding.tvCompactSubtitle.alpha = 1f
                binding.appBarLayout.setBackgroundColor(Color.TRANSPARENT)
                binding.vToolbarDivider.visibility = View.GONE

                val layoutParams = binding.collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
                layoutParams.scrollFlags = 0
                binding.collapsingToolbar.layoutParams = layoutParams

                if (config.isLiftOnScroll) {
                    binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                        if (Math.abs(verticalOffset) > 20) {
                            binding.appBarLayout.setBackgroundColor(Color.parseColor("#E6111113"))
                            binding.vToolbarDivider.visibility = View.VISIBLE
                        } else {
                            binding.appBarLayout.setBackgroundColor(Color.TRANSPARENT)
                            binding.vToolbarDivider.visibility = View.GONE
                        }
                    })
                }
            }
            ToolbarMode.HIDDEN -> {
                visibility = View.GONE
            }
        }

        ViewCompat.requestApplyInsets(binding.appBarLayout)
    }

    private fun setupActions(actions: List<ToolbarAction>) {
        binding.layoutActionContainer.removeAllViews()
        val context = binding.root.context

        // Display up to 2 actions
        actions.take(2).forEach { action ->
            val actionView = FrameLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (40 * resources.displayMetrics.density).toInt(),
                    (40 * resources.displayMetrics.density).toInt()
                ).apply {
                    setMargins((4 * resources.displayMetrics.density).toInt(), 0, 0, 0)
                }
                setBackgroundResource(R.drawable.bg_glass_circle)
                isClickable = true
                isFocusable = true
                contentDescription = action.contentDescription

                val iconView = ImageView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        (20 * resources.displayMetrics.density).toInt(),
                        (20 * resources.displayMetrics.density).toInt()
                    ).apply {
                        gravity = android.view.Gravity.CENTER
                    }
                    setImageResource(action.iconRes)
                    setColorFilter(Color.parseColor("#FAFAFA"))
                }
                addView(iconView)

                setOnClickListener { view ->
                    view.animate()
                        .scaleX(0.92f)
                        .scaleY(0.92f)
                        .setDuration(70)
                        .withEndAction {
                            view.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(90)
                                .withEndAction {
                                    action.onClick()
                                }
                                .start()
                        }
                        .start()
                }
            }
            binding.layoutActionContainer.addView(actionView)
        }
    }
}
