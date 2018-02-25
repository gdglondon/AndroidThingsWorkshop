## Setup

Setup wifi on your Android Things, replace the `$HERE` values
```
adb shell am startservice -n com.google.wifisetup/.WifiSetupService -a WifiSetupService.Connect -e ssid $YOUR_SSID_HERE -e passphrase $YOUR_WIFI_PASSWORD_HERE
```
Clone this repo

## Connect the pins

![](Diagrams/Temperature_Sensor/pi%20with%20temp%20sensor_bb.png)

## Create a FireBase project

## Setup FireBase on the common module

``` // Things, Mobile, commond build.gradle
    implementation 'com.google.firebase:firebase-database:11.0.4'
    implementation 'com.google.firebase:firebase-auth:11.0.4'
    implementation "com.google.android.gms:play-services-base:11.0.4"
    implementation "android.arch.lifecycle:extensions:1.1.0"
```

```
// HomeInformation.kt
data class HomeInformation(var button: Boolean = false,
                           var light: Boolean = false,
                           var temperature: Float = 0f)
```

```
// Common module - HomeInformationLiveData.kt
class HomeInformationLiveData(private val databaseReference: DatabaseReference) : LiveData<HomeInformation>() {

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val newValue = snapshot.getValue(HomeInformation::class.java)
            Timber.d("New data received! $newValue")
            value = newValue
        }

        override fun onCancelled(error: DatabaseError) {
            Timber.w(error.toException(), "onCancelled")
        }
    }

    override fun onActive() {
        databaseReference.addValueEventListener(valueEventListener)
    }

    override fun onInactive() {
        databaseReference.removeEventListener(valueEventListener)
    }
}
```

```
// Common module - HomeInformationStorage.kt
class HomeInformationStorage(private val reference: DatabaseReference) {
    companion object {
        private const val HOME_INFORMATION_LIGHT = "light"
        private const val HOME_INFORMATION_BUTTON = "button"
        private const val HOME_INFORMATION_TEMPERATURE = "temperature"
    }

    fun saveLightState(isOn: Boolean) {
        reference.child(HOME_INFORMATION_LIGHT).setValue(isOn)
    }

    fun saveButtonState(isPressed: Boolean) {
        reference.child(HOME_INFORMATION_BUTTON).setValue(isPressed)
    }

    fun saveTemperature(temperature: Float) {
        reference.child(HOME_INFORMATION_TEMPERATURE).setValue(temperature)
    }
}
```

## Setup FireBase on the Things

```
// ThingsApplication.kt
FirebaseApp.initializeApp(this)
```

```
    private fun loginFirebase() {
        val firebase = FirebaseAuth.getInstance()
        firebase.signInAnonymously()
                .addOnSuccessListener { observeData() }
                .addOnFailureListener { Timber.e("Failed to login $it") }
    }

    override fun onStart() {
        super.onStart()
        ...
        loginFirebase()
    }    
```

```
    // Homeactivity.kt Things
    private var homeInformationLiveData: HomeInformationLiveData? = null
    private var homeInformationStorage: HomeInformationStorage? = null

    private val homeDataObserver = Observer<HomeInformation> { led.setState(it?.light ?: false) }
    
    override fun onStop() {
        homeInformationLiveData?.removeObserver(homeDataObserver)
        ...
    }
    
    private fun observeData() {
        Timber.d("Logged in, observing data")
        val reference = FirebaseDatabase.getInstance().reference.child("home")
        homeInformationLiveData = HomeInformationLiveData(reference)
        homeInformationLiveData?.observe(this, homeDataObserver)
        homeInformationStorage = HomeInformationStorage(reference)
    }    
```

```
    private fun onSwitch(state: Boolean) {
        Timber.d("Button pressed: $state")
        homeInformationStorage?.saveButtonState(state)
        homeInformationStorage?.saveLightState(state)
    }

    private fun onTemp(state: Int) {
        Timber.d("Current Temperature: $state")
        homeInformationStorage?.saveTemperature(state.toFloat())
    }
```

## Setup FireBase on the Mobile

```
// ThingsApplication.kt
FirebaseApp.initializeApp(this)
```

```
<!-- activity_main.xml -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context="com.jamescoggan.workshopapp.MainActivity">

    <TextView
        android:id="@+id/temperature_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        tools:text="Temperature: 29 °C" />

    <TextView
        android:id="@+id/button_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        tools:text="Button is pressed" />

    <TextView
        android:id="@+id/led_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        tools:text="Led is on" />

    <ToggleButton
        android:id="@+id/led_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

</LinearLayout>
 
```

```
    private var homeInformationLiveData: HomeInformationLiveData? = null
    private var homeInformationStorage: HomeInformationStorage? = null

    private val homeDataObserver = Observer<HomeInformation> {
        val ledText = "Led is " + if (it?.light == true) "on" else "off"
        led_status.text = ledText

        val buttonText = "Button is " + if (it?.button == true) "pressed" else "not pressed"
        button_status.text = buttonText

        val tempText = "Temperature: ${it?.temperature} °C"
        temperature_status.text = tempText

        led_button.isChecked = it?.light ?: false
    }
```

```
    override fun onResume() {
        super.onResume()

        loginFirebase()
    }

    override fun onPause() {
        homeInformationLiveData?.removeObserver(homeDataObserver)

        super.onPause()
    }
    
    private fun loginFirebase() {
        val firebase = FirebaseAuth.getInstance()
        firebase.signInAnonymously()
                .addOnSuccessListener { observeData() }
                .addOnFailureListener { Timber.e("Failed to login $it") }
    } 
```

``` 
    private fun observeData() {
        Timber.d("Logged in, observing data")
        val reference = FirebaseDatabase.getInstance().reference.child("home")
        homeInformationLiveData = HomeInformationLiveData(reference)
        homeInformationLiveData?.observe(this, homeDataObserver)
        homeInformationStorage = HomeInformationStorage(reference)
    }
```

```
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        led_button.setOnCheckedChangeListener { _, state -> setLed(state) }
    }
```

## Configure your project in the console
[Firebase console](https://console.firebase.google.com/u/0/)

## Install node JS

### Windows
[Node js download](https://nodejs.org/en/download/)

### Linux
```
curl -sL https://deb.nodesource.com/setup_9.x | sudo -E bash -
apt-get install nodejs
```

### Mac
``` brew install node.js ```


## Install the Firebase CLI
```npm install -g firebase-tools```

## Login firebase
```firebase login```

## Create your project

- Create a project folder
- ```firebase init```

## Deploy your project 
```firebase deploy```