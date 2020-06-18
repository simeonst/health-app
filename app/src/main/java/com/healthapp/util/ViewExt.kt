package com.healthapp.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View


fun View.setAnimation(i: Int, onAttach: Boolean, duration: Long, delay: Long) {
    var position = i
    if (!onAttach) {
        position = -1
    }
    val isNotFirstItem = position == -1
    position++
    alpha = 0f
    val animatorSet = AnimatorSet()
    val animator = ObjectAnimator.ofFloat(this, "alpha", 0f, 0.5f, 1.0f)
    ObjectAnimator.ofFloat(this, "alpha", 0f).start()
    animator.startDelay = if (isNotFirstItem) delay / 2 else position * delay / 3
    animator.duration = duration
    animatorSet.play(animator)
    animator.start()
}
