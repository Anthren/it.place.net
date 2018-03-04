package yuyu.itplacenet.managers

import android.content.Intent
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import yuyu.itplacenet.models.User

@Suppress("DEPRECATION")
class AuthManager {

    private val auth = FirebaseAuth.getInstance()
    private var user = User()

    private fun refresh() {
        this.user = this.getCurrentUser()
    }

    fun isLogin() : Boolean {
        return this.auth.uid != null
    }

    fun getCurrentUser() : User {
        val curUser = this.auth.currentUser
        return if( curUser != null )
            User(curUser.displayName, curUser.phoneNumber, curUser.email)
        else
            User()
    }

    fun signInWithEmailAndPassword( email: String, password: String ) : Task<AuthResult> {
        val sign = this.auth.signInWithEmailAndPassword(email, password)
        refresh()
        return sign
    }

    // в классах такого типа лучше не использовать android-зависимости
    fun getRegistrationIntent() : Intent {
        val providers = Arrays.asList(
                AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build())

        return AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build()
    }

    fun logOut() {
        this.auth.signOut()
        refresh()
    }
}