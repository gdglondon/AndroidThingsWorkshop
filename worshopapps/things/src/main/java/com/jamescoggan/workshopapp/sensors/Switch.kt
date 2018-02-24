package com.jamescoggan.workshopapp.sensors

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService

class Switch(private val portName: String) : Sensor<Boolean> {

    private var gpio: Gpio? = null
    private var listener: OnStateChangeListener<Boolean>? = null

    override fun open() {
        close()
        gpio = PeripheralManagerService().openGpio(portName)
        gpio?.setDirection(Gpio.DIRECTION_IN)
        gpio?.setEdgeTriggerType(Gpio.EDGE_BOTH)
        gpio?.setActiveType(Gpio.ACTIVE_LOW)
        gpio?.registerGpioCallback(object : GpioCallback() {
            override fun onGpioEdge(gpio: Gpio?): Boolean {
                gpio?.value?.let {
                    listener?.onStateChanged(it)
                }
                return true
            }
        })
    }

    override fun setListener(listener: OnStateChangeListener<Boolean>?) {
        this.listener = listener
    }

    override fun close() {
        listener = null
        gpio?.close().also {
            gpio = null
        }
    }

}
