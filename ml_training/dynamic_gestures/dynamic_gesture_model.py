import numpy as np
import joblib
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, MinMaxScaler
import tensorflow as tf
from keras import layers, models
from test_gesture import mock_gesture
from convert_csv_to_npz import convert_csv_to_npz
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
    print("Label encoding order:", label_encoder.classes_)  # 👈 This line shows the mapping

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
    Converts the trained GRU model into a TensorFlow Lite model with adjusted
    converter settings to handle tensor list ops.
    """
    import joblib
    import tensorflow as tf

    # Load the trained GRU model, scaler, and label encoder
    gru_model = tf.keras.models.load_model("gesture_gru_model.h5")
    scaler = joblib.load("scaler_gru.pkl")
    label_encoder = joblib.load("label_encoder_gru.pkl")
    print("Loaded GRU model, scaler, and label encoder.")
    print("Gesture classes:", label_encoder.classes_)

    # Set up the TFLite converter
    converter = tf.lite.TFLiteConverter.from_keras_model(gru_model)
    # Enable select TensorFlow ops for unsupported operations and disable lowering tensor list ops.
    converter.target_spec.supported_ops = [
        tf.lite.OpsSet.TFLITE_BUILTINS,
        tf.lite.OpsSet.SELECT_TF_OPS
    ]
    converter._experimental_lower_tensor_list_ops = False
    converter.experimental_enable_resource_variables = True

    # Convert the model to TFLite format
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
        sequence_data (list of lists): Each inner list represents sensor readings for a time step.
            Example:
            [
                [38, 18, 134, 67, 180, -2.58, -1.77, 4.54, 0.37, -0.39, -0.04],
                [28, 10, 116, 36, 180, -2.61, -1.86, 4.65, 0.38, -0.35, -0.06],
                ...,
                [59, 20, 128, 72, 180, -2.55, -1.92, 4.82, 0.37, -0.33, -0.06]
            ]
            Each inner array is one time step. The data is assumed to be already ordered.
            (Optionally, you can add the time step index as an extra feature.)

    Returns:
        str: The predicted gesture label.
    """
    import numpy as np
    import joblib
    import tensorflow as tf

    # Load the trained GRU model, scaler, and label encoder
    gru_model = tf.keras.models.load_model("gesture_gru_model.h5")
    scaler = joblib.load("scaler_gru.pkl")
    label_encoder = joblib.load("label_encoder_gru.pkl")

    # Convert the input (list of lists) to a NumPy array.
    sequence_array = np.array(sequence_data)

    # Optionally, if you want to add the time step index as an extra feature,
    # uncomment the next two lines. This will add a new first column with indices.
    # time_indices = np.arange(sequence_array.shape[0]).reshape(-1, 1)
    # sequence_array = np.hstack((time_indices, sequence_array))

    # Check that we have a 2D array (time_steps, num_features)
    if sequence_array.ndim != 2:
        print("Error: Input should be a 2D array where each row is a time step.")
        return None

    # Add a batch dimension: resulting shape (1, time_steps, num_features)
    sequence_array = sequence_array.reshape(1, *sequence_array.shape)

    # Normalize the input: reshape to 2D, scale, then reshape back to 3D.
    time_steps, num_features = sequence_array.shape[1:]
    seq_reshaped = sequence_array.reshape(-1, num_features)
    seq_scaled = scaler.transform(seq_reshaped)
    seq_scaled = seq_scaled.reshape(1, time_steps, num_features)

    # Predict gesture probabilities and choose the most likely class.
    prediction_probs = gru_model.predict(seq_scaled)
    prediction_class = np.argmax(prediction_probs, axis=1)
    predicted_label = label_encoder.inverse_transform(prediction_class)[0]

    return predicted_label


if __name__ == "__main__":
    # convert csv o npz
    # convert_csv_to_npz()
    # First, train the GRU model on your dynamic gesture dataset
    # trainModel()

    # Convert the trained model to TensorFlow Lite format for mobile deployment
    # convert_to_tflite()

    # # Example prediction:
    # # Create a dummy sequence with shape (time_steps, num_features).
    # # Replace this dummy data with actual sensor readings.
    # dummy_sequence = np.random.rand(100, 11)  # e.g., 100 time steps, 11 features
    predicted = predict_gesture(mock_gesture)
    print(f"Predicted Gesture: {predicted}")
