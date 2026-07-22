package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.presentation.widgets.row.InfoRowView

class BookingReceiptCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val rowBookingRef: InfoRowView
    private val rowTxnId: InfoRowView
    private val rowPayId: InfoRowView
    private val rowDate: InfoRowView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 26f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 6f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val headerText = TextView(context).apply {
            text = "Payment Audit & Receipt"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val density = context.resources.displayMetrics.density

        rowBookingRef = InfoRowView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (12 * density).toInt()
            }
        }

        rowTxnId = InfoRowView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (8 * density).toInt()
            }
        }

        rowPayId = InfoRowView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (8 * density).toInt()
            }
        }

        rowDate = InfoRowView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (8 * density).toInt()
            }
        }

        container.addView(headerText)
        container.addView(rowBookingRef)
        container.addView(rowTxnId)
        container.addView(rowPayId)
        container.addView(rowDate)

        addView(container)

        setReceiptDetails("BK_10293", "TXN_987654", "pay_8849201")
    }

    fun setReceiptDetails(bookingId: String, txnId: String, payId: String) {
        rowBookingRef.setInfo("Booking Reference", bookingId)
        rowTxnId.setInfo("Razorpay Order ID", txnId)
        rowPayId.setInfo("Payment ID", payId)
        rowDate.setInfo("Payment Timestamp", "Today, 03:30 PM")
    }
}
