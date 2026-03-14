package org.example;


import javax.xml.crypto.Data;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class BruteForceSearch {
    public static void search(String searchQuery){
        int bestRanking = 0;
        Path bestRankingPath = null;
        int numberofDocumentsSearch = 0;

        Date start = new Date();

        try {
            for (Path path : Files.newDirectoryStream(Path.of("../../documents/"))) {
                String documentString = Files.readString(path);
                numberofDocumentsSearch +=1;
                String[] searchWords = searchQuery.split(" ");
                String[] documentWords = documentString.split(" ");
                int localRanking = 0;
                for (String searchWord : searchWords ) {
                    for (String documentWord: documentWords) {
                        if (searchWord.equals(documentWord))
                            localRanking +=1;
                    }
                }
                if (localRanking >= bestRanking) {
                    bestRanking = localRanking;
                    bestRankingPath = path;
                }
            }
            String finalDocument = Files.readString(bestRankingPath);
            System.out.println(finalDocument);
            System.out.println("NUMBER OF DOCUEMTNS SEARCH " + numberofDocumentsSearch);
            Date end = new Date();
            System.out.println("Time " + (end.getTime() - start.getTime()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
