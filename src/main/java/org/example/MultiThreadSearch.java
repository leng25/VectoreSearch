package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class MultiThreadSearch {

    AtomicInteger numberOfDocumentsSearch = new AtomicInteger();
    int bestRanking= 0;
    Path bestRankingPath;
    Date start =  new Date();

    public void search(String searchQuery){

        ExecutorService executor = Executors.newFixedThreadPool(8);

        try(DirectoryStream<Path> paths =
                    Files.newDirectoryStream(Path.of("../../documents"))){
            for (Path path : paths){
               executor.submit(() -> {
                   String documentString = null;
                   try {
                       documentString = Files.readString(path);
                       numberOfDocumentsSearch.incrementAndGet();
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
                   } catch (IOException e) {
                       throw new RuntimeException(e);
                   }
               });
           }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String finalDocument = null;
        try {
            finalDocument = Files.readString(bestRankingPath);
            System.out.println(finalDocument);
            System.out.println(" NUMBER OF DOCUEMTNS SEARCH " + numberOfDocumentsSearch);
            Date end = new Date();
            System.out.println("time " + (end.getTime() - start.getTime()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
