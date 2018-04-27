package yuyu.itplacenet.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.os.Build
import android.view.View

class ProgressBar() {

    private var form: View? = null
    private var progress: View? = null

    constructor(form: View, progress: View) : this() {
        setFields(form, progress)
    }

    fun setFields(form: View, progress: View) {
        this.form = form
        this.progress = progress
    }

    fun show() {
        toggleProgress(true)
    }

    fun hide() {
        toggleProgress(false)
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun toggleProgress(show: Boolean) {
        val shortAnimTime = android.R.integer.config_shortAnimTime.toLong()

        setFormVisibility(show)
        form?.animate()
                ?.setDuration(shortAnimTime)
                ?.alpha((if (show) 0 else 1).toFloat())
                ?.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        setFormVisibility(show)
                    }
                })

        setProgressVisibility(show)
        progress?.animate()
                ?.setDuration(shortAnimTime)
                ?.alpha((if (show) 1 else 0).toFloat())
                ?.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        setProgressVisibility(show)
                    }
                })
    }

    // TODO: да, можно очень удобная штука
    // можно написать extension-функциию для любого типа вью, например
    //    fun View.show() {
    //        visibility = View.VISIBLE
    //    }

    private fun setFormVisibility(show: Boolean) {
        form?.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun setProgressVisibility(show: Boolean) {
        progress?.visibility = if (show) View.VISIBLE else View.GONE
    }
}

