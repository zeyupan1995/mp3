import numpy as np
import time
import scipy.spatial as sp
import heapq as hq

# Perceptron classifier

class KNN():

    def __init__(self, k, sf):
        self.train_image_list = []
        self.train_label_list = []
        self.test_image_list = []
        self.test_label_list = []
        self.k = k
        self.similarity_function = sf
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

    def calculate_distance(self, image_a, image_b):
        if self.similarity_function == "cosine":
            return sp.distance.cosine(image_a, image_b)
        elif self.similarity_function == "correlation":
            return sp.distance.correlation(image_a, image_b)
        elif self.similarity_function == "euclidean":
            return sp.distance.euclidean(image_a, image_b)

    def find_knn(self, image):

        heap = []
        knn_list = []

        for i in range(0, len(self.train_label_list)):
            distance = self.calculate_distance(self.train_image_list[i], image)
            hq.heappush(heap, (distance, self.train_label_list[i]))

        for i in range(0, self.k):
            knn_list.append(hq.heappop(heap))

        count = np.zeros(10)
        for j in knn_list:
            count[j[1]] += 1

        max = 0
        max_count = 0
        for i in range(0, 10):
            if count[i] > max_count:
                max_count = count[i]
                max = i

        return int(max)

    def test(self):

        for i in range(0, len(self.test_label_list)):
            expected = self.test_label_list[i]
            predicted = self.find_knn(self.test_image_list[i])

            if expected == predicted:
                self.test_accurate_number += 1

            self.confusion_matrix[predicted][expected] += 1

    def brute_force_test_for_one_image(self):

        expected = self.test_label_list[0]
        predicted = self.find_knn(self.test_image_list[0])

    def calculate_accuracy(self):

        print "Overall Accuracy:"
        print float(self.test_accurate_number) / len(self.test_label_list)

        print "Confusion Matrix:"
        print self.confusion_matrix

        # fig, ax = plt.subplots()
        # t = np.arange(0, len(self.training_curve))
        # ax.plot(t, self.training_curve)
        #
        # ax.set(xlabel='echos', ylabel='accuracy',
        #        title='Training curve')
        # ax.grid()
        #
        # fig.savefig("perceptron_train_curve.png")
        # plt.show()

# train data

start = time.clock()
classifier = KNN(3, "cosine")
classifier.data_reader("digitdata/optdigits-orig_train.txt", "train")
classifier.data_reader("digitdata/optdigits-orig_test.txt", "test")
# classifier.test()
classifier.brute_force_test_for_one_image()
print time.clock() - start
# classifier.calculate_accuracy()