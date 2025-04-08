def convert_label(labelname):
    # Read from the sample.txt file
    with open("sample.txt", "r") as file:
        lines = file.readlines()

    # Replace the last value in each line with the new labelname
    updated_lines = []
    for line in lines:
        parts = line.strip().split(",")

        parts[-1] = labelname  # Replace the label

        parts.pop(0)
        parts.pop(0)

        print(len(parts))
        updated_lines.append(",".join(parts))


    # Write the updated content to dataset.csv
    with open("processeddata.csv", "w") as file:
        for line in updated_lines:
            file.write(line + "\n")

# Example usage:
convert_label("wave")