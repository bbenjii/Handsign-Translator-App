package com.example.handsign_translator_app.bluetooth;

import java.util.Random;

public class BluetoothModule {

    private int min = 0, max = 10;
    private int finger1_raw_data;
    private int finger2_raw_data;
    private int finger3_raw_data;
    private int finger4_raw_data;
    private int finger5_raw_data;

    public BluetoothModule() {
    }

    public int[] getGloveData() {
        int min = 0, max = 10;
        finger1_raw_data = randomNumber(0, 20);
        finger2_raw_data = randomNumber(0, 20);
        finger3_raw_data = randomNumber(0, 20);
        finger4_raw_data = randomNumber(150, 180);
        finger5_raw_data = randomNumber(150, 180);

//        finger1_raw_data = randomNumber(0, 180);
//        finger2_raw_data = randomNumber(0, 180);
//        finger3_raw_data = randomNumber(0, 180);
//        finger4_raw_data = randomNumber(0, 180);
//        finger5_raw_data = randomNumber(0, 180);

        int[] flexReadings = {finger1_raw_data, finger2_raw_data, finger3_raw_data, finger4_raw_data, finger5_raw_data};

        return flexReadings;
    }

    private static int randomNumber(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    private static void printFlexReadings(int[] readings) {
        StringBuilder readingString = new StringBuilder();
        for (int i = 0; i < readings.length; i++) {
            readingString.append("Flex").append(i + 1).append(": ").append(readings[i]).append("\t");
        }
        System.out.println(readingString);
    }


}
