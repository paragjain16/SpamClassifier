package utilities;

import test.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Parag on 07-12-2014.
 */
public class GenerateResultsSVM {
    public static void main(String[] args) {
        HashMap<String, results> map = new HashMap<String, results>();
        String fileName = "";
        File f = null;
        PrintWriter pw = null;
        int[] trainSize = {4000, 2000};
        String[] classifier = {"svm"};
        boolean[] cf = {false};
        int[] ngram = {0, 3, 4, 5, 6};
        double[] nu = {0.1, 0.2};
        boolean[] rp = {false, true};
        int[] dim = {0, 900, 1000};
        try {
            for (int m = 0; m < trainSize.length; m++) {
                    for (int j = 0; j < cf.length; j++) {
                        if (cf[j]) {
                            for (int k = 1; k < ngram.length; k++) {
                                fileName = trainSize[m] + classifier[0] + cf[j] + ngram[k] + rp[0] + dim[0]+".csv";
                                Test.spamCount = trainSize[m] / 2;
                                Test.hamCount = trainSize[m] / 2;
                                Test.runProgram(classifier[0], cf[j], ngram[k], rp[0], dim[0]);
                                f = new File(fileName);
                                pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
                                int[] tokens = Test.interestingTokens;
                                StringBuilder sb = new StringBuilder();
                                for(int t=0; t< tokens.length; t++){
                                    sb.append((tokens[t] == Integer.MAX_VALUE? "All":tokens[t])+",");
                                }
                                pw.println(sb.toString());
                                sb = new StringBuilder();
                                for(int t=0; t< tokens.length; t++){
                                    sb.append((((double) Test.accuracySpam[t] / (double) Test.totalSpam[t]) * 100)+",");
                                }
                                pw.println(sb.toString());
                                sb = new StringBuilder();
                                for(int t=0; t< tokens.length; t++){
                                    sb.append((((double) Test.accuracyHam[t] / (double) Test.totalHam[t]) * 100)+",");
                                }
                                pw.println(sb.toString());
                                pw.close();
                            }
                        } else {
                            fileName = trainSize[m] + classifier[0] + cf[j] + ngram[0] + rp[0] + dim[0]+".csv";
                            Test.spamCount = trainSize[m] / 2;
                            Test.hamCount = trainSize[m] / 2;
                            Test.runProgram(classifier[0], cf[j], ngram[0], rp[0], dim[0]);
                            f = new File(fileName);
                            pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
                            int[] tokens = Test.interestingTokens;
                            StringBuilder sb = new StringBuilder();
                            for(int t=0; t< tokens.length; t++){
                                sb.append((tokens[t] == Integer.MAX_VALUE? "All":tokens[t])+",");
                            }
                            pw.println(sb.toString());
                            sb = new StringBuilder();
                            for(int t=0; t< tokens.length; t++){
                                sb.append((((double) Test.accuracySpam[t] / (double) Test.totalSpam[t]) * 100)+",");
                            }
                            pw.println(sb.toString());
                            sb = new StringBuilder();
                            for(int t=0; t< tokens.length; t++){
                                sb.append((((double) Test.accuracyHam[t] / (double) Test.totalHam[t]) * 100)+",");
                            }
                            pw.println(sb.toString());
                            pw.close();
                        }

                    }

            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    static class results{
        String[] cols;
        int[] tokens;
        double[][] values;
        int row=0;
        String fileName;
        long[] features;
        results(String[] cols, int[] tokens, double[][] values,String fileName, long[] features){
            this.cols = cols;
            this.tokens = tokens;
            this.values = values;
            this.fileName = fileName;
            this.features = features;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(" ").append(",");
            for(int i=0; i<tokens.length; i++){
                sb.append((tokens[i] == Integer.MAX_VALUE? "All":tokens[i])).append(",");
            }
            sb.append("\n");
            for(int i=0; i<cols.length; i++){
                sb.append(cols[i]).append(",");

                for(int j=0; j<values[i].length; j++)
                    sb.append(values[i][j]).append(",");
                sb.append(features[i]);
                sb.append("\n");
            }
            return sb.toString();
        }
        public void write(){
            try{
                File f = new File(fileName);
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
                pw.println(this);
                pw.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}
