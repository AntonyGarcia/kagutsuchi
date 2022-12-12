import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

def loadDataset(fileName, samples):
    x = []
    y = []
    train_data = pd.read_csv(fileName, header=None)
    y = np.array(train_data.iloc[0:samples, 1])
    x = np.array(train_data.iloc[0:samples, 2:])
    return x, y


x, y = loadDataset("../../datasets/test.csv", 30)
fig = plt.figure(figsize=(2, 5))

for i in range(len(x)):
    digit = x[i]
    digit_pixels = digit.reshape(256, 256)
    fig.add_subplot(3,10, i + 1)
    plt.axis('off')
    plt.imshow(digit_pixels)
plt.show()
