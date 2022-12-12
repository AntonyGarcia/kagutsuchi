import os
import cupy as cp
from PIL import Image
import pandas as pd

csv_array = []  # Array to save all pixels (global array)
index = 0

for pictures in os.scandir("../../images/exported_images/test_dataset"):  # Read al images in this path
    pixel_array = []  # Array to save all pixels from image in a row
    original_image = Image.open(pictures.path)  # Open the image
    pixels = original_image.load()  # Load pixels from image in a 2D array
    index = index + 1
    label = 0
    if pictures.path.find("_pos.jpg") != -1:
        label = 1
    pixel_array.append(label)

    for i in range(256):
        for j in range(256):
            p = int(pixels[j, i])  # Load each pixel in the 2D array
            pixel_array.append(p)  # Append each pixel to the row array

    csv_array.append(pixel_array)  # Append each row array to the global array
    
csv_array =cp.asarray(csv_array)  # Convert array to numpy array
#cp.random.shuffle(csv_array)
pd.DataFrame(csv_array.get()).to_csv("../../datasets/test.csv", header=None)  # Save pixels in a file
