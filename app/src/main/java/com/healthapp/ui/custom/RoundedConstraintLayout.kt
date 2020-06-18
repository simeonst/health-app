package com.healthapp.ui.custom

import android.content.Context
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.healthapp.R

class RoundedConstraintLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {

        if (this.background != null) {

            val border = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                context.resources.getDrawable(R.drawable.alert_card_background, null)
            } else {
                @Suppress("DEPRECATION")
                context.resources.getDrawable(R.drawable.alert_card_background)
            }

            val ld = LayerDrawable(arrayOf(this.background, border))
            this.background = ld

        } else {
            this.setBackgroundResource(R.drawable.alert_card_background)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            elevation = context.resources.getDimension(R.dimen.alert_dialog_elevation)
        }

    }
}