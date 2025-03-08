#include "BluetoothSerial.h"  // Include the BluetoothSerial library

BluetoothSerial SerialBT;     // Create a BluetoothSerial object

// Define the analog pin
const int flexSensorPin = 4; // Use any available ADC pin

void setup() {
  Serial.begin(115200);           // Start serial communication for debugging
  SerialBT.begin("ESP32_BT");     // Initialize Bluetooth with the device name "ESP32_BT"
  Serial.println("Bluetooth started, waiting for connections...");
}

void loop() {
  // Read the sensor value
  demo(180, 0, 180, 180, 180); //1
  demo(180,0,0,180,180); //2
  demo(0, 0, 0, 180, 180); //3
 demo(180, 0, 0, 0, 0);//4
  demo(0, 0, 0, 0, 0);//5

 
}



void demo(int v0,int v1, int v2, int v3,int v4){
    int sensorValue = digitalRead(flexSensorPin);
  float voltage = (sensorValue / 4095.0) * 3.3;

  // Define the other values (can be fixed or dynamically computed)
  int value0= v0;
  int value1 = v1;
  int value2 = v2;
  int value3 = v3;
  int value4 = v4;
  // Debug output to Serial Monitor
  Serial.println("Sensor Value: " + String(sensorValue) + " | Voltage: " + String(voltage));

  // Construct a string with all 5 values, separated by commas
  String data = String(value0) + "," + String(value1) + "," + String(value2) + "," + String(value3) + "," + String(value4);

  // String data= String(sensorValue);

  // Send the constructed string over Bluetooth
  SerialBT.println(data);

  // Optional: Print the sent data for debugging
  Serial.println("Sent Data: " + data);

  delay(3000); // Delay to limit update rate
}
