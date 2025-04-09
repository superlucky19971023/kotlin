import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment

/**
 * A utility class that adds drag-to-dismiss functionality to any Fragment.
 * This allows users to drag the fragment up to dismiss it.
 */
class DragToTopDismissHelper(
    private val fragment: Fragment,
    private val rootView: View,
    private val config: Config = Config()
) {
    // Configuration options for customizing the drag behavior
    data class Config(
        val dragHandleAreaPercentage: Float = 0.2f, // Bottom percentage of view that acts as drag handle
        val dismissThreshold: Float = 0.3f, // Percentage of screen height to trigger dismiss
        val minVelocityToDismiss: Float = -1000f, // Minimum velocity (px/s) to trigger dismiss (negative for upward)
        val animationDuration: Long = 200, // Duration of dismiss/restore animations
        val allowHorizontalDrag: Boolean = false, // Whether to allow horizontal dragging
        val onDismiss: (() -> Unit)? = null // Optional callback when dismissed
    )

    private var initialY: Float = 0f
    private var initialX: Float = 0f
    private var dY: Float = 0f
    private var dX: Float = 0f
    private var velocityTracker: VelocityTracker? = null
    private var isDragging = false

    init {
        setupDragToDismiss()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDragToDismiss() {
        rootView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Only start dragging if touch is near the bottom of the fragment
                    if (event.y > rootView.height * (1 - config.dragHandleAreaPercentage)) {
                        initialY = event.rawY
                        initialX = event.rawX
                        dY = view.translationY
                        dX = view.translationX

                        // Initialize velocity tracker
                        if (velocityTracker == null) {
                            velocityTracker = VelocityTracker.obtain()
                        } else {
                            velocityTracker?.clear()
                        }
                        velocityTracker?.addMovement(event)

                        isDragging = true
                        true
                    } else {
                        isDragging = false
                        false
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDragging) {
                        velocityTracker?.addMovement(event)

                        // Calculate new Y position
                        val newY = dY + event.rawY - initialY

                        // Only allow upward movement (negative translation)
                        if (newY <= 0) {
                            view.translationY = newY

                            // Apply horizontal movement if enabled
                            if (config.allowHorizontalDrag) {
                                val newX = dX + event.rawX - initialX
                                view.translationX = newX
                            }

                            // Gradually fade out as the fragment moves up
                            val alpha = 1.0f - (Math.abs(newY) / view.height)
                            view.alpha = if (alpha < 0) 0f else alpha
                        }
                        true
                    } else {
                        false
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        velocityTracker?.addMovement(event)
                        velocityTracker?.computeCurrentVelocity(1000) // pixels per second
                        val yVelocity = velocityTracker?.yVelocity ?: 0f

                        // Determine whether to dismiss or restore the fragment
                        val translationY = view.translationY
                        val screenHeight = view.height

                        // For upward movement, we check if translation is more negative than threshold
                        // or if velocity is more negative than minVelocityToDismiss
                        if (translationY < -screenHeight * config.dismissThreshold ||
                            yVelocity < config.minVelocityToDismiss) {
                            // Dismiss the fragment
                            animateDismiss(view)
                        } else {
                            // Restore the fragment
                            animateRestore(view)
                        }

                        // Clean up
                        velocityTracker?.recycle()
                        velocityTracker = null
                        isDragging = false
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }

    private fun animateDismiss(view: View) {
        val screenHeight = view.height
        // Animate upward to dismiss
        ObjectAnimator.ofFloat(view, "translationY", view.translationY, -screenHeight.toFloat()).apply {
            duration = config.animationDuration
            interpolator = DecelerateInterpolator()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    // Execute custom dismiss callback if provided
                    config.onDismiss?.invoke()

                    // Remove the fragment when animation completes
                    fragment.parentFragmentManager.beginTransaction()
                        .remove(fragment)
                        .commit()
                }
            })
            start()
        }

        // Fade out simultaneously
        ObjectAnimator.ofFloat(view, "alpha", view.alpha, 0f).apply {
            duration = config.animationDuration
            start()
        }
    }

    private fun animateRestore(view: View) {
        ObjectAnimator.ofFloat(view, "translationY", view.translationY, 0f).apply {
            duration = config.animationDuration
            interpolator = DecelerateInterpolator()
            start()
        }

        // Reset horizontal position if needed
        if (config.allowHorizontalDrag && view.translationX != 0f) {
            ObjectAnimator.ofFloat(view, "translationX", view.translationX, 0f).apply {
                duration = config.animationDuration
                interpolator = DecelerateInterpolator()
                start()
            }
        }

        // Restore opacity
        ObjectAnimator.ofFloat(view, "alpha", view.alpha, 1f).apply {
            duration = config.animationDuration
            start()
        }
    }
}

/**
 * Extension function to easily add drag-to-dismiss functionality to any Fragment
 */
fun Fragment.enableDragToTopDismiss(
    rootView: View,
    config: DragToTopDismissHelper.Config = DragToTopDismissHelper.Config()
): DragToTopDismissHelper {
    return DragToTopDismissHelper(this, rootView, config)
}

