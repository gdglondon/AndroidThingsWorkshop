package com.jamescoggan.workshopapp.actuators

interface Actuator<in T> {
    fun open()
    fun close()
    fun setState(state: T)
}
