package test;

import java.io.IOException;
import java.util.*;

import bayes.NaiveBayes;
import reader.DataReader;
import svm.SVMTest;
import svm.SVMTestTFIDF;

/**
 * Created by Parag on 24-11-2014.
 */

public class Test {
    private static NaiveBayes nbayes;
    private static List<String> testSet;
    private static List<String> trainSet;
    private static List<String> spamHams;
    private static boolean crossValidation = true;
    public static int spamCount = 2000;
    public static int hamCount = 2000;
    public static int[] interestingTokens = { 5, 10, 15, 20, 30, 40, 50, 80, 100, Integer.MAX_VALUE};
    public static int[] accuracyHam = new int[interestingTokens.length];
    public static int[] accuracySpam = new int[interestingTokens.length];
    public static int[] totalHam = new int[interestingTokens.length];
    public static int[] totalSpam = new int[interestingTokens.length];
    public static long numFeatures;
    private static String path = "dataset";//"C:/Users/Parag/Desktop/Project/trec07p";
    private static String datapath = path + "/data/";
    private static String spamHamFile = path + "/full/index";

	public static void main(String[] args) {
        if(args.length < 5){
            System.out.println("USAGE: java -Xmx3g -jar SpamClassifier.jar <arg1> <arg2> <arg3> <arg4> <arg5>");
            System.out.println("All arguments are required");
            System.out.println("<arg1> - svm or nb - choose svm for SVM or nb for naive bayes classifier");
            System.out.println("<arg2> - true or false - true if character features needs to be used, false if Bag of words features should be used");
            System.out.println("<arg3> - 3 or 4 or...n - n gram for character feature - have to use any integer even if <arg2> is false");
            System.out.println("<arg4> - true or false - true if dimensionality reduction using random projection needs to be performed- ignored with naive bayes classifier");
            System.out.println("<arg5> - positive integer greater than zero - reduced dimensionality size - have to supply any integer even if <arg4> is false");
            System.out.println("Example - java -Xmx3g -jar SpamClassifier.jar svm false 2 true 1000");
            System.exit(0);
        }
        String classifier = args[0];
        boolean cf = Boolean.parseBoolean(args[1]);
        int ngram = Integer.parseInt(args[2]);
        boolean rp = Boolean.parseBoolean(args[3]);
        int dim = Integer.parseInt(args[4]);
        runProgram(classifier,cf, ngram, rp, dim);
	}

    public static void runProgram(String classifier, boolean cf, int ngram, boolean rp, int dim){
        accuracyHam = new int[interestingTokens.length];
        accuracySpam = new int[interestingTokens.length];
        totalHam = new int[interestingTokens.length];
        totalSpam = new int[interestingTokens.length];
        numFeatures=0;
        /**
         * 80% for train. 20% for test
         */
        spamHams = DataReader.readFile(spamHamFile);
        //Collections.shuffle(spamHams);
        trainSet = new ArrayList<String>((int)(0.8*(spamCount+hamCount)));
        testSet = new ArrayList<String>((int)(0.2*(spamCount+hamCount)));
        if(classifier.equals("nb")) {
            naiveBayes(cf, ngram);
        }else if(classifier.equals("svm")) {
            svm_test(cf, ngram, rp, dim);
        }else{
            System.out.println("USAGE: java -Xmx3g -jar SpamClassifier.jar <arg1> <arg2> <arg3> <arg4> <arg5>");
            System.out.println("All arguments are required");
            System.out.println("<arg1> - svm or nb - choose svm for SVM or nb for naive bayes classifier");
            System.out.println("<arg2> - true or false - true if character features needs to be used, false if Bag of words features should be used");
            System.out.println("<arg3> - 3 or 4 or...n - n gram for character feature - have to use any integer even if <arg2> is false");
            System.out.println("<arg4> - true or false - true if dimensionality reduction using random projection needs to be performed- ignored with naive bayes classifier");
            System.out.println("<arg5> - positive integer greater than zero - reduced dimensionality size - have to supply any integer even if <arg4> is false");
            System.out.println("Example - java -Xmx3g -jar SpamClassifier.jar svm false 2 true 1000");
            System.exit(0);
        }
    }
	
	public static void svm_test(boolean cf, int ngram, boolean rp, int dimSize){
		SVMTest svm = new SVMTest();
        svm.setRP(rp);
        svm.setReducedDimensionSize(dimSize);
        svm.cf = cf;
        svm.ngram = ngram;
        if(rp){
            spamCount = 1000;
            hamCount = 1000;
        }
		try {
            if(!crossValidation) {
                int testSpam = (int) (0.2 * spamCount);
                int testHam = (int) (0.2 * hamCount);
                spamCount -= testSpam;
                hamCount -= testHam;

                int i = 0;
                while (i < spamHams.size() && (spamCount > 0 || hamCount > 0 || testSpam > 0 || testHam > 0)) {
                    String email = spamHams.get(i);
                    if (email.startsWith("spam")) {
                        if (spamCount > 0) {
                            trainSet.add(email);
                            spamCount--;
                        } else if (testSpam > 0) {
                            testSet.add(email);
                            testSpam--;
                        }
                    } else {
                        if (hamCount > 0) {
                            trainSet.add(email);
                            hamCount--;
                        } else if (testHam > 0) {
                            testSet.add(email);
                            testHam--;
                        }
                    }
                    i++;
                }
                svm.svmTrain(trainSet);
                numFeatures = svm.numFeatures;
                svm.svmPredict(testSet);
            }else {
                ArrayList<String> spamCollection = new ArrayList<String>(spamCount);
                ArrayList<String> hamCollection = new ArrayList<String>(hamCount);
                int i = 0;
                while (i < spamHams.size() && (spamCount > 0 || hamCount > 0)) {
                    String email = spamHams.get(i);
                    if (email.startsWith("spam") && spamCount > 0) {
                        spamCollection.add(email);
                        spamCount--;
                    } else if(email.startsWith("ham") && hamCount > 0) {
                        hamCollection.add(email);
                        hamCount--;
                    }
                    i++;
                }
                int cv =1;
                for (i = 1; i <= 16; i*=2) {
                    System.out.println();
                    System.out.println("=================== Cross Validation - Fold "+cv+++" ===================");
                    System.out.println();
                    int j=i;
                    int partSpam = spamCollection.size()/5;
                    int partHam = hamCollection.size()/5;
                    int startSpam = 0;
                    int startHam = 0;
                    for(int k=0; k<5; k++) {
                        if ((j == (1 << k))) {
                            //add in test set
                            testSet.addAll(spamCollection.subList(startSpam, startSpam + partSpam));
                            testSet.addAll(hamCollection.subList(startHam, startHam + partHam));
                        } else{
                            trainSet.addAll(spamCollection.subList(startSpam, startSpam + partSpam));
                            trainSet.addAll(hamCollection.subList(startHam, startHam + partHam));
                        }
                        startSpam += partSpam;
                        startHam += partHam;
                    }
                    svm.svmTrain(trainSet);
                    svm.svmPredict(testSet);
                    numFeatures += svm.numFeatures;
                    testSet.clear();
                    trainSet.clear();
                }
                numFeatures = numFeatures /5;
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Number of features "+numFeatures);
        System.out.println("\nSpam Accuracy = "+(double)svm.spam/svm.totalSpam*100+
                "% ("+svm.spam+"/"+svm.totalSpam+") (classification)");
        System.out.println("Ham Accuracy = "+(double)svm.ham/svm.totalHam*100+
                "% ("+svm.ham+"/"+svm.totalHam+") (classification)\n");

	}

    public static void naiveBayes(boolean cf, int ngram){
        if(!crossValidation) {
            int testSpam = (int)(0.2*spamCount);
            int testHam = (int)(0.2*hamCount) ;
            spamCount -=testSpam;
            hamCount -= testHam;
            int i = 0;
            while (i < spamHams.size() && (spamCount > 0 || hamCount > 0 || testSpam > 0 || testHam > 0)) {
                String email = spamHams.get(i);
                if (email.startsWith("spam")) {
                    if (spamCount > 0) {
                        trainSet.add(email);
                        spamCount--;
                    } else if (testSpam > 0) {
                        testSet.add(email);
                        testSpam--;
                    }
                } else {
                    if (hamCount > 0) {
                        trainSet.add(email);
                        hamCount--;
                    } else if (testHam > 0) {
                        testSet.add(email);
                        testHam--;
                    }
                }
                i++;
            }
            naiveBayesClassifier(cf, ngram);
        }else {
            ArrayList<String> spamCollection = new ArrayList<String>(spamCount);
            ArrayList<String> hamCollection = new ArrayList<String>(hamCount);
            int i = 0;
            while (i < spamHams.size() && (spamCount > 0 || hamCount > 0)) {
                String email = spamHams.get(i);

                if (email.startsWith("spam") && spamCount > 0) {
                    spamCollection.add(email);
                    spamCount--;
                } else if(email.startsWith("ham") && hamCount > 0) {
                    hamCollection.add(email);
                    hamCount--;
                }
                i++;
            }
            int cv=1;
            for (i = 1; i <= 16; i*=2) {
                System.out.println();
                System.out.println("=================== Cross Validation - Fold "+cv+++" ===================");
                System.out.println();
                int j=i;
                int partSpam = spamCollection.size()/5;
                int partHam = hamCollection.size()/5;
                int startSpam = 0;
                int startHam = 0;
                for(int k=0; k<5; k++) {
                    if ((j == (1 << k))) {
                        //add in test set
                        testSet.addAll(spamCollection.subList(startSpam, startSpam + partSpam));
                        testSet.addAll(hamCollection.subList(startHam, startHam + partHam));
                    } else{
                        trainSet.addAll(spamCollection.subList(startSpam, startSpam + partSpam));
                        trainSet.addAll(hamCollection.subList(startHam, startHam + partHam));
                    }
                    startSpam += partSpam;
                    startHam += partHam;
                }
                naiveBayesClassifier(cf, ngram);

                testSet.clear();
                trainSet.clear();
            }
            numFeatures/=5;
        }
        for (int i = 0; i < interestingTokens.length; i++) {
            System.out.println();
            System.out.println("[SPAM]" + (interestingTokens[i]==Integer.MAX_VALUE ? "All":interestingTokens[i]) + "-"
                    + accuracySpam[i] + "/" + totalSpam[i] + " = "
                    + (double) accuracySpam[i] / (double) totalSpam[i]);
            System.out.println("[HAM]" + (interestingTokens[i]==Integer.MAX_VALUE ? "All":interestingTokens[i]) + "-"
                    + accuracyHam[i] + "/" + totalHam[i] + " = "
                    + (double) accuracyHam[i] / (double) totalHam[i]);
        }

    }

	// For TREC dataSet
	public static void naiveBayesClassifier(boolean cf, int ngram) {
        String emailPath = null;
            int[] count = new int[2];
            nbayes = new NaiveBayes(cf, ngram);

            for (String line : trainSet) {
                // emailPath =
                // "..../Project/Datasets/TREC/trec07p/full/../data/inmail.1";
                emailPath = datapath;
                String subPath = line.substring(line.lastIndexOf("/") + 1);
                emailPath += subPath;
                if (line.startsWith("spam")) {
                    parseEmail(emailPath, true, nbayes);
                    count[0]++;
                } else {
                    parseEmail(emailPath, false, nbayes);
                    count[1]++;
                }
            }
            System.out.println("[TRAIN SPAM COUNT]: " + count[0]);
            System.out.println("[TRAIN HAM COUNT]: " + count[1]);

        /**
         * For dealing with rare words --> Smoothing
         */
		nbayes.calcSpamicityWithSmoothing();
        for (String line : testSet) {
            // emailPath =
            // "..../Project/Datasets/TREC/trec07p/full/../data/inmail.68676";
            emailPath = datapath;
            String subPath = line.substring(line.lastIndexOf("/") + 1);
            emailPath += subPath;
            SortedSet<Map.Entry<String, Double>> interestingWords = nbayes.findInterestingWords(DataReader.readFile(emailPath));
            for (int i = 0; i < interestingTokens.length; i++) {
                double p = nbayes.calcSpamProbability(interestingWords, interestingTokens[i]);
                //System.out.println(p);

                if (line.startsWith("spam")) {
                    totalSpam[i]++;
                    if (p >= 0.9) {
                        accuracySpam[i]++;
                        /*if(line.contains("inmail.3") && interestingTokens[i] == 30) {
                            System.out.println(line + " " + p);
                            System.out.println("\nCorrect mail " + line + " " + interestingTokens[i]);
                            for (Iterator<Map.Entry<String, Double>> it = interestingWords.iterator(); it.hasNext(); ) {
                                Map.Entry<String, Double> entry = it.next();
                                System.out.println(entry.getKey() + " " + entry.getValue()+" "+nbayes.spamicityMap.get(entry.getKey()));
                            }
                            System.out.println("End of mail\n");
                        }*/

                    }else{
                      /* if( interestingTokens[i] == 30) {
                            System.out.println(line + " " + p);
                            System.out.println("\n Incorrect mail " + line + " " + interestingTokens[i]);
                            for (Iterator<Map.Entry<String, Double>> it = interestingWords.iterator(); it.hasNext(); ) {
                                Map.Entry<String, Double> entry = it.next();
                                System.out.println(entry.getKey() + " " + entry.getValue()+" "+nbayes.spamicityMap.get(entry.getKey()));
                            }
                            System.out.println("End of mail\n");
                        }*/
                    }
                } else {
                    totalHam[i]++;
                    if (p < 0.9)
                        accuracyHam[i]++;
                    else {
                        //System.out.println("Incorrectly classified as Spam " + line + " having p = " + p);
                        /*System.out.println("Incorrectly classified as Spam - " + line + " for "
                                +(interestingTokens[i]==Integer.MAX_VALUE ? "all tokens ":interestingTokens[i]+" interesting tokens"));*/

                          /*  System.out.println(line + " " + p);
                            System.out.println("\n Incorrect fp " + line + " " + interestingTokens[i]);
                            for (Iterator<Map.Entry<String, Double>> it = interestingWords.iterator(); it.hasNext(); ) {
                                Map.Entry<String, Double> entry = it.next();
                                System.out.println(entry.getKey() + " " + entry.getValue()+" "+nbayes.spamicityMap.get(entry.getKey()));
                            }
                            System.out.println("End of mail\n");*/

                    }
                }
            }
            interestingWords.clear();
        }
        numFeatures += nbayes.numFeatures;
	}
	/**
	 * Only TEXT. No HTML. Feature = Words (text separated by any non-word
	 * character)
	 * 
	 * @param emailPath
	 *
	 * @param isSpam
	 * @param nbayes
	 */
	public static void parseEmail(String emailPath, boolean isSpam, NaiveBayes nbayes) {
		//List<String> email = DataReader.readFile(emailPath);

		// Only text based
		if (isSpam) {
			nbayes.trainSpam(emailPath);

		} else {
			nbayes.trainHam(emailPath);
		}
	}
}
//END OF CODE