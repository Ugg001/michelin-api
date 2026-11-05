package M2fst.michelin.search;

import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Repris de CommentIndexRepository du TP. Ici on indexe le commentaire, pas un ISBN.
@Repository
public class EvaluationIndexRepository {

    private final Directory directory;
    private final StandardAnalyzer analyzer;
    private final QueryParser parser;
    private IndexWriter indexWriter;
    private IndexSearcher indexSearcher;

    public EvaluationIndexRepository(@Value("${app.lucene.index-path}") String indexDirectoryPath) {
        try {
            Path path = Paths.get(indexDirectoryPath);
            this.directory = FSDirectory.open(path);
            this.analyzer = new StandardAnalyzer();
            this.parser = new QueryParser("commentaire", this.analyzer);
            this.indexWriter = new IndexWriter(this.directory, new IndexWriterConfig(this.analyzer));
            this.refreshSearcher();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void index(int id, String commentaire) {
        try {
            Document document = new Document();
            document.add(new StringField("id", Integer.toString(id), Field.Store.YES));
            document.add(new TextField("commentaire", commentaire, Field.Store.YES));
            this.indexWriter.updateDocument(new Term("id", Integer.toString(id)), document);
            this.indexWriter.commit();
            this.refreshSearcher();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void delete(int id) {
        try {
            this.indexWriter.deleteDocuments(new Term("id", Integer.toString(id)));
            this.indexWriter.commit();
            this.refreshSearcher();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public List<Integer> searchIds(String text) {
        try {
            // escape : un mot comme "note:3" ne doit pas casser la requête Lucene
            Query query = this.parser.parse(QueryParser.escape(text));
            TopDocs topDocs = this.indexSearcher.search(query, 10);
            List<Integer> ids = new ArrayList<>();
            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                String id = this.indexSearcher.storedFields().document(topDocs.scoreDocs[i].doc).get("id");
                ids.add(Integer.valueOf(id));
            }
            return ids;
        } catch (ParseException ex) {
            throw new IllegalArgumentException("La recherche n'est pas valide");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void refreshSearcher() throws IOException {
        if (this.indexSearcher == null) {
            this.indexSearcher = new IndexSearcher(DirectoryReader.open(this.indexWriter));
            return;
        }
        DirectoryReader liveReader = (DirectoryReader) this.indexSearcher.getIndexReader();
        DirectoryReader newReader = DirectoryReader.openIfChanged(liveReader, this.indexWriter);
        if (newReader != null) {
            this.indexSearcher = new IndexSearcher(newReader);
        }
    }

    @PreDestroy
    public void close() {
        try {
            if (this.indexWriter != null) {
                this.indexWriter.close();
            }
            this.directory.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
