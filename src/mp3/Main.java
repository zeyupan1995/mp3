package mp3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<ArrayList<String>> images = new ArrayList<ArrayList<String>>();
		List<String> labels = new ArrayList<String>();
		Map<String, Integer> labelFreq = new HashMap<String, Integer>();
		readFile(images, labels);
		getLabelFreq(labels, labelFreq);
		
		List<List<List<Double>>> trainRes = likelihoods(images, labels, labelFreq);
		
		System.out.println("images size: " + images.size());
	    System.out.println("labels size: " + labels.size());
		System.out.println(labelFreq);
		System.out.println(trainRes.size());
		System.out.println(trainRes);
	}
	
	
	public static void readFile(List<ArrayList<String>> images, List<String> labels) {
		
		try (BufferedReader br = new BufferedReader(new FileReader("././digitdata/optdigits-orig_train.txt"))) {
		    String line;
		    int countLine = 1;
		    List<String> tempImg = new ArrayList<String>();
		    
		    while ((line = br.readLine()) != null) {
		       // process the line.
		    		if (countLine == 33) {
		    			labels.add(line.trim());
		    			countLine = 1;
		    			tempImg.remove(tempImg);
		    			continue;
		    		} 
		    		tempImg.add(line.trim());
		    		if (countLine == 32) {
		    			images.add((ArrayList<String>) tempImg);
		    		}
		    		countLine++;
		    }
		    
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	
	public static void getLabelFreq(List<String> labels, Map<String, Integer> labelFreq) {
		for (String s: labels) {
			if (labelFreq.containsKey(s)) {
				labelFreq.put(s, labelFreq.get(s) + 1);
			} else {
				labelFreq.put(s, 1);
			}
		}
	}
	
	public static List<List<List<Double>>> likelihoods(List<ArrayList<String>> images, List<String> labels, Map<String, Integer> labelFreq) {
		int laplace = 1;
		List<List<List<Double>>> result = new ArrayList<List<List<Double>>>();
		for (int digit = 0; digit < 10; digit++) {

			int frequency = labelFreq.get(Integer.toString(digit));
			List<List<Double>> rows = new ArrayList<List<Double>>();
			for (int i = 0; i < 32; i++) {
				List<Double> cols = new ArrayList<Double>();
				for (int j = 0; j < 32; j++) {
					int countOnes = 0;
					for (int k = 0; k < images.size(); k++) {
						if (images.get(k).get(i).charAt(j) == '1')
							countOnes++;
					}
					cols.add((double) ((countOnes + laplace) / (frequency + laplace * 2)));
				}
				rows.add(cols);
			}
			result.add(rows);
		}
		return result;
			
	}

}
