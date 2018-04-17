package yuyu.itplacenet

import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

import kotlinx.android.synthetic.main.activity_maps.*

import yuyu.itplacenet.helpers.MapHelper
import yuyu.itplacenet.helpers.PermissionHelper
import yuyu.itplacenet.helpers.UserHelper
import yuyu.itplacenet.utils.*


class MapsActivity : AppCompatActivity(),
                        OnMapReadyCallback,
                        GoogleApiClient.ConnectionCallbacks {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val permissionHelper = PermissionHelper(this)
    private val mapHelper = MapHelper(this)
    private val userHelper = UserHelper(this, true)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    updateMyLocation(location)
                }
            }
        }

        location.setOnClickListener {
            startLocationUpdates()
        }
    }

    public override fun onStart() {
        super.onStart()
        googleApiClient.connect()
    }

    override fun onDestroy() {
        googleApiClient.disconnect()
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

        plus.setOnClickListener {
            mapHelper.zoomIn()
        }
        minus.setOnClickListener {
            mapHelper.zoomOut()
        }
    }

    // Мое местоположение

    override fun onConnected(bundle: Bundle?) {
        startLocationUpdates()
    }

    override fun onConnectionSuspended(p0: Int) {}


    private fun startLocationUpdates() {
        if( permissionHelper.mayGetDeviceLocation() ) {
            checkCurrentLocationSettings()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    private fun checkCurrentLocationSettings() {
        val locationRequest = mapHelper.createLocationRequest()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { _ ->
            // All location settings are satisfied
            requestLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied
                val statusCode = (exception as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            exception.startResolutionForResult(this, RC_CHECK_LOCALE_SETTINGS)
                        } catch (sie: IntentSender.SendIntentException) {
                            // Ignore the error.
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        toast(getString(R.string.error_location_settings))
                    }
                }

            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        if (googleApiClient.isConnected) {
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        if( location == null ) {
                            val locationRequest = mapHelper.createLocationRequest()
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
                        } else {
                            updateMyLocation(location)
                        }
                    }
        }
    }

    private fun updateMyLocation(location: Location) {
        val position = LatLng(location.latitude, location.longitude)
        userHelper.updateCoordinates(location.latitude, location.longitude)
        mapHelper.setMyMarker(position)
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == RC_CHECK_PERMISSION_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkCurrentLocationSettings()
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