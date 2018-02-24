package com.jamescoggan.workshopapp.actuators

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService

class Led(private val portName: String) : Actuator<Boolean> {

    private var gpio: Gpio? = null

    override fun open() {
        close()
        gpio = PeripheralManagerService().openGpio(portName)
        gpio?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
    }

    override fun close() {
        gpio?.close().also {
            gpio = null
        }
    }

    override fun setState(state: Boolean) {
        gpio?.value = state
    }

}
