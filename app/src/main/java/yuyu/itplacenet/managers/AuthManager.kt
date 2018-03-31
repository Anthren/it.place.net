package yuyu.itplacenet.managers

import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import yuyu.itplacenet.models.User

@Suppress("DEPRECATION")
class AuthManager {

    private val auth = FirebaseAuth.getInstance()

    var user = User()
        get() = getCurrentUser()
        private set

    val userId: String?
        get() = this.auth.uid

    val isLogin: Boolean
        get() = this.auth.uid != null


    fun signInWithEmailAndPassword( email: String, password: String ) : Task<AuthResult> {
        return this.auth.signInWithEmailAndPassword(email, password)
    }

    fun makeRegistrationIntentBuilder() : AuthUI.SignInIntentBuilder {
        val providers = Arrays.asList(
                AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
        )
        return AuthUI.getInstance()
                     .createSignInIntentBuilder()
                     .setAvailableProviders(providers)
    }

    fun logOut() {
        this.auth.signOut()
    }


    private fun getCurrentUser() : User {
        val curUser = this.auth.currentUser
        return when (curUser) {
            null -> User()
            else -> User(name=curUser.displayName, email=curUser.email, phone=curUser.phoneNumber, photo=curUser.photoUrl?.toString())
        }
    }
}