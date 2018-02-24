# Test
![Sensor datasheet](schematic/sensor-datasheet.pdf)
![](schematic/pinout-raspberrypi.png)
![](schematic/temp-sensor-pinout.png)
![](schematic/Temperature-Sensor-Connections.png)

```    private lateinit var i2cDevice: I2cDevice
       
       override fun onStart() {
           super.onStart()
           i2cDevice = PeripheralManagerService().openI2cDevice("I2C1", ADDRESS)
   
   
           while (true) {
   
               val array = ByteArray(1)
               i2cDevice.write(array, 1)
   
               val input = ByteArray(1)
               i2cDevice.read(input, 1)
   
               val temperature: Int = input[0].toInt() and 0xff
               Log.e("Sensor", "$temperature")
   
               Thread.sleep(3000)
           }
           
       }
   
       override fun onDestroy() {
           super.onDestroy()
           i2cDevice.close()
       }```