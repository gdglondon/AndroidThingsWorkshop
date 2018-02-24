package com.jamescoggan.workshopapp.port

import android.os.Build
import java.lang.IllegalStateException

private const val DEVICE_RPI3 = "rpi3"
private const val DEVICE_IMX6UL_PICO = "imx6ul_pico"
private const val DEVICE_IMX7D_PICO = "imx7d_pico"

val gpioForButton: String // Pin 40
    get() {
        return when (Build.DEVICE) {
            DEVICE_RPI3 -> "BCM21"
            DEVICE_IMX6UL_PICO -> "GPIO2_IO03"
            DEVICE_IMX7D_PICO -> "GPIO6_IO14"
            else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
        }
    }

val gpioForLED: String // Pin 31
    get() {
        return when (Build.DEVICE) {
            DEVICE_RPI3 -> "BCM6"
            DEVICE_IMX6UL_PICO -> "GPIO4_IO22"
            DEVICE_IMX7D_PICO -> "GPIO2_IO02"
            else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
        }
    }

val i2cForTempSensor: String // Pins 3 and 5
    get() {
        return when (Build.DEVICE) {
            DEVICE_RPI3 -> "I2C1"
            DEVICE_IMX6UL_PICO -> "I2C1"
            DEVICE_IMX7D_PICO -> "I2C1"
            else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
        }
    }
