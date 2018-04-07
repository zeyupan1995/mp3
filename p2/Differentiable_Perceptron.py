#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Time    : 4/5/18 4:53 PM
# @Author  : Hanfei Lin
# @Site    : 
# @File    : Differentiable_Perceptron.py
# @Software: PyCharm Community Edition

import numpy as np
import math
from matplotlib import mpl
from matplotlib import pyplot as plt

# Perceptron classifier

class Perceptron():

    def __init__(self, weight_mode, bias_mode, order, round, learning_rate_decay_function):
        self.__train_image_list = []
        self.__train_label_list = []
        self.__test_image_list = []
        self.__test_label_list = []
        self.__weight = []
        self.__bias = 1
        self.__weight_mode = weight_mode
        self.__bias_mode = bias_mode
        self.__order = order
        self.__round = round
        self.__training_curve = []
        self.__train_accurate_number = 0
        self.__test_accurate_number = 0
        self.__lrdf = learning_rate_decay_function
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

    def initial_weight_to_random(self, bias_digit):
        for i in range(0, 10):
            weight = np.random.randint(0, 10, size = 32 * 32 + bias_digit) * 0.1
            self.__weight.append(weight)

    def initial_weight_to_zero(self, bias_digit):
        for i in range(0, 10):
            weight = np.zeros(32 * 32 + bias_digit)
            self.__weight.append(weight)

    def train(self):

        order = np.arange(len(self.__train_label_list))
        if self.__order == "random":
            np.random.shuffle(order)

        if self.__bias_mode == 1:
            for i in range(0, len(self.__train_label_list)):
                self.__train_image_list[i] = np.append(self.__train_image_list[i], 1)

        if self.__weight_mode == 0:
            self.initial_weight_to_zero(self.__bias_mode)
        else:
            self.initial_weight_to_random(self.__bias_mode)

        t = 0
        count = 0
        learning_rate = 1

        for t in range(0, self.__round):

            for i in order:

                expected = self.__train_label_list[i]
                predicted = 0
                max_score = 0

                for j in range(0, len(self.__weight)):

                    numerator = math.exp(np.dot(self.__train_image_list[i], self.__weight[j]) / 1000)
                    denominator = 0

                    for w in self.__weight:
                        denominator += math.exp(np.dot(self.__train_image_list[i], w) / 1000)

                    score = numerator / float(denominator)

                    if score > max_score:
                        max_score = score
                        predicted = j

                if predicted != expected:
                    for k in range(0, 32 * 32 + self.__bias_mode):
                        self.__weight[expected][k] += learning_rate * self.__train_image_list[i][k]
                        self.__weight[predicted][k] -= learning_rate * self.__train_image_list[i][k]
                else:
                    self.__train_accurate_number += 1

                count += 1

            self.__training_curve.append(self.__train_accurate_number / float(len(self.__train_label_list)))

            t += 1
            self.__train_accurate_number = 0

            if self.__lrdf == "polinomial_decay":
                learning_rate *= 1 / t
            elif self.__lrdf == "exponential_decay":
                learning_rate *= math.exp(1 / t)


    def test(self):

        if self.__bias_mode == 1:
            for i in range(0, len(self.__test_label_list)):
                self.__test_image_list[i] = np.append(self.__test_image_list[i], 1)

        for i in range(0, len(self.__test_label_list)):

            expected = self.__test_label_list[i]
            predicted = 0
            max_score = 0

            for j in range(0, len(self.__weight)):

                numerator = math.exp(np.dot(self.__test_image_list[i], self.__weight[j]) / 1000)
                denominator = 0

                for w in self.__weight:
                    denominator += math.exp(np.dot(self.__test_image_list[i], w) / 1000)

                score = numerator / float(denominator)

                if score > max_score:
                    max_score = score
                    predicted = j

            if expected == predicted:
                self.__test_accurate_number += 1

            self.__confusion_matrix[predicted][expected] += 1

    def calculate_accuracy(self):

        print "Training curve:"
        print self.__training_curve

        print "Overall Accuracy:"
        print float(self.__test_accurate_number) / len(self.__test_label_list)

        print "Confusion Matrix:"
        print self.__confusion_matrix

        # for i in range(0, 10):
        #
        #     image = [[0 for col in range(32)] for row in range(32)]
        #     for m in range(0, 32):
        #         for n in range(0,32):
        #             image[m][n] = self.__weight[i][m * 32 + n]
        #
        #     image = np.asarray(image)
        #
        #     norm = mpl.colors.Normalize(vmin=-1, vmax=1)
        #     im = plt.imshow(image, interpolation="nearest")
        #     plt.colorbar(im, norm = norm)
        #     plt.show()
            # plt.savefig(str(i) + '.png')

        # fig, ax = plt.subplots()
        # t = np.arange(0, self.__round)
        # ax.plot(t, self.__training_curve)
        #
        # ax.set(xlabel='echos', ylabel='accuracy',
        #        title='Training curve')
        # ax.grid()
        #
        # fig.savefig("perceptron_train_curve.png")
        # plt.show()


# train data

classifier = Perceptron(1, 1, "fixed", 30, "polinomial_decay")
classifier.data_reader("digitdata/optdigits-orig_train.txt", "train")
classifier.train()

# test data

classifier.data_reader("digitdata/optdigits-orig_test.txt", "test")
classifier.test()
classifier.calculate_accuracy()