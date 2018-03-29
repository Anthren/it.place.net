package yuyu.itplacenet.helpers

import android.app.Activity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import yuyu.itplacenet.R

class MapHelper(private val activity: Activity) {

    private lateinit var googleMap: GoogleMap
    private lateinit var myMarker: Marker
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var zoomLevel = 10f
    private val locationUpdateInterval: Long = 10000
    private val fastestLocationUpdateInterval: Long = locationUpdateInterval / 2

    // Карта

    fun setGoogleMap( gMap: GoogleMap ) {
        googleMap = gMap
        //googleMap.isMyLocationEnabled = true
        //googleMap.uiSettings.isMyLocationButtonEnabled = false
    }

    fun moveMyMarker( position: LatLng ) {
        if( ! ::myMarker.isInitialized ) {
            myMarker = this.addMyMarker(position)
        } else {
            myMarker.position = position
        }
        this.moveCamera(position)
    }

    private fun addMyMarker( position: LatLng ) : Marker {
        return googleMap.addMarker(MarkerOptions()
                        .position(position)
                        .title(activity.getString(R.string.my_marker))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.myself)))
    }

    private fun moveCamera( position: LatLng ) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, zoomLevel)
        //googleMap.moveCamera(cameraUpdate)
        googleMap.animateCamera(cameraUpdate)
    }

    fun zoomIn() {
        zoomLevel++
        googleMap.animateCamera(CameraUpdateFactory.zoomIn())
    }

    fun zoomOut() {
        zoomLevel--
        googleMap.animateCamera(CameraUpdateFactory.zoomOut())
    }

    // Мое местоположение

    fun createLocationRequest() : LocationRequest {
        return LocationRequest().apply {
            interval = locationUpdateInterval
            fastestInterval = fastestLocationUpdateInterval
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

}