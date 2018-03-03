package yuyu.itplacenet.helpers

import android.util.Patterns


class UserHelper {

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length > 4
    }

}