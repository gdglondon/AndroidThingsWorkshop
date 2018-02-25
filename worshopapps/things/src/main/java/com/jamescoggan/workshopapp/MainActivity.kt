package com.jamescoggan.workshopapp

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.jamescoggan.common.data.HomeInformation
import com.jamescoggan.common.data.HomeInformationLiveData
import com.jamescoggan.common.data.HomeInformationStorage
import com.jamescoggan.workshopapp.actuators.Actuator
import com.jamescoggan.workshopapp.actuators.Led
import com.jamescoggan.workshopapp.port.gpioForButton
import com.jamescoggan.workshopapp.port.gpioForLED
import com.jamescoggan.workshopapp.port.i2cForTempSensor
import com.jamescoggan.workshopapp.sensors.OnStateChangeListener
import com.jamescoggan.workshopapp.sensors.Sensor
import com.jamescoggan.workshopapp.sensors.Switch
import com.jamescoggan.workshopapp.sensors.TemperatureSensor
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REFRESH_TIME = 2000L // Refresh every 2 seconds
    }

    private var led: Actuator<Boolean> = Led(gpioForLED)
    private var tempSensor: Sensor<Int> = TemperatureSensor(i2cForTempSensor, REFRESH_TIME)
    private var switch: Sensor<Boolean> = Switch(gpioForButton)
    private var homeInformationLiveData: HomeInformationLiveData? = null
    private var homeInformationStorage: HomeInformationStorage? = null

    private val switchListener = object : OnStateChangeListener<Boolean> {
        override fun onStateChanged(state: Boolean) = onSwitch(state)
    }

    private val tempSensorListener = object : OnStateChangeListener<Int> {
        override fun onStateChanged(state: Int) = onTemp(state)
    }

    private val homeDataObserver = Observer<HomeInformation> { led.setState(it?.light ?: false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        led.open()
        switch.open()
        tempSensor.open()

        switch.setListener(switchListener)
        tempSensor.setListener(tempSensorListener)
        loginFirebase()
    }

    override fun onStop() {
        homeInformationLiveData?.removeObserver(homeDataObserver)
        led.close()
        switch.close()
        tempSensor.close()

        super.onStop()
    }

    private fun onSwitch(state: Boolean) {
        Timber.d("Button pressed: $state")
        homeInformationStorage?.saveButtonState(state)
        homeInformationStorage?.saveLightState(state)
    }

    private fun onTemp(state: Int) {
        Timber.d("Current Temperature: $state")
        homeInformationStorage?.saveTemperature(state.toFloat())
    }

    private fun loginFirebase() {
        val firebase = FirebaseAuth.getInstance()
        firebase.signInAnonymously()
                .addOnSuccessListener { observeData() }
                .addOnFailureListener { Timber.e("Failed to login $it") }
    }

    private fun observeData() {
        Timber.d("Logged in, observing data")
        val reference = FirebaseDatabase.getInstance().reference.child("home")
        homeInformationLiveData = HomeInformationLiveData(reference)
        homeInformationLiveData?.observe(this, homeDataObserver)
        homeInformationStorage = HomeInformationStorage(reference)
    }
}
