import uuid

def process_gesture_file(input_filename, gesture_label, output_filename):
    with open(input_filename, 'r') as file:
        lines = file.readlines()

    cleaned_lines = []
    current_sample_uuid = str(uuid.uuid4())
    last_sample_id = None

    for line in lines:
        line = line.strip()

        # Skip lines that aren't data
        if not line or any(key in line.lower() for key in ["done", "countdown", "3...2...1"]):
            continue

        # Get the sample number (first value before comma)
        split_line = line.split(',')
        if not split_line[0].isdigit():
            continue

        sample_id = int(split_line[0])
        if last_sample_id is None or sample_id != last_sample_id:
            current_sample_uuid = str(uuid.uuid4())
            last_sample_id = sample_id

        # Replace gestureLabel with given gesture_label
        if split_line[-1].strip().lower() == "gesturelabel":
            split_line[-1] = gesture_label

        # Add UUID prefix to sample number
        split_line[0] = f"{current_sample_uuid}_{split_line[0]}"

        cleaned_lines.append(','.join(split_line))

    # Save the processed lines to a new file
    with open(output_filename, 'w') as out_file:
        for line in cleaned_lines:
            out_file.write(f"{line}\n")

    print(f"Processed data saved to: {output_filename}")

process_gesture_file("sample.txt", "dynamic_wave", "processed_sample.txt")