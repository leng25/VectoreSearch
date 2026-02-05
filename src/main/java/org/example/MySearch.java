package org.example;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MySearch {
    public static void search(String searchQuery){
        int bestRanking = 0;
        Path bestRankingPath = null;
        int numberofDocumentsSearch = 0;
        try {
            for (Path path : Files.newDirectoryStream(Path.of("../../documents/"))) {
                String documentString = Files.readString(path);
                numberofDocumentsSearch +=1;
                //List<String> documentWord = List.of(documentString.split(" "));
                int localRanking = 0;
                for (String searchWord : searchQuery.split(" ")) {
                    for (String documentWord : documentString.split(" ")) {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
