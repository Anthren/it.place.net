package yuyu.itplacenet

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            gotoLogin()
        }

        hello_text.text = getString( R.string.string_hello ).format( user.toString() )

        profile_edit_button.setOnClickListener {
            gotoProfileEdit()
        }

        logoff_button.setOnClickListener {
            auth.signOut()
            gotoLogin()
        }
    }

    private fun gotoLogin() {
        startActivity( Intent(this@MainActivity, LoginActivity::class.java) )
    }

    private fun gotoProfileEdit() {
        startActivity( Intent(this@MainActivity, ProfileEditActivity::class.java) )
    }

}
