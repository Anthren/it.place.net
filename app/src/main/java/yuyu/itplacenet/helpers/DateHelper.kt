package yuyu.itplacenet.helpers

import android.content.Context
import java.util.*
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import yuyu.itplacenet.R
import java.text.SimpleDateFormat

class DateHelper( private val context: Context ) {

    private val timezone = TimeZone.getTimeZone("UTC")
    private val locale = Locale.ENGLISH

    private fun getMoment() : Calendar {
        return Calendar.getInstance(this.timezone)
    }

    fun getTimeInMillis() : Long {
        val moment = this.getMoment()
        return moment.timeInMillis
    }

    fun beforeOnMinutes( minutes: Int ) : Long {
        val moment = this.getMoment()
        moment.add(Calendar.MINUTE, -minutes)
        return moment.timeInMillis
    }

    fun getDateFormat( format: String ) : String {
        return SimpleDateFormat(format, this.locale).format(Date())
    }

    // Приблизительная разница в датах
    fun diffString( date1: Long?, date2: Long? = null ) : String {

        if( date1 == null ) return context.getString(R.string.minutes, "???")

        val d1 = Instant.ofEpochMilli(date1)
        val d2 = Instant.ofEpochMilli(date2 ?: this.getTimeInMillis())

        val diff = Duration.between(d1, d2)

        if( diff.isZero ) return context.getString(R.string.minutes, "1")

        val d = diff.toDays()
        val h = diff.toHours() - d*24
        val m = diff.toString().substringAfter("PT").substringAfter("H").substringBefore("M")

        var diffStr = ""
        if( d > 0 )    diffStr += context.getString(R.string.days,    d.toString()) + " "
        if( h > 0 )    diffStr += context.getString(R.string.hours,   h.toString()) + " "
        if( m != "0" ) diffStr += context.getString(R.string.minutes, m) + " "
        return diffStr.trim()
    }

}