package com.mucommander.search;

import com.mucommander.utils.Callback;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.sandbox.queries.regex.RegexQuery;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: sstolpovskiy
 * Date: 01.07.13
 * Time: 17:20
 * To change this template use File | Settings | File Templates.
 */
public class SearchTask extends SwingWorker<Boolean, String> {

    private final String targetFolder;
    private final String searchString;
    private final DefaultListModel listModel;
    private final Callback finishCallBack;
    private final Pattern regexp;
    private Thread jobThread;

    private static final String indexPath = "/Users/sstolpovskiy/muIndex/";
    private Directory dir;
    private Collection<String> documentsToRemoveFromIndex = new ArrayList<String>();

    public SearchTask(String targetFolder, String searchString, DefaultListModel defaultListModel, Callback callback) {
        this.targetFolder = targetFolder;
        this.searchString = prepareRegexpString(searchString);
        this.regexp = Pattern.compile(this.searchString);
        this.listModel = defaultListModel;
        this.finishCallBack = callback;
    }

    private String prepareRegexpString(String searchString) {
        if (searchString != null & !searchString.isEmpty()){
            return ".*" + searchString.replace("*", ".*") + ".*";
        }
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        try {
            if (searchString == null) {
                return false;
            }
            dir = FSDirectory.open(new File(indexPath));
            searchStringInIndex();
            removeNotExistingDocs();
            indexFolder();
            searchStringInIndex();
            removeNotExistingDocs();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return true;
    }

    private void removeNotExistingDocs() {
        IndexWriter writer = null;
        try {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_43, analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            writer = new IndexWriter(dir, iwc);
            for (String path : documentsToRemoveFromIndex) {
                writer.deleteDocuments(new Term(SearchFields.PATH, path));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void process(List<String> chunks) {
        for (String foundFile : chunks) {
            if (!listModel.contains(foundFile)) {
                listModel.addElement(foundFile);
            }
        }
    }

    @Override
    protected void done() {
        finishCallBack.call();
    }

    private void indexFolder() {
        try {
            boolean create = false;
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_43, analyzer);

            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                // Add new documents to an existing index:
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            // Optional: for better indexing performance, if you
            // are indexing many documents, increase the RAM
            // buffer.  But if you do this, increase the max heap
            // size to the JVM (eg add -Xmx512m or -Xmx1g):
            //
            // iwc.setRAMBufferSizeMB(256.0);

            final IndexWriter writer = new IndexWriter(dir, iwc);
//            indexDocs(writer, docDir);
            FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    if (Thread.currentThread().isInterrupted()) {
                        return FileVisitResult.TERMINATE;
                    }
                    indexDoc(writer, file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    System.out.println("can't read:" + file.toString());
                    return FileVisitResult.CONTINUE;
                }
            };
            try {
                System.out.println(SwingUtilities.isEventDispatchThread());
                Files.walkFileTree(Paths.get(targetFolder), fv);
            } catch (IOException e) {
                e.printStackTrace();
            }

            writer.close();
            System.out.println("Folder indexed");
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    private void searchStringInIndex() {
        IndexReader indexReader = null;
        try {
            indexReader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(indexReader);
            BooleanQuery query = getQuery();

            TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
            searcher.search(query, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            // `i` is just a number of document in Lucene. Note, that this number may change after document deletion
            for (int i = 0; i < hits.length; i++) {
                Document hitDoc = searcher.doc(hits[i].doc);  // getting actual document
                if (new File(hitDoc.get(SearchFields.PATH)).exists()) {
                    publish(hitDoc.get(SearchFields.PATH));
                } else {
                    documentsToRemoveFromIndex.add(hitDoc.get(SearchFields.PATH));
                }
            }
            System.out.println("Starting finished");
        } catch (Exception e) {
            e.fillInStackTrace();
        } finally {
            if (indexReader != null) {
                try {
                    indexReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private BooleanQuery getQuery() {
        BooleanQuery query = new BooleanQuery();
        query.add(new RegexQuery(new Term(SearchFields.FILE_NAME, searchString)), BooleanClause.Occur.MUST);
        query.add(new PrefixQuery(new Term(SearchFields.PATH, targetFolder)), BooleanClause.Occur.MUST);
        return query;
    }


    private void indexDoc(IndexWriter writer, Path path) throws IOException {

        File file = path.toFile();
        // make a new, empty document
        Document doc = new Document();
        // Add the path of the file as a field named "path".  Use a
        // field that is indexed (i.e. searchable), but don't tokenize
        // the field into separate words and don't index term frequency
        // or positional information:
        Field pathField = new StringField(SearchFields.PATH, file.getAbsolutePath(), Field.Store.YES);
        doc.add(pathField);

        Field fileName = new StringField(SearchFields.FILE_NAME, file.getName(), Field.Store.YES);
        doc.add(fileName);

        // Add the last modified date of the file a field named "modified".
        // Use a LongField that is indexed (i.e. efficiently filterable with
        // NumericRangeFilter).  This indexes to milli-second resolution, which
        // is often too fine.  You could instead create a number based on
        // year/month/day/hour/minutes/seconds, down the resolution you require.
        // For example the long value 2011021714 would mean
        // February 17, 2011, 2-3 PM.
        doc.add(new LongField("modified", file.lastModified(), Field.Store.NO));

        // Add the contents of the file to a field named "contents".  Specify a Reader,
        // so that the text of the file is tokenized and indexed, but not stored.
        // Note that FileReader expects the file to be in UTF-8 encoding.
        // If that's not the case searching for special characters will fail.
//            doc.add(new TextField("contents", Files.newBufferedReader(path, StandardCharsets.UTF_8)));

        if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
            // New index, so we just add the document (no old document can be there):
            System.out.println("adding " + file);
            writer.addDocument(doc);
        } else {
            // Existing index (an old copy of this document may have been indexed) so
            // we use updateDocument instead to replace the old one matching the exact
            // path, if present:
            System.out.println("updating " + file);
            writer.updateDocument(new Term(SearchFields.PATH, file.getPath()), doc);
        }

        if (regexp.matcher(file.getName()).matches()){
            publish(file.getAbsolutePath());
    }

    }
}
