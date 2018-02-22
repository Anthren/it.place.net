package yuyu.itplacenet

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_profile_edit.*
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.MaskFormatWatcher

class ProfileEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        loadUserData()
    }

    private fun loadUserData() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val userId = user.uid
            val userName = user.displayName
            val userPhone = user.phoneNumber
            val userEmail = user.email
            val userPhotoUrl = user.photoUrl

            user_name_text.setText(userName)
            user_name.setText(userName)
            user_phone.setText(userPhone)
            makePhoneMask(user_phone)
            user_email.setText(userEmail)
            profile_photo.setImageURI(userPhotoUrl)
        }
    }

    private fun makePhoneMask(phoneEditText: EditText) {
        val formatWatcher = MaskFormatWatcher(
                MaskImpl.createTerminated(PredefinedSlots.RUS_PHONE_NUMBER)
        )
        formatWatcher.installOn(phoneEditText)
    }

}
