package yuyu.itplacenet

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.Fragment
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

import yuyu.itplacenet.helpers.ImageHelper
import yuyu.itplacenet.helpers.UserHelper
import yuyu.itplacenet.models.User
import yuyu.itplacenet.utils.*


class MainActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener {

    private val userHelper = UserHelper(this)
    private val imageHelper = ImageHelper(this)

    private lateinit var navigationView: NavigationView
    private lateinit var headerLayout: View
    private lateinit var headerImage: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                        this,
                        drawer_layout,
                        toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView = findViewById<NavigationView>(R.id.nav_view)
        headerLayout = navigationView.getHeaderView(0)
        headerImage = headerLayout.findViewById<ImageButton>(R.id.nav_header_bg)

        setDefaultFragment()

        headerImage.setOnClickListener {
            startActivity( Intent(this, ProfileEditActivity::class.java) )
        }
        login.setOnClickListener {
            startActivityForResult(Intent(this, LoginActivity::class.java), RC_LOG_IN)
        }
        logout.setOnClickListener {
            userHelper.logOut()
            setDefaultFragment()
            prepareNavigationDrawer()
        }

        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onStart() {
        super.onStart()
        prepareNavigationDrawer()
    }

    /* Вход / выход */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_LOG_IN) {
            if (resultCode == RESULT_OK) {
                toast(getString(R.string.success_sign_in))
            }
        }
    }

    /* Действия с Navigation Drawer */

    private fun setDefaultFragment() {
        val navMenu = navigationView.menu
        val defaultMenuItem = navMenu.findItem(R.id.nav_map)
        drawFragment(defaultMenuItem)
    }

    private fun prepareNavigationDrawer() {
        val navMenu = navigationView.menu
        val logoffHeaderImage = headerLayout.findViewById<ImageView>(R.id.nav_header_bg_logoff)

        if( userHelper.isLogin ) {
            logoffHeaderImage.visibility = View.GONE
            headerImage.isClickable = true
            login.visibility = View.GONE
            logout.visibility = View.VISIBLE
            navMenu.findItem(R.id.nav_dialog).isVisible = true
            loadUserData()
        } else {
            logoffHeaderImage.visibility = View.VISIBLE
            headerImage.isClickable = false
            login.visibility = View.VISIBLE
            logout.visibility = View.GONE
            navMenu.findItem(R.id.nav_dialog).isVisible = false
        }

        closeDrawer()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem) : Boolean {
        drawFragment(item)
        closeDrawer()
        return true
    }

    private fun closeDrawer() {
        drawer_layout.closeDrawer(GravityCompat.START)
    }

    private fun drawFragment(item: MenuItem) {
        if( item.isChecked )
            return

        var fragment: Fragment? = null

        val fragmentClass = when( item.itemId ) {
            R.id.nav_map -> {
                MapsFragment::class.java
            }
            R.id.nav_dialog -> {
                MapsFragment::class.java
            }
            else -> {
                MapsFragment::class.java
            }
        }

        try {
            fragment = fragmentClass.newInstance() as Fragment
        } catch (e: Exception) {
            e.printStackTrace()
        }

        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
        item.isChecked = true
        title = item.title
    }

    /* Данные пользователя */

    private fun loadUserData() {
        val successCallback = { u: User ->
            updateProfileView(u)
            updateUserPhotoView(u)
        }
        userHelper.loadUserData(successCallback)
    }

    private fun updateProfileView( user: User ) {
        nav_header_user_name.text = user.name
        nav_header_user_email.text = user.email
    }

    private fun updateUserPhotoView( user: User ) {
        val photoBitmap = userHelper.loadPhotoFromBase64(user.photo)
        if( photoBitmap != null ) {
            setUserPhotoToView(photoBitmap)
        }
    }

    private fun setUserPhotoToView(photoBitmap: Bitmap, showBlurBg: Boolean = true) {
        nav_header_photo.setImageBitmap(photoBitmap)
        if( showBlurBg ) nav_header_bg.setImageBitmap(imageHelper.blurImage(photoBitmap))
    }
}
