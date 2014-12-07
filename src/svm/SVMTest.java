package svm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import randomprojection.RandomProjection;

/**
 * Created by Parag on 28-11-2014.
 */

public class SVMTest {
	private String regex = "\\s+";
	private Map<String, Integer> wordMap;
    private boolean rp = false;
    private int reducedDimensionSize=10;
    private RandomProjection randomProjection;
    public boolean cf = false;
    public int ngram = 4;
    private ArrayList<Integer> randomNodes;
    private int numDataPoints = 30;
    private static String path = "C:/Users/Parag/Desktop/Project/trec07p";
    private static String datapath = path + "/data/";
    public double totalHam =0;
    public double totalSpam=0;
    public double ham=0;
    public double spam=0;

	public SVMTest(){
        randomNodes = new ArrayList<Integer>(numDataPoints);
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
		param.kernel_type = svm_parameter.RBF;

        param.degree = 1;
		param.nu = 0.1;
		param.gamma = 0.0001;
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
        for(int fileId = 0;fileId<train.size();fileId++){
            String file = train.get(fileId);
            emailPath = datapath;
            String subPath = file.substring(file.lastIndexOf("/") + 1);
            emailPath += subPath;
            BufferedReader reader = new BufferedReader(new FileReader(emailPath));
            String line = null;

		 	while ((line = reader.readLine()) != null){
		 		for (String word : line.split(this.regex)){
                    String s = word.trim();//.toLowerCase();
		 			if(!wordMap.containsKey(s)){
		 				wordMap.put(s,index);
		 				index++;
		 			}
		 		}
		 	}

		 	reader.close();
        }
        System.out.println("Number of features "+index);
	}

    protected void genCharMap(List<String> train) throws IOException{
        wordMap = new HashMap<String, Integer>();
        int index = 0;
        String emailPath = null;
        for(int fileId = 0;fileId < train.size(); fileId++){
            String file = train.get(fileId);
            emailPath = datapath;
            String subPath = file.substring(file.lastIndexOf("/") + 1);
            emailPath += subPath;
            BufferedReader reader = new BufferedReader(new FileReader(emailPath));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if(line.length() < ngram){
                    String s = line;//line.toLowerCase();
                    if(!wordMap.containsKey(s)){
                        wordMap.put(s,index);
                        index++;
                    }
                }else {
                    for (int i = 0; i <= line.length() - ngram; i++) {
                        String s = line.substring(i, i + ngram);//.toLowerCase();
                        if (!wordMap.containsKey(s)) {
                            wordMap.put(s, index);
                            index++;
                        }
                    }
                }
            }
            System.out.println("Number of features "+index);
            reader.close();
        }
    }
	protected svm_problem genProblem(List<String> train, boolean cf) throws IOException{
		/**
		 * TRAINING
		 */
		System.out.println("[SVM TRAIN START]");
		svm_problem prob = new svm_problem();
		prob.x = new svm_node[train.size()][];
        prob.y = new double[train.size()];
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
                            String s = line.substring(i, i+ngram);//.toLowerCase();
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
				x[i] = new svm_node();
				x[i].index = it.next();
                x[i].value = 1.0;
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
            calculateDistance(randomNodes, prob.x);
            randomProjection = new RandomProjection(reducedDimensionSize, wordMap.size());
            randomProjection.convertToRandomProjection(prob.x);
            System.out.println("Pairwise distances after random projection ");
            calculateDistance(randomNodes, prob.x);
        }
        prob.l = train.size();
        System.out.println("[SVM TRAIN END]");
		return prob;
	}

    public void chooseDataPoints(svm_node[][] dataNodes){
        Random r = new Random();
        int size = dataNodes.length;
        int i = numDataPoints;
        while( i > 0 ){
            int index = r.nextInt(size);
            randomNodes.add(index);
            i--;
        }
    }
    public void calculateDistance(ArrayList<Integer> dataNodes, svm_node[][] nodes){
        int size = dataNodes.size();
        for(int i = 0; i < size/2; i++){
            System.out.println(/*"Distance between Data Node "+(i+1)+" with dimensionality "+nodes[dataNodes.get(i)].length
                    +" and data Data Node "+(size-i)+" with dimensionality "+nodes[dataNodes.get(size-i-1)].length+" is "*/
                    distanceBetweenVector(nodes[dataNodes.get(i)], nodes[dataNodes.get(size-i-1)]));
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
		double []actualLabels = new double[testFiles.size()];
		double []predictedLables = new double[testFiles.size()];
		
        for(int fileId = 0;fileId<testFiles.size();fileId++){
			TreeSet<Integer> sortedIndices = new TreeSet<Integer>();
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
                        String s = line.substring(i, i+ngram);//.toLowerCase();
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
                    }
                }//File read and stored
            }
		 	svm_node[] x = new svm_node[sortedIndices.size()+1];
		 	int i = 0;
            Iterator<Integer> it = sortedIndices.iterator();
		 	while(it.hasNext()) {
				x[i] = new svm_node();
				x[i].index = it.next();
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
                predictedLables[fileId] = svm.svm_predict(model,x);
            }
		 	reader.close();
        }
        
        /**
         * Calculate accuracy
         */
		int correct = 0;
		int total = actualLabels.length;
		double error = 0;

        for(int i=0;i<predictedLables.length;i++){
            if(actualLabels[i] == -1.0){
                totalSpam++;
                if(predictedLables[i] == -1.0)
                    spam++;
            }else{
                totalHam++;
                if(predictedLables[i] == 1.0)
                    ham++;
        	}
        }
	}
}