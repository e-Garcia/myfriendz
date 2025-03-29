package com.egarcia.myfriendz.showFriend.utils

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.egarcia.myfriendz.R
import java.time.LocalDate

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("app:friendContactStatusBackground")
    fun setFriendContactStatusBackground(view: View, lastContacted: LocalDate) {
        val context = view.context
        val backgroundColor = determineStatusBackground(context, lastContacted)

        val gradientDrawable = GradientDrawable()
        gradientDrawable.setColor(backgroundColor)

        view.background = gradientDrawable
    }
}


fun determineStatusBackground(context: Context, lastContacted: LocalDate) : Int {
    val sixMonthsAgo = LocalDate.now().minusMonths(6)
    val threeMonthsAgo = LocalDate.now().minusMonths(3)
    val oneMonthAgo = LocalDate.now().minusMonths(1)

    return when {
        lastContacted.isBefore(sixMonthsAgo) -> ContextCompat.getColor(context, R.color.red)
        lastContacted.isBefore(threeMonthsAgo) -> ContextCompat.getColor(context, R.color.orange)
        lastContacted.isBefore(oneMonthAgo) -> ContextCompat.getColor(context, R.color.yellow)
        else -> ContextCompat.getColor(context, R.color.green)
    }
}