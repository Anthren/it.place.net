package yuyu.itplacenet.models

data class Coordinates @JvmOverloads constructor(
        var user:      String? = null,
        var latitude:  Double? = null,
        var longitude: Double? = null,
        var timestamp: Long? = null
)