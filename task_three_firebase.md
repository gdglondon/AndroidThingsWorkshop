## Setup

Since the FireBase services are stored in the cloud, we need to setup the wifi on our Android Things so it can communicate to the internet.

To setup the wifi on your Android Things you can use the setup utility tool or use the command below.
Setup wifi on your Android Things, replace the `$HERE` values
```
adb shell am startservice -n com.google.wifisetup/.WifiSetupService -a WifiSetupService.Connect -e ssid $YOUR_SSID_HERE -e passphrase $YOUR_WIFI_PASSWORD_HERE
```

To make the lesson easier, the base code for it is already done (based on the previous tasks)
So clone this repository and open the `worshopapps` folder.

This project contains 3 modules: 
- common: where the common code between the modules is stored
- things: this is our Android Things application
- mobile: this is our mobile phone application

## Connect the pins

Now that we have our software setup, we need to connect our hardware together.
The pins are same as the previous tasks, so if you are already setup, you don't need this.

![](Diagrams/Temperature_Sensor/pi%20with%20temp%20sensor_bb.png)

## Create a FireBase project

##### Step 1

There are 2 ways to setup Firebase, one is to go in the [console](https://console.firebase.google.com) and created your own project, or you can use Android Studio to do ir for your.

![](images/step1.png)

##### Step 2

Android Studio will open a new tab that gives your a list of all the features you can automatically install.
In this case we wan't to use the FireBase Realtime database.

![](images/step2.png)

##### Step 3

Select the option

![](images/step3.png)

##### Step 4
First of all, you need to connect your Android Studio to FireBase, so select the button connect to FireBase

![](images/step4.png)

##### Step 5

![](images/step5.png)

##### Step 6

You need to select what project you want to connect, or you can create a new one.

![](images/step6.png)

##### Step 7

Once connected you can choose the module you want to connect to FireBase

![](images/step7.png)

##### Step 8

And add the changes to your gradle files. In this case we wan't to connect both

![](images/step8.png)

##### Step 9

You have added one module to the FireBase project, you need to do the same for the other module, go back to step 6 and do it for the second module

![](images/step9.png)

##### Step 10

Now open the [FireBase Console](https://console.firebase.google.com)

![](images/step10.png)

##### Step 11

And open the authentication tab

![](images/step11.png)

##### Step 12

For this lesson we are going to use the anonymous login, but remember you need to change this for a production build.
So select the anonymous option.

![](images/step12.png)

##### Step 13

And activate it

![](images/step13.png)



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