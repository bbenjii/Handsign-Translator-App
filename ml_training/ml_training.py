import pandas as pd
import numpy as np
import tensorflow as tf
import keras
from keras import layers
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, MinMaxScaler

# Class that handles training the hand gesture recognition neural network
class HandGestureMLTraining:

    @staticmethod
    def train_NN_model(filepath: str):
        # Load the dataset from a CSV file
        file_path = "gesture_data.csv"  # Update this path if needed
        df = pd.read_csv(file_path)
        # Remove any extra spaces from column names
        df.columns = df.columns.str.strip()

        # Separate features (sensor readings) and labels (gesture names)
        X = df.drop(columns=["LABEL"])  # Features, e.g., THUMB, INDEX, MIDDLE, RING, LITTLE
        y = df["LABEL"]  # Target variable: gesture label

        # Encode gesture labels to numeric values
        label_encoder = LabelEncoder()
        y_encoded = label_encoder.fit_transform(y)

        # Normalize sensor readings to a [0,1] range
        scaler = MinMaxScaler()
        X_scaled = scaler.fit_transform(X)

        # Split data into training and testing sets (90% training, 10% testing)
        X_train, X_test, y_train, y_test = train_test_split(X_scaled, y_encoded, test_size=0.1, random_state=42)

        # Build a simple Neural Network model using Keras
        model = keras.Sequential([
            # First dense layer with 64 neurons and ReLU activation; input_shape based on number of features
            layers.Dense(64, activation="relu", input_shape=(X_train.shape[1],)),
            # Second dense layer with 32 neurons and ReLU activation
            layers.Dense(32, activation="relu"),
            # Output layer with softmax activation; number of neurons equals number of gesture classes
            layers.Dense(len(label_encoder.classes_), activation="softmax")
        ])

        # Compile the model with Adam optimizer and sparse categorical crossentropy loss
        model.compile(optimizer="adam", loss="sparse_categorical_crossentropy", metrics=["accuracy"])

        # Train the model for 100 epochs with a batch size of 8, using a validation split from test data
        model.fit(X_train, y_train, epochs=100, batch_size=8, validation_data=(X_test, y_test))

        # Evaluate the model on the test set and print the test accuracy
        test_loss, test_acc = model.evaluate(X_test, y_test)
        print(f"Neural Network Test Accuracy: {test_acc * 100:.2f}%")

        # Save the trained Keras model to an .h5 file
        model.save("gesture_nn_model.h5")
        # Save the class labels used for encoding
        np.save("label_classes.npy", label_encoder.classes_)
        # Save the normalization parameters from the scaler (here using data_max_ as an example)
        np.save("scaler.npy", scaler.data_max_)

        print("Neural Network model saved as gesture_nn_model.h5")

    @staticmethod
    def convert_to_tfLite():
        # Load the trained Keras model from the file
        model = keras.models.load_model("gesture_nn_model.h5")

        # Create a TFLiteConverter instance from the Keras model and convert it
        converter = tf.lite.TFLiteConverter.from_keras_model(model)
        tflite_model = converter.convert()

        # Save the TensorFlow Lite model to a file
        with open("gesture_model.tflite", "wb") as f:
            f.write(tflite_model)

        print("TensorFlow Lite model saved as gesture_model.tflite")

# Main execution block to train the model and convert it to TensorFlow Lite
if __name__ == '__main__':
    def main():
        # Specify the file path for the training data (if needed, can be updated)
        filepath = 'gesture_data.csv'
        # Train the neural network model and generate necessary training files
        HandGestureMLTraining.train_NN_model(filepath)
        # Convert the trained model to TensorFlow Lite format
        HandGestureMLTraining.convert_to_tfLite()

    main()