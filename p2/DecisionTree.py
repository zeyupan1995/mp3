from sklearn import tree
import numpy as np
import time
import scipy.spatial as sp
import heapq as hq

# Perceptron classifier

class DecisionTree():

    def __init__(self):
        self.__train_image_list = []
        self.__train_label_list = []
        self.__test_image_list = []
        self.__test_label_list = []

        self.__test_accurate_number = 0
        self.__confusion_matrix = [[0 for col in range(10)] for row in range(10)]

    def data_reader(self, path, mode):
        count = 0
        image = []
        with open(path, "r") as f:
            for line in f:
                line = line.strip('\n')
                count += 1
                if count < 33:
                    image += list(line)
                if count == 33:
                    if mode == "train":
                        self.__train_image_list.append(np.array(image, dtype=float))
                        self.__train_label_list.append(int(line))
                    elif mode == "test":
                        self.__test_image_list.append(np.array(image, dtype=float))
                        self.__test_label_list.append(int(line))
                    image = []
                    count = 0
        f.close()

    def train(self):

        self.__clf = tree.DecisionTreeClassifier()
        self.__clf = self.__clf.fit(self.__train_image_list, self.__train_label_list)

    def test(self):

        for i in range(0, len(self.__test_label_list)):
            expected = self.__test_label_list[i]
            predicted = self.__clf.predict(self.__test_image_list[i].reshape(1, len(self.__test_image_list[i])))

            if expected == predicted:
                self.__test_accurate_number += 1

            self.__confusion_matrix[predicted][expected] += 1


    def calculate_accuracy(self):

        print "Overall Accuracy:"
        print float(self.__test_accurate_number) / len(self.__test_label_list)

        print "Confusion Matrix:"
        print self.__confusion_matrix

# train data

classifier = DecisionTree()
classifier.data_reader("digitdata/optdigits-orig_train.txt", "train")
classifier.data_reader("digitdata/optdigits-orig_test.txt", "test")
classifier.train()
classifier.test()
classifier.calculate_accuracy()