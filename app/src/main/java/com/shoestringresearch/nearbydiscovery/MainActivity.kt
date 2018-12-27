package com.shoestringresearch.nearbydiscovery

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

  companion object {
    val SERVICE_ID = this::class.java.`package`!!.name
    val REQUIRED_PERMISSIONS = arrayOf(
      Manifest.permission.BLUETOOTH,
      Manifest.permission.BLUETOOTH_ADMIN,
      Manifest.permission.ACCESS_WIFI_STATE,
      Manifest.permission.CHANGE_WIFI_STATE,
      Manifest.permission.ACCESS_COARSE_LOCATION)
  }

  private lateinit var nearbyClient : ConnectionsClient

  // Associate peer endpointId to its UI View.
  private val peers = HashMap<String, TextView>()

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Get permissions.
    REQUIRED_PERMISSIONS.filter {
      ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }.let { ungranted ->
      if (!ungranted.isEmpty())
        ActivityCompat.requestPermissions(this, ungranted.toTypedArray(), 0)
    }

    nearbyClient = Nearby.getConnectionsClient(this)
    nearbyName.text = Build.MANUFACTURER + " " + Build.MODEL + " " +
        (Random().nextInt() and 0x7fffffff).toString(16)

    activateNearby.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        startNearby()
      }
      else {
        stopNearby()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    if (activateNearby.isChecked) {
      startNearby()
    }
  }

  override fun onPause() {
    if (activateNearby.isChecked) {
      stopNearby()
    }
    super.onPause()
  }

  private fun startNearby() {
    // Start both Nearby advertising...
    val advertisingOptions = AdvertisingOptions.Builder()
      .setStrategy(Strategy.P2P_CLUSTER)
      .build()
    nearbyClient.startAdvertising(
      nearbyName.text.toString(),
      SERVICE_ID,
      ConnectionLifecycleCallbackImpl(),
      advertisingOptions)
      .addOnSuccessListener {
        Log.d("MainActivity", "Nearby advertising started")
      }
      .addOnFailureListener {
        Log.e("MainActivity", "Nearby advertising failed", it)
      }

    // ...and discovery.
    val discoveryOptions = DiscoveryOptions.Builder()
      .setStrategy(Strategy.P2P_CLUSTER)
      .build()
    nearbyClient.startDiscovery(
        SERVICE_ID,
        EndpointDiscoveryCallbackImpl(),
        discoveryOptions)
      .addOnSuccessListener {
        Log.d("MainActivity", "Nearby discovery started")
      }
      .addOnFailureListener {
        Log.e("MainActivity", "Nearby discovery failed", it)
      }
  }

  private fun stopNearby() {
    nearbyClient.stopAdvertising()
    nearbyClient.stopDiscovery()
    nearbyClient.stopAllEndpoints()

    peersContainer.removeAllViews()
    peers.clear()
    Log.d("MainActivity", "Nearby stopped")
  }

  inner class ConnectionLifecycleCallbackImpl : ConnectionLifecycleCallback() {
    override fun onConnectionResult(p0: String, p1: ConnectionResolution) {
      TODO("not implemented")
    }

    override fun onDisconnected(p0: String) {
      TODO("not implemented")
    }

    override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
      TODO("not implemented")
    }
  }

  inner class EndpointDiscoveryCallbackImpl : EndpointDiscoveryCallback() {
    @SuppressLint("SetTextI18n")
    override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
      if (endpointId in peers) {
        Log.e("MainActivity", "discovered endpointId '$endpointId' already present")
      }

      // Add peer to UI.
      val entry = TextView(this@MainActivity)
      entry.text = "$endpointId ${info.endpointName}"
      entry.textSize = 14f
      peersContainer.addView(entry)

      // Allow view lookup via endpoint id for later removal.
      peers[endpointId] = entry
      Log.d("MainActivity", "added ${entry.text}")
    }

    override fun onEndpointLost(endpointId: String) {
      if (endpointId !in peers) {
        Log.e("MainActivity", "lost endpoint '$endpointId' not present")
        return
      }

      // Remove peer from UI.
      peersContainer.removeView(peers[endpointId])
      peers.remove(endpointId)
      Log.d("MainActivity", "removed endpoint '$endpointId'")
    }
  }

}
