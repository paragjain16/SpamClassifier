package svm;

import libsvm.*;
import randomprojection.RandomProjection;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Parag on 28-11-2014.
 */

public class SVMTestTFIDF {

	private List<File> files;
	private String regex = "\\s+";
    private Map<String, Integer> wordMap;
	private ArrayList<Map<Integer, Double>> tfMaps;
    private int totalDocs =0;
    private Map<Integer, Double> idfSpam;
    private Map<Integer, Double> idfHam;
    private Map<String, Double> tf;
    private int totalSpam=0;
    private int totalHam=0;
    private boolean rp = false;
    private int reducedDimensionSize=10;
    private RandomProjection randomProjection;
    private boolean cf = false;
    private int ngram = 4;
    private ArrayList<svm_node[]> randomNodes;
    private int numDataPoints = 30;
    private static String path = "C:/Users/Parag/Desktop/Project/trec07p";
    private static String datapath = path + "/data/";

	public SVMTestTFIDF(){
		//wordMap = new HashMap<String, Integer>();
		files = new ArrayList<File>();
        tfMaps = new ArrayList<Map<Integer, Double>>();
        randomNodes = new ArrayList<svm_node[]>(numDataPoints);
	}

	public void setRP(boolean rp){
        this.rp = rp;
    }
    public void setReducedDimensionSize(int size){
        this.reducedDimensionSize = size;
    }
    public void svmTrain(List<String> train){
        svm_problem prob = new svm_problem();
        try{
            if(cf)
                genCharMap(train);
            else
                genWordMap(train);
            prob = genProblem(train, cf);
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
		/**
		 * SET PARAMETERS
		 */
		svm_parameter param = new svm_parameter();
		param.svm_type = svm_parameter.NU_SVC;
		param.kernel_type = svm_parameter.LINEAR;

        param.degree = 1;
		param.nu = 0.005;
		param.gamma = 0.001;
		param.eps = 0.001;
		param.C = 3.0;
		
		svm.svm_check_parameter(prob, param);
		svm_model model =  svm.svm_train(prob, param);
		try {
			svm.svm_save_model("spam_svm.model",model);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

    protected void genWordMap(List<String> train) throws IOException{
        wordMap = new HashMap<String, Integer>();
        int index = 0;
        String emailPath = null;
        idfSpam = new HashMap<Integer, Double>();
        idfHam = new HashMap<Integer, Double>();

        for(int fileId = 0;fileId<train.size();fileId++){
            String file = train.get(fileId);
            emailPath = datapath;
            String subPath = file.substring(file.lastIndexOf("/") + 1);
            emailPath += subPath;
            BufferedReader reader = new BufferedReader(new FileReader(emailPath));
            String line = null;
            totalDocs++;
            HashMap<Integer, Double> tfMap = new HashMap<Integer, Double>();

            HashSet<Integer> wordSpamIndex = new HashSet<Integer>();
            HashSet<Integer> wordHamIndex = new HashSet<Integer>();
            if(file.startsWith("spam")){
                totalSpam++;
            }else{
                totalHam++;
            }
            int totalWords=0;
            double maxCount = 0.0;
		 	while ((line = reader.readLine()) != null){
		 		for (String word : line.split(this.regex)){
                    totalWords++;
                    String s = word.trim();//.toLowerCase();
		 			if(!wordMap.containsKey(s)){
		 				wordMap.put(s,index);
		 				index++;
		 			}
                    int idx = wordMap.get(s);
                    if(file.startsWith("spam")){
                        wordSpamIndex.add(idx);
                    }else{
                        wordHamIndex.add(idx);
                    }
                    if(tfMap.containsKey(idx)){
                        double count = tfMap.get(idx);
                        count += 1.0;
                        if(count > maxCount)
                            maxCount=count;
                        tfMap.put(idx, count);
                    }else{
                        tfMap.put(idx, 1.0);
                        if(1.0 > maxCount)
                            maxCount = 1.0;
                    }
		 		}
		 	}
		 	reader.close();
            //Calculate TF  Index -> TF
            //Normalize tf counts by max count
            for (Map.Entry<Integer, Double> entry : tfMap.entrySet()) {
                double tf = entry.getValue()/maxCount;
                entry.setValue(tf);
            }
            Iterator<Integer> it = wordHamIndex.iterator();
            while(it.hasNext()){
                int ix = it.next();
                if(idfHam.containsKey(ix))
                    idfHam.put(ix, idfHam.get(ix)+1.0);
                else
                    idfHam.put(ix, 1.0);
            }
            it = wordSpamIndex.iterator();
            while(it.hasNext()){
                int ix = it.next();
                if(idfSpam.containsKey(ix))
                    idfSpam.put(ix, idfSpam.get(ix)+1.0);
                else
                    idfSpam.put(ix, 1.0);
            }

            tfMaps.add(fileId, tfMap);
        }
        //Calculate IDF - IDF
        for (Map.Entry<Integer, Double> entry : idfSpam.entrySet()) {
            double idf = Math.log(totalSpam/(1.0+entry.getValue()));
            entry.setValue(idf);
        }
        for (Map.Entry<Integer, Double> entry : idfHam.entrySet()) {
            double idf = Math.log(totalHam/(1.0+entry.getValue()));
            entry.setValue(idf);
        }
	}

    protected void genCharMap(List<String> train) throws IOException{
        wordMap = new HashMap<String, Integer>();
        int index = 0;
        String emailPath = null;
        idfSpam = new HashMap<Integer, Double>();
        idfHam = new HashMap<Integer, Double>();

        for(int fileId = 0;fileId<train.size();fileId++){
            String file = train.get(fileId);
            emailPath = datapath;
            String subPath = file.substring(file.lastIndexOf("/") + 1);
            emailPath += subPath;
            BufferedReader reader = new BufferedReader(new FileReader(emailPath));
            String line = null;
            totalDocs++;
            HashMap<Integer, Double> tfMap = new HashMap<Integer, Double>();

            HashSet<Integer> wordSpamIndex = new HashSet<Integer>();
            HashSet<Integer> wordHamIndex = new HashSet<Integer>();
            if(file.startsWith("spam")){
                totalSpam++;
            }else{
                totalHam++;
            }
            int totalWords=0;
            double maxCount = 0.0;
            while ((line = reader.readLine()) != null) {
                if(line.length() < ngram){
                    String s = line;//line.toLowerCase();
                    totalWords++;
                    if(!wordMap.containsKey(s)){
                        wordMap.put(s,index);
                        index++;
                    }
                    int idx = wordMap.get(s);
                    if(file.startsWith("spam")){
                        wordSpamIndex.add(idx);
                    }else{
                        wordHamIndex.add(idx);
                    }
                    if(tfMap.containsKey(idx)){
                        double count = tfMap.get(idx);
                        count += 1.0;
                        if(count > maxCount)
                            maxCount=count;
                        tfMap.put(idx, count);
                    }else{
                        tfMap.put(idx, 1.0);
                        if(1.0 > maxCount)
                            maxCount = 1.0;
                    }
                }else {
                    for (int i = 0; i <= line.length() - ngram; i++) {
                        String s = line.substring(i, i + ngram);//.toLowerCase();
                        totalWords++;
                        if(!wordMap.containsKey(s)){
                            wordMap.put(s,index);
                            index++;
                        }
                        int idx = wordMap.get(s);
                        if(file.startsWith("spam")){
                            wordSpamIndex.add(idx);
                        }else{
                            wordHamIndex.add(idx);
                        }
                        if(tfMap.containsKey(idx)){
                            double count = tfMap.get(idx);
                            count += 1.0;
                            if(count > maxCount)
                                maxCount=count;
                            tfMap.put(idx, count);
                        }else{
                            tfMap.put(idx, 1.0);
                            if(1.0 > maxCount)
                                maxCount = 1.0;
                        }
                    }
                }
            }
            reader.close();
            //Calculate TF  Index -> TF
            //Normalize tf counts by max count
            for (Map.Entry<Integer, Double> entry : tfMap.entrySet()) {
                double tf = entry.getValue()/maxCount;
                entry.setValue(tf);
            }
            Iterator<Integer> it = wordHamIndex.iterator();
            while(it.hasNext()){
                int ix = it.next();
                if(idfHam.containsKey(ix))
                    idfHam.put(ix, idfHam.get(ix)+1.0);
                else
                    idfHam.put(ix, 1.0);
            }
            it = wordSpamIndex.iterator();
            while(it.hasNext()){
                int ix = it.next();
                if(idfSpam.containsKey(ix))
                    idfSpam.put(ix, idfSpam.get(ix)+1.0);
                else
                    idfSpam.put(ix, 1.0);
            }

            tfMaps.add(fileId, tfMap);
        }
        //Calculate IDF - IDF
        for (Map.Entry<Integer, Double> entry : idfSpam.entrySet()) {
            double idf = Math.log(totalSpam/(1.0+entry.getValue()));
            entry.setValue(idf);
        }
        for (Map.Entry<Integer, Double> entry : idfHam.entrySet()) {
            double idf = Math.log(totalHam/(1.0+entry.getValue()));
            entry.setValue(idf);
        }

    }
	protected svm_problem genProblem(List<String> train, boolean cf) throws IOException{
		/**
		 * TRAINING
		 */
		System.out.println("[SVM TRAIN START]");
		
		svm_problem prob = new svm_problem();
		prob.x = new svm_node[files.size()][];
		
        prob.y = new double[files.size()];

        String emailPath = null;
        for(int fileId = 0;fileId<train.size();fileId++){
            TreeSet<Integer> indices = new TreeSet<Integer>();
            String file = train.get(fileId);
            emailPath = datapath;
            String subPath = file.substring(file.lastIndexOf("/") + 1);
            emailPath += subPath;
            if(file.startsWith("spam")){
                prob.y[fileId] = -1.0;
            }else{
                prob.y[fileId] = +1.0;
            }
            BufferedReader reader = new BufferedReader(new FileReader(emailPath));
            String line = null;
		 	if(cf) {
                while ((line = reader.readLine()) != null) {
                    if(line.length() < ngram){
                        String s = line;//line.toLowerCase();
                        int idx = wordMap.get(s);
                        indices.add(idx);
                    }else {
                        for (int i = 0; i <= line.length() - ngram; i++) {
                            String s = line.substring(i, i + ngram);//.toLowerCase();
                            int idx = wordMap.get(s);
                            indices.add(idx);
                        }
                    }
                }//File read and stored
            }else{
                while ((line = reader.readLine()) != null) {
                    for (String word : line.split(this.regex)) {
                        int idx = wordMap.get(word.trim());//.toLowerCase());
                        indices.add(idx);
                    }
                }//File read and stored
            }
		 	
		 	int i=0;
		 	svm_node[] x = new svm_node[indices.size()+1];
            Iterator<Integer> it = indices.iterator();
			while (it.hasNext()) {
                int idx = it.next();
				x[i] = new svm_node();
				x[i].index = idx;
                if(file.startsWith("spam"))
                    x[i].value = tfMaps.get(fileId).get(idx) * idfSpam.get(idx);//1.0;
                else
                    x[i].value = tfMaps.get(fileId).get(idx) * idfHam.get(idx);
                i++;
			}
			x[i] = new svm_node();
			x[i].index = -1;
			x[i].value = 1.0;
			prob.x[fileId] = x;

		 	reader.close();
    	}
        if(rp){
            chooseDataPoints(prob.x);
            System.out.println("Pairwise distances before random projection ");
            calculateDistance(randomNodes);
            randomProjection = new RandomProjection(reducedDimensionSize, wordMap.size());
            randomProjection.convertToRandomProjection(prob.x);
            System.out.println("Pairwise distances after random projection ");
            calculateDistance(randomNodes);
        }
        prob.l = files.size();
        System.out.println("[SVM TRAIN END]");
		return prob;
	}
    public void chooseDataPoints(svm_node[][] dataNodes){
        Random r = new Random();
        int size = dataNodes.length;
        int i = numDataPoints;
        while( i > 0 ){
            int index = r.nextInt(size);
            randomNodes.add(dataNodes[index]);
            i--;
        }
    }
    public void calculateDistance(ArrayList<svm_node[]> dataNodes){
        int size = dataNodes.size();
        for(int i = 0; i < size/2; i++){
            System.out.println("Distance between Data Node "+(i+1)+" and "+(size-i)+" is "
                    + distanceBetweenVector(dataNodes.get(i), dataNodes.get(size-i-1)));
        }
    }

    public double distanceBetweenVector(svm_node[] v1, svm_node[] v2){
        double distance = 0.0;
        int i=0;
        int j=0;
        int index1 = 0;
        int index2 = 0;
        double value1= 0.0;
        double value2 = 0.0;
        while(i < v1.length-1 && j < v2.length-1){
            index1 = v1[i].index;
            value1 = v1[i].value;
            index2 = v2[j].index;
            value2 = v2[j].value;
            if(index1 == index2){
                distance += ((value1- value2)*(value1- value2));
                i++;
                j++;
            }else if(index1 > index2){
                distance += ((0 - value2)*(0 - value2));
                j++;
            }else{
                distance += ((value1 - 0)*(value1 - 0));
                i++;
            }
        }
        if( i < v1.length - 1){
            while(i < v1.length -1){
                distance += (v1[i].value * v1[i].value);
                i++;
            }
        }
        if( j < v2.length - 1){
            while(j < v2.length -1){
                distance += (v2[j].value * v2[j].value);
                j++;
            }
        }
        return Math.sqrt(distance);
    }
	
	public void svmPredict(List<String> testFiles) throws IOException{
    	svm_model model = svm.svm_load_model("spam_svm.model");
        String emailPath = null;
        double maxCount = 0.0;
		double []actualLabels = new double[testFiles.size()];
		double []predictedLables = new double[testFiles.size()];
        double[] predictionProbabilities = new double[2*testFiles.size()];
		int j=0;
        for(int fileId = 0;fileId<testFiles.size();fileId++){
			TreeSet<Integer> sortedIndices = new TreeSet<Integer>();
            HashMap<Integer, Double> tfMap = new HashMap<Integer, Double>();
            String file = testFiles.get(fileId);
            emailPath = datapath;
            String subPath = file.substring(file.lastIndexOf("/") + 1);
            emailPath += subPath;
            BufferedReader reader = new BufferedReader(new FileReader(emailPath));
            String line = null;
            if(file.startsWith("spam")){
                actualLabels[fileId] = -1.0;
            }else{
                actualLabels[fileId] = +1.0;
            }
            if(cf) {
                while ((line = reader.readLine()) != null) {
                    if(line.length() < ngram){
                        String s = line;//.toLowerCase();
                        if (wordMap.containsKey(s))
                            sortedIndices.add(wordMap.get(s));
                    }
                    for (int i = 0; i <= line.length() - ngram; i++) {
                        String s = line.substring(i, i + ngram);//.toLowerCase();
                        if (wordMap.containsKey(s))
                            sortedIndices.add(wordMap.get(s));
                    }
                }//File read and stored
            }else {

                while ((line = reader.readLine()) != null) {
                    for (String word : line.split(this.regex)) {
                        String s = word.trim();//.toLowerCase();
                        if (wordMap.containsKey(s))
                            sortedIndices.add(wordMap.get(s));
                        else
                            continue;
                        int idx = wordMap.get(s);
                        if(tfMap.containsKey(idx)){
                            double count = tfMap.get(idx);
                            count += 1.0;
                            if(count > maxCount)
                                maxCount=count;
                            tfMap.put(idx, count);
                        }else{
                            tfMap.put(idx, 1.0);
                            if(1.0 > maxCount)
                                maxCount = 1.0;
                        }
                    }
                }//File read and stored
            }

            for (Map.Entry<Integer, Double> entry : tfMap.entrySet()) {
                double tf = entry.getValue()/maxCount;
                entry.setValue(tf);
            }

		 	svm_node[] x = new svm_node[sortedIndices.size()+1];
		 	int i = 0;
            Iterator<Integer> it = sortedIndices.iterator();
		 	while(it.hasNext()) {
				x[i] = new svm_node();
                int idx = it.next();
				x[i].index = idx;
                if(idfSpam.containsKey(idx) && idfHam.containsKey(idx))
                    x[i].value = tfMap.get(idx) * Math.abs(idfSpam.get(idx)-idfHam.get(idx));
                else if(idfSpam.containsKey(idx))
				    x[i].value = tfMap.get(idx) * idfSpam.get(idx);
                else if(idfHam.containsKey(idx))
                    x[i].value = tfMap.get(idx) * idfHam.get(idx);
                else
                    x[i].value = 1.0;
				i++;
			}
            x[i] = new svm_node();
            x[i].index = -1;
            x[i].value = 1.0;

            if(rp) {
                svm_node[] rp_x = randomProjection.calculateRandomProjectionNode(x);
                predictedLables[fileId] = svm.svm_predict(model,rp_x);

            }else{
                double[] predict = new double[2];
                predictedLables[fileId] = svm.svm_predict(model,x);
                predictionProbabilities[j] = predict[0];
                predictionProbabilities[j+1] = predict[1];
            }
            j = j+2;
		 	reader.close();
        }
        
        /**
         * Calculate accuracy
         */
		int correct = 0;
		int total = actualLabels.length;
		double error = 0;
        int totalspam = 0;
        int totalham = 0;
		int spam =0;
        int ham =0;

        for(int i=0;i<predictedLables.length;i++){
            if(actualLabels[i] == -1.0){
                totalspam++;
                if(predictedLables[i] == -1.0)
                    spam++;
            }else{
                totalham++;
                if(predictedLables[i] == 1.0)
                    ham++;
        	}
        }

        System.out.println("Spam Accuracy = "+(double)spam/totalspam*100+
				 "% ("+spam+"/"+totalspam+") (classification)\n");
            System.out.println("Ham Accuracy = "+(double)ham/totalham*100+
                    "% ("+ham+"/"+totalham+") (classification)\n");
	}
}