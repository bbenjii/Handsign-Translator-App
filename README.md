# Sign Language Translator Glove

## Overview

The **Sign Language Translator Glove** is an innovative project aimed at bridging the communication gap between Deaf or hard-of-hearing individuals and non-signers by translating sign language gestures into text and speech in real-time. The system combines wearable technology embedded with flex sensors, Bluetooth connectivity, and machine learning (ML) to facilitate intuitive and dynamic communication.

## Project Description

The glove integrates flex sensors that capture precise movements of fingers and hands. An ESP32 microcontroller processes these sensor readings, transmitting data wirelessly via Bluetooth to an Android mobile application. A machine learning model then classifies the gestures and provides real-time translation in the form of on-screen text or audible speech.

## Target Audience
- Deaf or hard-of-hearing individuals
- Families and caregivers of Deaf individuals
- Educational institutions (schools, universities)
- Healthcare facilities
- Accessibility-focused businesses
- Individuals learning sign language

## Key Features

### Hardware
- **Flex Sensors:** Detect finger movements accurately.
- **ESP32 Microcontroller:** Processes sensor data and manages Bluetooth communication.
- **Power Optimization:** Designed for an entire day's use without frequent recharging.
- **Comfort and Ergonomics:** Lightweight and adaptable for various hand sizes.

### Mobile Application
- **Real-Time Gesture Translation:** Instantly converts gestures into text or spoken language.
- **Customizable Gestures:** Users can personalize translations to specific words or phrases.
- **Gesture History:** Tracks and logs past translations for user review.
- **Learning Activities:** Interactive sessions help users learn and practice sign language.
- **Text-to-ASL Interface:** Facilitates two-way communication by translating text into ASL gestures visually.

### Machine Learning
- **High Accuracy:** Uses Random Forest classification, achieving ~85% accuracy.
- **Dynamic Gesture Recognition:** Capable of recognizing both static and dynamic gestures.
- **Adaptability:** Continuously improves recognition accuracy with user-specific data.

## Technical Components

### System Architecture
- Glove with flex sensors → ESP32 (Bluetooth Communication) → Android Application → ML Model → Text/Speech Output

### Software
- **Bluetooth Module:** Manages real-time data streaming between glove and mobile app.
- **Gesture Controller:** Ensures gesture stability and processes data before classification.
- **ML Module:** Employs TensorFlow Lite for on-device inference without internet dependency.
- **User Interface:** Built with Android Studio, ensuring a responsive and user-friendly experience.

## Installation and Usage

### Requirements
- Android smartphone
- ESP32 microcontroller
- Flex sensors (5x)
- Arduino IDE for ESP32 programming
- Android Studio for application installation

### Setup
1. Clone the repository.
2. Connect and program ESP32 with the provided Arduino sketch.
3. Deploy Android application via Android Studio.
4. Pair the glove via Bluetooth in the application.

## Simulation and Testing
Simulations comparing different classification methods (basic heuristics, Decision Tree, Random Forest) confirmed Random Forest as the optimal model. Simulated accuracy:
- Basic heuristic: 48%
- Decision Tree: 66%
- Random Forest: 85%

## Project Status
The project has successfully completed multiple sprints, including full integration tests, robust hardware and software implementations, and comprehensive end-user documentation.

## Future Enhancements
- Two-handed gesture recognition
- Real-time speech-to-gesture generation
- Haptic feedback integration
- Expanded dataset for improved accuracy across different users

## Contribution
Contributions are welcome! Please open issues for bug reports or feature requests and submit pull requests for proposed improvements.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

This project was developed as part of the COEN 390 Product Design Project course at Concordia University during Winter 2025.

