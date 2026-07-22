package com.navassist.android.presentation.widgets.search

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import com.navassist.android.R

class DestinationSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val searchEditText: EditText
    private val clearImageView: ImageView

    var onQueryChangedListener: ((String) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val density = context.resources.displayMetrics.density
        val paddingHorizontalPx = (16 * density).toInt()
        val paddingVerticalPx = (12 * density).toInt()

        setPadding(paddingHorizontalPx, paddingVerticalPx, paddingHorizontalPx, paddingVerticalPx)
        setBackgroundResource(R.drawable.bg_input_field)

        val searchIcon = ImageView(context).apply {
            layoutParams = LayoutParams((20 * density).toInt(), (20 * density).toInt())
            setImageResource(R.drawable.ic_benefit_pickup)
        }

        searchEditText = EditText(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (12 * density).toInt()
                marginEnd = (12 * density).toInt()
            }
            background = null
            hint = "Where are you headed?"
            setHintTextColor(Color.parseColor("#71717A"))
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 15f
        }

        clearImageView = ImageView(context).apply {
            layoutParams = LayoutParams((20 * density).toInt(), (20 * density).toInt())
            setImageResource(R.drawable.ic_eye)
            visibility = GONE
        }

        addView(searchIcon)
        addView(searchEditText)
        addView(clearImageView)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                clearImageView.visibility = if (query.isNotEmpty()) VISIBLE else GONE
                onQueryChangedListener?.invoke(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        clearImageView.setOnClickListener {
            searchEditText.text?.clear()
        }
    }
}
