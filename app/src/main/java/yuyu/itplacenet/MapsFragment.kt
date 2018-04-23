package yuyu.itplacenet

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import yuyu.itplacenet.helpers.LocationHelper
import yuyu.itplacenet.helpers.MapHelper
import yuyu.itplacenet.helpers.PermissionHelper
import yuyu.itplacenet.helpers.UserHelper
import yuyu.itplacenet.utils.*


class MapsFragment : Fragment(),
        OnMapReadyCallback {

    private lateinit var mapHelper: MapHelper
    private lateinit var locationHelper: LocationHelper
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var userHelper: UserHelper

    private lateinit var mapFragment: SupportMapFragment


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity = activity
        if (isAdded && activity != null) {
            val rootView = inflater.inflate(R.layout.fragment_maps, container, false)

            mapHelper = MapHelper(this.requireActivity())
            locationHelper = LocationHelper(this.requireActivity())
            permissionHelper = PermissionHelper(this.requireActivity())
            userHelper = UserHelper(this.requireActivity(), true)

            mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

            mapHelper.init()
            locationHelper.init({ location: Location -> updateMyLocation(location) })

            rootView.findViewById<ImageButton>(R.id.plus).setOnClickListener {
                mapHelper.zoomIn()
            }
            rootView.findViewById<ImageButton>(R.id.minus).setOnClickListener {
                mapHelper.zoomOut()
            }
            rootView.findViewById<ImageButton>(R.id.location).setOnClickListener {
                startLocationUpdates()
            }
            return rootView
        }
        return null
    }

    override fun onStart() {
        super.onStart()
        locationHelper.connect()
    }

    override fun onDestroy() {
        locationHelper.disconnect()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        startDisplayFriendsPosition()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        stopDisplayFriendsPosition()
    }

    // Карта

    override fun onMapReady(googleMap: GoogleMap) {
        mapHelper.setMap(googleMap)
    }

    // Мое местоположение

    private fun startLocationUpdates() {
        if( permissionHelper.mayGetDeviceLocation() ) {
            locationHelper.startLocationUpdates()
        }
    }

    private fun stopLocationUpdates() {
        locationHelper.stopLocationUpdates()
    }

    private fun updateMyLocation(location: Location) {
        val position = LatLng(location.latitude, location.longitude)
        userHelper.updateCoordinates(location.latitude, location.longitude)
        mapHelper.setMyMarker(position)
    }

    // Разрешения

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == RC_CHECK_PERMISSION_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationHelper.startLocationUpdates()
            } else {
                toast(getString(R.string.error_my_location))
            }
        }
    }

    // Друзья

    private fun startDisplayFriendsPosition() {
        val changedCallback = { id: String,
                                name: String,
                                photo: String,
                                lat: Double?,
                                lng: Double?,
                                lastUpdate: String ->
            if( lat != null && lng != null ) {
                val position = LatLng(lat, lng)
                mapHelper.setFriendMarker(id, name, position, lastUpdate, photo)
            }
        }
        val removedCallback = { id: String ->
            mapHelper.removeFriendMarker(id)
        }
        userHelper.startFriendsPositionListener(changedCallback, removedCallback)
    }

    private fun stopDisplayFriendsPosition() {
        userHelper.stopFriendsPositionListener()
    }
}