package yuyu.itplacenet.helpers

import android.content.Context

import yuyu.itplacenet.R
import yuyu.itplacenet.managers.AuthManager
import yuyu.itplacenet.managers.DBManager
import yuyu.itplacenet.models.User
import yuyu.itplacenet.utils.*


class UserHelper(
        private val context: Context
) {

    private val auth = AuthManager()
    private val db = DBManager()

    private var userId: String? = auth.userId
        get() = field ?: auth.userId
        set(value) {
            if( value != null ) {
                field = value
            }
        }

    private fun msgLoadError( e: Exception ) {
        context.toast(context.getString(R.string.error_load_failed) + ": " + e)
    }
    private fun msgSaveDone() {
        context.toast(context.getString(R.string.note_save_done))
    }
    private fun msgSaveError( e: Exception ) {
        context.toast(context.getString(R.string.error_save_failed) + ": " + e)
    }
    private fun msgUserNotFound() {
        context.toast(context.getString(R.string.error_user_not_found))
    }

    private fun checkUserExist( existCallback: ((User) -> Unit)? = null,
                                notExistCallback: (() -> Unit)? = null,
                                failureCallback: (() -> Unit)? = null
    ) {
        val id = userId
        var user: User

        if( id != null ) {
            db.getUserData(id)
                    .addOnSuccessListener({
                        if( it.exists() ) {
                            user = db.parseUserData(it)
                            existCallback?.invoke(user)
                        } else {
                            notExistCallback?.invoke()
                        }
                    })
                    .addOnFailureListener({ e: Exception ->
                        this.msgLoadError(e)
                        failureCallback?.invoke()
                    })
        } else {
            msgUserNotFound()
        }
    }

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

}