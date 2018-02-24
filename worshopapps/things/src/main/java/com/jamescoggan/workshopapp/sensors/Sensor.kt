package com.jamescoggan.workshopapp.sensors

interface OnStateChangeListener<in T> {
    fun onStateChanged(state: T)
}

interface Sensor<out T> {
    fun open()
    fun setListener(listener: OnStateChangeListener<T>?)
    fun close()
}
