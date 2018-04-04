import numpy as np
from matplotlib import pyplot as plt

# Perceptron classifier

class Perceptron():

    def __init__(self, weight_mode, bias_mode, order, round):
        self.train_image_list = []
        self.train_label_list = []
        self.test_image_list = []
        self.test_label_list = []
        self.weight = []
        self.bias = 1
        self.weight_mode = weight_mode
        self.bias_mode = bias_mode
        self.order = order
        self.round = round
        self.training_curve = []
        self.train_accurate_number = 0
        self.test_accurate_number = 0
        self.confusion_matrix = [[0 for col in range(10)] for row in range(10)]


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
                        self.train_image_list.append(np.array(image, dtype=float))
                        self.train_label_list.append(int(line))
                    elif mode == "test":
                        self.test_image_list.append(np.array(image, dtype=float))
                        self.test_label_list.append(int(line))
                    image = []
                    count = 0
        f.close()

    def initial_weight_to_random(self, bias_digit):
        for i in range(0, 10):
            weight = np.random.randint(0, 10, size = 32 * 32 + bias_digit) * 0.1
            self.weight.append(weight)

    def initial_weight_to_zero(self, bias_digit):
        for i in range(0, 10):
            weight = np.zeros(32 * 32 + bias_digit)
            self.weight.append(weight)

    def train(self):

        order = np.arange(len(self.train_label_list))
        if self.order == "random":
            np.random.shuffle(order)

        if self.bias_mode == 1:
            for i in range(0, len(self.train_label_list)):
                self.train_image_list[i] = np.append(self.train_image_list[i], 1)

        if self.weight_mode == 0:
            self.initial_weight_to_zero(self.bias_mode)
        else:
            self.initial_weight_to_random(self.bias_mode)

        t = 0
        count = 0
        for t in range(0, self.round):

            for i in order:

                expected = self.train_label_list[i]
                predicted = 0
                max_score = 0

                for j in range(0, len(self.weight)):
                    score = np.dot(self.train_image_list[i], self.weight[j])
                    if score > max_score:
                        max_score = score
                        predicted = j

                if predicted != expected:
                    for k in range(0, 32 * 32 + self.bias_mode):
                        self.weight[expected][k] += self.train_image_list[i][k]
                        self.weight[predicted][k] -= self.train_image_list[i][k]
                else:
                    self.train_accurate_number += 1

                count += 1

                self.training_curve.append(self.train_accurate_number / float(count))

            t += 1


    def test(self):

        if self.bias_mode == 1:
            for i in range(0, len(self.test_label_list)):
                self.test_image_list[i] = np.append(self.test_image_list[i], 1)

        for i in range(0, len(self.test_label_list)):

            expected = self.test_label_list[i]
            predicted = 0
            max_score = 0

            for j in range(0, len(self.weight)):
                score = np.dot(self.test_image_list[i], self.weight[j])
                if score > max_score:
                    max_score = score
                    predicted = j

            if expected == predicted:
                self.test_accurate_number += 1

            self.confusion_matrix[predicted][expected] += 1

    def calculate_accuracy(self):

        print "Training curve:"
        print self.training_curve

        print "Overall Accuracy:"
        print float(self.test_accurate_number) / len(self.test_label_list)

        print "Confusion Matrix:"
        print self.confusion_matrix

        fig, ax = plt.subplots()
        t = np.arange(0, len(self.training_curve))
        ax.plot(t, self.training_curve)

        ax.set(xlabel='echos', ylabel='accuracy',
               title='Training curve')
        ax.grid()

        fig.savefig("perceptron_train_curve.png")
        plt.show()

# train data

classifier = Perceptron(0, 0, "fixed", 2)
classifier.data_reader("digitdata/optdigits-orig_train.txt", "train")
classifier.train()

# test data

classifier.data_reader("digitdata/optdigits-orig_test.txt", "test")
classifier.test()
classifier.calculate_accuracy()