
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class DataReader {

    static List<Image> readDigitFile(String filename, int imageHeight, int imageWidth) {
        List<Image> res = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            LOOP:
            while (true) {
                Image image = new Image(imageHeight, imageWidth);
                for (int i = 0; i < imageHeight; i++) {
                    String line = br.readLine();
                    if (line == null) {
                        break LOOP;
                    }
                    for (int j = 0; j < imageWidth; j++) {
                        image.pix[i][j] = line.charAt(j);
                    }

                }
                String line = br.readLine();
                image.type = Integer.valueOf(line.trim());
                res.add(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    static List<Image> readFaceFile(String imageFilename, String labelFilename, int imageHeight,
        int imageWidth) {

        List<Image> res = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(imageFilename));
            BufferedReader br2 = new BufferedReader(new FileReader(labelFilename))) {
            LOOP:
            while (true) {
                Image image = new Image(imageHeight, imageWidth);
                for (int i = 0; i < imageHeight; i++) {
                    String line = br.readLine();
                    if (line == null) {
                        break LOOP;
                    }
                    for (int j = 0; j < imageWidth; j++) {
                        image.pix[i][j] = line.charAt(j) == '#' ? '1' : '0';
                    }

                }
                String line = br2.readLine();
                image.type = Integer.valueOf(line.trim());
                res.add(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }


}
