package yuyu.itplacenet

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import yuyu.itplacenet.utils.*
import yuyu.itplacenet.helpers.UserHelper
import yuyu.itplacenet.managers.AuthManager
import yuyu.itplacenet.ui.ProgressBar
import yuyu.itplacenet.ui.Validator
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private val auth = AuthManager()
    private val validator = Validator(this)
    private val userHelper = UserHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        registration_button.setOnClickListener {
            gotoRegistration()
        }

        email_sign_in_button.setOnClickListener {
            attemptLogin()
        }
    }

    /* Логин */

    private fun attemptLogin() {
        val fields = mapOf(
                "password" to password,
                "email" to email
        )
        if( validator.validate(fields) ) {
            userSingIn(email.str(), password.str())
        }
    }

    private fun userSingIn(email: String, password: String) {
        val answerIntent = Intent()
        val progressBar = ProgressBar(login_form, login_progress)

        progressBar.show()
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, { task ->
                    if (task.isSuccessful) {
                        setResult(RESULT_OK, answerIntent)
                        finish()
                    } else {
                        toast("${getString(R.string.error_sign_in_failed)}: ${task.exception}")
                        progressBar.hide() // не работает, хз почему
                        hide() // так работает
                    }
                })
    }

    private fun toggleProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        login_form.visibility = if (show) View.GONE else View.VISIBLE
        login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }
    private fun hide() {
        toggleProgress(false)
    }

    /* Регистрация */

    private fun gotoRegistration() {
        startActivityForResult( auth.makeRegistrationIntentBuilder().build(), RC_SIGN_IN )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                userHelper.addUserIfNotExist(auth.user)
                gotoMain()
            } else {
                toast(getString(R.string.error_sign_in_failed))
            }
        }
    }

    private fun gotoMain() {
        startActivity( Intent(this, MainActivity::class.java) )
    }

}
