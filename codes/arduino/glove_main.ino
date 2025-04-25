#include "BluetoothSerial.h"

BluetoothSerial SerialBT;

#include <Wire.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>


Adafruit_MPU6050 mpu;

const int flexSensorPin1 = 36; //thumb Use any available ADC pin
const int flexSensorPin2 = 39;//index
const int flexSensorPin3 = 34; //middle finger  
const int flexSensorPin4 = 35; //ring finger
const int flexSensorPin5 = 32; //small finger


// Calibration values
const int minValues[5] = {0, 0, 0, 0, 0};     
const int maxValues[5] = {675, 800, 750, 120, 430};

void setup() {
  Serial.begin(115200);      
  SerialBT.begin("ESP32_BT");     // Initialize Bluetooth with the device name "ESP32_BT"
  Serial.println("Bluetooth started, waiting for connections...");
  Wire.begin();
  Serial.println("Bluetooth started, waiting for connections...");

    
  // Print header
  pinMode(flexSensorPin1, INPUT);
  pinMode(flexSensorPin2, INPUT);
  pinMode(flexSensorPin3, INPUT);
  pinMode(flexSensorPin4, INPUT);
  pinMode(flexSensorPin5, INPUT);
}

int flexMapPrecise(int sensorValue, int minVal, int maxVal) {
  // Constrain the value to prevent out-of-range mapping
  int constrainedValue = constrain(sensorValue, minVal, maxVal);
  
  int angle = map(constrainedValue, minVal, maxVal, 0, 180);
  
  return angle;
}


int count = 0;

void loop() {
  
  // Read the sensor values
  int sensorValue1 = analogRead(flexSensorPin1);
  int sensorValue2 = analogRead(flexSensorPin2);
  int sensorValue3 = analogRead(flexSensorPin3);
  int sensorValue4 = analogRead(flexSensorPin4);
  int sensorValue5 = analogRead(flexSensorPin5);


  // Create data string with timestamp
  String data = String(flexMapPrecise(sensorValue1, 0, maxValues[0])) + "," + 
    String(flexMapPrecise(sensorValue2, 0, maxValues[1])) + "," + 
    String(flexMapPrecise(sensorValue3, 0, maxValues[2])) + "," + 
    String(flexMapPrecise(sensorValue4, 0, maxValues[3])) + "," + 
    String(flexMapPrecise(sensorValue5, 0, maxValues[4]));

  Serial.println(data);
  SerialBT.println(data);
  delay(20); 
}
