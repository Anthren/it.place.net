package yuyu.itplacenet.helpers

import android.app.Activity
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import yuyu.itplacenet.R
import yuyu.itplacenet.utils.getSize

class MapHelper(private val activity: Activity) {

    private lateinit var googleMap: GoogleMap
    private lateinit var myMarker: Marker
    private var friendsMarkers = HashMap<String,Marker>()

    private var zoomLevel = 10f
    private val locationUpdateInterval: Long = 10000
    private val fastestLocationUpdateInterval: Long = locationUpdateInterval / 2

    // Карта

    fun setGoogleMap( gMap: GoogleMap ) {
        googleMap = gMap
        //googleMap.isMyLocationEnabled = true
        //googleMap.uiSettings.isMyLocationButtonEnabled = false
    }

    // Маркеры

    private fun addMyMarker( position: LatLng ) {
        myMarker = googleMap.addMarker(MarkerOptions()
                .position(position)
                .title(activity.getString(R.string.my_marker))
                .snippet(position.toString())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.myself)))
    }

    fun setMyMarker(position: LatLng ) {
        if( ::googleMap.isInitialized ) {
            if (!::myMarker.isInitialized) {
                this.addMyMarker(position)
            } else {
                myMarker.position = position
                myMarker.snippet = "${position.latitude} ${position.longitude}"
            }
            this.moveCamera(position)
        }
    }

    private fun getFriendSnippet( lastUpdate: String ) : String {
        return activity.getString(R.string.was_here, lastUpdate)
    }

    private fun addFriendMarker( id: String, name: String, position: LatLng, lastUpdate: String ) {
        friendsMarkers[id] = googleMap.addMarker(MarkerOptions()
                .position(position)
                .title(name)
                .snippet(this.getFriendSnippet(lastUpdate)))
    }

    fun setFriendMarker( id: String, name: String, position: LatLng, lastUpdate: String ) {
        if( ::googleMap.isInitialized ) {
            if (friendsMarkers.containsKey(id)) {
                friendsMarkers.getValue(id).position = position
                friendsMarkers.getValue(id).snippet = this.getFriendSnippet(lastUpdate)
            } else {
                this.addFriendMarker(id, name, position, lastUpdate)
            }
        }
    }

    fun changeFriendMarkerIcon(id: String, userPhoto: String?) {
        if( ::googleMap.isInitialized ) {
            if (friendsMarkers.containsKey(id) && userPhoto != null) {
                friendsMarkers.getValue(id).setIcon(this.createFriendIcon(userPhoto))
                friendsMarkers.getValue(id).setAnchor(this.getFriendAnchorCenter(), 1f)
            }
        }
    }

    fun removeFriendMarker(id: String) {
        if( ::googleMap.isInitialized ) {
            friendsMarkers.getValue(id).remove()
            friendsMarkers.remove(id)
        }
    }

    private fun createFriendIcon(userPhotoStr: String) : BitmapDescriptor {
        val imageHelper = ImageHelper(activity)
        val userHelper = UserHelper(activity)
        val photoSize = activity.getSize(R.dimen.map_photo_size)
        val borderWidth = activity.getSize(R.dimen.map_photo_border_size)

        val pinImage = imageHelper.loadFromRes(R.drawable.pin)

        var userPhoto = userHelper.loadPhotoFromBase64(userPhotoStr,true)
            userPhoto = imageHelper.roundWithWhiteBorder(userPhoto!!, photoSize, borderWidth)

        val pinOptions = mapOf(
                "margin_top"    to activity.getSize(R.dimen.map_marker_margin_top),
                "margin_bottom" to activity.getSize(R.dimen.map_marker_margin_bottom),
                "margin_right"  to activity.getSize(R.dimen.map_marker_margin_right)
        )
        val markerIcon = imageHelper.glue(
                bmp1 = pinImage,  bmp1Options = pinOptions,
                bmp2 = userPhoto, bmp2Options = null
        )
        return BitmapDescriptorFactory.fromBitmap(markerIcon)
    }

    private fun getFriendAnchorCenter() : Float {
        val anchorCenter =  activity.getSize(R.dimen.map_marker_width).div(2) /
                            (
                                activity.getSize(R.dimen.map_marker_width) +
                                activity.getSize(R.dimen.map_marker_margin_right) +
                                activity.getSize(R.dimen.map_photo_size) +
                                activity.getSize(R.dimen.map_photo_border_size).times(2)
                            )
        return anchorCenter.toFloat()
    }

    // Камера

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