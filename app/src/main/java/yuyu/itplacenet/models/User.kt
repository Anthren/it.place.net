package yuyu.itplacenet.models

// можно из этого сделать датакласс и поля сделать неопциональными
class User() {

    var name = ""
    var email = ""
    var phone = ""
    var photo = ""

    constructor(name: String?, phone: String?, email: String?) : this() {
        this.name  = name  ?: ""
        this.phone = phone ?: ""
        this.email = email ?: ""
    }

}