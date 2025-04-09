import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

import joblib
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, MinMaxScaler
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score

import tensorflow as tf
import keras
from keras import layers

import os

def convert_to_tflite():
    """
    Converts the trained Random Forest model into a TensorFlow Lite model
    by first converting it into a neural network equivalent.
    """
    # Load the trained Random Forest model, scaler, and label encoder
    rf_model = joblib.load("model_files/gesture_random_forest.pkl")
    scaler = joblib.load("model_files/scaler.pkl")
    label_encoder = joblib.load("model_files/label_encoder.pkl")
    print(label_encoder.classes_)

    # Generate synthetic input data for defining the NN model
    num_features = 5  # Assuming THUMB, INDEX, MIDDLE, RING, LITTLE
    num_classes = len(label_encoder.classes_)  # Number of unique gestures

    # Create a Neural Network equivalent of the Random Forest Model
    nn_model = keras.Sequential([
        layers.Input(shape=(num_features,)),
        layers.Dense(64, activation="relu"),
        layers.Dense(32, activation="relu"),
        layers.Dense(num_classes, activation="softmax")
    ])

    # Compile the model
    nn_model.compile(optimizer="adam", loss="sparse_categorical_crossentropy", metrics=["accuracy"])

    # Train the neural network using the same dataset
    file_path = "data_processing/dataset.csv"
    df = pd.read_csv(file_path)
    df.columns = df.columns.str.strip()

    X = df.drop(columns=["LABEL"])  # Features
    y = df["LABEL"]  # Labels

    y_encoded = label_encoder.transform(y)  # Convert labels to numerical values
    X_scaled = scaler.transform(X)  # Normalize features

    # Train the neural network to mimic the RF model
    nn_model.fit(X_scaled, y_encoded, epochs=50, batch_size=8, verbose=1)

    # Convert to TensorFlow Lite
    converter = tf.lite.TFLiteConverter.from_keras_model(nn_model)
    tflite_model = converter.convert()

    # Save the TensorFlow Lite model
    tflite_model_path = "model_files/gesture_model.tflite"
    with open(tflite_model_path, "wb") as f:
        f.write(tflite_model)

    print(f"TensorFlow Lite model saved as {tflite_model_path}")

def trainModel():
    print(os.getcwd())

    # Load dataset
    file_path = "data_processing/dataset.csv"  # Change path if needed
    df = pd.read_csv(file_path)


    # Remove leading spaces in column names (if any)
    df.columns = df.columns.str.strip()

    # Separate features (sensor readings) and labels (gestures)
    X = df.drop(columns=["LABEL"])  # Features: THUMB, INDEX, MIDDLE, RING, LITTLE
    y = df["LABEL"]  # Target: Gesture label

    # Encode gesture labels as numbers (e.g., "1" -> 0, "2" -> 1, etc.)
    label_encoder = LabelEncoder()
    y_encoded = label_encoder.fit_transform(y)

    # Normalize sensor readings (scale between 0 and 1)
    scaler = MinMaxScaler()
    X_scaled = scaler.fit_transform(X)
    # Split data into training (80%) and testing (20%) sets
    X_train, X_test, y_train, y_test = train_test_split(X_scaled, y_encoded, test_size=0.2, random_state=42)

    # Train Random Forest Classifier
    rf_model = RandomForestClassifier(n_estimators=100, random_state=42)
    rf_model.fit(X_train, y_train)

    # Predict on test data
    y_pred = rf_model.predict(X_test)

    # Evaluate accuracy
    accuracy = accuracy_score(y_test, y_pred)
    print(f"Random Forest Model Accuracy: {accuracy * 100:.2f}%")

    # Save trained model and label encoder
    joblib.dump(rf_model, "model_files/gesture_random_forest.pkl")
    joblib.dump(label_encoder, "model_files/label_encoder.pkl")
    joblib.dump(scaler, "model_files/scaler.pkl")

    print("Model saved as gesture_random_forest.pkl")


def predict_gesture(current_flex_readings):
    """
    Predicts the gesture based on current flex sensor readings.

    Args:
        current_flex_readings (list): A list of 4 flex sensor values [THUMB, INDEX, MIDDLE, RING]

    Returns:
        str: The predicted gesture label.
    """

    # Load saved model, label encoder, and scaler
    rf_model = joblib.load("model_files/gesture_random_forest.pkl")
    label_encoder = joblib.load("model_files/label_encoder.pkl")
    scaler = joblib.load("model_files/scaler.pkl")

    # Convert input to a NumPy array and reshape for model
    flex_array = np.array(current_flex_readings).reshape(1, -1)

    # Normalize input using the same scaler used during training
    flex_array_scaled = scaler.transform(flex_array)

    # Predict using the trained Random Forest model
    prediction_encoded = rf_model.predict(flex_array_scaled)

    # Decode numeric prediction back to the original gesture label
    predicted_label = label_encoder.inverse_transform(prediction_encoded)[0]

    return predicted_label

def plot_diagram():

    df = pd.read_csv('static_gesture_data.csv')
    df.plot()
    plt.show()

# plot_diagram()


# print(label_encoder.classes_)
# trainModel()

if __name__ == '__main__':
    # First train model
    trainModel()

    #then convert to tflite
    convert_to_tflite()

    # sample_reading = [0, 0, 0, 0, 0]  # Replace with real sensor values
    # predicted_gesture = predict_gesture(sample_reading)
    # print(f"Predicted Gesture: {predicted_gesture}")