package yuyu.itplacenet.helpers

import java.util.*

class DateHelper {

    private val timezone = TimeZone.getTimeZone("UTC")

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

}