package org.example;

public class Main {
    static void main() {
        //BruteForceSearch search = new BruteForceSearch();
        String searchQuery = "quest journey Sunken City Lumina";


        //LuceneSearch luceneSearch = new LuceneSearch();
        //luceneSearch.search(searchQuery);

        MultiThreadSearch search =  new MultiThreadSearch();
        search.search(searchQuery);
    }


}
