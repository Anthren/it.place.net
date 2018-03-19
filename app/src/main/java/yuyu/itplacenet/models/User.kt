package yuyu.itplacenet.models

data class User @JvmOverloads constructor(
        var name:  String? = "",
        var email: String? = "",
        var phone: String? = "",
        var photo: String? = ""
)