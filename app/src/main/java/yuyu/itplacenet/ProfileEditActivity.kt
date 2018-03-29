package yuyu.itplacenet

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.Manifest.permission.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.support.design.widget.Snackbar
import android.widget.PopupMenu
import com.theartofdev.edmodo.cropper.CropImage
import java.io.FileNotFoundException
import yuyu.itplacenet.helpers.ImageHelper
import yuyu.itplacenet.helpers.PermissionHelper
import yuyu.itplacenet.managers.AuthManager
import yuyu.itplacenet.managers.DBManager
import yuyu.itplacenet.models.User
import yuyu.itplacenet.ui.ProgressBar
import yuyu.itplacenet.ui.Validator
import yuyu.itplacenet.utils.*
import kotlinx.android.synthetic.main.activity_profile_edit.*


class ProfileEditActivity : AppCompatActivity() {

    private val auth = AuthManager()
    private val db = DBManager()
    private val validator = Validator(this)
    private val imageHelper = ImageHelper(this)
    private val permissionHelper = PermissionHelper(this)
    private val progressBar = ProgressBar()

    private var user: User = auth.user
    private var userId: String? = auth.userId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        progressBar.setFields(profile_edit_form, load_user_data_progress)
        loadUserData()
        user_phone.makePhoneMask()
        checkIsSaveAvailable()

        user_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                validator.validate( mapOf("name" to user_name) )
                checkIsSaveAvailable()
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        user_email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                validator.validate( mapOf("email" to user_email) )
                checkIsSaveAvailable()
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        save_button.setOnClickListener {
            saveChanges()
        }

        change_photo_button.setOnClickListener{
            showPopup(it)
        }
    }

    /* Загрузка данных */

    private fun loadUserData() {
        progressBar.show()
        val id = userId
        if( id != null ) {
            db.getUserData(id)
                    .addOnSuccessListener({
                        user = db.parseUserData(it)
                        updateProfileView(user)
                        updateUserPhotoView(user)
                        completeProcess(null)
                    })
                    .addOnFailureListener({ e: Exception ->
                        completeProcess(getString(R.string.error_load_failed) + ": " + e)
                    })
        } else {
            completeProcess(getString(R.string.error_load_failed))
        }
    }

    private fun updateProfileView( user: User ) {
        user_name_text.text = user.name
        user_name.setText(user.name)
        user_phone.setText(user.phone)
        user_email.setText(user.email)
    }

    private fun updateUserPhotoView( user: User ) {
        val photo = user.photo
        if( photo != null && photo != "" ) {
            val photoBitmap = imageHelper.base64ToBitmap(photo)
            setUserPhotoToView(photoBitmap)
        }
    }

    /* Сохранение данных */

    private fun saveChanges() {
        if (validateAll()) {
            progressBar.show()

            val id = userId
            val user = User(name=user_name.str(), email=user_email.str(), phone=user_phone.str())

            if( id != null ) {
                db.setUserData(id,user)
                        .addOnSuccessListener({
                            updateProfileView(user)
                            completeProcess(getString(R.string.note_save_done))
                        })
                        .addOnFailureListener({ e: Exception ->
                            completeProcess(getString(R.string.error_save_failed) + ": " + e)
                        })
            } else {
                db.addUser(user)
                        .addOnSuccessListener({
                            userId = db.getResultId(it)
                            updateProfileView(user)
                            completeProcess(getString(R.string.note_save_done))
                        })
                        .addOnFailureListener({ e: Exception ->
                            completeProcess(getString(R.string.error_save_failed) + ": " + e)
                        })
            }
        }
    }

    private fun validateAll(): Boolean {
        val fields = mapOf(
                "email" to user_email,
                "name" to user_name
        )
        return validator.validate(fields)
    }

    private fun checkIsSaveAvailable() {
        if( !save_button.isEnabled ) {
            if( !user_name.isEmpty() && !user_email.isEmpty() ) {
                save_button.isEnabled = true
            }
        }
    }

    /* Progress Bar */

    private fun completeProcess( msgString: String? ) {
        if( msgString != null ) {
            toast(msgString)
        }
        progressBar.hide() // не работает
        hide() // работает
    }

    private fun toggleProgress(show: Boolean) {
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
    private fun hide() {
        toggleProgress(false)
    }

    /* Фотография */

    // Спрашиваем, что делать
    private fun showPopup(view: View) {
        val popup = PopupMenu( this, view )
        popup.inflate(R.menu.user_photo_popupmenu)

        popup.setOnMenuItemClickListener { item ->
            when( item.itemId ) {
                R.id.from_gallery -> {
                    runGallery()
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

    // Запускаем галерею
    private fun runGallery() {
        val photoPickerIntent = imageHelper.createGalleryIntent()
        startActivityForResult(photoPickerIntent, RC_LOAD_FROM_GALLERY)
    }

    // Запускаем камеру
    private fun runCamera() {
        if( !permissionHelper.mayUseCamera() )
            return
        val cameraIntent = imageHelper.createCameraIntent()
        if( cameraIntent.isIntentAvailable(this) ) {
            try {
                val photoURI = imageHelper.createImageFile()
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                user.photo = photoURI.toString()
            }
            catch( e: Exception ) {
                toast(getString(R.string.error_create_temp_file) + " " + e)
            }
            startActivityForResult(cameraIntent, RC_LOAD_FROM_CAMERA)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == RC_CHECK_PERMISSION_CAMERA) {
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
                        val photoUri = imageHelper.loadPhotoFromGallery(data)
                        performCrop( photoUri )
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                RC_LOAD_FROM_CAMERA ->
                    try {
                        if (data.hasExtra("data")) {
                            val photo = imageHelper.loadSmallCameraPhoto(data)
                            setUserPhoto( photo )
                        } else if( user.photo != null ) {
                            val photoUri = imageHelper.loadPhotoFromCamera(user.photo!!)
                            performCrop( photoUri )
                        }
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                setUserPhoto( result.uri )
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                toast(result.error.toString())
            }
        }
    }

    // Кадрирование
    private fun performCrop(imageUri: Uri) {
        val size = resources.getDimensionPixelSize(R.dimen.profile_photo_size)
        CropImage.activity(imageUri)
                .setAspectRatio(1,1)
                .setFixAspectRatio(true)
                .setMinCropResultSize(size, size)
                .start(this)
    }

    // Устанавливаем сжатую картинку в профиль
    private fun setUserPhoto(imageUri: Uri?, imageBitmap: Bitmap? = null) {
        val photoBitmap = imageHelper.scale(profile_photo, imageUri, imageBitmap)

        if( photoBitmap != null ) {
            setUserPhotoToView(photoBitmap)
            saveUserPhotoToDB(photoBitmap)
        }
    }
    private fun setUserPhoto(imageBitmap: Bitmap) {
        return setUserPhoto(null, imageBitmap)
    }

    private fun setUserPhotoToView( photoBitmap: Bitmap ) {
        profile_photo.setImageBitmap( photoBitmap )
        profile_photo_bg.setImageBitmap( imageHelper.blurImage(photoBitmap) )
    }

    private fun saveUserPhotoToDB( photoBitmap: Bitmap ) {
        val id = userId
        val photoString = imageHelper.bitmapToBase64(photoBitmap)

        if( id != null ) {
            db.updateUserData(id,"photo", photoString)
                    .addOnSuccessListener({
                        completeProcess(getString(R.string.note_save_done))
                    })
                    .addOnFailureListener({ e: Exception ->
                        completeProcess(getString(R.string.error_save_failed) + ": " + e)
                    })
        } else {
            toast(getString(R.string.error_save_failed)+ ": " + getString(R.string.error_user_not_found))
        }
    }
}
