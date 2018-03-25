package yuyu.itplacenet

import android.content.pm.PackageManager
import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import kotlinx.android.synthetic.main.activity_maps.*

import yuyu.itplacenet.helpers.PermissionHelper
import yuyu.itplacenet.utils.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private val zoomLevel = 10f

    private val permissionHelper = PermissionHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        val uln = LatLng(54.19, 48.23)

        val myMarker = gMap.addMarker(MarkerOptions()
                .position(uln)
                .title(getString(R.string.my_marker))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.myself)))

        //gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uln,zoomLevel))

        val cameraPosition = CameraPosition.Builder()
                .target(uln)
                .zoom(zoomLevel)
                .build()
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        gMap.animateCamera(cameraUpdate)

        if( permissionHelper.mayGetDeviceLocation() ) {
            myLocation()
        }

        plus.setOnClickListener {
            mapZoomIn()
        }
        minus.setOnClickListener {
            mapZoomOut()
        }
    }

    @SuppressLint("MissingPermission")
    private fun myLocation() {
        gMap.isMyLocationEnabled = true
    }

    private fun mapZoomIn() {
        val cameraUpdate = CameraUpdateFactory.zoomIn()
        gMap.animateCamera(cameraUpdate)
    }

    private fun mapZoomOut() {
        val cameraUpdate = CameraUpdateFactory.zoomOut()
        gMap.animateCamera(cameraUpdate)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == RC_CHECK_PERMISSION_LOCATION) {
            if (grantResults.size == 1 &&
                permissions[0] == ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                myLocation()
            } else {
                toast(getString(R.string.error_my_location))
            }
        }
    }
}
