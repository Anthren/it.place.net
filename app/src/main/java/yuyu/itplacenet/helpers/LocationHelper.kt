package yuyu.itplacenet.helpers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.places.Places
import yuyu.itplacenet.R
import yuyu.itplacenet.utils.*

class LocationHelper(private val activity: Activity) :
        GoogleApiClient.ConnectionCallbacks {

    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var updateMyLocationFun: (Location) -> Unit

    private var locationCallback: LocationCallback =
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult?) {
                            locationResult ?: return
                            for (location in locationResult.locations) {
                                updateMyLocationFun(location)
                            }
                        }
                    }

    private val locationUpdateInterval: Long = 10000
    private val fastestLocationUpdateInterval: Long = locationUpdateInterval / 2

    fun init( function: (Location) -> Unit ) {
        googleApiClient = GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        updateMyLocationFun = function
    }


    fun connect() {
        googleApiClient.connect()
    }
    fun disconnect() {
        googleApiClient.disconnect()
    }

    override fun onConnected(bundle: Bundle?) {
        startLocationUpdates()
    }
    override fun onConnectionSuspended(p0: Int) {}


    fun startLocationUpdates() {
        checkCurrentLocationSettings()
    }
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    private fun checkCurrentLocationSettings() {
        val locationRequest = this.createLocationRequest()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(activity)
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
                            exception.startResolutionForResult(activity, RC_CHECK_LOCALE_SETTINGS)
                        } catch (sie: IntentSender.SendIntentException) {
                            // Ignore the error.
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        activity.toast(activity.getString(R.string.error_location_settings))
                    }
                }

            }
        }
    }

    private fun createLocationRequest() : LocationRequest {
        return LocationRequest().apply {
            interval = locationUpdateInterval
            fastestInterval = fastestLocationUpdateInterval
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        if (googleApiClient.isConnected) {
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        if( location == null ) {
                            val locationRequest = this.createLocationRequest()
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
                        } else {
                            updateMyLocationFun(location)
                        }
                    }
        }
    }

}