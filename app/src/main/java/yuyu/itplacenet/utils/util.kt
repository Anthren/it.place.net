package yuyu.itplacenet.utils

import android.content.Context
import android.widget.Toast



fun isEmailValid( email: String ): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isPasswordValid( password: String ): Boolean {
    return password.length > 4
}

fun message( context: Context, text: String? ) {
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
}
