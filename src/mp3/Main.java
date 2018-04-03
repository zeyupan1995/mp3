package mp3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<ArrayList<String>> images = new ArrayList<ArrayList<String>>();
		List<String> labels = new ArrayList<String>();
		readFile(images, labels);
	}
	
	
	public static void readFile(List<ArrayList<String>> images, List<String> labels) {
		
		try (BufferedReader br = new BufferedReader(new FileReader("././digitdata/optdigits-orig_train.txt"))) {
		    String line;
		    int countLine = 1;
		    List<String> tempImg = new ArrayList<String>();
		    
		    while ((line = br.readLine()) != null) {
		       // process the line.
		    		if (countLine == 33) {
		    			labels.add(line);
		    			countLine = 1;
		    			tempImg.remove(tempImg);
		    			continue;
		    		} 
		    		tempImg.add(line);
		    		if (countLine == 32) {
		    			images.add((ArrayList<String>) tempImg);
		    		}
		    		countLine++;
		    }
		    
		    System.out.println("images size: " + images.size());
		    System.out.println("labels size: " + labels.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
}
