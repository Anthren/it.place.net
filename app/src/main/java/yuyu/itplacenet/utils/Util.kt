package yuyu.itplacenet.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.EditText
import android.widget.Toast
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.MaskFormatWatcher
import java.security.MessageDigest
import java.text.DecimalFormat


@JvmOverloads
fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_LONG) =
        Toast.makeText(this.applicationContext, message, duration).show()

fun Context.getSize(id: Int) : Int =
        this.resources.getDimensionPixelSize(id)

fun Intent.isIntentAvailable(context: Context) : Boolean {
    val packageManager = context.packageManager
    val list = packageManager.queryIntentActivities(this, PackageManager.MATCH_DEFAULT_ONLY)
    return list.isNotEmpty()
}

fun EditText.str() : String =
        this.text.toString()

fun EditText.isEmpty() : Boolean =
        this.str().isEmpty()

fun EditText.makePhoneMask() {
    val formatWatcher = MaskFormatWatcher(
            MaskImpl.createTerminated(PredefinedSlots.RUS_PHONE_NUMBER)
    )
    formatWatcher.installOn(this)
}

fun md5(s: String): String {
    val digest = MessageDigest.getInstance("MD5")
    digest.update(s.toByteArray())
    val messageDigest = digest.digest()

    val hexString = StringBuffer()
    messageDigest.forEach { hexString.append(Integer.toHexString(0xFF and it.toInt())) }

    return hexString.toString()
}

fun formatNumber( number: Double, pattern: String ) : String {
    return DecimalFormat(pattern).format(number)
}