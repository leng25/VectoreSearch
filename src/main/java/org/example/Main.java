package org.example;

import org.example.HNSW.HnswGraph;

public class Main {
    static void main() {

        // 6s time
        // BruteForceSearch search = new BruteForceSearch();
        //String searchQuery = "quest journey Sunken City Lumina";

        // 1s time
        //LuceneSearch search = new LuceneSearch();
        //luceneSearch.search(searchQuery);

        // 2.5s time
        //MultiThreadSearch search =  new MultiThreadSearch();
        //search.search(searchQuery);

        HnswGraph hnswGraph = new HnswGraph(4, 10);
        hnswGraph.insertNode(new float[]{0.1F, 0.2F, 0.3F});
    }

}
