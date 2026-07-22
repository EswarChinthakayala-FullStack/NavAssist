package com.navassist.android.presentation.wallet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.navassist.android.R
import com.navassist.android.databinding.FragmentWalletBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.wallet.adapter.WalletTransactionAdapter
import com.navassist.android.presentation.wallet.bottomsheet.TopUpBottomSheet
import com.navassist.android.presentation.wallet.bottomsheet.TransactionDetailsBottomSheet
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject

@AndroidEntryPoint
class WalletFragment : BaseFragment<FragmentWalletBinding>(FragmentWalletBinding::inflate) {

    private val viewModel: WalletViewModel by viewModels()
    private lateinit var transactionAdapter: WalletTransactionAdapter

    override fun setupViews() {
        setupAdapter()
        setupListeners()
    }

    private fun setupAdapter() {
        transactionAdapter = WalletTransactionAdapter { item ->
            val detailsSheet = TransactionDetailsBottomSheet.newInstance(item) { txnId ->
                showToast("Downloading receipt for $txnId...")
            }
            detailsSheet.show(childFragmentManager, TransactionDetailsBottomSheet.TAG)
        }
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = transactionAdapter
    }

    private fun setupListeners() {
        // Add Money Trigger
        binding.quickTopUpCard.onAddMoneyClickListener = {
            val topUpSheet = TopUpBottomSheet.newInstance { amount ->
                viewModel.initiateTopUp(amount)
            }
            topUpSheet.show(childFragmentManager, TopUpBottomSheet.TAG)
        }

        // Filter Chips
        binding.chipAll.setOnClickListener { viewModel.setFilter("all") }
        binding.chipCredits.setOnClickListener { viewModel.setFilter("credits") }
        binding.chipDebits.setOnClickListener { viewModel.setFilter("debits") }
        binding.chipRefunds.setOnClickListener { viewModel.setFilter("refunds") }

        // Search Input Watcher
        binding.etSearchTransaction.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.loadTransactions(searchQuery = s?.toString()?.trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Floating Refresh FAB
        binding.fabRefresh.setOnClickListener {
            binding.coordinatorLayout.announceForAccessibility("Refreshing wallet balance and activity")
            showToast("Refreshing wallet balance...")
            viewModel.loadWalletData()
        }
    }

    private fun launchRazorpayCheckout(orderId: String, keyId: String, amount: Double) {
        val checkout = Checkout()
        checkout.setKeyID(keyId)

        try {
            val options = JSONObject().apply {
                put("name", "NavAssist Wallet")
                put("description", "Wallet Balance Top-Up")
                put("theme.color", "#09090B")
                put("currency", "INR")
                put("amount", (amount * 100).toInt())
                put("order_id", orderId)
                put("prefill.email", "guest@navassist.app")
                put("prefill.contact", "9876543210")
            }
            checkout.open(requireActivity(), options)
        } catch (e: Exception) {
            showSnackbar("Error initializing Razorpay Checkout: ${e.message}")
        }
    }

    fun onPaymentSuccess(razorpayPaymentId: String?) {
        val paymentId = razorpayPaymentId ?: "pay_mock_${System.currentTimeMillis()}"
        viewModel.onRazorpayPaymentSuccess("order_topup", paymentId, "sig_mock")
    }

    fun onPaymentError(code: Int, response: String?) {
        viewModel.onRazorpayPaymentError(code, response)
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Wallet Balance State
                launch {
                    viewModel.balanceState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.layoutContent.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.layoutContent.visibility = View.VISIBLE

                                val info = state.data
                                binding.heroBalanceCard.bindBalance(
                                    info.availableBalance,
                                    info.pendingBalance,
                                    info.cashbackBalance,
                                    info.lastUpdated
                                )

                                binding.analyticsCard.bindAnalytics(
                                    credits = info.availableBalance + 500.0,
                                    debits = 250.0,
                                    cashback = info.cashbackBalance,
                                    refunds = info.pendingBalance
                                )
                            }
                            is UiState.Error -> {
                                binding.skeletonView.stopShimmer()
                                showSnackbar(state.message)
                            }
                            else -> {}
                        }
                    }
                }

                // Collect Transactions State
                launch {
                    viewModel.transactionsState.collect { state ->
                        if (state is UiState.Success) {
                            val list = state.data
                            if (list.isEmpty()) {
                                binding.emptyTransactionView.visibility = View.VISIBLE
                                binding.rvTransactions.visibility = View.GONE
                            } else {
                                binding.emptyTransactionView.visibility = View.GONE
                                binding.rvTransactions.visibility = View.VISIBLE
                                transactionAdapter.submitList(list)
                            }
                        }
                    }
                }

                // Collect Effects
                launch {
                    viewModel.effects.collect { effect ->
                        when (effect) {
                            is WalletEffect.ShowToast -> showToast(effect.message)
                            is WalletEffect.ShowSnackbar -> showSnackbar(effect.message)
                            is WalletEffect.LaunchRazorpayCheckout -> {
                                launchRazorpayCheckout(effect.orderId, effect.razorpayKeyId, effect.amount)
                            }
                        }
                    }
                }
            }
        }
    }
}
