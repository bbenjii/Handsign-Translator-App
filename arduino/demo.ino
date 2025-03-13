#include "BluetoothSerial.h"  // Include the BluetoothSerial library

BluetoothSerial SerialBT;     // Create a BluetoothSerial object

// Define the analog pin
const int flexSensorPin1 = 36; //thumb Use any available ADC pin
const int flexSensorPin2 = 39;//index
const int flexSensorPin3 = 34; //middle finger
const int flexSensorPin4 = 35; //ring finger
const int flexSensorPin5 = 32; //small finger


void setup() {
  Serial.begin(115200);           // Start serial communication for debugging
  SerialBT.begin("ESP32_BT");     // Initialize Bluetooth with the device name "ESP32_BT"
  Serial.println("Bluetooth started, waiting for connections...");

  pinMode(flexSensorPin1, INPUT);
  pinMode(flexSensorPin2, INPUT);
  pinMode(flexSensorPin3, INPUT);
  pinMode(flexSensorPin4, INPUT);
  pinMode(flexSensorPin5, INPUT);

}

int flexMap(int sensorValue){
  int angle = map(sensorValue, 0, 1920, 0, 180);
  return angle;
}

void loop() {
  // Read the sensor value
  int sensorValue1 = analogRead(flexSensorPin1);
  int sensorValue2 = analogRead(flexSensorPin2);
  int sensorValue3 = analogRead(flexSensorPin3);
  int sensorValue4 = analogRead(flexSensorPin4);
  // int sensorValue5 = analogRead(flexSensorPin5);

  // int sensorValue4 = 1900;
  int sensorValue5 = 1900;

  

    
   

  // Define the other values (can be fixed or dynamically computed)
  
  // Debug output to Serial Monitor
  
  // Construct a string with all 5 values, separated by commas
  String data = String((sensorValue1)) + "," + String((sensorValue2)) + "," + String((sensorValue3)) 
                    + "," + String((sensorValue4)) + "," + String((sensorValue5)) ;

  // Send the constructed string over Bluetooth
  SerialBT.println(data);

  // Optional: Print the sent data for debugging
  Serial.println("Sent Data: " + data);

  delay(100); // Delay to limit update rate
 
}


