package test;

import java.io.*;
import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Parag on 07-12-2014.
 */
public class GenerateResults {
    public static void main(String[] args) {
        main1(null);
        HashMap<String, results> map = new HashMap<String, results>();
        String fileName = "";
        File f = null;
        PrintWriter pw = null;
        int[] trainSize = {4000, 2000};
        String[] classifier = {"nb" ,"svm"};
        boolean[] cf = {false, true};
        int[] ngram = {0, 3, 4, 5, 6};
        boolean[] rp = {false, true};
        int[] dim = {0, 900, 1000};
        try {
            for (int m = 0; m < trainSize.length; m++) {
                for (int i = 0; i < classifier.length - 1; i++) {
                    for (int j = 0; j < cf.length; j++) {
                        if (cf[j]) {
                            for (int k = 1; k < ngram.length; k++) {
                                fileName += trainSize[m] + classifier[i] + cf[j] + ngram[k] + rp[0] + dim[0];
                                Test.spamCount = trainSize[m] / 2;
                                Test.hamCount = trainSize[m] / 2;
                                Test.runProgram(classifier[i], cf[j], ngram[k], rp[0], dim[0]);
                                int[] tokens = Test.interestingTokens;
                                double[] spamvalues = new double[tokens.length];
                                double[] hamvalues = new double[tokens.length];


                                for(int t=0; t< tokens.length; t++){
                                    spamvalues[t] = (double) Test.accuracySpam[t] / (double) Test.totalSpam[t] * 100;
                                }
                                for(int t=0; t< tokens.length; t++){
                                    hamvalues[t] = (double) Test.accuracyHam[t] / (double) Test.totalHam[t] * 100;
                                }

                                if(map.containsKey(trainSize[m] + classifier[i] + ngram[0] + rp[0] + dim[0]+"Spam")) {
                                    results r = map.get(trainSize[m] + classifier[i] + ngram[0] + rp[0] + dim[0]+"Spam");
                                    r.values[r.row] = spamvalues;
                                    r.cols[r.row] = ngram[0]+" gram";
                                    r.row++;
                                    r = map.get(trainSize[m] + classifier[i] + ngram[0] + rp[0] + dim[0]+"Ham");
                                    r.values[r.row] = spamvalues;
                                    r.cols[r.row] = ngram[0]+" gram";
                                    r.row++;
                                }
                                else{
                                    results[] rs = new results[2];
                                    double[][] values = new double[ngram.length][];
                                    values[0] = spamvalues;
                                    rs[0] = new results(new String[ngram.length], tokens, values, fileName+"Spam"+".csv");
                                    rs[0].cols[0] = "Spam";
                                    rs[0].row++;
                                    map.put(trainSize[m] + classifier[i] + ngram[0] + rp[0] + dim[0]+"Spam", rs[0]);
                                    double[][] values1 = new double[ngram.length][];
                                    values1[0] = hamvalues;
                                    rs[1] = new results(new String[ngram.length], tokens, values,fileName+"Ham"+".csv");
                                    rs[1].cols[0] = "Ham";
                                    rs[1].row++;
                                    map.put(trainSize[m] + classifier[i] + ngram[0] + rp[0] + dim[0]+"Ham", rs[0]);
                                }
                            }
                        } else {
                            fileName += trainSize[m] + classifier[i] + cf[j] + ngram[0] + rp[0] + dim[0];
                            Test.spamCount = trainSize[m] / 2;
                            Test.hamCount = trainSize[m] / 2;
                            Test.runProgram(classifier[i], cf[j], ngram[0], rp[0], dim[0]);
                            int[] tokens = Test.interestingTokens;
                            double[] spamvalues = new double[tokens.length];
                            double[] hamvalues = new double[tokens.length];


                            for(int t=0; t< tokens.length; t++){
                                spamvalues[t] = (double) Test.accuracySpam[t] / (double) Test.totalSpam[t] * 100;
                            }
                            for(int t=0; t< tokens.length; t++){
                               hamvalues[t] = (double) Test.accuracyHam[t] / (double) Test.totalHam[t] * 100;
                            }

                            if(map.containsKey(trainSize[m] + classifier[i] + ngram[0] + rp[0] + dim[0]+"Spam")) {
                                results r = map.get(trainSize[m] + classifier[i] + ngram[0] + rp[0] + dim[0]+"Spam");
                                r.values[r.row] = spamvalues;
                                r.cols[r.row] = ngram[0]+" gram";
                                r.row++;
                                r = map.get(trainSize[m] + classifier[i] + ngram[0] + rp[0] + dim[0]+"Ham");
                                r.values[r.row] = spamvalues;
                                r.cols[r.row] = ngram[0]+" gram";
                                r.row++;
                            }
                            else{
                                results[] rs = new results[2];
                                double[][] values = new double[ngram.length][];
                                values[0] = spamvalues;
                                rs[0] = new results(new String[ngram.length], tokens, values, fileName+"Spam"+".csv");
                                rs[0].cols[0] = "Spam";
                                rs[0].row++;
                                map.put(trainSize[m] + classifier[i] + ngram[0] + rp[0] + dim[0]+"Spam", rs[0]);
                                double[][] values1 = new double[ngram.length][];
                                values1[0] = hamvalues;
                                rs[1] = new results(new String[ngram.length], tokens, values,fileName+"Ham"+".csv");
                                rs[1].cols[0] = "Ham";
                                rs[1].row++;
                                map.put(trainSize[m] + classifier[i] + ngram[0] + rp[0] + dim[0]+"Ham", rs[0]);
                            }
                        }

                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        for (Map.Entry<String, results> entry :  map.entrySet() ) {
            results rs = entry.getValue();
            rs.write();
        }
    }
    static class results{
        String[] cols;
        int[] tokens;
        double [][] values;
        int row=0;
        String fileName;
        results(String[] cols, int[] tokens, double [][] values,String fileName){
            this.cols = cols;
            this.tokens = tokens;
            this.values = values;
            this.fileName = fileName;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<cols.length; i++){
                sb.append(cols[i]).append(",");
                for(int j=0; j<values[i].length; j++)
                    sb.append(values[i][j]).append(",");
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
    public static void main1(String[] args) {
        HashMap<String, results> map = new HashMap<String, results>();
        String fileName = "";
        File f = null;
        PrintWriter pw = null;
        int[] trainSize = {4000, 2000};
        String[] classifier = {"nb" ,"svm"};
        boolean[] cf = {false, true};
        int[] ngram = {0, 3, 4, 5, 6};
        boolean[] rp = {false, true};
        int[] dim = {0, 900, 1000};
        try {
            for (int m = 0; m < trainSize.length; m++) {
                for (int i = 0; i < classifier.length - 1; i++) {
                    for (int j = 0; j < cf.length; j++) {
                        if (cf[j]) {
                            for (int k = 1; k < ngram.length; k++) {
                                fileName = trainSize[m] + classifier[i] + cf[j] + ngram[k] + rp[0] + dim[0]+".csv";
                                Test.spamCount = trainSize[m] / 2;
                                Test.hamCount = trainSize[m] / 2;
                                Test.runProgram(classifier[i], cf[j], ngram[k], rp[0], dim[0]);
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
                            fileName = trainSize[m] + classifier[i] + cf[j] + ngram[0] + rp[0] + dim[0]+".csv";
                            Test.spamCount = trainSize[m] / 2;
                            Test.hamCount = trainSize[m] / 2;
                            Test.runProgram(classifier[i], cf[j], ngram[0], rp[0], dim[0]);
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
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
