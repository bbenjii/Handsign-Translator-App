import pandas as pd
import numpy as np


def convert_csv_to_npz():
    # Load the CSV file containing the time-series data
    df = pd.read_csv("dynamic_gesture_dataset_generated.csv")

    # df = pd.read_csv("dynamic_gesture_dataset.csv")

    # For example, assume the CSV has the following columns:
    # sample_id,time,sensor1,sensor2, ..., sensor11, label

    # Group the data by sample_id so that each group represents one gesture sample.
    groups = df.groupby(["label", "sample_id"])

    print(f"Samples count: {len(groups)}")

    sequences = []
    labels = []

    # # Determine the sensor columns (adjust this list as needed)
    sensor_cols = [col for col in df.columns if col.startswith("sensor")]
    print(f"Sensor columns: {sensor_cols}")


    for label, group in groups:
        print(f"Processing label: {label}")
        # print(f"Processing \n{group}")
        # # Sort the group by time (if the CSV isn't already ordered)
        group = group.sort_values(by="timestamp")
        # # Convert the sensor readings for this sample to a 2D NumPy array of shape (time_steps, num_features)
        sequence = group[sensor_cols].to_numpy()
        sequences.append(sequence)
        # # Assume that the label is the same for all rows of a given sample; pick the first one.
        labels.append(group["label"].iloc[0])

    # # Convert lists to NumPy arrays.
    # # X will have shape (num_samples, time_steps, num_features)
    X = np.array(sequences)
    # # y will be a 1D array of labels with length equal to num_samples.
    y = np.array(labels)
    #
    # # Save the arrays into an NPZ file for easier loading later.
    np.savez("gesture_sequence_data.npz", X=X, y=y)
    print("Data saved in gesture_sequence_data.npz")


def synthetic_data():
    # Generating a synthetic CSV file for gesture training data

    import math
    import numpy as np
    import pandas as pd

    # Settings
    num_samples_per_gesture = 100  # 100 samples per gesture
    time_steps = 50  # Number of time steps per sample
    dt = 0.1  # Time increment in seconds

    gestures = ['static 1', 'static 5', 'dynamic no', 'dynamic wave']
    rows = []

    for gesture in gestures:
        for s in range(num_samples_per_gesture):
            # Create a unique sample_id (e.g., static_1_1, static_1_2, etc.)
            sample_id = f"{gesture.replace(' ', '_')}_{s + 1}"
            for t in range(time_steps):
                timestamp = t * dt
                # Generate flex sensor readings (sensors 1-5)
                if gesture == 'static 1':
                    flex_values = 30 + np.random.normal(0, 0.5, 5)
                elif gesture == 'static 5':
                    flex_values = 70 + np.random.normal(0, 0.5, 5)
                elif gesture == 'dynamic no':
                    # Simulate a linear change from 20 to 40 plus noise
                    flex_values = 20 + (20 * t / time_steps) + np.random.normal(0, 1, 5)
                elif gesture == 'dynamic wave':
                    # Simulate a sine wave pattern around 50
                    flex_values = 50 + 10 * np.sin(2 * math.pi * t / time_steps) + np.random.normal(0, 1, 5)

                # Generate IMU sensor readings (sensors 6-11)
                if gesture.startswith('static'):
                    imu_values = np.random.normal(0, 0.2, 6)
                elif gesture == 'dynamic no':
                    # For "dynamic no", simulate a step change halfway through the sample
                    if t < time_steps / 2:
                        imu_values = np.random.normal(0, 0.5, 6)
                    else:
                        imu_values = 5 + np.random.normal(0, 0.5, 6)
                elif gesture == 'dynamic wave':
                    imu_values = 2 * np.sin(2 * math.pi * t / time_steps) + np.random.normal(0, 0.3, 6)

                # Combine sensor values
                sensor_values = np.concatenate([flex_values, imu_values])

                # Build the row dictionary
                row = {
                    "sample_id": sample_id,
                    "timestamp": timestamp,
                    "label": gesture
                }
                # Add sensor1 to sensor11
                for i, value in enumerate(sensor_values, start=1):
                    row[f"sensor{i}"] = value
                rows.append(row)

    # Create DataFrame and save to CSV
    df = pd.DataFrame(rows)
    output_filename = "dynamic_gesture_dataset_generated.csv"
    df.to_csv(output_filename, index=False)
    print("Generated CSV file with shape:", df.shape)
    print("CSV file saved as", output_filename)

# synthetic_data()