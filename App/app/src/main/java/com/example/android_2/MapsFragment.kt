package com.example.android_2

import android.Manifest
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_UI
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Math.toDegrees
import kotlin.math.roundToInt


class MapsFragment : Fragment(), OnMapReadyCallback, SensorEventListener, LocationListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var currentLocation: Location
    internal var mCurrLocationMarker: Marker? = null
    internal lateinit var mLocationRequest: LocationRequest
    internal lateinit var mLocationCallback: LocationCallback
    var mMap: GoogleMap? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private lateinit var compassImage: ImageView
    private lateinit var accelerometerSensor: Sensor
    private lateinit var magneticSensor: Sensor

    private var accelerometerData = FloatArray(3)
    private var magnetometerData = FloatArray(3)
    private var rotationMatrix = FloatArray(9)
    private var orientationAngles = FloatArray(3)
    private val permissionCode = 101
    var lat: Double = 0.0
    var lon: Double = 0.0
    private val callback = OnMapReadyCallback { googleMap ->
        val yourLocation = LatLng(lat, lon)
        mMap = googleMap
        mMap!!.addMarker(MarkerOptions().position(yourLocation).title("Your Location" + lat + lon))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(yourLocation, 17f))
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        val lm: LocationManager? = context!!.getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(context!!.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context!!.applicationContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionCode)
            compassImage = view.findViewById(R.id.compass)
            return view
        }
        lm!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1.0f, this)
        fetchLocation()
        compassImage = view.findViewById(R.id.compass)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometerSensor, SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magneticSensor, SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometerSensor);
        sensorManager.unregisterListener(this, magneticSensor);
    }

    private fun fetchLocation() {
        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(context!!)
        if (ActivityCompat.checkSelfPermission(
                        context!!.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context!!.applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionCode)
            return
        }
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener {
            if (it != null) {
                lat = it.latitude
                lon = it.longitude
        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(callback)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("I am here!")
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f))
        googleMap?.addMarker(markerOptions)
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context!!.applicationContext,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient()
                mMap!!.isMyLocationEnabled = true
            }
        } else {
            buildGoogleApiClient()
            mMap!!.isMyLocationEnabled = true
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == permissionCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                fetchLocation()
            }
        }
    }

    @Synchronized
    fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(context!!)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient.connect()
    }

    override fun onConnected(bundle: Bundle?) {
        fetchLocation()
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (ContextCompat.checkSelfPermission(context!!.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper()!!)
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Toast.makeText(context!!.applicationContext, "connection suspended", Toast.LENGTH_SHORT).show()
    }


    override fun onLocationChanged(location: Location) {
        fetchLocation()
//        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        supportMapFragment.getMapAsync(callback)
//        currentLocation = location
//        if (mCurrLocationMarker != null) {
//            mCurrLocationMarker!!.remove()
//        }
//        //Place current location marker
//        val latLng = LatLng(location.latitude, location.longitude)
//        val markerOptions = MarkerOptions()
//        markerOptions.position(latLng)
//        markerOptions.title("Current Position")
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//        mCurrLocationMarker = mMap!!.addMarker(markerOptions)
//
//        //move map camera
//        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
//        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))
//        Toast.makeText(context!!.applicationContext, "loc changed", Toast.LENGTH_SHORT).show()
//
//        //stop location updates
//        if (mGoogleApiClient != null) {
//            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
//        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == accelerometerSensor) {
            accelerometerData = event.values
        } else if (event?.sensor == magneticSensor) {
            magnetometerData = event.values
        }

        updateOrientationAngles()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerData, magnetometerData)
        val orientation = SensorManager.getOrientation(rotationMatrix, orientationAngles)
        val degrees = (toDegrees(orientation[0].toDouble()) + 360.0) % 360.0
        val angle = (degrees * 100).roundToInt() / 100
        compassImage.rotation = angle.toFloat() * -1
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(context!!.applicationContext, "connection failed", Toast.LENGTH_SHORT).show()
    }
}