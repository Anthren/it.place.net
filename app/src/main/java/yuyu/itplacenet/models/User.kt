package yuyu.itplacenet.models

data class User @JvmOverloads constructor(
        var name:  String? = "",
        var email: String? = "",
        var phone: String? = "",
        var photo: String? = null,
        var latitude:   Double? = null,
        var longitude:  Double? = null,
        var lastUpdate: Long?   = null
)