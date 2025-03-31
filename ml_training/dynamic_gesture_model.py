import numpy as np
import joblib
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, MinMaxScaler
import tensorflow as tf
from keras import layers, models

def trainModel():
    """
    Trains a GRU-based model for dynamic gesture recognition using time series data.
    Assumes data is stored in 'gesture_sequence_data.npz' with arrays 'X' and 'y'.
    """
    # Load dataset from NPZ file
    data = np.load("gesture_sequence_data.npz")
    X = data["X"]  # Shape: (num_samples, time_steps, num_features)
    y = data["y"]  # Gesture labels

    num_samples, time_steps, num_features = X.shape
    print(f"Loaded data: {num_samples} samples, {time_steps} time steps, {num_features} features per time step.")

    # Encode gesture labels to numerical values
    label_encoder = LabelEncoder()
    y_encoded = label_encoder.fit_transform(y)
    num_classes = len(label_encoder.classes_)

    # Normalize sensor readings: reshape to 2D, scale, then reshape back
    X_reshaped = X.reshape(-1, num_features)
    scaler = MinMaxScaler()
    X_scaled = scaler.fit_transform(X_reshaped)
    X_scaled = X_scaled.reshape(num_samples, time_steps, num_features)

    # Split the data into training and testing sets
    X_train, X_test, y_train, y_test = train_test_split(X_scaled, y_encoded, test_size=0.2, random_state=42)

    # Build the GRU-based model
    gru_model = models.Sequential([
        layers.Input(shape=(time_steps, num_features)),
        layers.GRU(64, return_sequences=True),
        layers.GRU(32),
        layers.Dense(32, activation="relu"),
        layers.Dense(num_classes, activation="softmax")
    ])

    gru_model.compile(optimizer="adam",
                      loss="sparse_categorical_crossentropy",
                      metrics=["accuracy"])
    gru_model.summary()

    # Train the model
    gru_model.fit(X_train, y_train, epochs=50, batch_size=8, verbose=1)

    # Evaluate on the test set
    loss, accuracy = gru_model.evaluate(X_test, y_test, verbose=0)
    print(f"GRU Model Test Accuracy: {accuracy * 100:.2f}%")

    # Save the trained model, scaler, and label encoder for later use
    gru_model.save("gesture_gru_model.h5")
    joblib.dump(scaler, "scaler_gru.pkl")
    joblib.dump(label_encoder, "label_encoder_gru.pkl")
    print("GRU model saved as gesture_gru_model.h5, scaler and label encoder saved.")


def convert_to_tflite():
    """
    Converts the trained GRU model into a TensorFlow Lite model.
    """
    # Load the trained GRU model, scaler, and label encoder
    gru_model = tf.keras.models.load_model("gesture_gru_model.h5")
    scaler = joblib.load("scaler_gru.pkl")
    label_encoder = joblib.load("label_encoder_gru.pkl")
    print("Loaded GRU model, scaler, and label encoder.")
    print("Gesture classes:", label_encoder.classes_)

    # Convert the Keras model to a TensorFlow Lite model
    converter = tf.lite.TFLiteConverter.from_keras_model(gru_model)
    tflite_model = converter.convert()

    # Save the TensorFlow Lite model
    tflite_model_path = "gesture_gru_model.tflite"
    with open(tflite_model_path, "wb") as f:
        f.write(tflite_model)
    print(f"TensorFlow Lite model saved as {tflite_model_path}")


def predict_gesture(sequence_data):
    """
    Predicts the gesture based on a sequence of sensor readings.

    Args:
        sequence_data (list or np.array): A 2D array with shape (time_steps, num_features)
            representing sensor readings over time.

    Returns:
        str: The predicted gesture label.
    """
    # Load the trained GRU model, scaler, and label encoder
    gru_model = tf.keras.models.load_model("gesture_gru_model.h5")
    scaler = joblib.load("scaler_gru.pkl")
    label_encoder = joblib.load("label_encoder_gru.pkl")

    # Ensure the input is a NumPy array and reshape to match model's expected shape
    sequence_array = np.array(sequence_data)
    if len(sequence_array.shape) == 2:
        # Add batch dimension: (1, time_steps, num_features)
        sequence_array = sequence_array.reshape(1, *sequence_array.shape)
    else:
        print("Error: Input should be a 2D array with shape (time_steps, num_features).")
        return None

    # Normalize the input: reshape to 2D for scaling then back to 3D
    time_steps, num_features = sequence_array.shape[1:]
    seq_reshaped = sequence_array.reshape(-1, num_features)
    seq_scaled = scaler.transform(seq_reshaped)
    seq_scaled = seq_scaled.reshape(1, time_steps, num_features)

    # Predict gesture probabilities and determine the predicted class
    prediction_probs = gru_model.predict(seq_scaled)
    prediction_class = np.argmax(prediction_probs, axis=1)
    predicted_label = label_encoder.inverse_transform(prediction_class)[0]

    return predicted_label


if __name__ == "__main__":
    # First, train the GRU model on your dynamic gesture dataset
    trainModel()

    # Convert the trained model to TensorFlow Lite format for mobile deployment
    convert_to_tflite()

    # Example prediction:
    # Create a dummy sequence with shape (time_steps, num_features).
    # Replace this dummy data with actual sensor readings.
    dummy_sequence = np.random.rand(100, 11)  # e.g., 100 time steps, 11 features
    predicted = predict_gesture(dummy_sequence)
    print(f"Predicted Gesture: {predicted}")
