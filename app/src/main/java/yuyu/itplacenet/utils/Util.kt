package yuyu.itplacenet.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.EditText
import android.widget.Toast
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.MaskFormatWatcher


@JvmOverloads
fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_LONG) =
        Toast.makeText(this.applicationContext, message, duration).show()

fun Intent.isIntentAvailable(context: Context) : Boolean {
    val packageManager = context.packageManager
    val list = packageManager.queryIntentActivities(this, PackageManager.MATCH_DEFAULT_ONLY)
    return list.isNotEmpty()
}

fun EditText.str() : String {
    return this.text.toString()
}

fun EditText.isEmpty() : Boolean {
    return this.str().isEmpty()
}

fun EditText.makePhoneMask() {
    val formatWatcher = MaskFormatWatcher(
            MaskImpl.createTerminated(PredefinedSlots.RUS_PHONE_NUMBER)
    )
    formatWatcher.installOn(this)
}
