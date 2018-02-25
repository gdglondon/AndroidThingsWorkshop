package com.jamescoggan.workshopapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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

    private val switchListener = object : OnStateChangeListener<Boolean> {
        override fun onStateChanged(state: Boolean) = onSwitch(state)
    }

    private val tempSensorListener = object : OnStateChangeListener<Int> {
        override fun onStateChanged(state: Int) = onTemp(state)
    }

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
    }

    override fun onStop() {
        led.close()
        switch.close()
        tempSensor.close()

        super.onStop()
    }

    private fun onSwitch(state: Boolean) {
        Timber.d("Button pressed: $state")
        led.setState(state)
    }

    private fun onTemp(state: Int) {
        Timber.d("Current Temperature: $state")
    }
}
