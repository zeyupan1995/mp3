package mp3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.VoiceStatus;

public class Main {
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<ArrayList<String>> images = new ArrayList<ArrayList<String>>();
		List<String> labels = new ArrayList<String>();
		Map<String, Integer> labelFreq = new HashMap<String, Integer>();
		String dir = "././digitdata/optdigits-orig_train.txt";
		readFile(images, labels, dir);
		getLabelFreq(labels, labelFreq);
		
		
		
		// **********Training*********
		List<List<List<Double>>> trainRes = training(images, labels, labelFreq);
		
		// **********Testing*********
		
		System.out.println("images size: " + images.size());
	    System.out.println("labels size: " + labels.size());
		System.out.println(labelFreq);
//		System.out.println(trainRes.size());
//		System.out.println(trainRes);
		
		
		List<ArrayList<String>> imagesTest = new ArrayList<ArrayList<String>>();
		List<String> labelsTest = new ArrayList<String>();
		dir = "././digitdata/optdigits-orig_test.txt";
		readFile(imagesTest, labelsTest, dir);
		
//		System.out.println("images size Test: " + imagesTest.size());
//	    System.out.println("labels size Test: " + labelsTest.size());
		List<List<Double>> protoTypical = new ArrayList<List<Double>>();
		
	    List<Integer> prediction = testing(imagesTest, labelFreq, trainRes, protoTypical);
	    System.out.println("Prediction::::: " + prediction);
	
	    
	    classification(prediction, labelsTest);
	    
	    confusionMatrix(prediction, labelsTest);
	    
	    protoTypicalIns(prediction, labelsTest, protoTypical, imagesTest);
	    
	    try {
			writeToFile(trainRes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void writeToFile(List<List<List<Double>>> trainRes) throws IOException {
//		String str = "Hello";
		String string = trainRes.toString();
	    BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
//	    writer.write(str);
	    writer.write(string);
	    writer.close();
	}
	public static void protoTypicalIns(List<Integer> prediction, List<String> labelsTest, List<List<Double>> protoTypical, List<ArrayList<String>> imagesTest) {
		
		for (int i = 0; i < 10; i++) {
			List<List<Double>> instance = new ArrayList<List<Double>>();
			for (int j = 0; j < labelsTest.size(); j++) {
				if (prediction.get(j) == i && Integer.parseInt(labelsTest.get(j)) == i) {
					instance.add(protoTypical.get(j));
				}
			}
//			System.out.println(instance);
			double resMax = -Double.MAX_VALUE;
			double resMin = Double.MAX_VALUE;
			int resMaxPos = 0;
			int resMinPos = 0;
			for (int k = 0; k < instance.size(); k++) {
				if (instance.get(k).get(0) > resMax) {
					resMax = instance.get(k).get(0);				
					resMaxPos = instance.get(k).get(1).intValue();
				}
				if (instance.get(k).get(0) < resMin) {
					resMin = instance.get(k).get(0);		
					resMinPos = instance.get(k).get(1).intValue();
				}
			}
			
			System.out.println("For Digit " + i);
			System.out.println("Most prototypical instance is image : " + resMaxPos);
			for (int row = 0; row < imagesTest.get(resMaxPos).size(); row++) {
				System.out.println(imagesTest.get(resMaxPos).get(row));
			}
			
			System.out.println("Least prototypical instance is image : " + resMinPos);
			
			for (int row = 0; row < imagesTest.get(resMinPos).size(); row++) {
				System.out.println(imagesTest.get(resMinPos).get(row));
			}
		}
	}
	public static void confusionMatrix(List<Integer> prediction, List<String> labelsTest) {
		List<List<Double>> result = new ArrayList<List<Double>>();
		for (int i = 0; i < 10; i++) {
			List<Double> col = new ArrayList<Double>();
			for (int j = 0; j < 10; j++) {
				int countTrue = 0, countPredict = 0;
				for (int k = 0; k < labelsTest.size(); k++) {
					if (i == Integer.parseInt(labelsTest.get(k)))
						countTrue++;
					if (prediction.get(k) == j && Integer.parseInt(labelsTest.get(k)) == i)
						countPredict++;
				}
				col.add(countPredict * 1.0 / countTrue);
			}
			result.add(col);
		}
		
		DecimalFormat df = new DecimalFormat("#.##");
		
		for(int i = 0; i < result.size(); i++) {
			for (int j = 0; j < result.get(0).size(); j++) {
				System.out.print(df.format(result.get(i).get(j)) + "\t");
			}
			System.out.println();
		}
		
		
	}
	
	public static void classification(List<Integer> prediction, List<String> labelsTest) {
		int overall = 0;
	    for (int i = 0; i < labelsTest.size(); i++) {
	    		if (prediction.get(i) == Integer.parseInt(labelsTest.get(i).trim())) {
	    			overall++;	
	    		}
	    }
	    System.out.println("Overall classification: " + overall * 1.0 / labelsTest.size());
	    
	    for (int i = 0; i < 10; i++) {
	    		int countMatch = 0;
	    		int countTotal = 0;
	    		for (int j = 0; j < labelsTest.size(); j++) {
	    			if (prediction.get(j) == Integer.parseInt(labelsTest.get(j)) && prediction.get(j) == i) {
	    				countMatch++;
	    			}
	    			if (Integer.parseInt(labelsTest.get(j)) == i) {
	    				countTotal++;
	    			}
	    		}
	    		System.out.println("Classification accuracy for digit " + i + ": " + countMatch * 1.0 / countTotal);
	    }
   
	}
	public static List<Integer> testing(List<ArrayList<String>> images, Map<String, Integer> labelFreq, List<List<List<Double>>> trainRes, List<List<Double>> protoTypical) {
		List<Integer> prediction = new ArrayList<Integer>();
		
		for (int i = 0; i < images.size(); i++) {
			List<Double> mapList = new ArrayList<Double>();
			for (int digit = 0; digit < 10; digit++) {
				double res = Math.log(labelFreq.get(Integer.toString(digit))/2436.0);
				for (int row = 0; row < 32; row++) {
					for (int col = 0; col < 32; col++) {
						if (images.get(i).get(row).charAt(col) == '1') {
							res += Math.log(trainRes.get(digit).get(row).get(col));
						} else {
							res += Math.log(1 - (trainRes.get(digit).get(row).get(col)));
						}
					}
				}
				mapList.add(res);
			}
			int idx = mapList.indexOf(Collections.max(mapList));
			double maxRes = Collections.max(mapList);
			List<Double> tmp = new ArrayList<Double>();
			tmp.add(maxRes);
			tmp.add(i * 1.0); // that image location
			protoTypical.add(tmp);
//			System.out.println(tmp);
			prediction.add(idx);
		}
		return prediction;
	}
	
	public static void readFile(List<ArrayList<String>> images, List<String> labels, String dir) {
		
		try (BufferedReader br = new BufferedReader(new FileReader(dir))) {
		    String line;
		    int countLine = 1;
		    List<String> tempImg = new ArrayList<String>();
		    
		    while ((line = br.readLine()) != null) {
		       // process the line.
		    		if (countLine == 33) {
		    			labels.add(line.trim());
		    			countLine = 1;
		    			tempImg = new ArrayList<String>();
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
	
	public static List<List<List<Double>>> training(List<ArrayList<String>> images, List<String> labels, Map<String, Integer> labelFreq) {
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
						if (Integer.valueOf(labels.get(k)) != digit)
							continue;
						if (images.get(k).get(i).charAt(j) == '1')
							countOnes++;
					}

					cols.add(((countOnes + laplace) * 1.0 / (frequency + laplace * 2)));
					
				}
				rows.add(cols);
			}
			result.add(rows);
		}
		return result;
			
	}

}
