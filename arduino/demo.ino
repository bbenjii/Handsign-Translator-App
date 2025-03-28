#include "BluetoothSerial.h"  // Include the BluetoothSerial library

BluetoothSerial SerialBT;     // Create a BluetoothSerial object

// Define the analog pin
const int flexSensorPin1 = 36; //thumb Use any available ADC pin
const int flexSensorPin2 = 39;//index
const int flexSensorPin3 = 34; //middle finger
const int flexSensorPin4 = 35; //ring finger
const int flexSensorPin5 = 32; //small finger

// Calibration values
const int minValues[5] = {0, 0, 0, 0, 0};     
const int maxValues[5] = {675, 800, 750, 120, 430};

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

// Precise mapping function that inverts the value if needed
int flexMapPrecise(int sensorValue, int minVal, int maxVal) {
  // Constrain the value to prevent out-of-range mapping
  int constrainedValue = constrain(sensorValue, minVal, maxVal);
  
  // Invert the mapping so that low values = 0, high values = 180
  // This assumes the sensor reads low when fully extended (finger down)
  // and high when fully bent (finger up)
  int angle = map(constrainedValue, minVal, maxVal, 0, 180);
  
  return angle;
}

void loop() {
  // Read the sensor values
  int sensorValue1 = analogRead(flexSensorPin1);
  int sensorValue2 = analogRead(flexSensorPin2);
  int sensorValue3 = analogRead(flexSensorPin3);
  int sensorValue4 = analogRead(flexSensorPin4);
  int sensorValue5 = analogRead(flexSensorPin5);

  // Create data string with precise mapping
  String data = 
    String(flexMapPrecise(sensorValue1, 0, maxValues[0])) + "," + 
    String(flexMapPrecise(sensorValue2, 0, maxValues[1])) + "," + 
    String(flexMapPrecise(sensorValue3, 0, maxValues[2])) + "," + 
    String(flexMapPrecise(sensorValue4, 0, maxValues[3])) + "," + 
    String(flexMapPrecise(sensorValue5, 0, maxValues[4]));

  // Send the constructed string over Bluetooth
  SerialBT.println(data);

  // Optional: Print the sent data for debugging
  Serial.println("Sent Data: " + data);

  delay(100); // Delay to limit update rate
}
