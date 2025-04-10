# ML Training Repository

This repository contains code to generate a synthetic dataset of gesture sensor readings, train machine learning models for gesture recognition, and convert the trained model into a TensorFlow Lite format for deployment in mobile (Android) projects.


## Installation

1. **Clone the Repository**

2. **Install Python Dependencies**

   Install the required packages using the provided `requirements.txt`:

   ```bash
   pip install -r ml_training/requirements.txt
   ```

## Data Generation

Before training the model, you need to create a dataset. The `data_generator.py` script generates synthetic sensor readings (for five fingers) along with a gesture label.

1. **Generate the Dataset**

   Run the data generator script and copy paste its output to a CSV file:

   ```bash
   python ml_training/data_generator.py > static_gesture_data.csv
   ```

   This will generate several sets of 500 readings for each gesture class and print the data in CSV format. You can edit the script if you want to customize the range or the number of samples.

2. **Save the Dataset**

   Ensure that the output is saved as `gesture_data.csv` in the repository root (or adjust the file path in `main.py` if needed).

## Model Training and Conversion

The `main.py` script performs both training and conversion of the model.

1. **Training the Model**

   - **Dataset Loading and Preprocessing:**  
     The script loads `gesture_data.csv`, removes any extra spaces in the column names, and separates the sensor readings (features) from the gesture labels (target).

   - **Label Encoding and Feature Scaling:**  
     The gesture labels are encoded as numbers using a `LabelEncoder`, and the sensor readings are normalized using a `MinMaxScaler`.

   - **Model Training:**  
     The dataset is split into training and testing sets. A Random Forest classifier is trained on the training data, and its accuracy is evaluated on the test set.

   - **Saving the Model Components:**  
     After training, the following components are saved:
     - Random Forest model as `gesture_random_forest.pkl`
     - Label encoder as `label_encoder.pkl`
     - Scaler as `scaler.pkl`

2. **Converting to TensorFlow Lite**

   The script then creates a neural network that serves as a neural equivalent of the Random Forest model. It trains this network on the same (scaled) dataset and converts the trained neural network to a TensorFlow Lite model using TensorFlow's TFLiteConverter.

   The converted model is saved as `gesture_model.tflite`.

3. **Run the Training and Conversion Process**

   Execute the main script to perform training and model conversion:

   ```bash
   python ml_training/main.py
   ```

## Deploying the TensorFlow Lite Model

After running the training process, a file named `gesture_model.tflite` will be generated. To deploy this model in an Android project:

1. **Locate the TensorFlow Lite Model**

   Find the `gesture_model.tflite` file in the repository directory.

2. **Copy to Android Project**

   Copy the `gesture_model.tflite` file into the `assets` folder of your Android project.

## Usage

- **Predicting Gestures:**  
  The repository includes a `predict_gesture` function in `main.py` that loads the saved Random Forest model, scaler, and label encoder to predict gestures from a given set of sensor readings. You can integrate or modify this function based on your application needs.

## Dependencies

Key Python packages used in this repository include:

- [pandas](https://pandas.pydata.org/)
- [numpy](https://numpy.org/)
- [tensorflow](https://www.tensorflow.org/)
- [keras](https://keras.io/)
- [scikit-learn](https://scikit-learn.org/)
- [joblib](https://joblib.readthedocs.io/)
- [matplotlib](https://matplotlib.org/)

