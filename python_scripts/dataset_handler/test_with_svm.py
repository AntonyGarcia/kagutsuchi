
import pandas as pd
import numpy as np
import time as time
from sklearn.svm import SVC

test_path = "../../datasets/test.csv"
train_path = "../../datasets/train.csv"

trainingSamples = 0
testingSamples = 0

"""
Here I set the global variables, which
will be used to test the computing time
for both training and testing
"""

startTrainingTime = 0
endTrainingTime = 0
trainingTime = 0

startTestingTime = 0
endTestingTime = 0
testingTime = 0
accuracy=0


def loadDataset(fileName):  # A function for loading the data from a dataset
    x = []  # Array for data inputs
    y = []  # Array for labels (expected outputs)
    train_data = pd.read_csv(fileName, header=None)  # Data has to be stored in a CSV file, separated by commas
    y = np.array(train_data.iloc[0:, 1])  # Labels column
    x = np.array(train_data.iloc[0:, 2:]) / 255  # Division by 255 is used for data normalization
    dataset_size = len(train_data)
    return x, y, dataset_size


def main():
    train_x, train_y, trainingSamples = loadDataset(train_path)  # Loading training data
    test_x, test_y, testingSamples = loadDataset(test_path)  # Loading testing data
    clf = SVC()  # Classifier object
    print("Start training")
    startTrainingTime = time.time()
    clf.fit(train_x, train_y)  # Training of a model by fitting training data to object
    endTrainingTime = time.time()
    trainingTime = endTrainingTime - startTrainingTime  # Training time calculation
    print(trainingTime)

    startTestingTime = time.time()
    accuracy = clf.score(test_x, test_y)
    endTestingTime = time.time()

    testingTime = endTestingTime-startTestingTime

    print("-------------------------------")
    print("Results")
    print("-------------------------------")
    print("Training samples: ", trainingSamples)
    print("Training time: ", round(trainingTime, 2), " s")
    print("Testing samples: ", testingSamples)
    print("Testing time: ", round(testingTime, 2), " s")
    print("Testing accuracy: ", accuracy)

if __name__ == "__main__":
    main()
