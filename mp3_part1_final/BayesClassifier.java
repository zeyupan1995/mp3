
import java.util.Arrays;
import java.util.List;

public class BayesClassifier {
    int[] typeCount; // The occurrence of training images
    double[] typeP;  //  P(class) of 0,1,2,3,4,5,6,7,8,9, occurrence/total

    int[][][][] cMap;  // for training, frequency(occurrence), [i][j][G][type], i&j is index of image , G & type for 1.2, G is total possible occurrence of 1 in square, type = 0,1,2,3..
    double[][][][] pMap;  // for training, possibility, P(g(i,j)|class)

    // store test tokens from that class that have the highest and lowest posterior probabilities
    double[] lowPosteriori;
    double[] highPosteriori;
    Image[] lowPosterioriImg;
    Image[] highPosterioriImg;

    boolean isOverlap;
    int windowHeight;
    int windowWidth;
    static final double SMOOTHINGVALUE = 0.3;

    private BayesClassifier(int classNum, int imgHeight, int imgWidth) {
        this(classNum, imgHeight, imgWidth, 1, 1, true);
    }

    private BayesClassifier(int typeNum, int imgHeight, int imgWidth, int windowHeight,
                           int windowWidth, boolean isOverlap) {
        int gt = windowHeight * windowWidth + 1;
        int height;
        int width;
        if (isOverlap) {
            height = imgHeight - windowHeight + 1;
            width = imgWidth - windowWidth + 1;
        } else {
            height = imgHeight / windowHeight;
            width = imgWidth / windowWidth;
        }
        this.cMap = new int[height][width][gt][typeNum];
        this.pMap = new double[height][width][gt][typeNum];
        this.typeCount = new int[typeNum];
        this.typeP = new double[typeNum];

        this.lowPosteriori = new double[typeNum];
        Arrays.fill(lowPosteriori,Double.MAX_VALUE);
        this.lowPosterioriImg = new Image[typeNum];

        this.highPosteriori = new double[typeNum];
        Arrays.fill(highPosteriori,-Double.MAX_VALUE);
        this.highPosterioriImg = new Image[typeNum];

        this.isOverlap = isOverlap;
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;
    }

    private void trainAllImg(List<Image> imageList) {
        for (Image image : imageList) {
            this.trainOneImg(image);
        }
        for (int i = 0; i < this.pMap.length; i++) {
            for (int j = 0; j < this.pMap[i].length; j++) {
                for (int k = 0; k < this.pMap[i][j].length; k++) {
                    for (int t = 0; t < this.typeCount.length; t++) {
                        this.pMap[i][j][k][t] =
                                (this.cMap[i][j][k][t] + SMOOTHINGVALUE) / ((double) this.typeCount[t]
                                        + SMOOTHINGVALUE);
                    }
                }
            }
        }
        int totalCount = 0;
        for (int i = 0; i < typeCount.length; i++) {
            totalCount++;
        }
        for (int i = 0; i < typeP.length; i++) {
            typeP[i] = ((double) typeCount[i] + SMOOTHINGVALUE) / (totalCount + SMOOTHINGVALUE);
        }
    }

    private double[] getPredictionArray(Image image) {
        double[] res = new double[this.typeCount.length];
        for (int t = 0; t < this.typeP.length; t++) {
            double result = Math.log(this.typeP[t]);
            for (int i = 0; i < this.pMap.length; i++) {
                for (int j = 0; j < this.pMap[i].length; j++) {
                    int G;
                    if (isOverlap) {
                        G = image.getG(i, j, windowHeight, windowWidth);
                    } else {
                        G = image
                                .getG(i * windowHeight, j * windowWidth, windowHeight, windowWidth);
                    }
                    result += Math.log(this.pMap[i][j][G][t]);
                }
            }
            res[t] = result;
        }
        return res;
    }

    private int predictOneImg(Image image) {
        double[] res = getPredictionArray(image);
        int index = -1;
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < res.length; i++) {
            if (res[i] > max) {
                max = res[i];
                index = i;
            }else if(res[i] < min){
                min=res[i];
            }
        }

        //update test tokens from that class that have the highest and lowest posterior probabilities
        if (image.type == index) { //prediction is true
            if (max > highPosteriori[index]) {
                highPosteriori[index] = max;
                highPosterioriImg[index] = image;
            }
            if (min < lowPosteriori[index]) {
                lowPosteriori[index] = min;
                lowPosterioriImg[index] = image;
            }
        }

        return index;
    }

    private double pridectAllImg(List<Image> imageList) {
        int[] testTypeCount = new int[this.typeCount.length];
        double[][] confusionArray = new double[this.typeCount.length][this.typeCount.length];
        int totalCount = 0;
        for (Image image : imageList) {
            int predictType = predictOneImg(image);
            if (predictType == image.type) {
                totalCount++;
            }
            confusionArray[predictType][image.type]++;
            testTypeCount[image.type]++;
        }
        for (int i = 0; i < confusionArray.length; i++) {
            for (int j = 0; j < confusionArray[i].length; j++) {
                confusionArray[i][j] /= testTypeCount[i];
                System.out.printf("%.3f ", confusionArray[i][j]);
            }
            System.out.println();
        }
        return totalCount / (double) imageList.size();
    }

    private void printLowHighProTestcase(){
       // print test tokens from that class that have the highest and lowest posterior probabilities
        for(int i=0;i<lowPosterioriImg.length;i++){
            System.out.println("For digit "+i+":");
            System.out.println("Lowest posterior probabilities:");
            System.out.println(lowPosterioriImg[i]);
            System.out.println("Highest posterior probabilities:");
            System.out.println(highPosterioriImg[i]+"\n\n");
        }
    }

    private Image calculateOddRatio(int type1, int type2) {
        Image image = new Image(this.pMap.length, this.pMap[0].length);
        for (int i = 0; i < this.pMap.length; i++) {
            for (int j = 0; j < this.pMap[0].length; j++) {
                if (Math.abs((this.pMap[i][j][1][type1] / this.pMap[i][j][1][type2])-1)<0.4){
                    image.pix[i][j] = '~';
                }
                else if (this.pMap[i][j][1][type1] > this.pMap[i][j][1][type2]) {
                    image.pix[i][j] = '+';
                } else {
                    image.pix[i][j] = '-';
                }
            }
        }
        return image;
    }

    private Image printLikelihood(int type1){
        Image image = new Image(this.pMap.length, this.pMap[0].length);
        for (int i = 0; i < this.pMap.length; i++) {
            for (int j = 0; j < this.pMap[0].length; j++) {
                if (this.pMap[i][j][1][type1]>0.667){
                    image.pix[i][j] = '+';
                }
                else if (this.pMap[i][j][1][type1]<0.333) {
                    image.pix[i][j] = '-';
                } else {
                    image.pix[i][j] = '~';
                }
            }
        }
        return image;
    }

    private void trainOneImg(Image image) {
        for (int i = 0; i < this.cMap.length; i++) {
            for (int j = 0; j < this.cMap[i].length; j++) {
                int G;
                if (isOverlap) {
                    G = image.getG(i, j, windowHeight, windowWidth);
                } else {
                    G = image.getG(i * windowHeight, j * windowWidth, windowHeight, windowWidth);
                }
                this.cMap[i][j][G][image.type]++;
            }
        }
        this.typeCount[image.type]++;
    }

    static void testDigit() {
        List<Image> train_list =
                DataReader.readDigitFile("./digitdata/optdigits-orig_train.txt", 32, 32);
        List<Image> test_list =
                DataReader.readDigitFile("./digitdata/optdigits-orig_test.txt", 32, 32);
        BayesClassifier bc = new BayesClassifier(10, 32, 32);//1.1

//        BayesClassifier bc = new BayesClassifier(10, 32, 32, 2, 4, true); //1.2

        long start_time = System.currentTimeMillis();
        bc.trainAllImg(train_list);
        System.out.println("Training time: "+ (System.currentTimeMillis()-start_time));

        start_time = System.currentTimeMillis();
        System.out.println(bc.pridectAllImg(test_list));
        System.out.println("Testing time: "+ (System.currentTimeMillis()-start_time));
        bc.printLowHighProTestcase();
        System.out.println(bc.printLikelihood(2));
        System.out.println(bc.printLikelihood(9));
        System.out.println(bc.calculateOddRatio(2, 9));

    }

    static void testFace() {
        List<Image> face_train_list = DataReader
                .readFaceFile("./facedata/facedatatrain", "./facedata/facedatatrainlabels", 70,
                        60);
        List<Image> face_test_list = DataReader
                .readFaceFile("./facedata/facedatatest", "./facedata/facedatatestlabels", 70, 60);
        BayesClassifier bc2 = new BayesClassifier(2, 70, 60, 4, 4, false);
        bc2.trainAllImg(face_train_list);
        System.out.println(bc2.pridectAllImg(face_test_list));
        System.out.println(bc2.calculateOddRatio(0, 1));


    }


    public static void main(String[] args) {
        System.out.println("test digit: ");
        testDigit();
        System.out.println("test face: ");
        testFace();
    }
}
