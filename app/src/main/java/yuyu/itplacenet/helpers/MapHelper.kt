package yuyu.itplacenet.helpers

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import java.util.ArrayList
import yuyu.itplacenet.R
import yuyu.itplacenet.models.Coordinates
import yuyu.itplacenet.models.FriendItem
import yuyu.itplacenet.ui.FriendClusterIconBuilder
import yuyu.itplacenet.ui.FriendMarkerIconBuilder
import yuyu.itplacenet.utils.getSize
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions


class MapHelper(private val activity: Activity) :
            GoogleMap.OnMarkerClickListener,
            GoogleMap.OnMapClickListener,
            GoogleMap.OnPolylineClickListener,
            ClusterManager.OnClusterItemClickListener<FriendItem>,
            ClusterManager.OnClusterItemInfoWindowClickListener<FriendItem> {

    private lateinit var googleMap: GoogleMap
    private lateinit var myMarker: Marker
    private lateinit var clusterManager: ClusterManager<FriendItem>

    private var friendsItems = HashMap<String,FriendItem>()
    private var polylines = ArrayList<Polyline>()

    private var zoomLevel = 11f
    private val locationUpdateInterval: Long = 10000
    private val fastestLocationUpdateInterval: Long = locationUpdateInterval / 2

    // Карта

    fun setMap( gMap: GoogleMap ) {
        googleMap = gMap
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.setOnMarkerClickListener(this)
        googleMap.setOnMapClickListener(this)
        googleMap.setOnPolylineClickListener(this)

        this.setClusterManager()
    }

    // Маркеры

    private fun addMyMarker( position: LatLng ) {
        myMarker = googleMap.addMarker(MarkerOptions()
                                .position(position)
                                .title(activity.getString(R.string.my_marker))
                                .snippet("${position.latitude}, ${position.longitude}")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.myself))
                                .anchor(0.5f, 0.36f))
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

    private fun addFriendMarker( id: String, name: String, position: LatLng, snippet: String, photo: Bitmap, photoHash: String ) {
        friendsItems[id] = FriendItem(id, position, name, snippet, photo, photoHash)
        clusterManager.addItem(friendsItems[id])
        clusterManager.cluster()
    }

    fun setFriendMarker( id: String, name: String, position: LatLng, lastUpdate: String, photoHash: String ) {
        if( ::googleMap.isInitialized ) {
            /*// Работает только для отдельных маркеров, не входящих в кластеры
            if( friendsItems.containsKey(id) ) {
                val oldFI = friendsItems.getValue(id)
                clusterManager.markerCollection.markers.forEach{ marker ->
                    if( marker.tag == id ) {
                        if (marker.position != position)   marker.position = position
                        if (marker.title    != name)       marker.title    = name
                        if (marker.snippet  != lastUpdate) marker.snippet  = lastUpdate

                        val photoBitmap: Bitmap
                        if( photoHash != oldFI.photoHash ) {
                            photoBitmap = ImageHelper(activity).loadBitmapFromName(photoHash)
                            val icon = this.createFriendMarkerIcon(photoBitmap)
                            marker.setIcon(icon)
                        } else {
                            photoBitmap = oldFI.photo
                        }

                        friendsItems[id] = FriendItem(id, position, name, lastUpdate, photoBitmap, photoHash)
                    }
                }
                clusterManager.cluster()
            } else {}*/
            if( friendsItems.containsKey(id) ) this.removeFriendMarker(id)
            val photoBitmap = ImageHelper(activity).loadBitmapFromName(photoHash)
            this.addFriendMarker(id, name, position, lastUpdate, photoBitmap, photoHash)
        }
    }

    fun removeFriendMarker( id: String ) {
        if( ::googleMap.isInitialized && friendsItems.containsKey(id) ) {
            clusterManager.removeItem(friendsItems[id])
            clusterManager.cluster()
            friendsItems.remove(id)
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
            marker.tag = clusterItem.id
        }

        override fun onClusterRendered(cluster: Cluster<FriendItem>, marker: Marker) {
            super.onClusterRendered(cluster, marker)
            marker.tag = -1
        }
    }

    override fun onClusterItemClick(item: FriendItem): Boolean {
        this.drawPolyline(item.id)
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

    private fun boundCamera( points: List<LatLng> ) {
        val builder = LatLngBounds.builder()
        points.forEach {
            builder.include(it)
        }
        val lineBounds = builder.build()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(lineBounds, 0))
    }

    // Мое местоположение

    fun createLocationRequest() : LocationRequest {
        return LocationRequest().apply {
            interval = locationUpdateInterval
            fastestInterval = fastestLocationUpdateInterval
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    // История перемещений

    private fun clearPolyline() {
        polylines.forEach(Polyline::remove)
    }

    override fun onMarkerClick( marker: Marker ): Boolean {
        this.clearPolyline()
        this.drawPolyline(marker.tag?.toString())
        return false
    }

    override fun onMapClick( p0: LatLng? ) {
        this.clearPolyline()
    }

    override fun onPolylineClick( polyline: Polyline ) {
        boundCamera(polyline.points)
    }

    private fun drawPolyline( userId: String? = null ): Boolean {
        val width: Float
        val zIndex: Float
        if( userId == null ) {
            width = 5f
            zIndex = 5f
        } else {
            width = 2f
            zIndex = 1f
        }

        val successCallback = { coordinates: List<Coordinates> ->
            if( coordinates.isNotEmpty() ) {
                val history = arrayListOf<LatLng>()
                coordinates.forEach{
                    val lat = it.latitude
                    val lng = it.longitude
                    if( lat != null && lng != null ) {
                        val position = LatLng(lat, lng)
                        history.add(position)
                    }
                }
                val polylineOptions = PolylineOptions()
                        .addAll(history)
                        .color(Color.BLACK)
                        .width(width)
                        .zIndex(zIndex)
                val line = googleMap.addPolyline(polylineOptions)
                line.isClickable = true
                polylines.add( line )
            }
        }

        UserHelper(activity).loadLocationHistory(userId, successCallback)
        return false
    }

}

