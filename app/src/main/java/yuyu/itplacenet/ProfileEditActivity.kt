package yuyu.itplacenet

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuInflater
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_profile_edit.*
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.MaskFormatWatcher
import yuyu.itplacenet.models.User
import yuyu.itplacenet.utils.isEmailValid
import yuyu.itplacenet.utils.message
import java.io.FileNotFoundException
import java.io.InputStream
import android.Manifest.permission.*
import android.content.pm.PackageManager
import android.os.Build
import android.support.design.widget.Snackbar


class ProfileEditActivity : AppCompatActivity() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private val dbUsers = "users"

    private var userId = ""
    private var userPhotoUrl: Uri? = Uri.EMPTY

    private var maySave = false

    private val RC_LOAD_FROM_GALLERY = 111
    private val RC_LOAD_FROM_CAMERA  = 112

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        loadUserData()
        makePhoneMask(user_phone)
        checkIsSaveAvailable()

        user_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                validateUserName()
                checkIsSaveAvailable()
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        user_email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                validateEmail()
                checkIsSaveAvailable()
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        save_button.setOnClickListener {
            saveChanges()
        }

        change_photo_button.setOnClickListener{ v:View ->
            showPopup(v)
        }
    }

    private fun loadUserData() {
        showProgress(true)
        if (currentUser != null) {
            userId = currentUser.uid
            userPhotoUrl = currentUser.photoUrl

            db.collection(dbUsers).document(userId)
                    .get()
                    .addOnSuccessListener({ documentSnapshot: DocumentSnapshot ->
                        val user:User = when( documentSnapshot.exists() ) {
                            true  -> documentSnapshot.toObject(User::class.java)
                            false -> User(currentUser.displayName, currentUser.phoneNumber, currentUser.email)
                        }
                        updateProfileView(user)
                        completeProcess(null)
                    })
                    .addOnFailureListener({ e: Exception ->
                        completeProcess(getString(R.string.error_load_failed) + " " + e)
                    })
        } else {
            message(this@ProfileEditActivity, getString(R.string.error_load_failed))
            showProgress(false)
        }
    }

    private fun updateProfileView( user: User ) {
        user_name_text.text = user.name
        user_name.setText(user.name)
        user_phone.setText(user.phone)
        user_email.setText(user.email)
        profile_photo.setImageURI(userPhotoUrl)
    }

    private fun makePhoneMask(phoneEditText: EditText) {
        val formatWatcher = MaskFormatWatcher(
                MaskImpl.createTerminated(PredefinedSlots.RUS_PHONE_NUMBER)
        )
        formatWatcher.installOn(phoneEditText)
    }

    private fun saveChanges() {
        if (validateAll()) {
            showProgress(true)

            val user = User(user_name.text.toString(), user_phone.text.toString(), user_email.text.toString())

            if (userId != "") {
                db.collection(dbUsers).document(userId)
                        .set(user, SetOptions.merge())
                        .addOnSuccessListener({
                            completeProcess(getString(R.string.note_save_done))
                        })
                        .addOnFailureListener({ e: Exception ->
                            completeProcess(getString(R.string.error_save_failed) + " " + e)
                        })
            } else {
                db.collection(dbUsers)
                        .add(user)
                        .addOnSuccessListener({ documentReference: DocumentReference ->
                            userId = documentReference.id
                            completeProcess(getString(R.string.note_save_done))
                        })
                        .addOnFailureListener({ e: Exception ->
                            completeProcess(getString(R.string.error_save_failed) + " " + e)
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

    private fun completeProcess( msgString: String? ) {
        if( msgString != null ) {
            message(this@ProfileEditActivity, msgString)
        }
        showProgress(false)
    }

    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        profile_edit_form.visibility = if (show) View.GONE else View.VISIBLE
        profile_edit_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        profile_edit_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        load_user_data_progress.visibility = if (show) View.VISIBLE else View.GONE
        load_user_data_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        load_user_data_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    private fun showPopup(v:View) {
        val popup = PopupMenu( this, v )
        val inflater : MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.user_photo_popupmenu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when( item.itemId ) {
                R.id.from_gallery -> {
                    val photoPickerIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    photoPickerIntent.type = "image/*"
                    startActivityForResult(photoPickerIntent, RC_LOAD_FROM_GALLERY)
                    true
                }
                R.id.from_camera -> {
                    runCamera()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun runCamera() {
        if( !mayUseCamera() ) return
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, RC_LOAD_FROM_CAMERA)
    }

    private fun mayUseCamera(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(CAMERA)) {
            Snackbar.make(profile_photo, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok,
                            { requestPermissions(arrayOf(CAMERA), RC_LOAD_FROM_CAMERA) })
        } else {
            requestPermissions(arrayOf(CAMERA), RC_LOAD_FROM_CAMERA)
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == RC_LOAD_FROM_CAMERA) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runCamera()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_LOAD_FROM_GALLERY) {
            if (resultCode == RESULT_OK) {
                try {
                    val imageUri: Uri = data.data
                    message(applicationContext, imageUri.toString())
                    val imageStream: InputStream = contentResolver.openInputStream(imageUri)
                    val imageBitmap: Bitmap = BitmapFactory.decodeStream(imageStream)
                    profile_photo.setImageBitmap(imageBitmap)
                } catch ( e: FileNotFoundException ) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == RC_LOAD_FROM_CAMERA) {
            if (resultCode == RESULT_OK) {
                try {
                    val imageBitmap: Bitmap = data.extras.get("data") as Bitmap
                    profile_photo.setImageBitmap(imageBitmap)
                } catch ( e: FileNotFoundException ) {
                    e.printStackTrace()
                }
            }
        }
    }
}
