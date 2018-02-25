package yuyu.itplacenet

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import yuyu.itplacenet.utils.*
import java.util.*

@Suppress("DEPRECATION")
class RegistrationActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val providers = Arrays.asList(
                AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build())

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser

                if( user != null ) {
                    message(this, user.toString() )
                    gotoMain()
                }
            } else {
                // Sign in failed, check response for error code
                message(this, getString(R.string.error_sign_in_failed))
                gotoLogin()
            }
        }
    }

    private fun gotoMain() {
        startActivity( Intent(this@RegistrationActivity, MainActivity::class.java) )
    }

    private fun gotoLogin() {
        startActivity( Intent(this@RegistrationActivity, LoginActivity::class.java) )
    }
}
