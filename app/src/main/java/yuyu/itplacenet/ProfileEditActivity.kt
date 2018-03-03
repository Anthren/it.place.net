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
import java.io.FileNotFoundException
import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.widget.ImageView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import yuyu.itplacenet.helpers.UserHelper
import yuyu.itplacenet.models.User
import yuyu.itplacenet.utils.*


class ProfileEditActivity : AppCompatActivity() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private val dbUsers = "users"

    private var userId = ""
    private var userPhotoUri: Uri? = Uri.EMPTY

    private var maySave = false

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

    /* Загрузка данных */

    private fun loadUserData() {
        showProgress(true)
        if (currentUser != null) {
            userId = currentUser.uid
            userPhotoUri = currentUser.photoUrl

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
            toast(getString(R.string.error_load_failed))
            showProgress(false)
        }
    }

    private fun updateProfileView( user: User ) {
        user_name_text.text = user.name
        user_name.setText(user.name)
        user_phone.setText(user.phone)
        user_email.setText(user.email)
        profile_photo.setImageURI(userPhotoUri)
    }

    private fun makePhoneMask(phoneEditText: EditText) {
        val formatWatcher = MaskFormatWatcher(
                MaskImpl.createTerminated(PredefinedSlots.RUS_PHONE_NUMBER)
        )
        formatWatcher.installOn(phoneEditText)
    }

    /* Сохранение данных */

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
        } else if (!UserHelper().isEmailValid(emailStr)) {
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

    /* Progress Bar */

    private fun completeProcess( msgString: String? ) {
        if( msgString != null ) {
            toast(msgString)
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

    /* Фотография */

    // Спрашиваем, что делать
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

    // Запускаем камеру
    private fun runCamera() {
        if( !mayUseCamera() ) return
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        if( isIntentAvailable(this, cameraIntent ) ) {
            try {
                val photoFile = createImageFile()
                val photoURI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".fileprovider", photoFile)
                } else {
                    Uri.fromFile(photoFile)
                }
                addImageToGallery(photoURI)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                userPhotoUri = photoURI
                startActivityForResult(cameraIntent, RC_LOAD_FROM_CAMERA)
            }
            catch( e: Exception ) {
                toast("Не получилось создать временный файл! " + e)
            }
        }
    }

    // Проверяем, можно ли запустить камеру
    private fun isIntentAvailable(context: Context, intent: Intent) : Boolean {
        val packageManager = context.packageManager
        val list: List<ResolveInfo> = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return list.isNotEmpty()
    }


    // Создаем временный файл
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile( imageFileName,".jpg", storageDir )
        val currentPhotoPath = image.absolutePath

        val values = ContentValues()
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.MediaColumns.DATA, currentPhotoPath)
        this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        return image
    }

    // Добавляем файл в галерею
    private fun addImageToGallery(photoURI: Uri) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = photoURI
        this.sendBroadcast(mediaScanIntent)
    }

    // Запрашиваем разрешение
    private fun mayUseCamera(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {
            return true
        }
        if (shouldShowRequestPermissionRationale(CAMERA) ||
            shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(profile_photo, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok,
                            { requestPermissions(arrayOf(CAMERA,WRITE_EXTERNAL_STORAGE), RC_LOAD_FROM_CAMERA) })
        } else {
            requestPermissions(arrayOf(CAMERA,WRITE_EXTERNAL_STORAGE), RC_LOAD_FROM_CAMERA)
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

    // Обрабатываем результат
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when( requestCode ) {
                RC_LOAD_FROM_GALLERY ->
                    try {
                        loadPhotoFromGallery(data)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                RC_LOAD_FROM_CAMERA ->
                    try {
                        if (data.hasExtra("data")) {
                            handleSmallCameraPhoto(data)
                        } else {
                            loadPhotoFromCamera()
                        }
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
            }
        }
    }

    // Загружаем картинку из галереи
    private fun loadPhotoFromGallery(intent: Intent) {
        val imageUri: Uri = intent.data
        setUserPhoto( profile_photo, imageUri )
        setUserPhoto( profile_photo_bg, imageUri )
    }

    // Загружаем миниатюру с камеры
    private fun handleSmallCameraPhoto(intent: Intent) {
        val imageBitmap = intent.getParcelableExtra<Bitmap>("data")
        profile_photo.setImageBitmap(imageBitmap)
        profile_photo_bg.setImageBitmap(imageBitmap)
    }

    // Загружаем фото с камеры
    private fun loadPhotoFromCamera() {
        setUserPhoto( profile_photo, userPhotoUri )
        setUserPhoto( profile_photo_bg, userPhotoUri )
    }

    // Устанавливаем сжатую картинку в профиль
    private fun setUserPhoto(imageView: ImageView, imageUri: Uri? ) {
        if( imageUri != null ) {
            val imageStream = contentResolver.openInputStream(imageUri)

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeStream(imageStream)
            val photoW = bmOptions.outWidth
            val photoH = bmOptions.outHeight

            val targetW: Int = imageView.width
            val targetH = imageView.height
            val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor
            val imageBitmap = BitmapFactory.decodeStream(imageStream, null, bmOptions)
            imageView.setImageBitmap(imageBitmap)
        }
    }
}
