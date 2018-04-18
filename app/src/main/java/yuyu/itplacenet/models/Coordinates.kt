package yuyu.itplacenet.models

data class Coordinates @JvmOverloads constructor(
        var user:      String,
        var latitude:  Double,
        var longitude: Double,
        var timestamp: Long
)