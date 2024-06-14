package com.example.glmaclient.persistentcloudanchor

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.glmaclient.persistentcloudanchor.R
import com.example.glmaclient.persistentcloudanchor.databinding.FragmentMapsClientBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsClient : Fragment() {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapsClientBinding

    private var currentLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101
    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        getCurrentLocationUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapsClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun getCurrentLocationUser() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissionCode
            )
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                Log.d("MapsClient", "Current location: ${currentLocation?.latitude}, ${currentLocation?.longitude}")
                updateMapLocation()
            } else {
                Log.e("MapsClient", "Location is null")
                Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("MapsClient", "Error getting location: ${exception.message}")
            Toast.makeText(requireContext(), "Error getting location: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMapLocation() {
        currentLocation?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            mMap.addMarker(MarkerOptions().position(latLng).title("Current Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationUser()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
