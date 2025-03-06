package com.example.handsign_translator_app.bluetooth;

import java.util.Random;

public class BluetoothModule {

    public BluetoothModule() {
    }

    public int[] getGloveData() {
        int min = 0, max = 10;
        int finger1_raw_data = randomNumber(0, 20);
        int finger2_raw_data = randomNumber(0, 20);
        int finger3_raw_data = randomNumber(0, 20);
        int finger4_raw_data = randomNumber(150, 180);
        int finger5_raw_data = randomNumber(150, 180);
//        int finger1_raw_data = randomNumber(0, 180);
//        int finger2_raw_data = randomNumber(0, 180);
//        int finger3_raw_data = randomNumber(0, 180);
//        int finger4_raw_data = randomNumber(0, 180);
//        int finger5_raw_data = randomNumber(0, 180);

        int[]  flexReadings = {finger1_raw_data, finger2_raw_data, finger3_raw_data, finger4_raw_data, finger5_raw_data};

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
