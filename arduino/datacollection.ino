#include <Arduino.h>

const int flexSensorPin1 = 36; // thumb
const int flexSensorPin2 = 39; // index
const int flexSensorPin3 = 34; // middle finger  
const int flexSensorPin4 = 35; // ring finger
const int flexSensorPin5 = 32; // small finger

// Calibration values
const int minValues[5] = {0, 0, 0, 0, 0};     
const int maxValues[5] = {675, 800, 750, 120, 430};

// Maps the sensor reading to an angle between 0 and 180.
int flexMapPrecise(int sensorValue, int minVal, int maxVal) {
  int constrainedValue = constrain(sensorValue, minVal, maxVal);
  return map(constrainedValue, minVal, maxVal, 0, 180);
}

int sample_id = 0;

void printData(int sampleId, int count, int sensorValue1, int sensorValue2, int sensorValue3, int sensorValue4, int sensorValue5) {
  String data = String(sampleId) + "," + 
                String(count) + "," + 
                String(sensorValue1) + "," + 
                String(sensorValue2) + "," + 
                String(sensorValue3) + "," + 
                String(sensorValue4) + "," + 
                String(sensorValue5)+",myGesture";
  Serial.println(data);
}

void collectSampleData() {
  sample_id++;
  // Collect 100 samples without any countdown.
  for (int count = 0; count < 100; count++) {
    int sensorValue1 = analogRead(flexSensorPin1);
    int sensorValue2 = analogRead(flexSensorPin2);
    int sensorValue3 = analogRead(flexSensorPin3);
    int sensorValue4 = analogRead(flexSensorPin4);
    int sensorValue5 = analogRead(flexSensorPin5);

    int angle1 = flexMapPrecise(sensorValue1, minValues[0], maxValues[0]);
    int angle2 = flexMapPrecise(sensorValue2, minValues[1], maxValues[1]);
    int angle3 = flexMapPrecise(sensorValue3, minValues[2], maxValues[2]);
    int angle4 = flexMapPrecise(sensorValue4, minValues[3], maxValues[3]);
    int angle5 = flexMapPrecise(sensorValue5, minValues[4], maxValues[4]);

    printData(sample_id, count, angle1, angle2, angle3, angle4, angle5);
  }
}

void setup() {
  Serial.begin(115200);
  
  // Configure flex sensor pins as input.
  pinMode(flexSensorPin1, INPUT);
  pinMode(flexSensorPin2, INPUT);
  pinMode(flexSensorPin3, INPUT);
  pinMode(flexSensorPin4, INPUT);
  pinMode(flexSensorPin5, INPUT);
}

void loop() {
  collectSampleData();
  delay(1000);
}
