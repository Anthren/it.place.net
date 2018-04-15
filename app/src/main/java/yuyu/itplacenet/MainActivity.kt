package yuyu.itplacenet

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import yuyu.itplacenet.managers.AuthManager
import yuyu.itplacenet.utils.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val auth = AuthManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if( !auth.isLogin ) {
            gotoLogin()
            return
        }
    }

    override fun onStart() {
        super.onStart()

        setHelloStr()

        profile_edit_button.setOnClickListener {
            gotoProfileEdit()
        }

        logoff_button.setOnClickListener {
            auth.logOut()
            gotoLogin()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_LOG_IN) {
            if (resultCode == RESULT_OK) {
                setHelloStr()
            }
        }
    }

    private fun setHelloStr() {
        hello_text.text = getString( R.string.string_hello, auth.user.name )
    }

    private fun gotoLogin() {
        startActivityForResult(Intent(this, LoginActivity::class.java), RC_LOG_IN)
    }

    private fun gotoProfileEdit() {
        startActivity( Intent(this, ProfileEditActivity::class.java) )
    }

    private fun gotoMaps() {
        startActivity( Intent(this, MapsActivity::class.java) )
    }

}
