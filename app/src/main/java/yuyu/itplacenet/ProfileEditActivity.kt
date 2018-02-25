package yuyu.itplacenet

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_profile_edit.*
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.MaskFormatWatcher
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.firestore.*
import yuyu.itplacenet.utils.*
import yuyu.itplacenet.models.*


class ProfileEditActivity : AppCompatActivity() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private val dbUsers = "users"

    private var userId = ""

    private var maySave = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        loadUserData()
        makePhoneMask(user_phone)
        checkIsSaveAvailable()

        user_name.setOnFocusChangeListener { v, hasFocus ->
            validateUserName()
        }
        user_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                checkIsSaveAvailable()
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        user_email.setOnFocusChangeListener { v, hasFocus ->
            validateEmail()
        }
        user_email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                checkIsSaveAvailable()
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        save_button.setOnClickListener {
            saveChanges()
        }
    }

    private fun loadUserData() {
        if (currentUser != null) {
            userId = currentUser.uid
            val userName = currentUser.displayName
            val userPhone = currentUser.phoneNumber
            val userEmail = currentUser.email
            val userPhotoUrl = currentUser.photoUrl

            user_name_text.setText(userName)
            user_name.setText(userName)
            user_phone.setText(userPhone)
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

    private fun saveChanges() {
        if( validateAll() ) {

            val user = User(user_name.text.toString(), user_phone.text.toString(), user_email.text.toString())

            if( userId != "" ) {
                db.collection(dbUsers).document(userId)
                        .set(user, SetOptions.merge())
                        .addOnSuccessListener({
                            message(this@ProfileEditActivity, getString(R.string.note_save_done))
                        })
                        .addOnFailureListener({ e: Exception ->
                            message(this@ProfileEditActivity, getString(R.string.error_save_failed) + " " + e)
                        })
            } else {
                db.collection(dbUsers)
                        .add(user)
                        .addOnSuccessListener({documentReference: DocumentReference ->
                            userId = documentReference.id
                            message(this@ProfileEditActivity, getString(R.string.note_save_done)+" "+userId)
                        })
                        .addOnFailureListener({ e: Exception ->
                            message(this@ProfileEditActivity, getString(R.string.error_save_failed) + " " + e)
                        })
            }
        }
    }

    private fun validateAll(): Boolean {
        var cancel = false
        var focusView: View? = null

        if( !validateUserName() ) {
            focusView = user_name
            cancel = true
        }

        if( !validateEmail() ) {
            focusView = user_email
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
            return false
        }

        return true
    }

    private fun validateUserName() : Boolean {
        var err = false
        var errStr = ""

        if( user_name.text.toString().isEmpty() ) {
            err = true
            errStr = getString(R.string.error_field_required)
        }

        user_name_layout.isErrorEnabled = err
        if( err ) {
            user_name_layout.error = errStr
            return false
        }
        return true
    }

    private fun validateEmail() : Boolean {
        var err = false
        var errStr = ""
        val emailStr = user_email.text.toString()

        if( emailStr.isEmpty() ) {
            err = true
            errStr = getString(R.string.error_field_required)
        } else if (!isEmailValid(emailStr)) {
            err = true
            errStr = getString(R.string.error_invalid_email)
        }

        user_email_layout.isErrorEnabled = err
        if( err ) {
            user_email_layout.error = errStr
            return false
        }
        return true
    }

    private fun checkIsSaveAvailable() {
        if( !maySave ) {
            if( !user_name.text.toString().isEmpty() && !user_email.text.toString().isEmpty() ) {
                save_button.isEnabled = true
                maySave = true
            }
        }
    }

}
