#include "BluetoothSerial.h"

BluetoothSerial SerialBT;

#include <Wire.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>


Adafruit_MPU6050 mpu;


float accelBias[3] = {0, 0, 0};
float gyroBias[3] = {0, 0, 0};


const int FILTER_WINDOW = 10;
float accelXBuffer[FILTER_WINDOW] = {0};
float accelYBuffer[FILTER_WINDOW] = {0};
float accelZBuffer[FILTER_WINDOW] = {0};
float gyroXBuffer[FILTER_WINDOW] = {0};
float gyroYBuffer[FILTER_WINDOW] = {0};
float gyroZBuffer[FILTER_WINDOW] = {0};
int bufferIndex = 0;

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
  // Start serial communication for debugging
  Wire.begin();
  Serial.println("Bluetooth started, waiting for connections...");

  if (!mpu.begin()) {
     Serial.println("MPU6050 not found! Check connections.");
      while (1);
  }

  mpu.setAccelerometerRange(MPU6050_RANGE_8_G);
  mpu.setGyroRange(MPU6050_RANGE_500_DEG);
  mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);
    
    // Calibration
  calibrateMPU6050();
    
  // Print header
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

void calibrateMPU6050() {
    int samples = 1000;
    Serial.println("Calibrating MPU6050... Keep the sensor still");
    
    for (int i = 0; i < samples; i++) {
        sensors_event_t a, g, temp;
        mpu.getEvent(&a, &g, &temp);
        
        // Accumulate bias
        accelBias[0] += a.acceleration.x;
        accelBias[1] += a.acceleration.y;
        accelBias[2] += a.acceleration.z - 9.8; // Subtract gravity
        
        gyroBias[0] += g.gyro.x;
        gyroBias[1] += g.gyro.y;
        gyroBias[2] += g.gyro.z;
        
        delay(1);
    }
    
    // Calculate average bias
    for (int i = 0; i < 3; i++) {
        accelBias[i] /= samples;
        gyroBias[i] /= samples;
    }
    
    Serial.println("Calibration Complete");
    for (int i = 0; i < 3; i++) {
        Serial.print(accelBias[i]);
        Serial.print(" ");
    }
    for (int i = 0; i < 3; i++) {
        Serial.print(gyroBias[i]);
        Serial.print(" ");
    }
}

float movingAverage(float* buffer, float newValue) {
    // Replace oldest value with new value
    buffer[bufferIndex] = newValue;
    
    // Calculate average
    float sum = 0;
    for (int i = 0; i < FILTER_WINDOW; i++) {
        sum += buffer[i];
    }
    
    return sum / FILTER_WINDOW;
}

int count = 0;

void loop() {
  sensors_event_t a, g, temp;
  mpu.getEvent(&a, &g, &temp);

  // Read the sensor values
  int sensorValue1 = analogRead(flexSensorPin1);
  int sensorValue2 = analogRead(flexSensorPin2);
  int sensorValue3 = analogRead(flexSensorPin3);
  int sensorValue4 = analogRead(flexSensorPin4);
  int sensorValue5 = analogRead(flexSensorPin5);

  float accelX = a.acceleration.x - accelBias[0];
  float accelY = a.acceleration.y - accelBias[1];
  float accelZ = a.acceleration.z - accelBias[2] - 9.8; // Subtract gravity
    
  float gyroX = g.gyro.x - gyroBias[0];
  float gyroY = g.gyro.y - gyroBias[1];
  float gyroZ = g.gyro.z - gyroBias[2];
    
  // Apply moving average filter
  float filteredAccelX = movingAverage(accelXBuffer, accelX);
  float filteredAccelY = movingAverage(accelYBuffer, accelY);
  float filteredAccelZ = movingAverage(accelZBuffer, accelZ);
    
  float filteredGyroX = movingAverage(gyroXBuffer, gyroX);
  float filteredGyroY = movingAverage(gyroYBuffer, gyroY);
  float filteredGyroZ = movingAverage(gyroZBuffer, gyroZ);
    
  // Update buffer index (circular buffer)
  bufferIndex = (bufferIndex + 1) % FILTER_WINDOW;

  unsigned long timestamp = millis(); // Get current time in milliseconds

  // Create data string with timestamp
  String data = String(count++) + "time," +
    String(flexMapPrecise(sensorValue1, 0, maxValues[0])) + "," + 
    String(flexMapPrecise(sensorValue2, 0, maxValues[1])) + "," + 
    String(flexMapPrecise(sensorValue3, 0, maxValues[2])) + "," + 
    String(flexMapPrecise(sensorValue4, 0, maxValues[3])) + "," + 
    String(flexMapPrecise(sensorValue5, 0, maxValues[4])) + "," +
    String(filteredAccelX) + "," + String(filteredAccelY) + "," + 
    String(filteredAccelZ) + "," + String(filteredGyroX) + "," +
    String(filteredGyroY) + "," + String(filteredGyroZ) + ",1";

  Serial.println(data);
  SerialBT.println(data);

  delay(20); // Delay to limit update rate
}
