package yuyu.itplacenet.utils

import android.content.Context
import android.widget.Toast


@JvmOverloads
fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}

