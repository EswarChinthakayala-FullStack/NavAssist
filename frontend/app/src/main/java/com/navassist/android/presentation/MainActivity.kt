package com.navassist.android.presentation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.navassist.android.R
import com.navassist.android.databinding.ActivityMainBinding
import com.navassist.android.presentation.common.toolbar.ActionType
import com.navassist.android.presentation.common.toolbar.ToolbarAction
import com.navassist.android.presentation.common.toolbar.ToolbarConfiguration
import com.navassist.android.presentation.common.toolbar.ToolbarMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // True Edge-to-Edge Window setup
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        // Handle System Navigation Bar Insets on Floating Bottom Navigation
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = view.layoutParams as android.view.ViewGroup.MarginLayoutParams
            params.bottomMargin = (16 * resources.displayMetrics.density).toInt() + navInsets.bottom
            view.layoutParams = params
            insets
        }

        // Global Responsive Viewport & IME Insets Manager on mainContainer
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContainer) { _, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            val isBottomNavVisible = binding.bottomNavigation.visibility == View.VISIBLE

            // Bottom Nav visible: leaves 92dp + navInsets.bottom so list items stop above floating bottom bar
            // Bottom Nav hidden: leaves maxOf(navInsets.bottom, imeInsets.bottom) so content pads above keyboard/gesture bar
            val bottomPadding = if (isBottomNavVisible) {
                (92 * resources.displayMetrics.density).toInt() + navInsets.bottom
            } else {
                maxOf(navInsets.bottom, imeInsets.bottom)
            }

            binding.navHostFragment.setPadding(0, 0, 0, bottomPadding)
            insets
        }

        // Global Toolbar Configuration Mapping per Destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val config = when (destination.id) {
                // Auth / Splash / Onboarding Screens -> HIDDEN Toolbar
                R.id.splashFragment, R.id.onboardingFragment, R.id.featureOverviewFragment,
                R.id.welcomeFragment, R.id.introFragment, R.id.loginFragment,
                R.id.registerFragment, R.id.forgotPasswordFragment, R.id.resetPasswordFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    ToolbarConfiguration(mode = ToolbarMode.HIDDEN)
                }

                // SOS Emergency Screen -> Navigation Toolbar
                R.id.sosFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    ToolbarConfiguration(
                        title = "Emergency SOS",
                        subtitle = "Live Security Alert",
                        mode = ToolbarMode.NAVIGATION,
                        showBackButton = true
                    )
                }

                // Home Map Dashboard -> Hidden Toolbar Mode (cardHeader handles top insets)
                R.id.homeFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    ToolbarConfiguration(
                        mode = ToolbarMode.HIDDEN
                    )
                }

                // Trip Detail -> Custom Header inside layout
                R.id.tripDetailFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    ToolbarConfiguration(
                        mode = ToolbarMode.HIDDEN
                    )
                }

                // Top-Level Hubs -> Collapsing Large Title Mode
                R.id.bookingsFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    ToolbarConfiguration(
                        title = "Bookings",
                        subtitle = "Manage & track all your assistant journeys",
                        mode = ToolbarMode.LARGE_TITLE,
                        showBackButton = false
                    )
                }

                R.id.walletFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    ToolbarConfiguration(
                        title = "My Wallet",
                        subtitle = "Manage balance, add funds, and view payments",
                        mode = ToolbarMode.LARGE_TITLE,
                        showBackButton = false
                    )
                }

                R.id.userProfileFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    ToolbarConfiguration(
                        title = "My Profile",
                        subtitle = "Verified Guest Account",
                        mode = ToolbarMode.LARGE_TITLE,
                        showBackButton = false,
                        actions = listOf(
                            ToolbarAction(ActionType.SETTINGS, R.drawable.ic_ms_shield, "Settings") {
                                navController.navigate(R.id.securityFragment)
                            }
                        )
                    )
                }

                R.id.chatFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    ToolbarConfiguration(
                        title = "Assistant Chat",
                        subtitle = "Live Messaging",
                        mode = ToolbarMode.NAVIGATION,
                        showBackButton = true
                    )
                }

                R.id.assistantHomeFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    ToolbarConfiguration(
                        title = "Assistant Dashboard",
                        subtitle = "Duty Mode Active",
                        mode = ToolbarMode.LARGE_TITLE,
                        showBackButton = false
                    )
                }

                R.id.adminDashboardFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    ToolbarConfiguration(
                        title = "Admin Console",
                        subtitle = "Enterprise Control Center",
                        mode = ToolbarMode.LARGE_TITLE,
                        showBackButton = false
                    )
                }

                // Secondary Flow & Detail Screens -> Navigation Mode (Compact 64dp with 44dp Circular Glass Back Button)
                else -> {
                    val titleStr = destination.label?.toString() ?: "NavAssist"
                    binding.bottomNavigation.visibility = View.GONE
                    ToolbarConfiguration(
                        title = titleStr,
                        mode = ToolbarMode.NAVIGATION,
                        showBackButton = true
                    )
                }
            }

            binding.globalToolbar.configure(config, navController)
            ViewCompat.requestApplyInsets(binding.mainContainer)
        }
    }
}
