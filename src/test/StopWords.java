package test;

import java.io.*;
import java.util.HashSet;

public class StopWords {
    private HashSet<String> stopwords;
    private boolean used = true;
    public StopWords(){
        readFile();
    }
    public void readFile(){
        stopwords = new HashSet<String>();
        BufferedReader br;
        try {
            InputStream is = stopwords.getClass().getResourceAsStream("stopwords.txt");
            if(is == null)
                br = new BufferedReader(new FileReader("stopwords.txt"));
            else
                br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                stopwords.add(line);
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        }
    }
    public boolean isStopWord(String word){
        if(used)
            return stopwords.contains(word);
        else
            return false;
    }
}
