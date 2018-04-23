package yuyu.itplacenet.helpers

import android.content.Context
import android.graphics.Bitmap
import com.google.firebase.firestore.ListenerRegistration

import yuyu.itplacenet.R
import yuyu.itplacenet.managers.AuthManager
import yuyu.itplacenet.managers.DBManager
import yuyu.itplacenet.models.Coordinates
import yuyu.itplacenet.models.User
import yuyu.itplacenet.utils.*


class UserHelper(
        private val context: Context,
        private val silentMode : Boolean = false
) {

    private val auth = AuthManager()
    private val db = DBManager()
    private val dateHelper = DateHelper(context)
    private val imageHelper = ImageHelper(context)

    private var lastUpdate: Long? = null
    private var lastSelect: Long? = null
    private val updatePeriod = 5 //In Minutes

    private lateinit var friendsListener: ListenerRegistration

    private var userId: String? = auth.userId
        get() = field ?: auth.userId
        set(value) {
            if( value != null ) {
                field = value
            }
        }

    val isLogin: Boolean
        get() = auth.isLogin

    /* Вход / выход */

    fun logOut() {
        auth.logOut()
    }

    /* Сообщения */

    private fun msg( text: String ) {
        if( !silentMode ) context.toast(text)
    }
    private fun msgLoadError( e: Exception ) {
        this.msg(context.getString(R.string.error_load_failed) + ": " + e)
    }
    private fun msgSaveDone() {
        this.msg(context.getString(R.string.note_save_done))
    }
    private fun msgSaveError( e: Exception ) {
        this.msg(context.getString(R.string.error_save_failed) + ": " + e)
    }
    private fun msgUserNotFound() {
        this.msg(context.getString(R.string.error_user_not_found))
    }

    // Проверяем, существует ли пользователь
    private fun checkUserExist( existCallback: ((User) -> Unit)? = null,
                                notExistCallback: (() -> Unit)? = null,
                                failureCallback: (() -> Unit)? = null
    ) {
        val id = userId

        if( id != null ) {
            db.getUserData(id)
                    .addOnSuccessListener({
                        if( it.exists() ) {
                            val user = db.parseUserData(it)
                            if (user != null) {
                                existCallback?.invoke(user)
                                return@addOnSuccessListener
                            }
                        }
                        notExistCallback?.invoke()
                    })
                    .addOnFailureListener({ e: Exception ->
                        this.msgLoadError(e)
                        failureCallback?.invoke()
                    })
        } else {
            msgUserNotFound()
        }
    }

    // Добавление пользователя
    private fun addUser( user: User,
                         successCallback: ((User) -> Unit)? = null,
                         failureCallback: (() -> Unit)? = null
    ) {
        val id = userId

        if( id != null ) {
            db.setUserData(id,user)
                    .addOnSuccessListener({
                        successCallback?.invoke(user)
                        this.msgSaveDone()
                    })
                    .addOnFailureListener({ e: Exception ->
                        this.msgSaveError(e)
                        failureCallback?.invoke()
                    })
        } else {
            db.addUser(user)
                    .addOnSuccessListener({
                        val resId = db.getResultId(it)
                        this.userId = resId
                        successCallback?.invoke(user)
                        this.msgSaveDone()
                    })
                    .addOnFailureListener({ e: Exception ->
                        this.msgSaveError(e)
                        failureCallback?.invoke()
                    })
        }
    }

    // Обновление полей текущего пользователя
    private fun updateFields( updates: Map<String,Any>,
                              successCallback: (() -> Unit)? = null,
                              failureCallback: (() -> Unit)? = null
    ) {
        val id = userId
        if (id != null) {
            db.updateUserData(id, updates)
                    .addOnSuccessListener({
                        successCallback?.invoke()
                        this.msgSaveDone()
                    })
                    .addOnFailureListener({ e: Exception ->
                        this.msgSaveError(e)
                        failureCallback?.invoke()
                    })
        } else {
            msgUserNotFound()
        }
    }

    // Загрузка данных

    fun loadUserData( successCallback: ((User) -> Unit)? = null,
                      failureCallback: (() -> Unit)? = null
    ) {
        val notExistCallback = {
            val user = auth.user
            if (successCallback != null) successCallback(user)
        }
        this.checkUserExist(successCallback, notExistCallback, failureCallback)
    }

    fun addUserIfNotExist( user: User ) {
        val notExistCallback = {
            this.addUser(user)
        }
        this.checkUserExist(null, notExistCallback)
    }

    // Фотография

    fun loadPhotoFromBase64( photoStr: String?, loadDefault: Boolean = false ) : Bitmap? {
        return when {
            photoStr != null && photoStr != "" -> imageHelper.base64ToBitmap(photoStr)
            loadDefault -> this.loadDefaultPhoto()
            else -> null
        }
    }

    private fun loadDefaultPhoto() : Bitmap {
        return imageHelper.loadFromRes(R.drawable.no_photo)
    }

    // Сохранение данных

    fun saveUserData( user: User,
                      successCallback: ((User) -> Unit)? = null,
                      failureCallback: (() -> Unit)? = null
    ) {
        val updates = HashMap<String,Any>()
        updates["name"]  = user.name  ?: ""
        updates["email"] = user.email ?: ""
        updates["phone"] = user.phone ?: ""

        val sc = {
            if (successCallback != null)  successCallback(user)
        }

        this.updateFields(updates, sc, failureCallback)
    }

    fun savePhoto( photoString: String ) {
        val updates = HashMap<String,Any>()
        updates["photo"] = photoString

        this.updateFields(updates)
    }

    /* Местоположение */

    // Обновление координат пользователя
    fun updateCoordinates(
            latitude: Double,
            longitude: Double
    ) {

        fun checkAndUpdate( latitude: Double,
                            longitude: Double
        ) {
            if( lastUpdate ?: 0 < dateHelper.beforeOnMinutes(updatePeriod) ) {
                // Обновляем базу пользователей
                val moment = dateHelper.getTimeInMillis()
                lastUpdate = moment
                lastSelect = moment

                val updates = HashMap<String,Any>()
                updates["latitude"]   = latitude
                updates["longitude"]  = longitude
                updates["lastUpdate"] = moment

                this.updateFields(updates)

                // Сохраняем историю перемещений
                val id = userId
                if( id != null ) {
                    val coordinates = Coordinates(id, latitude, longitude, moment)
                    this.addCoordinates(coordinates)
                }
            }
        }

        if( lastUpdate == null &&
            lastSelect ?: 0 < dateHelper.beforeOnMinutes(updatePeriod)
        ) {
            val sc = { user: User ->
                lastUpdate = user.lastUpdate
                checkAndUpdate(latitude, longitude)
            }
            this.loadUserData(sc)
            lastSelect = dateHelper.getTimeInMillis()

        } else {
            checkAndUpdate(latitude, longitude)
        }
    }

    // Добавление координат к истории
    private fun addCoordinates(coordinates: Coordinates,
                               successCallback: ((Coordinates) -> Unit)? = null,
                               failureCallback: (() -> Unit)? = null
    ) {
        db.addCoordinates(coordinates)
                .addOnSuccessListener({
                    successCallback?.invoke(coordinates)
                    this.msgSaveDone()
                })
                .addOnFailureListener({ e: Exception ->
                    this.msgSaveError(e)
                    failureCallback?.invoke()
                })
    }

    // Загрузка истории перемещений пользователя
    fun loadLocationHistory( userId: String? = null,
                             successCallback: ((List<Coordinates>) -> Unit)? = null,
                             failureCallback: (() -> Unit)? = null
    ) {
        val id = if( userId == null ) {
            val curUserId = this.userId
            (curUserId ?: return)
        } else {
            userId
        }
        val lastMoment = dateHelper.beforeOnHours(24)

        val sc = { history: List<Coordinates> ->
            if (successCallback != null) successCallback(history)
        }
        val fc = { e: Exception ->
            this.msgLoadError(e)
            if (failureCallback != null) failureCallback()
        }

        db.getUserCoordinatesHistory(id, lastMoment, sc, fc)
    }

    // Друзья

    fun startFriendsPositionListener(
            changedCallback: ((String,String,String,Double?,Double?,String) -> Unit)? = null,
            removedCallback: ((String) -> Unit)? = null
    ) {
        val curUserId = userId
        if (curUserId != null) {

            val cc = { id: String, user: User ->
                if( changedCallback != null && id != curUserId ) {
                    val name = user.name ?: ""
                    val latitude  = user.latitude
                    val longitude = user.longitude
                    val lastUpdate = context.getString(R.string.was_here, dateHelper.diffString(user.lastUpdate))

                    val photoHash = md5(user.photo ?: "")

                    if( ! imageHelper.isInternalImageExist(photoHash) ) {
                        val photoBitmap = imageHelper.scaleBitmap(
                                this.loadPhotoFromBase64(user.photo,true)!!,
                                context.getSize(R.dimen.map_photo_size),
                                context.getSize(R.dimen.map_photo_size)
                        )
                        imageHelper.saveInternalImage(photoBitmap, photoHash)
                    }

                    changedCallback(id, name, photoHash, latitude, longitude, lastUpdate)
                }
            }

            friendsListener = db.allUsersListener( cc, cc, removedCallback, {e: String -> msg(e)} )
        }

    }

    fun stopFriendsPositionListener() {
        if( ::friendsListener.isInitialized ) {
            friendsListener.remove()
        }
    }

}