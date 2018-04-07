import numpy as np
import time
import scipy.spatial as sp
import sklearn.neighbors as sn
import heapq as hq

# Perceptron classifier

class KNN():

    def __init__(self, k, sf):
        self.__train_image_list = []
        self.__train_label_list = []
        self.__test_image_list = []
        self.__test_label_list = []
        self.__k = k
        self.__similarity_function = sf
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

    def calculate_distance(self, image_a, image_b):
        if self.__similarity_function == "cosine":
            return sp.distance.cosine(image_a, image_b)
        elif self.__similarity_function == "correlation":
            return sp.distance.correlation(image_a, image_b)
        elif self.__similarity_function == "euclidean":
            return sp.distance.euclidean(image_a, image_b)

    def find_knn(self, image):

        heap = []
        knn_list = []

        for i in range(0, len(self.__train_label_list)):
            distance = self.calculate_distance(self.__train_image_list[i], image)
            hq.heappush(heap, (distance, self.__train_label_list[i]))

        for i in range(0, self.__k):
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

        for i in range(0, len(self.__test_label_list)):
            expected = self.__test_label_list[i]
            predicted = self.find_knn(self.__test_image_list[i])

            if expected == predicted:
                self.__test_accurate_number += 1

            self.__confusion_matrix[predicted][expected] += 1

    def brute_force_test_for_one_image(self):

        expected = self.__test_label_list[0]
        start = time.clock()
        predicted = self.find_knn(self.__test_image_list[0])
        print time.clock() - start

    def kd_tree_for_one_image(self):

        expected = self.__test_label_list[0]
        start = time.clock()
        tree = sp.cKDTree(self.__train_image_list)
        print time.clock() - start
        start = time.clock()
        predicted = tree.query(self.__test_image_list[0])
        print time.clock() - start

    def ball_tree_for_one_image(self):

        expected = self.__test_label_list[0]
        start = time.clock()
        tree = sn.BallTree(self.__train_image_list)
        print time.clock() - start
        start = time.clock()
        predicted = tree.query(self.__test_image_list[0].reshape(1, len(self.__test_image_list[0])))
        print time.clock() - start

    def calculate_accuracy(self):

        print "Overall Accuracy:"
        print float(self.__test_accurate_number) / len(self.__test_label_list)

        print "Confusion Matrix:"
        print self.__confusion_matrix

        # fig, ax = plt.subplots()
        # t = np.arange(0, len(self.__training_curve))
        # ax.plot(t, self.__training_curve)
        #
        # ax.set(xlabel='echos', ylabel='accuracy',
        #        title='Training curve')
        # ax.grid()
        #
        # fig.savefig("perceptron_train_curve.png")
        # plt.show()

# train data

classifier = KNN(3, "cosine")
classifier.data_reader("digitdata/optdigits-orig_train.txt", "train")
classifier.data_reader("digitdata/optdigits-orig_test.txt", "test")
# classifier.test()
# classifier.brute_force_test_for_one_image()
# classifier.kd_tree_for_one_image()
classifier.ball_tree_for_one_image()

# classifier.calculate_accuracy()