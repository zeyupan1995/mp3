
public class Image {
    char[][] pix;
    int type;

    public Image(int imageHeight, int imageWidth) {
        pix = new char[imageHeight][imageWidth];
        type = -1;
    }

    public int getG(int x, int y, int h, int w) {
        int count = 0;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (pix[x + i][y + j] == '1') {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("\n");
        for (int i = 0; i < pix.length; i++) {
            for (int j = 0; j < pix[i].length; j++) {
                if(pix[i][j]=='-')
                    sb.append("\033[0;33;1m");
                else if (pix[i][j]=='~')
                    sb.append("\033[0;32;42m");
                else
                    sb.append("\033[0;34;44m");
                sb.append(pix[i][j]);
                sb.append("\033[0m");
            }
            sb.append("\n");
        }
        sb.append("type: ").append(type).append("\n");
        return sb.toString();
    }
}
