package org.example;


import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

public class LuceneSearch {

    public void search(String queryString) {
        try {
            Date start = new Date();
            String field = "contents";
            DirectoryReader reader = null;
            reader = DirectoryReader.open(FSDirectory.open(Path.of("../../index/")));
            IndexSearcher searcher = new IndexSearcher(reader);
            StandardAnalyzer analyzer = new StandardAnalyzer();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            QueryParser parser = new QueryParser(field, analyzer);
            String line = queryString;
            line = line.trim();
            Query query = parser.parse(line);
            System.out.println("Searching for: " + query.toString(field));
            TopDocs topDocs = searcher.search(query, 1);
            Document doc = searcher.storedFields().document(topDocs.scoreDocs[0].doc);
            String path = doc.get("path");
            System.out.println(Files.readString(Path.of(path)));
            Date end = new Date();
            System.out.println("SEARCHING TIME " + (end.getTime() - start.getTime()) + " ms");
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }



    public void indexing(){
        try {
            Date start = new Date();
            Path docDir = Path.of("../../documents");
            FSDirectory dir = FSDirectory.open(Path.of("../../index/"));
            StandardAnalyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            try(IndexWriter writer = new IndexWriter(dir, iwc)){
                indexDocs(writer, docDir);
            } finally {
                IOUtils.close();
            }

            Date end = new Date();
            try(IndexReader reader = DirectoryReader.open(dir)){
                System.out.println(
                        "Indexed "
                                + reader.numDocs()
                                + " documents in "
                                + (end.getTime() - start.getTime())
                                + " ms");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void indexDocs(final IndexWriter writer, Path path) throws IOException {
        Files.walkFileTree(
                path,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        try {
                            indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                        } catch (
                                @SuppressWarnings("unused")
                                IOException ignore) {
                            ignore.printStackTrace(System.err);
                            // don't index files that can't be read.
                        }
                        return FileVisitResult.CONTINUE;
                    }
                }
        );
    }

    void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            // make a new, empty document
            Document doc = new Document();

            // Add the path of the file as a field named "path".  Use a
            // field that is indexed (i.e. searchable), but don't tokenize
            // the field into separate words and don't index term frequency
            // or positional information:
            doc.add(new KeywordField("path", file.toString(), Field.Store.YES));

            // Add the last modified date of the file a field named "modified".
            // Use a LongField that is indexed with points and doc values, and is efficient
            // for both filtering (LongField#newRangeQuery) and sorting
            // (LongField#newSortField).  This indexes to millisecond resolution, which
            // is often too fine.  You could instead create a number based on
            // year/month/day/hour/minutes/seconds, down the resolution you require.
            // For example the long value 2011021714 would mean
            // February 17, 2011, 2-3 PM.
            doc.add(new LongField("modified", lastModified, Field.Store.NO));

            // Add the contents of the file to a field named "contents".  Specify a Reader,
            // so that the text of the file is tokenized and indexed, but not stored.
            // Note that FileReader expects the file to be in UTF-8 encoding.
            // If that's not the case searching for special characters will fail.
            doc.add(
                    new TextField(
                            "contents",
                            new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));

            if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                // New index, so we just add the document (no old document can be there):
                System.out.println("adding " + file);
                writer.addDocument(doc);
            } else {
                // Existing index (an old copy of this document may have been indexed) so
                // we use updateDocument instead to replace the old one matching the exact
                // path, if present:
                System.out.println("updating " + file);
                writer.updateDocument(new Term("path", file.toString()), doc);
            }

            System.out.println(doc);
        }
    }


}
