package yuyu.itplacenet.helpers

import android.app.Activity
import android.graphics.Bitmap
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import java.util.ArrayList
import yuyu.itplacenet.R
import yuyu.itplacenet.models.FriendItem
import yuyu.itplacenet.ui.FriendClusterIconBuilder
import yuyu.itplacenet.ui.FriendMarkerIconBuilder
import yuyu.itplacenet.utils.getSize


class MapHelper(private val activity: Activity) :
            ClusterManager.OnClusterItemClickListener<FriendItem>,
            ClusterManager.OnClusterItemInfoWindowClickListener<FriendItem> {

    private lateinit var googleMap: GoogleMap
    private lateinit var myMarker: Marker
    private lateinit var clusterManager: ClusterManager<FriendItem>

    private var friendsItems = HashMap<String,FriendItem>()
    private var friendsItemsIds = HashMap<String,String>()

    private var zoomLevel = 11f
    private val locationUpdateInterval: Long = 10000
    private val fastestLocationUpdateInterval: Long = locationUpdateInterval / 2

    // Карта

    fun setMap( gMap: GoogleMap ) {
        googleMap = gMap
        googleMap.uiSettings.isMapToolbarEnabled = false

        this.setClusterManager()
    }

    // Маркеры

    private fun addMyMarker( position: LatLng ) {
        myMarker = googleMap.addMarker(MarkerOptions()
                                .position(position)
                                .title(activity.getString(R.string.my_marker))
                                .snippet("${position.latitude} ${position.longitude}")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.myself)))
    }

    fun setMyMarker( position: LatLng ) {
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

    private fun addFriendMarker( id: String, name: String, position: LatLng, snippet: String, photo: Bitmap ) {
        friendsItems[id] = FriendItem(id, position, name, snippet, photo)
        clusterManager.addItem(friendsItems[id])
        clusterManager.cluster()
    }

    fun setFriendMarker( id: String, name: String, position: LatLng, lastUpdate: String, photo: Bitmap ) {
        if( ::googleMap.isInitialized ) {
            if( friendsItems.containsKey(id) ) {
                /*clusterManager.markerCollection.markers.forEach{ marker ->
                    if( marker.id == friendsItemsIds[id] ) {
                        if (marker.position != position)   marker.position = position
                        if (marker.title    != name)       marker.title    = name
                        if (marker.snippet  != lastUpdate) marker.snippet  = lastUpdate

                        val icon = this.createFriendMarkerIcon(photo)
                        marker.setIcon(icon)

                        friendsItems[id] = FriendItem(id, position, name, lastUpdate, photo)
                    }
                }
                clusterManager.cluster()
                */
                this.removeFriendMarker(id)
                this.addFriendMarker(id, name, position, lastUpdate, photo)
            } else {
                this.addFriendMarker(id, name, position, lastUpdate, photo)
            }
        }
    }

    fun removeFriendMarker( id: String ) {
        if( ::googleMap.isInitialized && friendsItems.containsKey(id) ) {
            clusterManager.removeItem(friendsItems[id])
            clusterManager.cluster()
            friendsItems.remove(id)
            friendsItemsIds.remove(id)
        }
    }

    // Marker Options

    private fun createFriendMarkerIcon( userPhoto: Bitmap ) : BitmapDescriptor {
        val icon = FriendMarkerIconBuilder(activity, userPhoto).build()
        return BitmapDescriptorFactory.fromBitmap(icon)
    }

    private fun createFriendClusterIcon( userPhotos: ArrayList<Bitmap> ) : BitmapDescriptor {
        val icon = FriendClusterIconBuilder(activity, userPhotos).build()
        return BitmapDescriptorFactory.fromBitmap(icon)
    }

    private fun getFriendAnchorU() : Float {
        val markerWidth = activity.getSize(R.dimen.map_marker_width).toFloat()
        val commonWidth = markerWidth +
                          activity.getSize(R.dimen.map_marker_margin_right) +
                          activity.getSize(R.dimen.map_photo_size)

        return markerWidth.div(2).div(commonWidth)
    }
    private fun getFriendAnchorV() : Float {
        val markerHeight = activity.getSize(R.dimen.map_marker_margin_top) +
                           activity.getSize(R.dimen.map_marker_anchor_v)
        val commonHeight = activity.getSize(R.dimen.map_photo_size)

        return markerHeight.toFloat() / commonHeight.toFloat()
    }

    // Кластеризация маркеров

    private fun setClusterManager() {
        clusterManager = ClusterManager(activity, googleMap)
        clusterManager.renderer = this.PersonRenderer()
        googleMap.setOnCameraIdleListener(clusterManager)
        googleMap.setOnMarkerClickListener(clusterManager)
        googleMap.setOnInfoWindowClickListener(clusterManager)
        clusterManager.setOnClusterItemClickListener(this)
        clusterManager.setOnClusterItemInfoWindowClickListener(this)
    }

    inner class PersonRenderer : DefaultClusterRenderer<FriendItem>(activity.applicationContext, googleMap, clusterManager) {
        init {
            this.minClusterSize = 1
        }

        override fun onBeforeClusterItemRendered(clusterItem: FriendItem, markerOptions: MarkerOptions) {
            markerOptions.title(clusterItem.name)
                         .snippet(clusterItem.snippet)
                         .icon(createFriendMarkerIcon(clusterItem.photo))
                         .anchor(getFriendAnchorU(), getFriendAnchorV())
        }

        override fun onBeforeClusterRendered(cluster: Cluster<FriendItem>, markerOptions: MarkerOptions) {
            val profilePhotos = ArrayList<Bitmap>(cluster.size)
            cluster.items.forEach {
                profilePhotos.add(it.photo)
            }
            markerOptions.icon(createFriendClusterIcon(profilePhotos))
                         .anchor(getFriendAnchorU(), getFriendAnchorV())
        }

        override fun onClusterItemRendered(clusterItem: FriendItem, marker: Marker) {
            super.onClusterItemRendered(clusterItem, marker)
            friendsItemsIds[clusterItem.id] = marker.id
        }
    }

    override fun onClusterItemClick(item: FriendItem): Boolean {
        // показать polyline
        return false
    }

    override fun onClusterItemInfoWindowClick(item: FriendItem) {
        // перейти к чату
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

