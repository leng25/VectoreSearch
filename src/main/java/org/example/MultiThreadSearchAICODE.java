package org.example;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadSearchAICODE {

    AtomicInteger numberofDocumentsSearch = new AtomicInteger();
    int bestRanking = 0;
    Path bestRankingPath;

    Date start = new Date();

    public void search(String searchQuery){

        ExecutorService executor = Executors.newFixedThreadPool(8);

        List<Future<?>> futures = new ArrayList<>();

        try {
            for (Path path : Files.newDirectoryStream(Path.of("../../documents/"))){
                futures.add(executor.submit(searchFile(path, searchQuery)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Future<?> f : futures){
            try {
                f.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        executor.shutdown();

        String finalDocument = null;
        try {
            finalDocument = Files.readString(bestRankingPath);
            System.out.println(finalDocument);
            System.out.println(" NUMBER OF DOCUEMTNS SEARCH " + numberofDocumentsSearch);
            Date end = new Date();
            System.out.println("time " + (end.getTime() - start.getTime()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private Runnable searchFile(Path filePath, String searchQuery){
        return () -> {
            String documentString = null;
            try {
                documentString = Files.readString(filePath);
                numberofDocumentsSearch.incrementAndGet();
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
                    bestRankingPath = filePath;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

}
