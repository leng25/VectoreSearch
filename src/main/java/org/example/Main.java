package org.example;

public class Main {
    static void main() {
        MySearch search = new MySearch();
        String searchQuery = "quest journey Sunken City Lumina";
        search.search(searchQuery);

        LuceneSearch luceneSearch = new LuceneSearch();
        //luceneSearch.search(searchQuery);
    }

}
