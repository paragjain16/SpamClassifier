package bayes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import test.StopWords;

/**
 * Created by Parag on 25-11-2014.
 */

public class NaiveBayes {
    public HashMap<String, Integer> spamMap;
    public HashMap<String, Integer> hamMap;
    public HashMap<String, Double> spamicityMap;
    private double numberOfSpam;
    private double numberOfHam;
    private StopWords stopWords;
    //private final String regex = "\\W+";
	private final String regex = "[^a-zA-Z0-9$'-_!%*.<>]";
    //private final String regex = "\\s+";
    public boolean cf = false;
    public int ngram = 4;
    private boolean noHTML = false;
    private boolean noPrefix = false;
    private boolean trimChar = false;
    //private boolean trimWord = true;
    private boolean nullify = true;
    private boolean filterWord = true;
    private boolean filterLine = true;
    private double numberOfSpamWords = 0;
    private double numberOfHamWords = 0;
    private double spamProb = 0.5;
    private double hamProb = 0.5;
    double multiply = 1.0;
    int wrdcounts = 0;
    int wrdcounth = 0;
    public long numFeatures =0;


    // Word that doesn't occur in the hash table of word probabilities.
    double pWordSpamDefault = 0.4;

    double appreciate = 1.0;

    public NaiveBayes(boolean cf, int ngram) {
        spamMap = new HashMap<String, Integer>();
        hamMap = new HashMap<String, Integer>();
        spamicityMap = new HashMap<String, Double>();
        numberOfSpam = 0;
        numberOfHam = 0;
        //stopWords = new StopWords();
        if(cf){
            this.cf = cf;
            this.ngram = ngram;
            System.out.println("Using "+ngram+" characters as tokens");
        }else
            System.out.println("Regex used for delimiter = "+regex);
    }

    public String removeStopWords(String line){
        /*String[] arr = line.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for(String s: arr){
            if(stopWords.isStopWord(s))
                continue;
            sb.append(s).append(" ");
        }
        return sb.toString();*/
        return line;
    }

    /**
     * TRAINING
     *
     * @param file
     */
    public void trainSpam(String file) {
        numberOfSpam++;
        BufferedReader br;
        boolean end = false;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if(end)
                    break;
                //There will be attachments after this in the mail, ignore them
                if(line.startsWith("Content-Disposition"))
                    end = true;
                if(filterLine(line))
                    continue;
                if(noHTML) {
                    Document doc = Jsoup.parse(line);
                    line = doc.text();
                }
                if(cf) {
                    line = removeStopWords(line);
                    String prefix = getPrefix(line);
                    if(noPrefix)
                        prefix = "";
                        if(line.length() < ngram){
                            if(trimChar)
                                line = line.trim();
                            if (filterWord)
                                if(filterWord(line))
                                    continue;
                            line = prefix+line;
                            this.countSpam(line);
                        }else {
                            for (int i = 0; i <= line.length() - ngram; i++) {
                                String s = line.substring(i, i+ngram);
                                if(trimChar)
                                    s = s.trim();
                                if (filterWord)
                                    if(filterWord(s))
                                        continue;
                                s = prefix+s;
                                this.countSpam(s);
                            }
                        }
                }else {
                    String[] arr = line.split(regex);
                    String prefix = getPrefix(line);
                    if(noPrefix)
                        prefix = "";
                    for (String word : arr) {
                        if (filterWord)
                            if(filterWord(word))
                                continue;
                        word = prefix + word;
                        this.countSpam(word);
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        }
    }

    public void trainHam(String file) {
        numberOfHam++;
        BufferedReader br;
        boolean end =false;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if(end)
                    break;
                if(line.startsWith("Content-Disposition"))
                    end = true;
                if(filterLine(line))
                    continue;
                multiply = 1.0;
               /* if(line.startsWith("From")){
                    multiply = 2.0;
                }*/
                if(noHTML) {
                    Document doc = Jsoup.parse(line);
                    line = doc.text();
                }
                if (cf) {
                    line = removeStopWords(line);
                    String prefix = getPrefix(line);
                    if(noPrefix)
                        prefix = "";
                    if (line.length() < ngram) {
                        if(trimChar)
                            line = line.trim();
                        if (filterWord)
                            if(filterWord(line))
                                continue;
                        line = prefix + line;
                        this.countHam(line);
                    } else {
                        for (int i = 0; i <= line.length() - ngram; i++) {
                            String s = line.substring(i, i+ngram);
                            if(trimChar)
                                s = s.trim();
                            if (filterWord)
                                if(filterWord(s))
                                    continue;
                            s = prefix+s;
                            this.countHam(s);
                        }
                    }
                } else {
                    String[] arr = line.split(regex);
                    String prefix = getPrefix(line);
                    if(noPrefix)
                        prefix = "";
                    for (String word : arr) {
                        if (filterWord)
                            if(filterWord(word))
                                continue;
                        word = prefix + word;
                        this.countHam(word);
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        }
    }

    public void countSpam(String word) {
        numberOfSpamWords++;
        if (spamMap.containsKey(word)) {
            spamMap.put(word, spamMap.get(word) + 1);
        } else {
            spamMap.put(word, 1);
        }
    }

    public void countHam(String word) {
        numberOfHamWords++;
        int add = 1;
        if(add == 2) {
            numberOfHamWords++;
        }
        if (hamMap.containsKey(word)) {
            hamMap.put(word, hamMap.get(word) + add);
        } else {
            hamMap.put(word, add);
        }
    }

    public void calcSpamicityWithSmoothing() {
        double spamPrior = 0.5;
        double s = 3;
        numberOfSpamWords = Math.max(numberOfSpamWords, 1);
        numberOfHamWords = Math.max(numberOfHamWords, 1);
        //spamProb = numberOfSpam / (numberOfSpam+numberOfHam);
        //hamProb = numberOfHam / (numberOfSpam+numberOfHam);
        //System.out.println("Spam Prior " + spamProb);
        //System.out.println("Ham Prior "+hamProb);
        spamPrior = spamProb;

        for (Map.Entry<String, Integer> entry : spamMap.entrySet()) {
            String word = entry.getKey();
            double spamCount = entry.getValue();
            double spamicity = 0.99;// default for word only occurring in spam

            double hamCount = 0;
            if (hamMap.containsKey(word)) {
                hamCount = hamMap.get(word);

                double denom = spamProb * (spamCount / numberOfSpamWords) + hamProb * (hamCount / numberOfHamWords);
                if (denom != 0)
                    spamicity = spamProb * (spamCount / numberOfSpamWords) / denom;

            }

            spamicity = Math.max(0.01, Math.min(0.99, spamicity));
            //smoothing
            double n = 0;
            if (spamMap.containsKey(word))
                n += spamMap.get(word);
            if (hamMap.containsKey(word))
                n += hamMap.get(word);

            double correctedSpamicity = (s * spamPrior + n * spamicity) / (s + n);
            spamicityMap.put(word, correctedSpamicity);
        }
        double spamicity = 0.01;// default for HAM msgs
        for (Map.Entry<String, Integer> entry : hamMap.entrySet()) {
            String word = entry.getKey();
            if (!spamicityMap.containsKey(word)) {
                // Word only present in HAM messages for training set
                //smoothing
                double n = 0;
                if (spamMap.containsKey(word))
                    n += spamMap.get(word);
                if (hamMap.containsKey(word))
                    n += hamMap.get(word);

                double correctedSpamicity = (s * spamPrior + n * spamicity)/ (s + n);
                spamicityMap.put(word, correctedSpamicity);
            }
        }
        if(nullify) {
            this.spamMap = null;
            this.hamMap = null;
        }
        numFeatures = spamicityMap.size();
        System.out.println("Number of features = "+spamicityMap.size());
    }

    /**
     * END TRAINING
     */

    public SortedSet<Map.Entry<String, Double>> findInterestingWords(List<String> email) {
        SortedSet<Map.Entry<String, Double>> sortedSet = new TreeSet<Map.Entry<String, Double>>(

                new Comparator<Map.Entry<String, Double>>() {
                    @Override
                    public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                });
        HashMap<String, Double> myMap = new HashMap<String, Double>();

        pWordSpamDefault = 0.4;
        boolean end = false;
        for (String line : email) {
            if(end)
                break;
            if(line.startsWith("Content-Disposition"))
                end = true;
            if(filterLine(line))
                continue;
            double p_word = pWordSpamDefault;
            if(noHTML) {
                Document doc = Jsoup.parse(line);
                line = doc.text();
            }
            if (cf) {
                line = removeStopWords(line);
                String prefix = getPrefix(line);
                if(noPrefix)
                    prefix = "";
                if (line.length() < ngram) {
                    if(trimChar)
                        line = line.trim();
                    if (filterWord)
                        if(filterWord(line))
                            continue;
                    line = prefix + line;
                    if (spamicityMap.containsKey(line))
                        p_word = spamicityMap.get(line);
                    /*if(myMap.containsKey(line))
                        myMap.put(line, discount*myMap.get(line));
                    else*/
                        myMap.put(line, Math.abs(0.5 - p_word));
                } else {
                    for (int i = 0; i <= line.length() - ngram; i++) {
                        String s = line.substring(i, i+ngram);
                        if(trimChar)
                            s = s.trim();
                        if (filterWord)
                            if(filterWord(s))
                                continue;
                        s = prefix+s;
                        if (spamicityMap.containsKey(s))
                            p_word = spamicityMap.get(s);

                        /*if(myMap.containsKey(s))
                            myMap.put(s, discount*myMap.get(s));
                        else*/
                            myMap.put(s, Math.abs(0.5 - p_word));
                    }
                }
            }else {
                String[] arr = line.split(regex);
                String prefix = getPrefix(line);
                if(noPrefix)
                    prefix = "";
                for (String word : arr) {
                    if (filterWord)
                        if(filterWord(word))
                            continue;
                    word = prefix + word;
                    if (spamicityMap.containsKey(word))
                        p_word = spamicityMap.get(word);

                    /*if(myMap.containsKey(word))
                        myMap.put(word, discount*myMap.get(word));
                    else*/
                        myMap.put(word, Math.abs(0.5 - p_word));
                }
            }
        }
        //Printing map
//		printSortedMap(myMap);
        sortedSet.addAll(myMap.entrySet());
        return sortedSet;
    }
    public double calcSpamProbability(SortedSet<Map.Entry<String, Double>> sortedSet, int tokens){
        double eta =0.0;
        //double etan = 0.0;
        for(Iterator<Entry<String, Double>> it = sortedSet.iterator() ; tokens > 0 && it.hasNext(); tokens--){
            Map.Entry<String, Double> entry = (Entry<String, Double>) it.next();
            double p_word = 0.4;
            if(spamicityMap.containsKey(entry.getKey()))
                p_word = spamicityMap.get(entry.getKey());
            double p_log = Math.log(p_word);
            /*if(hamMap.containsKey(entry.getKey()))
                p_word = hamMap.get(entry.getKey())/numberOfHam /;
            double one_minus_p_log = Math.log(1 - p_word);*/
            double one_minus_p_log = Math.log(1 - p_word);
            eta = eta + one_minus_p_log - p_log;
            //etan = etan +  (one_minus_p_log);
        }
        //eta = spamProb * eta + hamProb * etan;
        double combinedProbability = 1 / (1 + Math.pow(Math.E, eta));
        return combinedProbability;

    }

    public String getPrefix(String emailLine){
        String prefix = "";
        if(emailLine.startsWith("From"))
            return "From*";
        else if(emailLine.startsWith("Subject"))
            return "Subject*";
        else if(emailLine.startsWith("Received"))
            return "Rec*";
        else if(emailLine.startsWith("To"))
            return "To*";
        else if(emailLine.startsWith("X-"))
            return "X*";
        return prefix;
    }

    public boolean filterLine(String line){
        //if(isEmailHeader(line)) return true;
            /*if (line.startsWith("X-"))
                return true;*/
        //Remove attachment data which is chunk of repeating alphanumeric/special characters
        if (line.length() > 30 && line.matches("[^ ]*"))
            return true;
        /*if(line.startsWith("Received"))
            return true;
        if(line.length() > 1000 && line.matches("[^ ]*"))
            return true;*/
        return false;
    }
    public boolean filterWord(String word){
       /*if(stopWords.isStopWord(word))
            return true;
        if(word.matches("[0-9 ]*"))
            return true;
        if(word.isEmpty())
            return true;*/
        return false;
    }
    public void printSortedMap(SortedMap<String, Double> sortedMap){
        for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
            System.out.println(entry.getKey()+" :-: "+entry.getValue()+" :-:"+ spamicityMap.get(entry.getKey()));

        }
    }

    public void printSortedSet(SortedSet<Map.Entry<String, Double>> sortedSet){
        for(Iterator<Entry<String, Double>> it = sortedSet.iterator() ; it.hasNext() ; ){
            Map.Entry<String, Double> entry = (Entry<String, Double>) it.next();
            System.out.println(entry.getKey()+" :-: "+entry.getValue()+" :-:"+ spamicityMap.get(entry.getKey()));

        }
    }


    public Map<String, Double> getSpamicityMap(){
        return this.spamicityMap;
    }

    public double getCountSpamMsg() {
        return this.numberOfSpam;
    }

    public double getCountHamMsg() {
        return this.numberOfHam;
    }

    private String[] headers = {"From",
            "Return-Path:",
            //"Received:",
            "Mailing-List:",
            "Precedence:",
            "List-Post:",
            "List-Help:",
            //"List-Unsubscribe:",
            //"List-Subscribe:",
            "List-Id:",
            "Delivered-To:",
            "Received:",
            "Delivered-To:",
            "X-Spam-Status:",
            "X-Spam-Check-By:",
            "Received-SPF:" ,
            "X-Mailing-List:",
            "X-Mailing-List-Name:",
            "List-Id:" ,
            "Delivered-To:",
            "Received-SPF:",
            //"From:",
            //"To:" ,
            "Resent-To:",
            "Mail-Followup-To:" ,
            "Reply-To:",
            "Date:" ,
            //"Subject:" ,
            "In-Reply-To:",
            "References:" ,
            "Message-ID:",
            "X-RT-Loop-Prevention:",
            "RT-Ticket:",
            "Managed-by:",
            "RT-Originator:",
            "MIME-Version:",
            "Content-Type:",
            "Content-Transfer-Encoding:",
            "X-RT-Original-Encoding:",
            "Resent-Message-Id:",
            "Resent-Date:",
            "Resent-From:",
            "X-Old-Spam-Check-By:",
            "X-Old-Spam-Status:"};
    private boolean isEmailHeader(String line){
        for(int i=0;i<headers.length;i++){
            if(line.startsWith(headers[i]))
                return true;
        }
        return false;
    }
}
//END OF CODE
