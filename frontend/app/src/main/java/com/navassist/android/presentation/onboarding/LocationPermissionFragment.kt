package com.navassist.android.presentation.onboarding

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.navassist.android.R
import com.navassist.android.data.local.datastore.SettingsDataStore
import com.navassist.android.databinding.FragmentLocationPermissionBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationPermissionFragment : BaseFragment<FragmentLocationPermissionBinding>(FragmentLocationPermissionBinding::inflate) {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            handlePermissionGranted()
        } else {
            handlePermissionDenied()
        }
    }

    override fun setupViews() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupClickListeners()
        startIllustrationPulseAnimation()
        startEntranceAnimations()
    }

    private fun setupClickListeners() {
        // Primary Action: Allow Location Access CTA
        binding.cardAllowCta.setOnClickListener { view ->
            view.animate()
                .scaleX(0.98f)
                .scaleY(0.98f)
                .setDuration(80)
                .withEndAction {
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(120)
                        .withEndAction {
                            requestPermissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                        .start()
                }
                .start()
        }

        // Secondary Action: Continue Without Location
        binding.btnSkipLocation.setOnClickListener {
            completeAndNavigateToHome()
        }
    }

    private fun handlePermissionGranted() {
        binding.pbLoading.visibility = View.VISIBLE
        binding.tvAllowCta.visibility = View.INVISIBLE
        binding.vArrowCircle.visibility = View.INVISIBLE

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                viewLifecycleOwner.lifecycleScope.launch {
                    if (location != null) {
                        settingsDataStore.saveLastKnownLocation(location.latitude, location.longitude)
                    }
                    completeAndNavigateToHome()
                }
            }.addOnFailureListener {
                completeAndNavigateToHome()
            }
        } catch (e: SecurityException) {
            completeAndNavigateToHome()
        }
    }

    private fun handlePermissionDenied() {
        val showRationale = shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_location_denied, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)

        val btnTryAgain = dialogView.findViewById<MaterialButton>(R.id.btnTryAgain)
        val btnOpenSettings = dialogView.findViewById<MaterialButton>(R.id.btnOpenSettings)
        val btnContinueAnyway = dialogView.findViewById<View>(R.id.btnContinueAnyway)

        if (showRationale) {
            btnTryAgain.visibility = View.VISIBLE
            btnOpenSettings.visibility = View.GONE
        } else {
            btnTryAgain.visibility = View.GONE
            btnOpenSettings.visibility = View.VISIBLE
        }

        btnTryAgain?.setOnClickListener {
            dialog.dismiss()
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        btnOpenSettings?.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
            }
            startActivity(intent)
        }

        btnContinueAnyway?.setOnClickListener {
            dialog.dismiss()
            completeAndNavigateToHome()
        }

        dialog.show()
    }

    private fun completeAndNavigateToHome() {
        viewLifecycleOwner.lifecycleScope.launch {
            settingsDataStore.setOnboardingCompleted(true)
            findNavController().navigate(R.id.action_location_to_home)
        }
    }

    private fun startIllustrationPulseAnimation() {
        val radarScaleX = ObjectAnimator.ofFloat(binding.vRadarRing, View.SCALE_X, 1.0f, 1.35f, 1.0f).apply {
            duration = 3000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        val radarScaleY = ObjectAnimator.ofFloat(binding.vRadarRing, View.SCALE_Y, 1.0f, 1.35f, 1.0f).apply {
            duration = 3000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        val radarAlpha = ObjectAnimator.ofFloat(binding.vRadarRing, View.ALPHA, 0.3f, 0.6f, 0.3f).apply {
            duration = 3000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        AnimatorSet().apply {
            playTogether(radarScaleX, radarScaleY, radarAlpha)
            start()
        }
    }

    private fun startEntranceAnimations() {
        binding.cardIllustrationContainer.alpha = 0f
        val illuScaleX = ObjectAnimator.ofFloat(binding.cardIllustrationContainer, View.SCALE_X, 0.85f, 1.0f)
        val illuScaleY = ObjectAnimator.ofFloat(binding.cardIllustrationContainer, View.SCALE_Y, 0.85f, 1.0f)
        val illuAlpha = ObjectAnimator.ofFloat(binding.cardIllustrationContainer, View.ALPHA, 0f, 1f)

        val illuAnimSet = AnimatorSet().apply {
            playTogether(illuScaleX, illuScaleY, illuAlpha)
            duration = 500
        }

        binding.tvHeadline.alpha = 0f
        binding.tvSubtitle.alpha = 0f
        val headlineFade = ObjectAnimator.ofFloat(binding.tvHeadline, View.ALPHA, 0f, 1f)
        val headlineTranslate = ObjectAnimator.ofFloat(binding.tvHeadline, View.TRANSLATION_Y, 16f, 0f)
        val subtitleFade = ObjectAnimator.ofFloat(binding.tvSubtitle, View.ALPHA, 0f, 1f)

        val textAnimSet = AnimatorSet().apply {
            playTogether(headlineFade, headlineTranslate, subtitleFade)
            duration = 350
            startDelay = 150
        }

        binding.cardBenefits.alpha = 0f
        val cardFade = ObjectAnimator.ofFloat(binding.cardBenefits, View.ALPHA, 0f, 1f)
        val cardTranslate = ObjectAnimator.ofFloat(binding.cardBenefits, View.TRANSLATION_Y, 20f, 0f)

        val cardAnimSet = AnimatorSet().apply {
            playTogether(cardFade, cardTranslate)
            duration = 450
            startDelay = 300
        }

        AnimatorSet().apply {
            playTogether(illuAnimSet, textAnimSet, cardAnimSet)
            start()
        }
    }
}
