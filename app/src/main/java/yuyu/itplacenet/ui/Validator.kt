package yuyu.itplacenet.ui

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.widget.EditText
import android.util.Patterns
import yuyu.itplacenet.R
import yuyu.itplacenet.utils.*

class Validator( private val context: Context ) {

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 4
    }

    // TODO В принципе эти методы можно сделать одним, а в него передавать сообщение об ошибке и функцию проверки
    // TODO Ну и лучше как-то разделить валидацию и показ ошибок. Будет больше кода, но меньше связность
    private fun validateUserName(editText: EditText): Boolean {
        var err = false
        var errStr = ""
        val userNameStr = editText.str()

        if (userNameStr.isEmpty()) {
            err = true
            errStr = context.getString(R.string.error_field_required)
        }

        val parentLayout: TextInputLayout = editText.parentForAccessibility as TextInputLayout

        parentLayout.isErrorEnabled = err
        if (err) {
            parentLayout.error = errStr
            return false
        }
        return true
    }

    private fun validateEmail(editText: EditText) : Boolean {
        var err = false
        var errStr = ""
        val emailStr = editText.str()

        if( emailStr.isEmpty() ) {
            err = true
            errStr = context.getString(R.string.error_field_required)
        } else if (!isEmailValid( emailStr )) {
            err = true
            errStr = context.getString(R.string.error_invalid_email)
        }

        val parentLayout: TextInputLayout = editText.parentForAccessibility as TextInputLayout

        parentLayout.isErrorEnabled = err
        if (err) {
            parentLayout.error = errStr
            return false
        }
        return true
    }

    private fun validatePassword(editText: EditText) : Boolean {
        var err = false
        var errStr = ""
        val passwordStr = editText.str()

        if (passwordStr.isEmpty()) {
            err = true
            errStr = context.getString(R.string.error_field_required)
        } else if (!isPasswordValid( passwordStr )) {
            err = true
            errStr = context.getString(R.string.error_invalid_password)
        }

        val parentLayout = editText.parentForAccessibility as TextInputLayout

        parentLayout.isErrorEnabled = err
        if (err) {
            parentLayout.error = errStr
            return false
        }
        return true
    }

    fun validate(fields: Map<String, EditText>): Boolean {
        var cancel = false
        var focusView: EditText? = null

        fields.forEach { (type, field) ->
            when (type) {
                "password" ->
                    if (!validatePassword(field)) {
                        focusView = field
                        cancel = true
                    }
                "email" ->
                    if (!validateEmail(field)) {
                        focusView = field
                        cancel = true
                    }
                "name" ->
                    if (!validateUserName(field)) {
                        focusView = field
                        cancel = true
                    }
            }
        }

        if (cancel) {
            focusView?.requestFocus()
            return false
        }

        return true
    }
}