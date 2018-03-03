package yuyu.itplacenet

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import yuyu.itplacenet.utils.*
import yuyu.itplacenet.managers.AuthManager
import yuyu.itplacenet.helpers.UserHelper
import yuyu.itplacenet.ui.ProgressBar
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

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
        // Reset errors.
        email.error = null
        password.error = null

        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(passwordStr)) {
            password.error = getString(R.string.error_field_required)
            focusView = password
            cancel = true
        } else if (!TextUtils.isEmpty(passwordStr) && !UserHelper().isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!UserHelper().isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {
            userSingIn(emailStr, passwordStr)
        }
    }

    private fun userSingIn(email: String, password: String) {
        val answerIntent = Intent()
        val auth = AuthManager()
        val progressBar = ProgressBar(login_form, login_progress)

        progressBar.show()
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, { task ->
                    if (task.isSuccessful) {
                        setResult(RESULT_OK, answerIntent)
                        finish()
                    } else {
                        toast("${getString(R.string.error_sign_in_failed)} ${task.exception}")
                        // не работает, хз почему, разобраться
                        //progressBar.hide()
                        // пока поступим так
                        setResult(RESULT_CANCELED, answerIntent)
                        finish()
                    }
                })
    }

    /* Регистрация */

    private fun gotoRegistration() {
        startActivityForResult( AuthManager().getRegistrationIntent(), RC_SIGN_IN )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
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
