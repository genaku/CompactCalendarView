package com.github.sundeepk.compactcalendarview

import android.view.animation.Animation
import android.view.animation.Transformation

internal class CollapsingAnimation(
        private val view: CompactCalendarView,
        private val compactCalendarController: CompactCalendarController,
        private val targetHeight: Int,
        private val targetGrowRadius: Int,
        private val down: Boolean
) : Animation() {

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val newHeight: Int
        val grow = if (down) {
            newHeight = (targetHeight * interpolatedTime).toInt()
            interpolatedTime * (targetGrowRadius * 2)
        } else {
            val progress = 1 - interpolatedTime
            newHeight = (targetHeight * progress).toInt()
            progress * (targetGrowRadius * 2)
        }
        compactCalendarController.setGrowProgress(grow)
        view.layoutParams.height = newHeight
        view.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}