import pandas as pd
import numpy as np
import time as time

from scipy.integrate._test_odeint_banded import __test_odeint_banded_error
from sklearn import svm
from sklearn.ensemble import RandomForestClassifier

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
    clf = RandomForestClassifier()  # Classifier object
    startTrainingTime = time.time()
    clf.fit(train_x, train_y)  # Training of a model by fitting training data to object
    endTrainingTime = time.time()
    trainingTime = endTrainingTime - startTrainingTime  # Training time calculation

    validResults = 0
    startTestingTime = time.time()
    for i in range(len(test_y)):  # A for loop to evaluate result vs expected results
        expectedResult = int(test_y[int(i)])  # Load expected result from testing dataset
        result = int(clf.predict(test_x[int(i)].reshape(1, len(test_x[int(i)]))))  # Calculate a result
        outcome = "Fail"
        if result == expectedResult:
            validResults = validResults + 1  # Counting valid results
            outcome = " OK "
        print("Nº ", i + 1, " | Expected result: ", expectedResult, " | Obtained result: ", result, " | ", outcome,
              " | Accuracy: ", round((validResults / (i + 1)) * 100, 2),
              "%")  # Printing the results for each label in testing dataset

    endTestingTime = time.time()
    testingTime = endTestingTime - startTestingTime  # Calculation of testing time

    print("-------------------------------")
    print("Results")
    print("-------------------------------")
    print("Training samples: ", trainingSamples)
    print("Training time: ", round(trainingTime, 2), " s")
    print("Testing samples: ", testingSamples)
    print("Testing time: ", round(testingTime, 2), " s")
    print("Testing accuracy: ", round((validResults / testingSamples) * 100, 2), "%")


if __name__ == "__main__":
    main()
