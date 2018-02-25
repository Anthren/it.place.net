package yuyu.itplacenet.models

import android.net.Uri
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
class User {

    var name: String? = ""
    var email: String? = ""
    var phone: String? = ""
    var photo: String? = ""
    //var photoUrl: Uri? = Uri.EMPTY

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    constructor(username: String?,
                phone: String?,
                email: String?) {
        this.name = username
        this.phone = phone
        this.email = email
    }

}