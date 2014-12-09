package utilities;

import reader.DataReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import static java.nio.file.StandardCopyOption.*;

/**
 * Created by Parag on 08-12-2014.
 */
public class CreateTestDir {
    private static String path = "C:/Users/Parag/Desktop/Project/trec07p";
    private static String datapath = path + "/data/";
    private static String spamHamFile = path + "/full/index";
    private static List<String> testSet;
    private static List<String> trainSet;
    private static List<String> spamHams;
    static int spamCount = 2000;
    static int hamCount = 2000;
    public static void main(String[] args) {
        spamHams = DataReader.readFile(spamHamFile);
        //Collections.shuffle(spamHams);
        trainSet = new ArrayList<String>((int)(0.8*(spamCount+hamCount)));
        testSet = new ArrayList<String>((int)(0.2*(spamCount+hamCount)));
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

        for(i=0; i<spamCollection.size(); i++){
            String emailPath = datapath;
            String file = spamCollection.get(i);
            String subPath = file.substring(file.lastIndexOf("/") + 1);
            emailPath += subPath;
            try {
                Files.copy(new File(emailPath).toPath(), new File("C:\\Users\\Parag\\Desktop\\Project\\newpath\\"+subPath).toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for(i=0; i<hamCollection.size(); i++){
            String emailPath = datapath;
            String file = hamCollection.get(i);
            String subPath = file.substring(file.lastIndexOf("/") + 1);
            emailPath += subPath;
            try {
                Files.copy(new File(emailPath).toPath(), new File("C:\\Users\\Parag\\Desktop\\Project\\newpath\\"+subPath).toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
